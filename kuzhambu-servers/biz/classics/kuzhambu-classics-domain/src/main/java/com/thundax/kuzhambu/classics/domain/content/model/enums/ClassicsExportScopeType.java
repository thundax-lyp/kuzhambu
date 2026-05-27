package com.thundax.kuzhambu.classics.domain.content.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum ClassicsExportScopeType {
    CATEGORY,
    VOLUME,
    FILTER_RESULT,
    SELECTED_ITEMS,
    SELECTED_ASSETS;

    public String value() {
        return name();
    }

    public static ClassicsExportScopeType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-13007", "classics.export.scope.type.invalid", "Unknown classics export scope type: " + value));
    }
}
