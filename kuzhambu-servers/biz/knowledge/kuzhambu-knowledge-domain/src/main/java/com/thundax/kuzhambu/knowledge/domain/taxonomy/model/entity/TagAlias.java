package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagAliasId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagAlias {
    private TagAliasId id;
    private TagAliasId aliasId;
    private TagId tagId;
    private String name;
    private TagSource source;
}
