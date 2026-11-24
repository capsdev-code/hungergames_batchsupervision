package com.capsdev.hungergames.seed;

import com.capsdev.hungergames.domain.Edition;
import com.capsdev.hungergames.domain.EditionError;
import com.capsdev.hungergames.domain.EditionStatus;
import com.capsdev.hungergames.domain.ErrorCategory;
import com.capsdev.hungergames.domain.EventLevel;
import com.capsdev.hungergames.domain.EventType;
import com.capsdev.hungergames.domain.Phase;
import com.capsdev.hungergames.domain.PhaseName;
import com.capsdev.hungergames.domain.PhaseStatus;
import com.capsdev.hungergames.domain.Tribute;
import com.capsdev.hungergames.domain.TributeEvent;
import com.capsdev.hungergames.domain.TributeState;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Données fictives, déterministes (graine fixe) pour que le front voie toujours les mêmes éditions.
 *
 * <ul>
 *   <li>74 : terminée, 24 tributs, 1 vainqueur</li>
 *   <li>75 : en cours (phase Arène)</li>
 *   <li>76 : échec FONCTIONNEL, le district 2 n'a aucun tribut</li>
 *   <li>77 : échec TECHNIQUE, timeout base de données pendant l'Arène</li>
 * </ul>
 */
@Component
public class SeedData {

    public static final int DISTRICTS = 12;
    public static final int TRIBUTES_PER_DISTRICT = 2;

    private static final List<String> NAMES = List.of(
            "Aelin", "Brannoc", "Cyrene", "Dorian", "Elowen", "Fenwick", "Galatea", "Hadrian",
            "Isolde", "Jarek", "Kestrel", "Lysandre", "Maelis", "Nerys", "Orsino", "Perrin",
            "Quillon", "Rhosyn", "Soren", "Talia", "Ulric", "Verena", "Wystan", "Xanthe",
            "Yorick", "Zephyra", "Alder", "Briar", "Corvin", "Dahlia", "Emrys", "Fiora",
            "Gideon", "Hesper", "Ivo", "Juniper", "Kaelen", "Linnea", "Marek", "Nyx");

    private static final List<String> FOODS = List.of(
            "des baies", "un lapin", "une source d'eau", "des racines comestibles",
            "du poisson", "des noix", "un nid d'oeufs");

    private static final List<String> GIFTS = List.of(
            "une gourde d'eau", "un baume de soin", "du pain chaud", "une couverture", "un briquet");

    public List<EditionData> editions() {
        return List.of(
                completed(),
                ongoing(),
                functionalFailure(),
                technicalFailure());
    }

    // ---------- Scénarios ----------

    private EditionData completed() {
        long id = 74;
        Instant start = Instant.parse("2026-10-05T08:00:00Z");
        Instant moissonEnd = start.plusSeconds(4 * 60);
        Instant arenaEnd = start.plusSeconds(46 * 60);
        Instant end = start.plusSeconds(50 * 60);

        Random rnd = new Random(id);
        Simulation sim = simulate(id, rnd, Set.of(), 23, moissonEnd, arenaEnd);

        Edition edition = new Edition(id, "74e Jeux de la Faim", EditionStatus.COMPLETED, start, end,
                List.of(
                        new Phase(PhaseName.MOISSON, PhaseStatus.COMPLETED, start, moissonEnd),
                        new Phase(PhaseName.ARENE, PhaseStatus.COMPLETED, moissonEnd, arenaEnd),
                        new Phase(PhaseName.COURONNEMENT, PhaseStatus.COMPLETED, arenaEnd, end)),
                List.of());
        return new EditionData(edition, sim.tributes(), sim.events());
    }

    private EditionData ongoing() {
        long id = 75;
        Instant start = Instant.parse("2026-11-02T08:00:00Z");
        Instant moissonEnd = start.plusSeconds(4 * 60);
        Instant now = start.plusSeconds(22 * 60);

        Random rnd = new Random(id);
        Simulation sim = simulate(id, rnd, Set.of(), 9, moissonEnd, now);

        Edition edition = new Edition(id, "75e Jeux de la Faim", EditionStatus.STARTED, start, null,
                List.of(
                        new Phase(PhaseName.MOISSON, PhaseStatus.COMPLETED, start, moissonEnd),
                        new Phase(PhaseName.ARENE, PhaseStatus.STARTED, moissonEnd, null),
                        new Phase(PhaseName.COURONNEMENT, PhaseStatus.PENDING, null, null)),
                List.of());
        return new EditionData(edition, sim.tributes(), sim.events());
    }

    private EditionData functionalFailure() {
        long id = 76;
        Instant start = Instant.parse("2026-11-09T08:00:00Z");
        Instant end = start.plusSeconds(60);

        Random rnd = new Random(id);
        // Le district 2 n'a fourni aucun tribut : la règle des 12 x 2 est violée.
        List<Tribute> tributes = buildTributes(id, rnd, Set.of(2));

        Edition edition = new Edition(id, "76e Jeux de la Faim", EditionStatus.FAILED, start, end,
                List.of(
                        new Phase(PhaseName.MOISSON, PhaseStatus.FAILED, start, end),
                        new Phase(PhaseName.ARENE, PhaseStatus.PENDING, null, null),
                        new Phase(PhaseName.COURONNEMENT, PhaseStatus.PENDING, null, null)),
                List.of(new EditionError(ErrorCategory.FUNCTIONAL, PhaseName.MOISSON, 2,
                        "District 2 : 0 tribut affecté, " + TRIBUTES_PER_DISTRICT + " attendus")));
        return new EditionData(edition, tributes, Map.of());
    }

