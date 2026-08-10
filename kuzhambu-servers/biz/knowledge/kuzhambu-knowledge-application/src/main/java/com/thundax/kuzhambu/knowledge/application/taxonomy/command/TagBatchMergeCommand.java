package com.thundax.kuzhambu.knowledge.application.taxonomy.command;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import java.util.List;

public record TagBatchMergeCommand(List<TagId> sourceTagIds, TagId targetTagId) {}
