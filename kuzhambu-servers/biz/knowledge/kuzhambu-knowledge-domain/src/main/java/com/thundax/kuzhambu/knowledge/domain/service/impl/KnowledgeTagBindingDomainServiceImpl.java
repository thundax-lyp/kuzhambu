package com.thundax.kuzhambu.knowledge.domain.service.impl;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.knowledge.domain.service.KnowledgeTagBindingDomainService;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagAlias;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagCategory;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagContentRef;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.ContentType;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagCategoryStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagReviewStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagContentRefId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagAliasRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagCategoryRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagContentRefRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeTagBindingDomainServiceImpl implements KnowledgeTagBindingDomainService {
    private static final TagCategoryId DEFAULT_CATEGORY_ID = TagCategoryId.of(1999L);

    private final TagRepository tagRepository;
    private final TagAliasRepository tagAliasRepository;
    private final TagCategoryRepository tagCategoryRepository;
    private final TagContentRefRepository tagContentRefRepository;
    private final SnowflakeIdGenerator idGenerator;

    @Autowired
    public KnowledgeTagBindingDomainServiceImpl(
            TagRepository tagRepository,
            TagAliasRepository tagAliasRepository,
            TagCategoryRepository tagCategoryRepository,
            TagContentRefRepository tagContentRefRepository) {
        this(
                tagRepository,
                tagAliasRepository,
                tagCategoryRepository,
                tagContentRefRepository,
                new SnowflakeIdGenerator());
    }

    KnowledgeTagBindingDomainServiceImpl(
            TagRepository tagRepository,
            TagAliasRepository tagAliasRepository,
            TagCategoryRepository tagCategoryRepository,
            TagContentRefRepository tagContentRefRepository,
            SnowflakeIdGenerator idGenerator) {
        this.tagRepository = tagRepository;
        this.tagAliasRepository = tagAliasRepository;
        this.tagCategoryRepository = tagCategoryRepository;
        this.tagContentRefRepository = tagContentRefRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public Tag resolveTagByNameOrAlias(String name) {
        String normalizedName = normalizeName(name);
        Tag directTag = tagRepository.getByName(normalizedName);
        if (directTag != null) {
            return resolveBindableTag(directTag, normalizedName);
        }

        TagAlias alias = tagAliasRepository.getByName(normalizedName);
        if (alias == null || alias.getTagId() == null) {
            return null;
        }

        Tag aliasTarget = tagRepository.getByTagId(alias.getTagId());
        if (aliasTarget == null) {
            return null;
        }
        return resolveBindableTag(aliasTarget, normalizedName);
    }

    @Override
    public Tag resolveOrCreateManualTag(String name) {
        Tag existing = resolveTagByNameOrAlias(name);
        if (existing != null) {
            return existing;
        }
        return createTag(normalizeName(name), TagSource.MANUAL, TagReviewStatus.APPROVED, new Date());
    }

    @Override
    public Tag resolveOrCreateAiTag(String name) {
        Tag existing = resolveTagByNameOrAlias(name);
        if (existing != null) {
            return existing;
        }
        return createTag(normalizeName(name), TagSource.AI_EXTRACTED, TagReviewStatus.PENDING, null);
    }

    @Override
    public void syncContentTagRef(
            TagId tagId, ContentType contentType, Long contentId, String contentTitle, TagSource source) {
        if (tagId == null || contentType == null || contentId == null) {
            return;
        }
        if (tagContentRefRepository.countByTagAndContentTypeAndContentId(tagId, contentType, contentId, null) > 0) {
            return;
        }

        TagContentRef ref = new TagContentRef();
        ref.setRefId(TagContentRefId.of(idGenerator.nextId().value()));
        ref.setTagId(tagId);
        ref.setContentType(contentType);
        ref.setContentId(contentId);
        ref.setContentTitle(StringUtils.defaultIfBlank(
                StringUtils.trimToNull(contentTitle), contentType.value() + ":" + contentId));
        ref.setSource(source == null ? TagSource.MANUAL : source);
        tagContentRefRepository.insert(ref);
    }

    @Override
    public void removeContentTagRef(TagId tagId, ContentType contentType, Long contentId) {
        if (tagId == null || contentType == null || contentId == null) {
            return;
        }
        List<TagContentRef> refs = tagContentRefRepository.listByTagId(tagId);
        if (refs == null || refs.isEmpty()) {
            return;
        }
        refs.stream()
                .filter(ref -> ref != null
                        && ref.getId() != null
                        && ref.getContentType() == contentType
                        && contentId.equals(ref.getContentId()))
                .forEach(ref -> tagContentRefRepository.deleteById(ref.getId()));
    }

    private Tag createTag(String normalizedName, TagSource source, TagReviewStatus reviewStatus, Date reviewedAt) {
        TagCategory category = requireDefaultCategory();
        Date now = new Date();
        Tag tag = new Tag();
        tag.setTagId(TagId.of(idGenerator.nextId().value()));
        tag.setName(normalizedName);
        tag.setCategoryId(category.getCategoryId());
        tag.setStatus(TagStatus.ENABLED);
        tag.setSource(source);
        tag.setReviewStatus(reviewStatus);
        tag.setCreatedAt(now);
        tag.setReviewedAt(reviewedAt);

        TagId createdTagId = tagRepository.insert(tag);
        return tagRepository.getByTagId(createdTagId);
    }

    private TagCategory requireDefaultCategory() {
        TagCategory category = tagCategoryRepository.getByCategoryId(DEFAULT_CATEGORY_ID);
        if (category == null) {
            throw new DomainException(
                    "KNOWLEDGE-10011",
                    "knowledge.taxonomy.tag.category.default.missing",
                    "Default category is missing: " + DEFAULT_CATEGORY_ID.value());
        }
        if (category.getStatus() != TagCategoryStatus.ENABLED) {
            throw new DomainException(
                    "KNOWLEDGE-10012",
                    "knowledge.taxonomy.tag.category.default.disabled",
                    "Default category is disabled: " + DEFAULT_CATEGORY_ID.value());
        }
        return category;
    }

    private void assertTagBindable(Tag tag, String name) {
        if (tag.getStatus() == TagStatus.DISABLED) {
            throw new DomainException(
                    "KNOWLEDGE-10013",
                    "knowledge.taxonomy.tag.binding.disabled",
                    "Tag is disabled and cannot be bound: " + name);
        }
    }

    private Tag resolveBindableTag(Tag tag, String name) {
        Tag current = tag;
        while (current != null && current.getMergedToTagId() != null) {
            Tag mergedTarget = tagRepository.getByTagId(current.getMergedToTagId());
            if (mergedTarget == null) {
                throw new DomainException(
                        "KNOWLEDGE-10014",
                        "knowledge.taxonomy.tag.binding.merge_target_missing",
                        "Merged target is missing for tag binding: " + name);
            }
            current = mergedTarget;
        }
        if (current == null) {
            return null;
        }
        assertTagBindable(current, name);
        return current;
    }

    private String normalizeName(String name) {
        String normalizedName = StringUtils.trimToNull(name);
        if (normalizedName == null) {
            throw new DomainException(
                    "KNOWLEDGE-10010", "knowledge.taxonomy.tag.binding.name.required", "Tag name is required");
        }
        return normalizedName;
    }
}
