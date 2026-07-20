package com.thundax.kuzhambu.knowledge.domain.taxonomy.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagCategory;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagCategoryStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;

public interface TagCategoryRepository {

    TagCategory getById(TagCategoryId id);

    TagCategory getByCategoryId(TagCategoryId categoryId);

    PageResult<TagCategory> page(String name, TagCategoryStatus status, int pageNo, int pageSize);

    TagCategoryId insert(TagCategory entity);

    int update(TagCategory entity);

    int maxPriority();

    int updateStatus(TagCategory entity);

    int countByName(String name, TagCategoryId excludedId);

    int countEnabledByCategoryId(TagCategoryId categoryId);
}
