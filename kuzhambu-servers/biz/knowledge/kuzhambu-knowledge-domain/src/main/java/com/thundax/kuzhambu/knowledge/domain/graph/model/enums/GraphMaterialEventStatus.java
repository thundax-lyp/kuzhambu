package com.thundax.kuzhambu.knowledge.domain.graph.model.enums;

public enum GraphMaterialEventStatus {
    SCHEDULED,
    PROCESSING,
    SUCCEEDED,
    FAILED;

    public String value() {
        return name();
    }

    public static GraphMaterialEventStatus from(String value) {
        for (GraphMaterialEventStatus item : values()) {
            if (item.value().equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown graph material event status: " + value);
    }
}
