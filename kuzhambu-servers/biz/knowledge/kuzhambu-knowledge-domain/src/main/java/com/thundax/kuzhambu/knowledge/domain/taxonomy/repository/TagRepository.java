package com.thundax.kuzhambu.knowledge.domain.taxonomy.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagReviewStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import java.util.List;

public interface TagRepository {

    Tag getById(TagId id);

    Tag getByTagId(TagId tagId);

    Tag getByName(String name);

    List<Tag> listByTagIds(List<TagId> tagIds);

    PageResult<Tag> page(
            String name,
            TagCategoryId categoryId,
            TagStatus status,
            TagSource source,
            TagReviewStatus reviewStatus,
            int pageNo,
            int pageSize);

    PageResult<Tag> pagePending(int pageNo, int pageSize);

    int countByName(String name, TagId excludedId);

    TagId insert(Tag entity);

    int update(Tag entity);

    int updateStatus(Tag entity);

    int updateReviewStatus(Tag entity);
}
