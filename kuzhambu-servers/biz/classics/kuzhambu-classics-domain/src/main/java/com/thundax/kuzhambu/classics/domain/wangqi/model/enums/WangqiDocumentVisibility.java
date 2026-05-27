package com.thundax.kuzhambu.classics.domain.wangqi.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum WangqiDocumentVisibility {
    PUBLIC,
    PRIVATE;

    public String value() {
        return name();
    }

    public static WangqiDocumentVisibility from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-11002",
                        "classics.wangqi.visibility.invalid",
                        "Unknown wangqi document visibility: " + value));
    }
}
