package com.capsdev.hungergames.domain;

public record Tribute(long id, String name, int district, TributeState state) {

    public Tribute withState(TributeState newState) {
        return new Tribute(id, name, district, newState);
    }
}
