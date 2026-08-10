package com.thundax.kuzhambu.knowledge.application.taxonomy.command;

import java.util.List;

public record TagCandidateApplyCommand(
        Long aiCandidateId, List<TagCandidateApplyItem> selectedTags, String reviewNote, Long reviewedBy) {}
