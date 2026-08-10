package com.thundax.kuzhambu.knowledge.application.taxonomy.command;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;

public record TagCategoryUpdateCommand(TagCategoryId id, String name, String description) {}
