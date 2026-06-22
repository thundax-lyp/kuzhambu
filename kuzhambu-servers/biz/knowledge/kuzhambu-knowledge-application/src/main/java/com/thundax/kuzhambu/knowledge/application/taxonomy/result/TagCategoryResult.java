package com.thundax.kuzhambu.knowledge.application.taxonomy.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagCategoryResult {
    private String id;
    private String name;
    private String description;
    private int priority;
    private String status;
}
