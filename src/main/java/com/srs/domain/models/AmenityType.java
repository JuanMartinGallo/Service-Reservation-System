package com.srs.domain.models;

public enum AmenityType {
    POOL("POOL"),
    SAUNA("SAUNA"),
    GYM("GYM");

    private final String name;

    private AmenityType(String name) {
        this.name = name;
    }

    public static AmenityType fromString(String name) {
        for (AmenityType type : AmenityType.values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AmenityType: " + name);
    }

    @Override
    public String toString() {
        return name;
    }
}