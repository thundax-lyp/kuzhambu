package com.thundax.kuzhambu.discovery.domain.search.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum SearchIntentType {
    KEYWORD_SEARCH,
    NATURAL_LANGUAGE_SEARCH,
    UNKNOWN;

    public String value() {
        return name();
    }

    public static SearchIntentType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "DISCOVERY-10001",
                        "discovery.search.intent-type.invalid",
                        "Unknown search intent type: " + value));
    }
}
