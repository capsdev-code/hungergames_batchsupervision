package com.capsdev.hungergames.domain;

import java.time.Instant;

public record TributeEvent(Instant timestamp, EventType type, EventLevel level, String message) {
}
