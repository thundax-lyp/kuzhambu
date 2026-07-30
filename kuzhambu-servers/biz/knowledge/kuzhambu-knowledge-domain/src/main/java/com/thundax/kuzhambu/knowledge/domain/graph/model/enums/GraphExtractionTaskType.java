package com.thundax.kuzhambu.knowledge.domain.graph.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

public enum GraphExtractionTaskType {
    RELATION,
    GRAPH,
    LINEAGE;

    public String value() {
        return name();
    }

    public static GraphExtractionTaskType from(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "KNOWLEDGE-30001",
                        "knowledge.graph.extraction.task.type.invalid",
                        "Unknown graph extraction task type: " + value));
    }
}