    private EditionData technicalFailure() {
        long id = 77;
        Instant start = Instant.parse("2026-11-16T08:00:00Z");
        Instant moissonEnd = start.plusSeconds(4 * 60);
        Instant failure = start.plusSeconds(20 * 60);

        Random rnd = new Random(id);
        Simulation sim = simulate(id, rnd, Set.of(), 6, moissonEnd, failure);

        Edition edition = new Edition(id, "77e Jeux de la Faim", EditionStatus.FAILED, start, failure,
                List.of(
                        new Phase(PhaseName.MOISSON, PhaseStatus.COMPLETED, start, moissonEnd),
                        new Phase(PhaseName.ARENE, PhaseStatus.FAILED, moissonEnd, failure),
                        new Phase(PhaseName.COURONNEMENT, PhaseStatus.PENDING, null, null)),
                List.of(new EditionError(ErrorCategory.TECHNICAL, PhaseName.ARENE, null,
                        "Timeout de connexion à la base de données après 30 s")));
        return new EditionData(edition, sim.tributes(), sim.events());
    }

    // ---------- Génération ----------

    private record Simulation(List<Tribute> tributes, Map<Long, List<TributeEvent>> events) {
    }

    private List<Tribute> buildTributes(long editionId, Random rnd, Set<Integer> emptyDistricts) {
        List<String> names = new ArrayList<>(NAMES);
        Collections.shuffle(names, rnd);
        List<Tribute> tributes = new ArrayList<>();
        int n = 0;
        for (int district = 1; district <= DISTRICTS; district++) {
            if (emptyDistricts.contains(district)) {
                continue;
            }
            for (int i = 0; i < TRIBUTES_PER_DISTRICT; i++) {
                tributes.add(new Tribute(editionId * 100 + n + 1, names.get(n), district, TributeState.ALIVE));
                n++;
            }
        }
        return tributes;
    }

    private Simulation simulate(long editionId, Random rnd, Set<Integer> emptyDistricts,
                                int eliminations, Instant from, Instant to) {
        List<Tribute> tributes = buildTributes(editionId, rnd, emptyDistricts);
        Map<Long, List<TributeEvent>> events = new HashMap<>();
        tributes.forEach(t -> events.put(t.id(), new ArrayList<>()));

        long window = to.getEpochSecond() - from.getEpochSecond();

        // Nourriture : 1 à 3 événements INFO par tribut
        for (Tribute t : tributes) {
            int count = 1 + rnd.nextInt(3);
            for (int i = 0; i < count; i++) {
                events.get(t.id()).add(new TributeEvent(randomTime(rnd, from, window), EventType.FOOD, EventLevel.INFO,
                        "A trouvé de la nourriture : " + FOODS.get(rnd.nextInt(FOODS.size()))));
            }
        }

        // Aide du Capitole : 5 parachutes (INFO)
        for (int i = 0; i < 5; i++) {
            Tribute t = tributes.get(rnd.nextInt(tributes.size()));
            events.get(t.id()).add(new TributeEvent(randomTime(rnd, from, window), EventType.SPONSOR_GIFT, EventLevel.INFO,
                    "Parachute du Capitole : " + GIFTS.get(rnd.nextInt(GIFTS.size()))));
        }

        // Éliminations : le tueur reçoit un événement WARN, la victime passe à ELIMINATED
        List<Integer> alive = new ArrayList<>();
        for (int i = 0; i < tributes.size(); i++) {
            alive.add(i);
        }
        Map<Long, Instant> eliminatedAt = new HashMap<>();
        for (int k = 0; k < eliminations; k++) {
            Instant when = from.plusSeconds(window * (k + 1) / (eliminations + 1));
            int victimIdx = alive.remove(rnd.nextInt(alive.size()));
            int killerIdx = alive.get(rnd.nextInt(alive.size()));
            Tribute victim = tributes.get(victimIdx);
            Tribute killer = tributes.get(killerIdx);
            tributes.set(victimIdx, victim.withState(TributeState.ELIMINATED));
            eliminatedAt.put(victim.id(), when);
            events.get(killer.id()).add(new TributeEvent(when, EventType.KILL, EventLevel.WARN,
                    "A éliminé " + victim.name() + " (district " + victim.district() + ")"));
        }

        // Cohérence : un tribut éliminé n'a plus d'événement après son élimination
        events.replaceAll((id, list) -> {
            Instant limit = eliminatedAt.get(id);
            List<TributeEvent> kept = new ArrayList<>(list);
            if (limit != null) {
                kept.removeIf(e -> e.timestamp().isAfter(limit));
            }
            kept.sort(Comparator.comparing(TributeEvent::timestamp));
            return kept;
        });

        return new Simulation(tributes, events);
    }

    private Instant randomTime(Random rnd, Instant from, long windowSeconds) {
        return from.plusSeconds(rnd.nextLong(windowSeconds));
    }
}
