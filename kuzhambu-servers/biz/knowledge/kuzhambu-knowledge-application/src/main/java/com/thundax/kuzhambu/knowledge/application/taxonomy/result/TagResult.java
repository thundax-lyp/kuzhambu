package com.thundax.kuzhambu.knowledge.application.taxonomy.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagResult {
    private String id;
    private String name;
    private String categoryId;
    private String categoryName;
    private String description;
    private String status;
    private String source;
    private String reviewStatus;
    private int contentRefCount;
    private Long createdAt;
    private Long reviewedAt;
}
