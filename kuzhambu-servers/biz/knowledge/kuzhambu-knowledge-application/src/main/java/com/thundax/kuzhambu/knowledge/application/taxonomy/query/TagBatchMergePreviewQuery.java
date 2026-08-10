package com.thundax.kuzhambu.knowledge.application.taxonomy.query;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import java.util.List;

public record TagBatchMergePreviewQuery(List<TagId> sourceTagIds, TagId targetTagId) {}
