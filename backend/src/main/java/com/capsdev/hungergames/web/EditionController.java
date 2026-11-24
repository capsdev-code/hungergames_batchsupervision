package com.capsdev.hungergames.web;

import com.capsdev.hungergames.domain.Tribute;
import com.capsdev.hungergames.domain.TributeEvent;
import com.capsdev.hungergames.service.EditionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/editions")
public class EditionController {

    private final EditionService service;

    public EditionController(EditionService service) {
        this.service = service;
    }

    @GetMapping
    public List<EditionSummary> list() {
        return service.findAll();
    }

    @GetMapping("/{editionId}")
    public EditionDetail detail(@PathVariable long editionId) {
        return service.findDetail(editionId);
    }

    @GetMapping("/{editionId}/tributes")
    public List<Tribute> tributes(@PathVariable long editionId) {
        return service.findTributes(editionId);
    }

    @GetMapping("/{editionId}/tributes/{tributeId}/events")
    public List<TributeEvent> events(@PathVariable long editionId, @PathVariable long tributeId) {
        return service.findEvents(editionId, tributeId);
    }

    /** Démo : lance une nouvelle édition simulée. Corps optionnel : {"scenario": "...", "missingDistrict": 2}. */
    @PostMapping
    public ResponseEntity<EditionSummary> launch(@RequestBody(required = false) LaunchRequest request) {
        return created(service.launch(request));
    }

    /** Relance une édition échouée. Corps optionnel, scénario NORMAL par défaut. */
    @PostMapping("/{editionId}/relaunch")
    public ResponseEntity<EditionSummary> relaunch(@PathVariable long editionId,
                                                   @RequestBody(required = false) LaunchRequest request) {
        return created(service.relaunch(editionId, request));
    }

    private static ResponseEntity<EditionSummary> created(EditionSummary summary) {
        return ResponseEntity.created(URI.create("/api/editions/" + summary.id())).body(summary);
    }
}
