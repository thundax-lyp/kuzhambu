package com.thundax.kuzhambu.knowledge.domain.taxonomy.repository;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagContentRef;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.ContentType;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagContentRefId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import java.util.List;

public interface TagContentRefRepository {

    List<TagContentRef> listByTagId(TagId tagId);

    int countByTagId(TagId tagId);

    int countByTagAndContentTypeAndContentId(
            TagId tagId, ContentType contentType, Long contentId, TagContentRefId excludedId);

    TagContentRefId insert(TagContentRef entity);

    int deleteById(TagContentRefId id);
}
