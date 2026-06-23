package com.thundax.kuzhambu.knowledge.domain.taxonomy.repository;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagAlias;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagAliasId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import java.util.List;

public interface TagAliasRepository {

    TagAlias getById(TagAliasId id);

    default TagAlias getByName(String name) {
        return null;
    }

    List<TagAlias> listByTagId(TagId tagId);

    int countByName(String name, TagAliasId excludedId);

    TagAliasId insert(TagAlias entity);

    int deleteById(TagAliasId id);
}
