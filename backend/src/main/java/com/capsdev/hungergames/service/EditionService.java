package com.capsdev.hungergames.service;

import com.capsdev.hungergames.domain.Edition;
import com.capsdev.hungergames.domain.Tribute;
import com.capsdev.hungergames.domain.TributeEvent;
import com.capsdev.hungergames.domain.TributeState;
import com.capsdev.hungergames.seed.EditionData;
import com.capsdev.hungergames.seed.SeedData;
import com.capsdev.hungergames.web.DistrictStatus;
import com.capsdev.hungergames.web.EditionDetail;
import com.capsdev.hungergames.web.EditionSummary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class EditionService {

    private final List<EditionData> editions;

    public EditionService(SeedData seedData) {
        this.editions = seedData.editions();
    }

    public List<EditionSummary> findAll() {
        return editions.stream()
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

    private EditionData find(long editionId) {
        return editions.stream()
                .filter(d -> d.edition().id() == editionId)
                .findFirst()
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
