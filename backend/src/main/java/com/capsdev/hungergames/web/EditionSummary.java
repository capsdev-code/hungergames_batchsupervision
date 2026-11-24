package com.capsdev.hungergames.web;

import com.capsdev.hungergames.domain.EditionStatus;

import java.time.Instant;

public record EditionSummary(
        long id,
        String label,
        EditionStatus status,
        Instant startTime,
        Instant endTime,
        int tributeCount,
        int aliveCount,
        int eliminatedCount,
        int errorCount) {
}
