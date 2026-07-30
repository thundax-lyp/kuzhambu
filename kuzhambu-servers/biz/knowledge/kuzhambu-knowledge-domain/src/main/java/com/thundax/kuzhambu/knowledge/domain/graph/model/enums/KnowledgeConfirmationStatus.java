package com.thundax.kuzhambu.knowledge.domain.graph.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

public enum KnowledgeConfirmationStatus {
    AI_EXTRACTED,
    CONFIRMED,
    MANUAL_CONFIRMED,
    PENDING;

    public String value() {
        return name();
    }

    public static KnowledgeConfirmationStatus from(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "KNOWLEDGE-30004",
                        "knowledge.confirmation.status.invalid",
                        "Unknown knowledge confirmation status: " + value));
    }
}
