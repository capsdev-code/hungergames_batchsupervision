package com.capsdev.hungergames.domain;

import java.time.Instant;

public record Phase(PhaseName name, PhaseStatus status, Instant startTime, Instant endTime) {
}
