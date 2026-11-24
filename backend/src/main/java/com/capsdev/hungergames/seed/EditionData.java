package com.capsdev.hungergames.seed;

import com.capsdev.hungergames.domain.Edition;
import com.capsdev.hungergames.domain.Tribute;
import com.capsdev.hungergames.domain.TributeEvent;

import java.util.List;
import java.util.Map;

/** Une édition avec ses tributs et leurs événements (clé : id du tribut). */
public record EditionData(Edition edition, List<Tribute> tributes, Map<Long, List<TributeEvent>> eventsByTribute) {
}
