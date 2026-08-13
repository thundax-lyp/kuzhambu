package com.thundax.kuzhambu.knowledge.domain.graph.model.enums;

public enum GraphNodeType {
    PERSON,
    GROUP,
    DYNASTY,
    ORGANIZATION,
    OFFICE,
    PLACE,
    BUILDING,
    WORK,
    EVENT,
    RITUAL,
    CONCEPT,
    OBJECT,
    MATERIAL,
    ANIMAL,
    PLANT,
    CELESTIAL_BODY,
    NATURAL_PHENOMENON,
    DEITY;

    public String value() {
        return name();
    }

    public static GraphNodeType from(String value) {
        for (GraphNodeType item : values()) {
            if (item.value().equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown graph node type: " + value);
    }
}
