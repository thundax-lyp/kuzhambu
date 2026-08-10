package com.thundax.kuzhambu.knowledge.application.taxonomy.command;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagReviewStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import java.time.Instant;

public record TagCreateCommand(
        TagId id,
        String name,
        TagCategoryId categoryId,
        String description,
        TagReviewStatus reviewStatus,
        String reviewNote,
        Instant reviewedAt) {}
