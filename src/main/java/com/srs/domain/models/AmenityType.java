package com.srs.domain.models;

public enum AmenityType {
    POOL("POOL"),
    SAUNA("SAUNA"),
    GYM("GYM");

    private final String name;

    private AmenityType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

