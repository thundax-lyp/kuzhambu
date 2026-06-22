package com.thundax.kuzhambu.knowledge.application.taxonomy.assembler;

import com.thundax.kuzhambu.knowledge.application.taxonomy.result.SynonymResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagAliasResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagCategoryResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagContentRefResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagDetailResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagResult;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Synonym;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagAlias;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagCategory;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagContentRef;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class TaxonomyApplicationAssembler {
    private TaxonomyApplicationAssembler() {}

    public static TagCategoryResult toResult(TagCategory category) {
        if (category == null) {
            return new TagCategoryResult();
        }

        return new TagCategoryResult(
                category.getCategoryId() == null
                        ? null
                        : String.valueOf(category.getCategoryId().value()),
                category.getName(),
                category.getDescription(),
                category.getPriority(),
                category.getStatus() == null ? null : category.getStatus().value());
    }

    public static TagResult toResult(Tag tag, String categoryName, int contentRefCount, String categoryId) {
        if (tag == null) {
            return new TagResult();
        }

        return new TagResult(
                tag.getTagId() == null ? null : String.valueOf(tag.getTagId().value()),
                tag.getName(),
                categoryId,
                categoryName,
                tag.getDescription(),
                tag.getStatus() == null ? null : tag.getStatus().value(),
                tag.getSource() == null ? null : tag.getSource().value(),
                tag.getReviewStatus() == null ? null : tag.getReviewStatus().value(),
                contentRefCount,
                tag.getCreatedAt() == null ? null : tag.getCreatedAt().getTime(),
                tag.getReviewedAt() == null ? null : tag.getReviewedAt().getTime());
    }

    public static TagResult toResult(Tag tag, String categoryName, int contentRefCount, Long categoryId) {
        return toResult(tag, categoryName, contentRefCount, categoryId == null ? null : String.valueOf(categoryId));
    }

    public static TagResult toResult(Tag tag, String categoryName, int contentRefCount) {
        return toResult(
                tag,
                categoryName,
                contentRefCount,
                tag == null || tag.getCategoryId() == null
                        ? null
                        : String.valueOf(tag.getCategoryId().value()));
    }

    public static TagDetailResult toDetailResult(
            Tag tag, List<TagAlias> aliases, List<TagContentRef> refs, String categoryName) {
        return new TagDetailResult(
                toResult(tag, categoryName, refs == null ? 0 : refs.size()),
                toAliasResultList(aliases),
                toContentRefResultList(refs));
    }

    public static TagAliasResult toAliasResult(TagAlias alias) {
        if (alias == null) {
            return new TagAliasResult();
        }

        return new TagAliasResult(
                alias.getAliasId() == null
                        ? null
                        : String.valueOf(alias.getAliasId().value()),
                alias.getName(),
                alias.getSource() == null ? null : alias.getSource().value());
    }

    public static List<TagAliasResult> toAliasResultList(List<TagAlias> list) {
        return list == null
                ? new ArrayList<>()
                : list.stream()
                        .filter(Objects::nonNull)
                        .map(TaxonomyApplicationAssembler::toAliasResult)
                        .collect(Collectors.toList());
    }

    public static TagContentRefResult toContentRefResult(TagContentRef ref) {
        if (ref == null) {
            return new TagContentRefResult();
        }

        return new TagContentRefResult(
                ref.getRefId() == null ? null : String.valueOf(ref.getRefId().value()),
                ref.getContentType() == null ? null : ref.getContentType().value(),
                ref.getContentId() == null ? null : String.valueOf(ref.getContentId()),
                ref.getContentTitle(),
                ref.getSource() == null ? null : ref.getSource().value());
    }

    public static List<TagContentRefResult> toContentRefResultList(List<TagContentRef> list) {
        return list == null
                ? new ArrayList<>()
                : list.stream()
                        .filter(Objects::nonNull)
                        .map(TaxonomyApplicationAssembler::toContentRefResult)
                        .collect(Collectors.toList());
    }

    public static SynonymResult toSynonymResult(Synonym synonym) {
        if (synonym == null) {
            return new SynonymResult();
        }

        return new SynonymResult(
                synonym.getSynonymId() == null
                        ? null
                        : String.valueOf(synonym.getSynonymId().value()),
                synonym.getTerm(),
                synonym.getSynonym(),
                synonym.getStatus() == null ? null : synonym.getStatus().value());
    }

    public static List<SynonymResult> toSynonymResultList(List<Synonym> list) {
        return list == null
                ? new ArrayList<>()
                : list.stream()
                        .filter(Objects::nonNull)
                        .map(TaxonomyApplicationAssembler::toSynonymResult)
                        .collect(Collectors.toList());
    }
}
