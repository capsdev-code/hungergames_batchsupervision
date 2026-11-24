package com.capsdev.hungergames.web;

import com.capsdev.hungergames.domain.EditionError;
import com.capsdev.hungergames.domain.Phase;

import java.util.List;

public record EditionDetail(
        EditionSummary summary,
        List<Phase> phases,
        List<EditionError> errors,
        List<DistrictStatus> districts) {
}
