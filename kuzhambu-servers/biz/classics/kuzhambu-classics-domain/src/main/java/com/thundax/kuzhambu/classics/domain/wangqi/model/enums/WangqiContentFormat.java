package com.thundax.kuzhambu.classics.domain.wangqi.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum WangqiContentFormat {
    TEXT,
    MARKDOWN,
    HTML;

    public String value() {
        return name();
    }

    public static WangqiContentFormat from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "CLASSICS-11001", "classics.wangqi.content.format.invalid", "Unknown wangqi content format: " + value));
    }
}
