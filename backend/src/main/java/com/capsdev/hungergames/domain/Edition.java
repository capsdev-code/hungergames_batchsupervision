package com.capsdev.hungergames.domain;

import java.time.Instant;
import java.util.List;

public record Edition(
        long id,
        String label,
        EditionStatus status,
        Instant startTime,
        Instant endTime,
        List<Phase> phases,
        List<EditionError> errors) {
}
