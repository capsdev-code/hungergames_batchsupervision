package com.capsdev.hungergames.service;

import com.capsdev.hungergames.domain.Edition;
import com.capsdev.hungergames.domain.EditionStatus;
import com.capsdev.hungergames.domain.Scenario;
import com.capsdev.hungergames.domain.Tribute;
import com.capsdev.hungergames.domain.TributeEvent;
import com.capsdev.hungergames.domain.TributeState;
import com.capsdev.hungergames.seed.EditionData;
import com.capsdev.hungergames.seed.SeedData;
import com.capsdev.hungergames.web.DistrictStatus;
import com.capsdev.hungergames.web.EditionDetail;
import com.capsdev.hungergames.web.EditionSummary;
import com.capsdev.hungergames.web.LaunchRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class EditionService {

    private static final int DEFAULT_MISSING_DISTRICT = 2;

    private final EditionRepository repository;
    private final BatchSimulator simulator;

    public EditionService(EditionRepository repository, BatchSimulator simulator) {
        this.repository = repository;
        this.simulator = simulator;
    }

    public List<EditionSummary> findAll() {
        return repository.findAll().stream()
                .map(this::toSummary)
                .sorted(Comparator.comparingLong(EditionSummary::id).reversed())
                .toList();
    }

    public EditionDetail findDetail(long editionId) {
        EditionData data = find(editionId);
        Edition edition = data.edition();
        List<DistrictStatus> districts = IntStream.rangeClosed(1, SeedData.DISTRICTS)
                .mapToObj(d -> new DistrictStatus(
                        d,
                        (int) data.tributes().stream().filter(t -> t.district() == d).count(),
                        SeedData.TRIBUTES_PER_DISTRICT))
                .toList();
        return new EditionDetail(toSummary(data), edition.phases(), edition.errors(), districts);
    }

    public List<Tribute> findTributes(long editionId) {
        return find(editionId).tributes();
    }

    public List<TributeEvent> findEvents(long editionId, long tributeId) {
        EditionData data = find(editionId);
        if (data.tributes().stream().noneMatch(t -> t.id() == tributeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tribut " + tributeId + " introuvable");
        }
        return data.eventsByTribute().getOrDefault(tributeId, List.of());
    }

    /** Lance une nouvelle édition (outil de démo : le vrai batch est lancé hors de la console). */
    public EditionSummary launch(LaunchRequest request) {
        Scenario scenario = request != null && request.scenario() != null ? request.scenario() : Scenario.NORMAL;
        int district = request != null && request.missingDistrict() != null
                ? request.missingDistrict() : DEFAULT_MISSING_DISTRICT;
        if (district < 1 || district > SeedData.DISTRICTS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "missingDistrict doit être compris entre 1 et " + SeedData.DISTRICTS);
        }
        long id = simulator.launch(scenario, district);
        return toSummary(find(id));
    }

    /** Relance après un échec : nouvelle édition, scénario NORMAL par défaut (données corrigées). */
    public EditionSummary relaunch(long editionId, LaunchRequest request) {
        EditionData data = find(editionId);
        if (data.edition().status() != EditionStatus.FAILED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seule une édition échouée peut être relancée (statut actuel : " + data.edition().status() + ")");
        }
        return launch(request);
    }

    private EditionData find(long editionId) {
        return repository.find(editionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Édition " + editionId + " introuvable"));
    }

    private EditionSummary toSummary(EditionData data) {
        Edition e = data.edition();
        int alive = (int) data.tributes().stream().filter(t -> t.state() == TributeState.ALIVE).count();
        int total = data.tributes().size();
        return new EditionSummary(e.id(), e.label(), e.status(), e.startTime(), e.endTime(),
                total, alive, total - alive, e.errors().size());
    }
}
