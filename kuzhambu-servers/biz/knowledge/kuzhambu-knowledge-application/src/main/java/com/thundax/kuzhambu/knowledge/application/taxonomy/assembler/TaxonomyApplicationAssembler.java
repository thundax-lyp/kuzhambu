package com.thundax.kuzhambu.knowledge.application.taxonomy.assembler;

import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagAliasResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagCategoryResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagContentRefResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagDetailResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagResult;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagAlias;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagCategory;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagContentRef;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;

public final class TaxonomyApplicationAssembler {
    private TaxonomyApplicationAssembler() {}

    @NonNull
    public static TagCategoryResult toResult(@NonNull TagCategory category) {
        Objects.requireNonNull(category, "category must not be null");

        return new TagCategoryResult(
                category.getId() == null
                        ? null
                        : String.valueOf(category.getId().value()),
                category.getName(),
                category.getDescription(),
                category.getPriority(),
                category.getStatus() == null ? null : category.getStatus().value());
    }

    @NonNull
    public static TagResult toResult(
            @NonNull Tag tag, @NonNull String categoryName, int contentRefCount, @NonNull String categoryId) {
        Objects.requireNonNull(tag, "tag must not be null");
        Objects.requireNonNull(categoryName, "categoryName must not be null");
        Objects.requireNonNull(categoryId, "categoryId must not be null");

        return new TagResult(
                tag.getId() == null ? null : String.valueOf(tag.getId().value()),
                tag.getName(),
                categoryId,
                categoryName,
                tag.getDescription(),
                tag.getStatus() == null ? null : tag.getStatus().value(),
                tag.getSource() == null ? null : tag.getSource().value(),
                tag.getReviewStatus() == null ? null : tag.getReviewStatus().value(),
                contentRefCount,
                tag.getCreatedAt() == null ? null : tag.getCreatedAt().toEpochMilli(),
                tag.getReviewedAt() == null ? null : tag.getReviewedAt().toEpochMilli());
    }

    @NonNull
    public static TagResult toResult(
            @NonNull Tag tag, @NonNull String categoryName, int contentRefCount, @NonNull Long categoryId) {
        return toResult(tag, categoryName, contentRefCount, String.valueOf(categoryId));
    }

    @NonNull
    public static TagResult toResult(@NonNull Tag tag, @NonNull String categoryName, int contentRefCount) {
        Objects.requireNonNull(tag, "tag must not be null");
        Objects.requireNonNull(categoryName, "categoryName must not be null");
        return toResult(
                tag,
                categoryName,
                contentRefCount,
                tag.getCategoryId() == null
                        ? ""
                        : String.valueOf(tag.getCategoryId().value()));
    }

    @NonNull
    public static TagDetailResult toDetailResult(
            @NonNull Tag tag,
            @NonNull List<TagAlias> aliases,
            @NonNull List<TagContentRef> refs,
            @NonNull String categoryName) {
        Objects.requireNonNull(tag, "tag must not be null");
        Objects.requireNonNull(aliases, "aliases must not be null");
        Objects.requireNonNull(refs, "refs must not be null");
        Objects.requireNonNull(categoryName, "categoryName must not be null");
        return new TagDetailResult(
                toResult(tag, categoryName, refs.size()), toAliasResultList(aliases), toContentRefResultList(refs));
    }

    @NonNull
    public static TagAliasResult toAliasResult(TagAlias alias) {
        Objects.requireNonNull(alias, "alias must not be null");

        return new TagAliasResult(
                alias.getId() == null ? null : String.valueOf(alias.getId().value()),
                alias.getName(),
                alias.getSource() == null ? null : alias.getSource().value());
    }

    @NonNull
    public static List<TagAliasResult> toAliasResultList(List<TagAlias> list) {
        Objects.requireNonNull(list, "list must not be null");
        return list.stream()
                .filter(Objects::nonNull)
                .map(TaxonomyApplicationAssembler::toAliasResult)
                .collect(Collectors.toList());
    }

    @NonNull
    public static TagContentRefResult toContentRefResult(TagContentRef ref) {
        Objects.requireNonNull(ref, "ref must not be null");

        return new TagContentRefResult(
                ref.getId() == null ? null : String.valueOf(ref.getId().value()),
                ref.getContentType() == null ? null : ref.getContentType().value(),
                ref.getContentId() == null ? null : String.valueOf(ref.getContentId()),
                ref.getContentTitle(),
                ref.getSource() == null ? null : ref.getSource().value());
    }

    @NonNull
    public static List<TagContentRefResult> toContentRefResultList(List<TagContentRef> list) {
        Objects.requireNonNull(list, "list must not be null");
        return list.stream()
                .filter(Objects::nonNull)
                .map(TaxonomyApplicationAssembler::toContentRefResult)
                .collect(Collectors.toList());
    }
}
