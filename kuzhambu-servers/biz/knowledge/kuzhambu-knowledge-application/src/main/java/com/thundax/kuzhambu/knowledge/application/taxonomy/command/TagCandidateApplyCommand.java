package com.thundax.kuzhambu.knowledge.application.taxonomy.command;

import java.math.BigDecimal;
import java.util.List;

public record TagCandidateApplyCommand(
        Long aiCandidateId, List<TagCandidateApplyItemCommand> selectedTags, String reviewNote, Long reviewedBy) {

    public record TagCandidateApplyItemCommand(
            String name,
            String categoryId,
            String categoryName,
            BigDecimal confidence,
            String reason,
            String matchedExistingTagId) {}
}
