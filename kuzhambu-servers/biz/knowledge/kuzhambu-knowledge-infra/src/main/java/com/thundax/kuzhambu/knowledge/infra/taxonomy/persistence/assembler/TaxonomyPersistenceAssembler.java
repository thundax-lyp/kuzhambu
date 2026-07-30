package com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagAliasIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagCategoryIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagContentRefIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagAlias;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagCategory;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagContentRef;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.ContentType;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagCategoryStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagReviewStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagAliasDO;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagCategoryDO;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagContentRefDO;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagDO;
import java.util.ArrayList;
import java.util.List;

public final class TaxonomyPersistenceAssembler {

    private TaxonomyPersistenceAssembler() {}

    public static TagCategoryDO toObject(TagCategory entity) {
        if (entity == null) {
            return null;
        }

        TagCategoryDO dataObject = new TagCategoryDO();
        dataObject.setCategoryId(TagCategoryIdCodec.toValue(entity.getId()));
        dataObject.setName(entity.getName());
        dataObject.setDescription(entity.getDescription());
        dataObject.setPriority(entity.getPriority());
        dataObject.setStatus(statusValue(entity.getStatus()));
        return dataObject;
    }

    public static TagCategory toDomain(TagCategoryDO dataObject) {
        if (dataObject == null) {
            return null;
        }

        TagCategory entity = new TagCategory();
        entity.setId(TagCategoryIdCodec.toDomain(dataObject.getCategoryId()));
        entity.setName(dataObject.getName());
        entity.setDescription(dataObject.getDescription());
        entity.setPriority(priorityOrDefault(dataObject.getPriority()));
        entity.setStatus(tagCategoryStatusFrom(dataObject.getStatus()));
        return entity;
    }

    public static List<TagCategory> toTagCategoryDomainList(List<TagCategoryDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }

