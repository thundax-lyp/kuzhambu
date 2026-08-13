package com.thundax.kuzhambu.knowledge.domain.graph.model.enums;

public enum GraphMaterialEventType {
    DELETED;

    public String value() {
        return name();
    }

    public static GraphMaterialEventType from(String value) {
        for (GraphMaterialEventType item : values()) {
            if (item.value().equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown graph material event type: " + value);
    }
}
