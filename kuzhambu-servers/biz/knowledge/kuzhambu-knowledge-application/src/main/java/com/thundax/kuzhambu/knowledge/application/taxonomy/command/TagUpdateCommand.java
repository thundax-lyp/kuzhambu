package com.thundax.kuzhambu.knowledge.application.taxonomy.command;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagUpdateCommand {
    private TagId id;
    private String name;
    private TagCategoryId categoryId;
    private String description;
}
