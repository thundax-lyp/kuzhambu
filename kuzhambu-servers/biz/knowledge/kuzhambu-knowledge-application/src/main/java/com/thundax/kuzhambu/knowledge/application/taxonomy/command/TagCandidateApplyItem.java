package com.thundax.kuzhambu.knowledge.application.taxonomy.command;

import java.math.BigDecimal;

public record TagCandidateApplyItem(
        String name,
        String categoryId,
        String categoryName,
        BigDecimal confidence,
        String reason,
        String matchedExistingTagId) {}
