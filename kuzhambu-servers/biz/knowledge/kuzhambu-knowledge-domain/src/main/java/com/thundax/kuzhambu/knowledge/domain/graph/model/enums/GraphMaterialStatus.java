package com.thundax.kuzhambu.knowledge.domain.graph.model.enums;

public enum GraphMaterialStatus {
    DRAFT,
    PUBLISHING,
    PUBLISHED,
    WITHDRAWING,
    FAILED;

    public String value() {
        return name();
    }

    public static GraphMaterialStatus from(String value) {
        for (GraphMaterialStatus item : values()) {
            if (item.value().equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown graph material status: " + value);
    }
}
