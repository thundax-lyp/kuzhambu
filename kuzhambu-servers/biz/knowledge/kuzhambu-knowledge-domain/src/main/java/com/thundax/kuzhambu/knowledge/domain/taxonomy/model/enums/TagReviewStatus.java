package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum TagReviewStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public String value() {
        return name();
    }

    public static TagReviewStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "KNOWLEDGE-10004",
                        "knowledge.taxonomy.tag.review-status.invalid",
                        "Unknown tag review status: " + value));
    }
}
