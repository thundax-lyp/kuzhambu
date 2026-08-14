package com.thundax.kuzhambu.knowledge.domain.graph.model.enums;

public enum GraphMaterialDeletionDecision {
    PRESERVE_CONTRIBUTION,
    WITHDRAW_ASSOCIATIONS;

    public String value() {
        return name();
    }

    public static GraphMaterialDeletionDecision from(String value) {
        if (value == null) {
            return null;
        }
        for (GraphMaterialDeletionDecision item : values()) {
            if (item.value().equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown graph material deletion decision: " + value);
    }
}
