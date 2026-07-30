package com.thundax.kuzhambu.knowledge.domain.graph.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

public enum GraphVersionStatus {
    APPLIED;

    public String value() {
        return name();
    }

    public static GraphVersionStatus from(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "KNOWLEDGE-30003",
                        "knowledge.graph.version.status.invalid",
                        "Unknown graph version status: " + value));
    }
}
