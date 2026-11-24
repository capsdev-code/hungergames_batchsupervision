package com.capsdev.hungergames.web;

/** Nombre de tributs affectés à un district, comparé au nombre attendu. */
public record DistrictStatus(int district, int tributeCount, int expected) {
}
