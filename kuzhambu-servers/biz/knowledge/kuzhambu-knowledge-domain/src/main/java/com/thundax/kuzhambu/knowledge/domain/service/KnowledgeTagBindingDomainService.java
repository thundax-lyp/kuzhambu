package com.thundax.kuzhambu.knowledge.domain.service;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.ContentType;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;

public interface KnowledgeTagBindingDomainService {

    Tag resolveTagByNameOrAlias(String name);

    Tag resolveOrCreateManualTag(String name);

    Tag resolveOrCreateAiTag(String name);

    void syncContentTagRef(TagId tagId, ContentType contentType, Long contentId, String contentTitle, TagSource source);

    void removeContentTagRef(TagId tagId, ContentType contentType, Long contentId);
}
