package com.capsdev.hungergames.service;

import com.capsdev.hungergames.domain.Edition;
import com.capsdev.hungergames.domain.EditionError;
import com.capsdev.hungergames.domain.EditionStatus;
import com.capsdev.hungergames.domain.ErrorCategory;
import com.capsdev.hungergames.domain.Phase;
import com.capsdev.hungergames.domain.PhaseName;
import com.capsdev.hungergames.domain.PhaseStatus;
import com.capsdev.hungergames.domain.Scenario;
import com.capsdev.hungergames.domain.Tribute;
import com.capsdev.hungergames.domain.TributeEvent;
import com.capsdev.hungergames.domain.TributeState;
import com.capsdev.hungergames.seed.EditionData;
import com.capsdev.hungergames.seed.SeedData;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simule le batch RH : une édition se déroule en trois phases (Moisson, Arène, Couronnement)
 * dans un thread dédié, et publie son avancement au fil de l'eau dans le repository.
 * Une seule édition peut tourner à la fois.
 */
@Service
public class BatchSimulator {

    private static final String TECHNICAL_MESSAGE = "Timeout de connexion à la base de données après 30 s";
    private static final int TECHNICAL_FAILURE_AFTER_ELIMINATIONS = 8;

    private final EditionRepository repository;
    private final SeedData seedData;
    private final long tickMs;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "batch-simulator");
        thread.setDaemon(true);
        return thread;
    });

    public BatchSimulator(EditionRepository repository, SeedData seedData,
                          @Value("${app.batch.tick-ms:1000}") long tickMs) {
        this.repository = repository;
        this.seedData = seedData;
        this.tickMs = tickMs;
    }

    /** Démarre une édition et retourne son id immédiatement. 409 si une édition simulée tourne déjà. */
    public long launch(Scenario scenario, int missingDistrict) {
        if (!running.compareAndSet(false, true)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une édition est déjà en cours d'exécution");
        }
        long id = repository.nextId();
        Instant start = Instant.now();
        Edition edition = new Edition(id, id + "e Jeux de la Faim", EditionStatus.STARTED, start, null,
                List.of(
                        new Phase(PhaseName.MOISSON, PhaseStatus.STARTED, start, null),
                        new Phase(PhaseName.ARENE, PhaseStatus.PENDING, null, null),
                        new Phase(PhaseName.COURONNEMENT, PhaseStatus.PENDING, null, null)),
                List.of());
        repository.save(new EditionData(edition, List.of(), Map.of()));

        executor.submit(() -> {
            try {
                new Run(id, edition, scenario, missingDistrict).execute();
            } finally {
                running.set(false);
            }
        });
        return id;
    }

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }

    /** Une exécution du batch : état local mutable, publié sous forme immuable à chaque étape. */
    private final class Run {
        private final long id;
        private final Scenario scenario;
        private final int missingDistrict;
        private final Random rnd;
        private Edition edition;
        private List<Tribute> tributes = new ArrayList<>();
        private final Map<Long, List<TributeEvent>> events = new HashMap<>();

        Run(long id, Edition edition, Scenario scenario, int missingDistrict) {
            this.id = id;
            this.edition = edition;
            this.scenario = scenario;
            this.missingDistrict = missingDistrict;
            this.rnd = new Random(id);
        }

        void execute() {
            try {
                moisson();
                if (edition.status() == EditionStatus.FAILED) {
                    return;
                }
                arene();
                if (edition.status() == EditionStatus.FAILED) {
                    return;
                }
                couronnement();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                fail(currentPhase(), new EditionError(ErrorCategory.TECHNICAL, currentPhase(), null,
                        "Erreur inattendue du batch : " + e.getMessage()));
            }
        }

        // Phase 1 : lecture des tributs et contrôle des 12 districts x 2 tributs
        private void moisson() throws InterruptedException {
            Thread.sleep(tickMs * 3);
            Set<Integer> empty = scenario == Scenario.MISSING_DISTRICT ? Set.of(missingDistrict) : Set.of();
            tributes = seedData.buildTributes(id, rnd, empty);
            tributes.forEach(t -> events.put(t.id(), new ArrayList<>()));

            List<EditionError> errors = new ArrayList<>();
            for (int district = 1; district <= SeedData.DISTRICTS; district++) {
                int d = district;
                long count = tributes.stream().filter(t -> t.district() == d).count();
                if (count < SeedData.TRIBUTES_PER_DISTRICT) {
                    errors.add(new EditionError(ErrorCategory.FUNCTIONAL, PhaseName.MOISSON, district,
                            "District " + district + " : " + count + " tribut affecté, "
                                    + SeedData.TRIBUTES_PER_DISTRICT + " attendus"));
                }
            }
            if (!errors.isEmpty()) {
                finish(PhaseName.MOISSON, PhaseStatus.FAILED, EditionStatus.FAILED, errors);
                return;
            }
            setPhase(PhaseName.MOISSON, PhaseStatus.COMPLETED, null, Instant.now());
            setPhase(PhaseName.ARENE, PhaseStatus.STARTED, Instant.now(), null);
            publish();
        }

        // Phase 2 : un événement par tick, une élimination tous les ticks jusqu'à un seul survivant
        private void arene() throws InterruptedException {
            int eliminations = 0;
            while (aliveTributes().size() > 1) {
                Thread.sleep(tickMs);
                if (scenario == Scenario.TECHNICAL_FAILURE && eliminations == TECHNICAL_FAILURE_AFTER_ELIMINATIONS) {
                    finish(PhaseName.ARENE, PhaseStatus.FAILED, EditionStatus.FAILED,
                            List.of(new EditionError(ErrorCategory.TECHNICAL, PhaseName.ARENE, null, TECHNICAL_MESSAGE)));
                    return;
                }
                Instant now = Instant.now();
                List<Tribute> alive = aliveTributes();

                events.get(pick(alive).id()).add(seedData.foodEvent(rnd, now));
                if (rnd.nextInt(5) == 0) {
                    events.get(pick(alive).id()).add(seedData.giftEvent(rnd, now));
                }

                Tribute victim = pick(alive);
                List<Tribute> others = alive.stream().filter(t -> t.id() != victim.id()).toList();
                Tribute killer = pick(others);
                tributes.replaceAll(t -> t.id() == victim.id() ? t.withState(TributeState.ELIMINATED) : t);
                events.get(killer.id()).add(seedData.killEvent(victim, now));
                eliminations++;
                publish();
            }
            setPhase(PhaseName.ARENE, PhaseStatus.COMPLETED, null, Instant.now());
            setPhase(PhaseName.COURONNEMENT, PhaseStatus.STARTED, Instant.now(), null);
            publish();
        }

        // Phase 3 : export du vainqueur
        private void couronnement() throws InterruptedException {
            Thread.sleep(tickMs * 3);
            finish(PhaseName.COURONNEMENT, PhaseStatus.COMPLETED, EditionStatus.COMPLETED, List.of());
        }

        private void fail(PhaseName phase, EditionError error) {
            finish(phase, PhaseStatus.FAILED, EditionStatus.FAILED, List.of(error));
        }

        private PhaseName currentPhase() {
            return edition.phases().stream()
                    .filter(p -> p.status() == PhaseStatus.STARTED)
                    .map(Phase::name)
                    .findFirst()
                    .orElse(PhaseName.MOISSON);
        }

        private void finish(PhaseName phase, PhaseStatus phaseStatus, EditionStatus status, List<EditionError> errors) {
            Instant now = Instant.now();
            setPhase(phase, phaseStatus, null, now);
            edition = new Edition(edition.id(), edition.label(), status, edition.startTime(), now,
                    edition.phases(), errors);
            publish();
        }

        private void setPhase(PhaseName name, PhaseStatus status, Instant start, Instant end) {
            List<Phase> phases = edition.phases().stream()
                    .map(p -> p.name() != name ? p : new Phase(name, status,
                            start != null ? start : p.startTime(),
                            end != null ? end : p.endTime()))
                    .toList();
            edition = new Edition(edition.id(), edition.label(), edition.status(), edition.startTime(),
                    edition.endTime(), phases, edition.errors());
        }

        private void publish() {
            Map<Long, List<TributeEvent>> snapshot = new HashMap<>();
            events.forEach((tributeId, list) -> snapshot.put(tributeId, List.copyOf(list)));
            EditionData data = new EditionData(edition, List.copyOf(tributes), Map.copyOf(snapshot));
            repository.update(id, current -> data);
        }

        private List<Tribute> aliveTributes() {
            return tributes.stream().filter(t -> t.state() == TributeState.ALIVE).toList();
        }

        private Tribute pick(List<Tribute> list) {
            return list.get(rnd.nextInt(list.size()));
        }
    }
}
