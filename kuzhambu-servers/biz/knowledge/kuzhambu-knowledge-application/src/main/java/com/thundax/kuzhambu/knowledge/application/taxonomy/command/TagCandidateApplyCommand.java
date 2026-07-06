package com.thundax.kuzhambu.knowledge.application.taxonomy.command;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagCandidateApplyCommand {
    private Long aiCandidateId;
    private List<TagCandidateApplyItemCommand> selectedTags;
    private String reviewNote;
    private Long reviewedBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagCandidateApplyItemCommand {
        private String name;
        private String categoryId;
        private String categoryName;
        private BigDecimal confidence;
        private String reason;
        private String matchedExistingTagId;
    }
}
