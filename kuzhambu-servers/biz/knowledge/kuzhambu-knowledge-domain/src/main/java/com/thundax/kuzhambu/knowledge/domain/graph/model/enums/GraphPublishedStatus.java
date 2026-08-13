package com.thundax.kuzhambu.knowledge.domain.graph.model.enums;

public enum GraphPublishedStatus {
    ACTIVE,
    DELETED;

    public String value() {
        return name();
    }

    public static GraphPublishedStatus from(String value) {
        for (GraphPublishedStatus item : values()) {
            if (item.value().equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown graph published status: " + value);
    }
}
