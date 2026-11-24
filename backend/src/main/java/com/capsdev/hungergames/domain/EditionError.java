package com.capsdev.hungergames.domain;

public record EditionError(ErrorCategory category, PhaseName phase, Integer district, String message) {
}