        List<TagCategory> entities = new ArrayList<>();
        for (TagCategoryDO dataObject : dataObjects) {
            entities.add(toDomain(dataObject));
        }
        return entities;
    }

    public static TagDO toObject(Tag entity) {
        if (entity == null) {
            return null;
        }

        TagDO dataObject = new TagDO();
        dataObject.setTagId(TagIdCodec.toValue(entity.getId()));
        dataObject.setName(entity.getName());
        dataObject.setCategoryId(TagCategoryIdCodec.toValue(entity.getCategoryId()));
        dataObject.setDescription(entity.getDescription());
        dataObject.setStatus(statusValue(entity.getStatus()));
        dataObject.setSource(sourceValue(entity.getSource()));
        dataObject.setReviewStatus(reviewStatusValue(entity.getReviewStatus()));
        dataObject.setReviewNote(entity.getReviewNote());
        dataObject.setCreatedAt(entity.getCreatedAt());
        dataObject.setReviewedAt(entity.getReviewedAt());
        dataObject.setMergedToTagId(TagIdCodec.toValue(entity.getMergedToTagId()));
        dataObject.setDeprecatedAt(entity.getDeprecatedAt());
        dataObject.setDeprecatedBy(entity.getDeprecatedBy());
        return dataObject;
    }

    public static Tag toDomain(TagDO dataObject) {
        if (dataObject == null) {
            return null;
        }

        Tag entity = new Tag();
        entity.setId(TagIdCodec.toDomain(dataObject.getTagId()));
        entity.setName(dataObject.getName());
        entity.setCategoryId(TagCategoryIdCodec.toDomain(dataObject.getCategoryId()));
        entity.setDescription(dataObject.getDescription());
        entity.setStatus(tagStatusFrom(dataObject.getStatus()));
        entity.setSource(sourceFrom(dataObject.getSource()));
        entity.setReviewStatus(reviewStatusFrom(dataObject.getReviewStatus()));
        entity.setReviewNote(dataObject.getReviewNote());
        entity.setCreatedAt(dataObject.getCreatedAt());
        entity.setReviewedAt(dataObject.getReviewedAt());
        entity.setMergedToTagId(TagIdCodec.toDomain(dataObject.getMergedToTagId()));
        entity.setDeprecatedAt(dataObject.getDeprecatedAt());
        entity.setDeprecatedBy(dataObject.getDeprecatedBy());
        return entity;
    }

    public static List<Tag> toTagDomainList(List<TagDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }

        List<Tag> entities = new ArrayList<>();
        for (TagDO dataObject : dataObjects) {
            entities.add(toDomain(dataObject));
        }
        return entities;
    }

    public static TagAliasDO toObject(TagAlias entity) {
        if (entity == null) {
            return null;
        }

        TagAliasDO dataObject = new TagAliasDO();
        dataObject.setAliasId(TagAliasIdCodec.toValue(entity.getId()));
        dataObject.setTagId(TagIdCodec.toValue(entity.getTagId()));
        dataObject.setName(entity.getName());
        dataObject.setSource(sourceValue(entity.getSource()));
        return dataObject;
    }

    public static TagAlias toDomain(TagAliasDO dataObject) {
        if (dataObject == null) {
            return null;
        }

        TagAlias entity = new TagAlias();
        entity.setId(TagAliasIdCodec.toDomain(dataObject.getAliasId()));
        entity.setTagId(TagIdCodec.toDomain(dataObject.getTagId()));
        entity.setName(dataObject.getName());
        entity.setSource(sourceFrom(dataObject.getSource()));
        return entity;
    }

    public static List<TagAlias> toTagAliasDomainList(List<TagAliasDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }

        List<TagAlias> entities = new ArrayList<>();
        for (TagAliasDO dataObject : dataObjects) {
            entities.add(toDomain(dataObject));
        }
        return entities;
    }

    public static TagContentRefDO toObject(TagContentRef entity) {
        if (entity == null) {
            return null;
        }

        TagContentRefDO dataObject = new TagContentRefDO();
        dataObject.setRefId(TagContentRefIdCodec.toValue(entity.getId()));
        dataObject.setTagId(TagIdCodec.toValue(entity.getTagId()));
        dataObject.setContentType(contentTypeValue(entity.getContentType()));
        dataObject.setContentId(entity.getContentId());
        dataObject.setContentTitle(entity.getContentTitle());
        dataObject.setSource(sourceValue(entity.getSource()));
        return dataObject;
    }

    public static TagContentRef toDomain(TagContentRefDO dataObject) {
        if (dataObject == null) {
            return null;
        }

        TagContentRef entity = new TagContentRef();
        entity.setId(TagContentRefIdCodec.toDomain(dataObject.getRefId()));
        entity.setTagId(TagIdCodec.toDomain(dataObject.getTagId()));
        entity.setContentType(contentTypeFrom(dataObject.getContentType()));
        entity.setContentId(dataObject.getContentId());
        entity.setContentTitle(dataObject.getContentTitle());
        entity.setSource(sourceFrom(dataObject.getSource()));
        return entity;
    }

    public static List<TagContentRef> toTagContentRefDomainList(List<TagContentRefDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }

        List<TagContentRef> entities = new ArrayList<>();
        for (TagContentRefDO dataObject : dataObjects) {
            entities.add(toDomain(dataObject));
        }
        return entities;
    }

    private static int priorityOrDefault(Integer priority) {
        return priority == null ? 0 : priority;
    }

    private static String statusValue(TagCategoryStatus status) {
        return status == null ? null : status.value();
    }

    private static TagCategoryStatus tagCategoryStatusFrom(String status) {
        return status == null ? null : TagCategoryStatus.from(status);
    }

    private static String statusValue(TagStatus status) {
        return status == null ? null : status.value();
    }

    private static TagStatus tagStatusFrom(String status) {
        return status == null ? null : TagStatus.from(status);
    }

    private static String sourceValue(TagSource source) {
        return source == null ? null : source.value();
    }

    private static TagSource sourceFrom(String source) {
        return source == null ? null : TagSource.from(source);
    }

    private static String reviewStatusValue(TagReviewStatus reviewStatus) {
        return reviewStatus == null ? null : reviewStatus.value();
    }

    private static TagReviewStatus reviewStatusFrom(String reviewStatus) {
        return reviewStatus == null ? null : TagReviewStatus.from(reviewStatus);
    }

    private static String contentTypeValue(ContentType contentType) {
        return contentType == null ? null : contentType.value();
    }

    private static ContentType contentTypeFrom(String contentType) {
        return contentType == null ? null : ContentType.from(contentType);
    }
}
