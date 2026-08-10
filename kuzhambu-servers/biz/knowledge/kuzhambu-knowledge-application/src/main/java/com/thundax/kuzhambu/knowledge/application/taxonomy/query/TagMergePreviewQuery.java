package com.thundax.kuzhambu.knowledge.application.taxonomy.query;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;

public record TagMergePreviewQuery(TagId sourceTagId, TagId targetTagId) {}
