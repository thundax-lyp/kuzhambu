package com.thundax.kuzhambu.knowledge.application.taxonomy.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.assembler.TaxonomyApplicationAssembler;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.SynonymCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.SynonymRemoveCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.SynonymStatusCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.SynonymUpdateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagAliasCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagAliasRemoveCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryStatusCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryUpdateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagReviewCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagStatusCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagUpdateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.SynonymPageQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagCategoryPageQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagMergePreviewQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagPageQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagReviewPageQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.SynonymResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagAliasResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagCategoryResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagDetailResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagMergePreviewResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.service.TaxonomyApplicationService;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Synonym;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagAlias;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagCategory;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.SynonymStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagCategoryStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagReviewStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.SynonymId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagAliasId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.SynonymRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagAliasRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagCategoryRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagContentRefRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class TaxonomyApplicationServiceImpl implements TaxonomyApplicationService {

    private static final String APPROVE_DECISION = "APPROVE";
    private static final String REJECT_DECISION = "REJECT";

    private final TagCategoryRepository tagCategoryRepository;
    private final TagRepository tagRepository;
    private final TagAliasRepository tagAliasRepository;
    private final TagContentRefRepository tagContentRefRepository;
    private final SynonymRepository synonymRepository;

    public TaxonomyApplicationServiceImpl(
            TagCategoryRepository tagCategoryRepository,
            TagRepository tagRepository,
            TagAliasRepository tagAliasRepository,
            TagContentRefRepository tagContentRefRepository,
            SynonymRepository synonymRepository) {
        this.tagCategoryRepository = tagCategoryRepository;
        this.tagRepository = tagRepository;
        this.tagAliasRepository = tagAliasRepository;
        this.tagContentRefRepository = tagContentRefRepository;
        this.synonymRepository = synonymRepository;
    }

    @Override
    public PageResult<TagCategoryResult> pageCategories(TagCategoryPageQuery query, PageQuery page) {
        TagCategoryPageQuery effectiveQuery = query == null ? new TagCategoryPageQuery() : query;
        PageQuery effectivePage = normalize(page);

        PageResult<TagCategory> pageResult = tagCategoryRepository.page(
                normalizeQueryText(effectiveQuery.getName()),
                effectiveQuery.getStatus(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());

        return PageResult.of(
                pageResult.getPageNo(),
                pageResult.getPageSize(),
                pageResult.getTotalCount(),
                pageResult.getRecords().stream()
                        .filter(Objects::nonNull)
                        .map(TaxonomyApplicationAssembler::toResult)
                        .collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagCategoryId createCategory(TagCategoryCreateCommand command) {
        TagCategoryCreateCommand effective = ensureCommand(command, "标签分类创建命令");
        TagCategoryId categoryId = ensureId(effective.getId(), "categoryId");
        String name = trimText(effective.getName(), "分类名称");

        TagCategory category = new TagCategory();
        category.setCategoryId(categoryId);
        category.setName(name);
        category.setDescription(trimOptionalText(effective.getDescription()));
        category.setPriority(command.getPriority());
        category.setStatus(effective.getStatus() == null ? TagCategoryStatus.ENABLED : effective.getStatus());

        if (tagCategoryRepository.countByName(name, categoryId) > 0) {
            throw new BizException("标签分类名已存在: " + name);
        }

        return tagCategoryRepository.insert(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(TagCategoryUpdateCommand command) {
        TagCategoryUpdateCommand effective = ensureCommand(command, "标签分类更新命令");
        TagCategoryId categoryId = ensureId(effective.getId(), "categoryId");
        String name = trimText(effective.getName(), "分类名称");

        TagCategory category = getExistingCategory(categoryId);
        TagCategory updated = new TagCategory();
        updated.setCategoryId(categoryId);
        updated.setId(category.getId());
        updated.setName(name);
        updated.setDescription(trimOptionalText(effective.getDescription()));
        updated.setPriority(command.getPriority());
        updated.setStatus(category.getStatus());

        if (tagCategoryRepository.countByName(name, categoryId) > 0) {
            throw new BizException("标签分类名已存在: " + name);
        }

        if (tagCategoryRepository.update(updated) != 1) {
            throw new BizException("标签分类更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeCategoryStatus(TagCategoryStatusCommand command) {
        TagCategoryStatusCommand effective = ensureCommand(command, "标签分类状态命令");
        TagCategoryId categoryId = ensureId(effective.getId(), "categoryId");

        TagCategory category = getExistingCategory(categoryId);
        TagCategoryStatus status = requireStatus(effective.getStatus(), "categoryStatus");

        if (TagCategoryStatus.DISABLED == status
                && category != null
                && tagCategoryRepository.countEnabledByCategoryId(categoryId) > 0) {
            throw new BizException("禁用分类前必须先下线该分类下所有标签");
        }

        TagCategory updated = new TagCategory();
        updated.setId(category.getId());
        updated.setCategoryId(categoryId);
        updated.setStatus(status);

        if (tagCategoryRepository.updateStatus(updated) != 1) {
            throw new BizException("标签分类状态更新失败");
        }
    }

    @Override
    public PageResult<TagResult> pageTags(TagPageQuery query, PageQuery page) {
        TagPageQuery effectiveQuery = query == null ? new TagPageQuery() : query;
        PageQuery effectivePage = normalize(page);

        PageResult<Tag> pageResult = tagRepository.page(
                normalizeQueryText(effectiveQuery.getName()),
                effectiveQuery.getCategoryId(),
                effectiveQuery.getStatus(),
                effectiveQuery.getSource(),
                effectiveQuery.getReviewStatus(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());

        return PageResult.of(
                pageResult.getPageNo(),
                pageResult.getPageSize(),
                pageResult.getTotalCount(),
                pageResult.getRecords().stream()
                        .filter(Objects::nonNull)
                        .map(tag -> TaxonomyApplicationAssembler.toResult(
                                tag,
                                getCategoryName(tag.getCategoryId()),
                                tagContentRefRepository.countByTagId(tag.getTagId())))
                        .collect(Collectors.toList()));
    }

    @Override
    public TagDetailResult getTagDetail(TagId id) {
        Tag tag = ensureTagExists(id);

        return TaxonomyApplicationAssembler.toDetailResult(
                tag,
                tagAliasRepository.listByTagId(tag.getTagId()),
                tagContentRefRepository.listByTagId(tag.getTagId()),
                getCategoryName(tag.getCategoryId()));
    }

    @Override
    public TagMergePreviewResult previewTagMergeImpact(TagMergePreviewQuery query) {
        ensureCommand(query, "标签合并影响预览查询");
        ensureId(query.getSourceTagId(), "sourceTagId");
        ensureId(query.getTargetTagId(), "targetTagId");
        throw new BizException("标签合并影响预览读模型尚未实现");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagId createTag(TagCreateCommand command) {
        TagCreateCommand effective = ensureCommand(command, "标签创建命令");
        TagId tagId = ensureId(effective.getId(), "tagId");
        String name = trimText(effective.getName(), "标签名称");

        if (tagRepository.countByName(name, null) > 0) {
            throw new BizException("标签名已存在: " + name);
        }

        TagCategoryId categoryId = normalizeId(effective.getCategoryId());
        if (categoryId != null) {
            TagCategory category = getExistingCategory(categoryId);
            if (category.getStatus() != TagCategoryStatus.ENABLED) {
                throw new BizException("审核通过标签必须关联启用中的分类");
            }
        }

        Tag tag = new Tag();
        tag.setTagId(tagId);
        tag.setName(name);
        tag.setCategoryId(categoryId);
        tag.setDescription(trimOptionalText(effective.getDescription()));
        tag.setStatus(TagStatus.ENABLED);
        tag.setSource(TagSource.MANUAL);
        tag.setReviewStatus(TagReviewStatus.APPROVED);
        tag.setReviewNote(trimOptionalText(effective.getReviewNote()));
        tag.setCreatedAt(new Date());
        tag.setReviewedAt(effective.getReviewedAt() == null ? new Date() : effective.getReviewedAt());

        return tagRepository.insert(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTag(TagUpdateCommand command) {
        TagUpdateCommand effective = ensureCommand(command, "标签更新命令");
        TagId tagId = ensureId(effective.getId(), "tagId");
        Tag existing = ensureTagExists(tagId);
        String name = trimText(effective.getName(), "标签名称");

        if (tagRepository.countByName(name, tagId) > 0) {
            throw new BizException("标签名已存在: " + name);
        }

        TagCategoryId categoryId = normalizeId(effective.getCategoryId());
        if (categoryId != null) {
            getExistingCategory(categoryId);
        }

        Tag updated = new Tag();
        updated.setId(existing.getId());
        updated.setTagId(tagId);
        updated.setName(name);
        updated.setCategoryId(categoryId);
        updated.setDescription(trimOptionalText(effective.getDescription()));
        updated.setStatus(existing.getStatus());
        updated.setSource(existing.getSource());
        updated.setReviewStatus(existing.getReviewStatus());
        updated.setReviewNote(existing.getReviewNote());
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setReviewedAt(existing.getReviewedAt());

        if (tagRepository.update(updated) != 1) {
            throw new BizException("标签更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeTagStatus(TagStatusCommand command) {
        TagStatusCommand effective = ensureCommand(command, "标签状态命令");
        TagId tagId = ensureId(effective.getId(), "tagId");
        Tag tag = ensureTagExists(tagId);
        TagStatus status = requireStatus(effective.getStatus(), "tagStatus");

        Tag updated = new Tag();
        updated.setId(tag.getId());
        updated.setTagId(tagId);
        updated.setName(tag.getName());
        updated.setCategoryId(tag.getCategoryId());
        updated.setDescription(tag.getDescription());
        updated.setSource(tag.getSource());
        updated.setReviewStatus(tag.getReviewStatus());
        updated.setReviewNote(tag.getReviewNote());
        updated.setCreatedAt(tag.getCreatedAt());
        updated.setReviewedAt(tag.getReviewedAt());
        updated.setStatus(status);

        if (tagRepository.updateStatus(updated) != 1) {
            throw new BizException("标签状态更新失败");
        }
    }

    @Override
    public PageResult<TagResult> pagePendingTags(TagReviewPageQuery query, PageQuery page) {
        TagReviewPageQuery effectiveQuery = query == null ? new TagReviewPageQuery() : query;
        PageQuery effectivePage = normalize(page);

        PageResult<Tag> pageResult = tagRepository.pagePending(effectivePage.getPageNo(), effectivePage.getPageSize());

        return PageResult.of(
                pageResult.getPageNo(),
                pageResult.getPageSize(),
                pageResult.getTotalCount(),
                pageResult.getRecords().stream()
                        .filter(Objects::nonNull)
                        .filter(tag -> keepOnlyPending(tag, effectiveQuery))
                        .map(tag -> TaxonomyApplicationAssembler.toResult(
                                tag,
                                getCategoryName(tag.getCategoryId()),
                                tagContentRefRepository.countByTagId(tag.getTagId())))
                        .collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewTag(TagReviewCommand command) {
        TagReviewCommand effective = ensureCommand(command, "标签审核命令");
        Tag tag = ensureTagExists(effective.getId());
        String decision = normalizeDecision(effective.getDecision());
        String reviewNote = trimOptionalText(effective.getReviewNote());

        Tag reviewed = new Tag();
        reviewed.setId(tag.getId());
        reviewed.setTagId(tag.getTagId());
        reviewed.setName(tag.getName());
        reviewed.setCategoryId(tag.getCategoryId());
        reviewed.setDescription(tag.getDescription());
        reviewed.setStatus(tag.getStatus());
        reviewed.setSource(tag.getSource());
        reviewed.setCreatedAt(tag.getCreatedAt());
        reviewed.setReviewedAt(new Date());

        if (APPROVE_DECISION.equals(decision)) {
            reviewed.setReviewStatus(TagReviewStatus.APPROVED);
            TagCategoryId categoryId = ensureId(tag.getCategoryId(), "categoryId");
            TagCategory category = getExistingCategory(categoryId);
            if (category.getStatus() != TagCategoryStatus.ENABLED) {
                throw new BizException("审核通过标签必须关联启用中的分类");
            }
            reviewed.setReviewNote(reviewNote);
        } else if (REJECT_DECISION.equals(decision)) {
            if (StringUtils.isBlank(reviewNote)) {
                throw new BizException("拒绝标签必须填写审核说明");
            }
            reviewed.setReviewStatus(TagReviewStatus.REJECTED);
            reviewed.setReviewNote(reviewNote);
        } else {
            throw new BizException("非法审核决策: " + decision);
        }

        if (tagRepository.updateReviewStatus(reviewed) != 1) {
            throw new BizException("标签审核失败");
        }
    }

    @Override
    public List<TagAliasResult> listTagAliases(TagId tagId) {
        Tag tag = ensureTagExists(tagId);
        return TaxonomyApplicationAssembler.toAliasResultList(tagAliasRepository.listByTagId(tag.getTagId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagAliasId createTagAlias(TagAliasCreateCommand command) {
        TagAliasCreateCommand effective = ensureCommand(command, "标签别名创建命令");
        TagAliasId aliasId = ensureId(effective.getId(), "aliasId");
        String name = trimText(effective.getName(), "别名");
        TagId tagId = ensureId(effective.getTagId(), "tagId");
        ensureTagExists(tagId);

        if (tagAliasRepository.countByName(name, aliasId) > 0) {
            throw new BizException("别名已存在: " + name);
        }

        TagAlias alias = new TagAlias();
        alias.setAliasId(aliasId);
        alias.setTagId(tagId);
        alias.setName(name);
        alias.setSource(effective.getSource() == null ? TagSource.MANUAL : effective.getSource());

        return tagAliasRepository.insert(alias);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeTagAlias(TagAliasRemoveCommand command) {
        TagAliasRemoveCommand effective = ensureCommand(command, "标签别名删除命令");
        TagAliasId id = ensureId(effective.getId(), "aliasId");
        tagAliasRepository.deleteById(id);
    }

    @Override
    public PageResult<SynonymResult> pageSynonyms(SynonymPageQuery query, PageQuery page) {
        SynonymPageQuery effectiveQuery = query == null ? new SynonymPageQuery() : query;
        PageQuery effectivePage = normalize(page);

        PageResult<Synonym> pageResult = synonymRepository.page(
                normalizeQueryText(effectiveQuery.getTerm()),
                normalizeQueryText(effectiveQuery.getSynonym()),
                effectiveQuery.getStatus(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());

        return PageResult.of(
                pageResult.getPageNo(),
                pageResult.getPageSize(),
                pageResult.getTotalCount(),
                TaxonomyApplicationAssembler.toSynonymResultList(pageResult.getRecords()).stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SynonymId createSynonym(SynonymCreateCommand command) {
        SynonymCreateCommand effective = ensureCommand(command, "同义词创建命令");
        SynonymId synonymId = ensureId(effective.getId(), "synonymId");
        String term = trimText(effective.getTerm(), "同义词项");
        String synonymText = trimText(effective.getSynonym(), "同义词");

        if (term.equals(synonymText)) {
            throw new BizException("同义词不能与项重复");
        }
        if (synonymRepository.countByPair(term, synonymText, synonymId) > 0) {
            throw new BizException("同义词组已存在: " + term + "|" + synonymText);
        }

        Synonym synonym = new Synonym();
        synonym.setSynonymId(synonymId);
        synonym.setTerm(term);
        synonym.setSynonym(synonymText);
        synonym.setStatus(command.getStatus() == null ? SynonymStatus.ENABLED : command.getStatus());

        return synonymRepository.insert(synonym);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSynonym(SynonymUpdateCommand command) {
        SynonymUpdateCommand effective = ensureCommand(command, "同义词更新命令");
        SynonymId id = ensureId(effective.getId(), "synonymId");
        Synonym existing = ensureSynonymExists(id);
        String term = trimText(effective.getTerm(), "同义词项");
        String synonymText = trimText(effective.getSynonym(), "同义词");

        if (term.equals(synonymText)) {
            throw new BizException("同义词不能与项重复");
        }
        if (synonymRepository.countByPair(term, synonymText, id) > 0) {
            throw new BizException("同义词组已存在: " + term + "|" + synonymText);
        }

        Synonym updated = new Synonym();
        updated.setId(existing.getId());
        updated.setSynonymId(id);
        updated.setTerm(term);
        updated.setSynonym(synonymText);
        updated.setStatus(existing.getStatus());

        if (synonymRepository.update(updated) != 1) {
            throw new BizException("同义词更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeSynonymStatus(SynonymStatusCommand command) {
        SynonymStatusCommand effective = ensureCommand(command, "同义词状态命令");
        SynonymId id = ensureId(effective.getId(), "synonymId");
        SynonymStatus status = requireStatus(effective.getStatus(), "synonymStatus");
        Synonym updated = ensureSynonymExists(id);
        updated.setStatus(status);

        if (synonymRepository.updateStatus(updated) != 1) {
            throw new BizException("同义词状态更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeSynonym(SynonymRemoveCommand command) {
        SynonymRemoveCommand effective = ensureCommand(command, "同义词删除命令");
        SynonymId id = ensureId(effective.getId(), "synonymId");
        synonymRepository.deleteById(id);
    }

    private TagCategory getExistingCategory(TagCategoryId categoryId) {
        TagCategoryId effectiveCategoryId = ensureId(categoryId, "categoryId");
        TagCategory category = tagCategoryRepository.getByCategoryId(categoryId);
        if (category == null) {
            throw new BizException("标签分类不存在: " + effectiveCategoryId.value());
        }
        return category;
    }

    private Tag ensureTagExists(TagId tagId) {
        TagId effectiveTagId = ensureId(tagId, "tagId");
        Tag tag = tagRepository.getByTagId(tagId);
        if (tag == null) {
            throw new BizException("标签不存在: " + effectiveTagId.value());
        }
        return tag;
    }

    private Synonym ensureSynonymExists(SynonymId id) {
        SynonymId effectiveSynonymId = ensureId(id, "synonymId");
        Synonym synonym = synonymRepository.getById(id);
        if (synonym == null) {
            throw new BizException("同义词不存在: " + effectiveSynonymId.value());
        }
        return synonym;
    }

    private String getCategoryName(TagCategoryId categoryId) {
        if (categoryId == null) {
            return null;
        }

        TagCategory category = tagCategoryRepository.getByCategoryId(categoryId);
        return category == null ? null : category.getName();
    }

    private <T extends Enum<T>> T requireStatus(T status, String field) {
        if (status == null) {
            throw new BizException("参数为空: " + field);
        }
        return status;
    }

    private String trimText(String text, String field) {
        String trimmed = StringUtils.trimToNull(text);
        if (trimmed == null) {
            throw new BizException("参数不能为空: " + field);
        }
        return trimmed;
    }

    private String trimOptionalText(String text) {
        return StringUtils.trimToNull(text);
    }

    private String normalizeQueryText(String text) {
        return StringUtils.trimToNull(text);
    }

    private TagCategoryId normalizeId(TagCategoryId id) {
        return id;
    }

    private <T> T ensureCommand(T command, String field) {
        if (command == null) {
            throw new BizException("参数为空: " + field);
        }
        return command;
    }

    private <T> T ensureId(T value, String field) {
        if (value == null) {
            throw new BizException("参数为空: " + field);
        }
        return value;
    }

    private boolean keepOnlyPending(Tag tag, TagReviewPageQuery query) {
        if (tag == null) {
            return false;
        }
        if (tag.getReviewStatus() != TagReviewStatus.PENDING || !isAiGeneratedSource(tag.getSource())) {
            return false;
        }
        if (query != null && query.getSource() != null && !isAiGeneratedSource(query.getSource())) {
            return false;
        }
        String name = StringUtils.trimToNull(query == null ? null : query.getName());
        if (name != null && !StringUtils.containsIgnoreCase(tag.getName(), name)) {
            return false;
        }
        return true;
    }

    private String normalizeDecision(String decision) {
        String normalized = StringUtils.trimToNull(decision);
        if (normalized == null) {
            throw new BizException("参数为空: decision");
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private PageQuery normalize(PageQuery page) {
        PageQuery effective = page == null ? new PageQuery() : page;
        effective.normalize();
        return effective;
    }

    private boolean isAiGeneratedSource(TagSource source) {
        return source == TagSource.AI_EXTRACTED;
    }
}
