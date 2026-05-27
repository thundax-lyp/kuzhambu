package com.thundax.kuzhambu.classics.domain.sancai.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum SancaiCategoryType {
    FORMAL,
    FRONT_MATTER;

    public String value() {
        return name();
    }

    public static SancaiCategoryType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-10001",
                        "classics.sancai.category.type.invalid",
                        "Unknown sancai category type: " + value));
    }
}
