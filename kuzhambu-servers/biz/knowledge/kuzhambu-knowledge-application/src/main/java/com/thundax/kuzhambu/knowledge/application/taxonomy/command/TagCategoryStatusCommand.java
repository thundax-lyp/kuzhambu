package com.thundax.kuzhambu.knowledge.application.taxonomy.command;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagCategoryStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;

public record TagCategoryStatusCommand(TagCategoryId id, TagCategoryStatus status) {}
