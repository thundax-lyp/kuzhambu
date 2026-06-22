package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagReviewStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    private TagId id;
    private TagId tagId;
    private String name;
    private TagCategoryId categoryId;
    private String description;
    private TagStatus status;
    private TagSource source;
    private TagReviewStatus reviewStatus;
    private String reviewNote;
    private Date createdAt;
    private Date reviewedAt;
}
