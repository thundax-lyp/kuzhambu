package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity;

import com.thundax.kuzhambu.common.core.exception.DomainException;
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
    private TagId mergedToTagId;
    private Date deprecatedAt;
    private Long deprecatedBy;

    public Tag(
            TagId id,
            TagId tagId,
            String name,
            TagCategoryId categoryId,
            String description,
            TagStatus status,
            TagSource source,
            TagReviewStatus reviewStatus,
            String reviewNote,
            Date createdAt,
            Date reviewedAt) {
        this(
                id,
                tagId,
                name,
                categoryId,
                description,
                status,
                source,
                reviewStatus,
                reviewNote,
                createdAt,
                reviewedAt,
                null,
                null,
                null);
    }

    public boolean isMerged() {
        return mergedToTagId != null;
    }

    public boolean isUsableForNewBinding() {
        return status == TagStatus.ENABLED && !isMerged() && !isDeprecated();
    }

    public boolean isDeprecated() {
        return status == TagStatus.DISABLED || deprecatedAt != null;
    }

    public void mergeInto(Tag targetTag) {
        if (targetTag == null || targetTag.getTagId() == null) {
            throw new DomainException("目标标签不能为空");
        }
        if (tagId == null) {
            throw new DomainException("源标签不能为空");
        }
        if (tagId.equals(targetTag.getTagId())) {
            throw new DomainException("源标签不能并入自身");
        }
        if (!isUsableForNewBinding()) {
            throw new DomainException("源标签当前不可用于合并");
        }
        if (!targetTag.isUsableForNewBinding()) {
            throw new DomainException("目标标签当前不可作为合并目标");
        }
        mergedToTagId = targetTag.getTagId();
    }

    public void deprecate(Date operatedAt, Long operatorId) {
        if (tagId == null) {
            throw new DomainException("待废弃标签不能为空");
        }
        if (isDeprecated()) {
            throw new DomainException("标签已废弃");
        }
        status = TagStatus.DISABLED;
        deprecatedAt = operatedAt == null ? new Date() : operatedAt;
        deprecatedBy = operatorId;
    }
}
