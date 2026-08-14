package com.thundax.kuzhambu.knowledge.domain.graph.model.enums;

public enum GraphMaterialDeletionStatus {
    PRECHECKED,
    AWAITING_DECISION,
    PENDING,
    RUNNING,
    SUCCEEDED,
    FAILED;

    public String value() {
        return name();
    }

    public static GraphMaterialDeletionStatus from(String value) {
        for (GraphMaterialDeletionStatus item : values()) {
            if (item.value().equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown graph material deletion status: " + value);
    }
}
