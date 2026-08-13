package com.thundax.kuzhambu.knowledge.domain.graph.model.enums;

public enum GraphSourceType {
    AI,
    MANUAL,
    IMPORT,
    MATERIAL;

    public String value() {
        return name();
    }

    public static GraphSourceType from(String value) {
        for (GraphSourceType item : values()) {
            if (item.value().equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown graph source type: " + value);
    }
}
