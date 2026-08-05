package com.thundax.kuzhambu.knowledge.application.taxonomy.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.GetAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeAiExtractionFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.KnowledgeAiExtractionFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.assembler.TaxonomyApplicationAssembler;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagAliasCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagAliasRemoveCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchDeprecateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchMergeCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchReviewCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCandidateApplyCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCandidateApplyCommand.TagCandidateApplyItemCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryStatusCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryUpdateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagDeprecateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagMergeCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagReviewCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagStatusCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagUpdateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagBatchMergePreviewQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagCategoryPageQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagGovernanceMetricsQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagMergePreviewQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagPageQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagReviewPageQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagAliasResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagBatchMergePreviewResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagCategoryResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagDetailResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagExtractionResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagGovernanceMetricsResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagMergePreviewResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.service.TaxonomyApplicationService;
import com.thundax.kuzhambu.knowledge.domain.service.KnowledgeTagBindingDomainService;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagCategoryIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagAlias;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagCategory;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagContentRef;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagCategoryStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagReviewStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.readmodel.TagGovernanceMetrics;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagAliasId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagAliasRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagCategoryRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagContentRefRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagGovernanceMetricsRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class TaxonomyApplicationServiceImpl implements TaxonomyApplicationService {

    private static final String CAPABILITY_KNOWLEDGE_TAG_EXTRACT = "KNOWLEDGE_TAG_EXTRACT";
    private static final String APPROVE_DECISION = "APPROVE";
    private static final String REJECT_DECISION = "REJECT";
    private static final int TAXONOMY_CONTEXT_PAGE_SIZE = 200;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    private final AiFacade aiFacade;
    private final TagCategoryRepository tagCategoryRepository;
    private final TagRepository tagRepository;
    private final TagAliasRepository tagAliasRepository;
    private final TagContentRefRepository tagContentRefRepository;
    private final KnowledgeTagBindingDomainService knowledgeTagBindingDomainService;
    private final TagGovernanceMetricsRepository tagGovernanceMetricsRepository;

    public TaxonomyApplicationServiceImpl(
            AiFacade aiFacade,
            TagCategoryRepository tagCategoryRepository,
            TagRepository tagRepository,
            TagAliasRepository tagAliasRepository,
            TagContentRefRepository tagContentRefRepository,
            KnowledgeTagBindingDomainService knowledgeTagBindingDomainService,
            TagGovernanceMetricsRepository tagGovernanceMetricsRepository) {
        this.aiFacade = aiFacade;
        this.tagCategoryRepository = tagCategoryRepository;
        this.tagRepository = tagRepository;
        this.tagAliasRepository = tagAliasRepository;
        this.tagContentRefRepository = tagContentRefRepository;
        this.knowledgeTagBindingDomainService = knowledgeTagBindingDomainService;
        this.tagGovernanceMetricsRepository = tagGovernanceMetricsRepository;
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
        category.setId(categoryId);
        category.setName(name);
        category.setDescription(trimOptionalText(effective.getDescription()));
        category.setPriority(tagCategoryRepository.maxPriority() + 1);
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
        updated.setId(category.getId());
        updated.setName(name);
        updated.setDescription(trimOptionalText(effective.getDescription()));
        updated.setPriority(category.getPriority());
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
                                tagContentRefRepository.countByTagId(tag.getId())))
                        .collect(Collectors.toList()));
    }

    @Override
    public TagDetailResult getTagDetail(TagId id) {
        Tag tag = ensureTagExists(id);

        return TaxonomyApplicationAssembler.toDetailResult(
                tag,
                tagAliasRepository.listByTagId(tag.getId()),
                tagContentRefRepository.listByTagId(tag.getId()),
                getCategoryName(tag.getCategoryId()));
    }

    @Override
    public TagMergePreviewResult previewTagMergeImpact(TagMergePreviewQuery query) {
        ensureCommand(query, "标签合并影响预览查询");
        Tag sourceTag = ensureTagExists(query.getSourceTagId());
        Tag targetTag = ensureTagExists(query.getTargetTagId());
        List<TagAlias> aliasesToMerge = tagAliasRepository.listByTagId(sourceTag.getId());
        List<TagContentRef> impactedContentRefs = tagContentRefRepository.listByTagId(sourceTag.getId());
        int pendingReviewCount = sourceTag.getReviewStatus() == TagReviewStatus.PENDING ? 1 : 0;
        int governedRecordCount = pendingReviewCount + aliasesToMerge.size() + impactedContentRefs.size();

        return new TagMergePreviewResult(
                TaxonomyApplicationAssembler.toResult(
                        sourceTag, getCategoryName(sourceTag.getCategoryId()), impactedContentRefs.size()),
                TaxonomyApplicationAssembler.toResult(
                        targetTag,
                        getCategoryName(targetTag.getCategoryId()),
                        tagContentRefRepository.countByTagId(targetTag.getId())),
                TaxonomyApplicationAssembler.toAliasResultList(aliasesToMerge),
                TaxonomyApplicationAssembler.toContentRefResultList(impactedContentRefs),
                pendingReviewCount,
                governedRecordCount);
    }

    @Override
    public TagBatchMergePreviewResult previewTagBatchMergeImpact(TagBatchMergePreviewQuery query) {
        TagBatchMergePreviewQuery effective = ensureCommand(query, "标签批量合并影响预览查询");
        List<TagId> sourceTagIds = normalizeTagIds(effective.getSourceTagIds(), "sourceTagIds");
        Tag targetTag = ensureTagUsableForBatchMergeTarget(ensureTagExists(effective.getTargetTagId()));
        List<Tag> sourceTags = getExistingTags(sourceTagIds, "sourceTagIds");
        ensureBatchMergeSourceTags(sourceTags, targetTag);

        List<TagAlias> aliasesToMerge = new ArrayList<>();
        List<TagContentRef> impactedContentRefs = new ArrayList<>();
        int pendingReviewCount = 0;
        for (Tag sourceTag : sourceTags) {
            aliasesToMerge.addAll(tagAliasRepository.listByTagId(sourceTag.getId()));
            impactedContentRefs.addAll(tagContentRefRepository.listByTagId(sourceTag.getId()));
            if (sourceTag.getReviewStatus() == TagReviewStatus.PENDING) {
                pendingReviewCount++;
            }
        }

        int governedRecordCount = pendingReviewCount + aliasesToMerge.size() + impactedContentRefs.size();
        return new TagBatchMergePreviewResult(
                sourceTags.stream()
                        .map(tag -> TaxonomyApplicationAssembler.toResult(
                                tag,
                                getCategoryName(tag.getCategoryId()),
                                tagContentRefRepository.countByTagId(tag.getId())))
                        .collect(Collectors.toList()),
                TaxonomyApplicationAssembler.toResult(
                        targetTag,
                        getCategoryName(targetTag.getCategoryId()),
                        tagContentRefRepository.countByTagId(targetTag.getId())),
                TaxonomyApplicationAssembler.toAliasResultList(aliasesToMerge),
                TaxonomyApplicationAssembler.toContentRefResultList(impactedContentRefs),
                pendingReviewCount,
                governedRecordCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyTagMerge(TagMergeCommand command) {
        TagMergeCommand effective = ensureCommand(command, "标签合并命令");
        Tag sourceTag = ensureTagExists(effective.getSourceTagId());
        Tag targetTag = ensureTagExists(effective.getTargetTagId());
        List<TagContentRef> sourceContentRefs = tagContentRefRepository.listByTagId(sourceTag.getId());
        sourceTag.mergeInto(targetTag);
        if (tagRepository.update(sourceTag) != 1) {
            throw new BizException("标签合并状态更新失败");
        }
        sourceContentRefs.stream()
                .filter(ref -> ref != null && ref.getContentType() != null && ref.getContentId() != null)
                .forEach(ref -> knowledgeTagBindingDomainService.syncContentTagRef(
                        targetTag.getId(),
                        ref.getContentType(),
                        ref.getContentId(),
                        ref.getContentTitle(),
                        ref.getSource()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyTagBatchMerge(TagBatchMergeCommand command) {
        TagBatchMergeCommand effective = ensureCommand(command, "标签批量合并命令");
        List<TagId> sourceTagIds = normalizeTagIds(effective.getSourceTagIds(), "sourceTagIds");
        Tag targetTag = ensureTagUsableForBatchMergeTarget(ensureTagExists(effective.getTargetTagId()));
        List<Tag> sourceTags = getExistingTags(sourceTagIds, "sourceTagIds");
        ensureBatchMergeSourceTags(sourceTags, targetTag);

        Map<TagId, List<TagContentRef>> contentRefsBySource = sourceTags.stream()
                .collect(Collectors.toMap(
                        Tag::getId,
                        sourceTag -> tagContentRefRepository.listByTagId(sourceTag.getId()),
                        (left, right) -> left,
                        LinkedHashMap::new));
        for (Tag sourceTag : sourceTags) {
            sourceTag.mergeInto(targetTag);
            if (tagRepository.update(sourceTag) != 1) {
                throw new BizException("标签批量合并状态更新失败");
            }
            contentRefsBySource.getOrDefault(sourceTag.getId(), List.of()).stream()
                    .filter(ref -> ref != null && ref.getContentType() != null && ref.getContentId() != null)
                    .forEach(ref -> knowledgeTagBindingDomainService.syncContentTagRef(
                            targetTag.getId(),
                            ref.getContentType(),
                            ref.getContentId(),
                            ref.getContentTitle(),
                            ref.getSource()));
        }
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
        tag.setId(tagId);
        tag.setName(name);
        tag.setCategoryId(categoryId);
        tag.setDescription(trimOptionalText(effective.getDescription()));
        tag.setStatus(TagStatus.ENABLED);
        tag.setSource(TagSource.MANUAL);
        tag.setReviewStatus(TagReviewStatus.APPROVED);
        tag.setReviewNote(trimOptionalText(effective.getReviewNote()));
        tag.setCreatedAt(Instant.now());
        tag.setReviewedAt(effective.getReviewedAt() == null ? Instant.now() : effective.getReviewedAt());

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
    @Transactional(rollbackFor = Exception.class)
    public void deprecateTag(TagDeprecateCommand command) {
        TagDeprecateCommand effective = ensureCommand(command, "标签废弃命令");
        Tag tag = ensureTagExists(ensureId(effective.getId(), "tagId"));
        tag.deprecate(Instant.now(), null);
        if (tagRepository.update(tag) != 1) {
            throw new BizException("标签废弃状态更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeprecateTags(TagBatchDeprecateCommand command) {
        TagBatchDeprecateCommand effective = ensureCommand(command, "标签批量废弃命令");
        List<Tag> tags = getExistingTags(normalizeTagIds(effective.getTagIds(), "tagIds"), "tagIds");
        for (Tag tag : tags) {
            if (tag.isDeprecated()) {
                throw new BizException("标签已废弃: " + tag.getId().value());
            }
        }
        for (Tag tag : tags) {
            tag.deprecate(Instant.now(), null);
            if (tagRepository.update(tag) != 1) {
                throw new BizException("标签批量废弃状态更新失败");
            }
        }
    }

    @Override
    public TagGovernanceMetricsResult getTagGovernanceMetrics(TagGovernanceMetricsQuery query) {
        TagGovernanceMetricsQuery effective = ensureCommand(query, "标签治理统计查询");
        TagGovernanceMetrics metrics =
                tagGovernanceMetricsRepository.getMetrics(effective.getTopLimit(), effective.getRecentMonths());
        return new TagGovernanceMetricsResult(
                metrics.getTopTags().stream()
                        .map(item -> new TagGovernanceMetricsResult.TagUsageMetric(
                                item.getTagName(), item.getContentRefCount()))
                        .collect(Collectors.toList()),
                metrics.getCategoryDistributions().stream()
                        .map(item -> new TagGovernanceMetricsResult.CategoryDistributionMetric(
                                item.getCategoryName(), item.getTagCount()))
                        .collect(Collectors.toList()),
                metrics.getSourceRatios().stream()
                        .map(item ->
                                new TagGovernanceMetricsResult.SourceRatioMetric(item.getSource(), item.getTagCount()))
                        .collect(Collectors.toList()),
                metrics.getMonthlyNewTags().stream()
                        .map(item ->
                                new TagGovernanceMetricsResult.MonthlyNewTagMetric(item.getMonth(), item.getTagCount()))
                        .collect(Collectors.toList()));
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
                                tagContentRefRepository.countByTagId(tag.getId())))
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
        reviewed.setName(tag.getName());
        reviewed.setCategoryId(tag.getCategoryId());
        reviewed.setDescription(tag.getDescription());
        reviewed.setStatus(tag.getStatus());
        reviewed.setSource(tag.getSource());
        reviewed.setCreatedAt(tag.getCreatedAt());
        reviewed.setReviewedAt(Instant.now());

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
    @Transactional(rollbackFor = Exception.class)
    public void batchReviewTags(TagBatchReviewCommand command) {
        TagBatchReviewCommand effective = ensureCommand(command, "标签批量审核命令");
        String decision = normalizeDecision(effective.getDecision());
        String reviewNote = trimOptionalText(effective.getReviewNote());
        List<Tag> tags = getExistingTags(normalizeTagIds(effective.getTagIds(), "tagIds"), "tagIds");
        TagCategoryId categoryId = null;
        if (APPROVE_DECISION.equals(decision)) {
            categoryId = ensureId(effective.getCategoryId(), "categoryId");
            TagCategory category = getExistingCategory(categoryId);
            if (category.getStatus() != TagCategoryStatus.ENABLED) {
                throw new BizException("审核通过标签必须关联启用中的分类");
            }
        } else if (REJECT_DECISION.equals(decision) && StringUtils.isBlank(reviewNote)) {
            throw new BizException("拒绝标签必须填写审核说明");
        } else if (!REJECT_DECISION.equals(decision)) {
            throw new BizException("非法审核决策: " + decision);
        }

        for (Tag tag : tags) {
            if (tag.getReviewStatus() != TagReviewStatus.PENDING) {
                throw new BizException("标签不是待审核状态: " + tag.getId().value());
            }
        }
        for (Tag tag : tags) {
            Tag reviewed = buildReviewedTag(tag, decision, categoryId, reviewNote);
            if (tagRepository.updateReviewStatus(reviewed) != 1) {
                throw new BizException("标签批量审核失败");
            }
        }
    }

    @Override
    public TagExtractionResult extractTags(TagExtractionCommand command) {
        TagExtractionCommand effective = ensureCommand(command, "标签抽取命令");
        String sourceContentType = trimText(effective.getSourceContentType(), "sourceContentType");
        Long sourceContentId = ensureId(effective.getSourceContentId(), "sourceContentId");
        String contentText = trimText(effective.getContentText(), "contentText");
        Long modelId = ensureId(effective.getModelId(), "modelId");
        String modelName = trimText(effective.getModelName(), "modelName");
        Long requestedBy = ensureId(effective.getRequestedBy(), "requestedBy");
        int maxTags = effective.getMaxTags() == null ? 10 : effective.getMaxTags();
        if (maxTags < 1) {
            throw new BizException("maxTags must be greater than 0");
        }
        boolean allowNewTags = effective.getAllowNewTags() == null || Boolean.TRUE.equals(effective.getAllowNewTags());

        String requestId = "knowledge-tag-" + UUID.randomUUID();
        KnowledgeAiExtractionFacadeResponse response =
                aiFacade.extractKnowledgeTags(KnowledgeAiExtractionFacadeRequest.builder()
                        .taskType("TAG")
                        .scopeType("CONTENT")
                        .scopeJson(toJson(scopePayload(sourceContentType, sourceContentId)))
                        .sourceContentType(sourceContentType)
                        .sourceContentId(sourceContentId)
                        .requestedBy(requestedBy)
                        .serviceRole("KNOWLEDGE")
                        .modelId(modelId)
                        .modelName(modelName)
                        .promptVersionId(effective.getPromptVersionId())
                        .requestId(requestId)
                        .traceId(requestId)
                        .promptMessagesJson(promptMessagesJson())
                        .promptVariablesJson(toJson(promptVariables(maxTags, allowNewTags)))
                        .inputPayloadJson(toJson(inputPayload(
                                effective, sourceContentType, sourceContentId, contentText, maxTags, allowNewTags)))
                        .outputSchemaJson(outputSchemaJson())
                        .forceJson(true)
                        .locale("zh-CN")
                        .build());

        return new TagExtractionResult(
                response == null ? null : response.getCallId(),
                response == null ? null : response.getCandidateId(),
                response == null ? null : response.getStatus(),
                response == null ? null : response.getResultFormat(),
                response == null ? null : response.getResultPayload(),
                response == null ? null : response.getErrorType(),
                response == null ? null : response.getErrorMessage());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyExtractedTags(TagCandidateApplyCommand command) {
        TagCandidateApplyCommand effective = ensureCommand(command, "标签候选应用命令");
        Long candidateId = ensureId(effective.getAiCandidateId(), "aiCandidateId");
        Long reviewedBy = ensureId(effective.getReviewedBy(), "reviewedBy");
        List<TagCandidateApplyItemCommand> selectedTags = effective.getSelectedTags();
        if (selectedTags == null || selectedTags.isEmpty()) {
            throw new BizException("selectedTags must not be empty");
        }

        AiCandidateFacadeDto candidate = aiFacade.getCandidate(
                GetAiCandidateFacadeRequest.builder().candidateId(candidateId).build());
        if (candidate == null) {
            throw new BizException("AI candidate not found: " + candidateId);
        }
        if (!CAPABILITY_KNOWLEDGE_TAG_EXTRACT.equals(candidate.getCapability())) {
            throw new BizException("AI candidate capability mismatch: " + candidate.getCapability());
        }
        AiCandidateFacadeDto pendingCandidate =
                aiFacade.requirePendingCandidate(RequirePendingAiCandidateFacadeRequest.builder()
                        .candidateId(candidateId)
                        .contentType(candidate.getContentType())
                        .contentId(candidate.getContentId())
                        .capability(CAPABILITY_KNOWLEDGE_TAG_EXTRACT)
                        .build());

        for (TagCandidateApplyItemCommand item : selectedTags) {
            applySelectedTag(item, effective.getReviewNote());
        }
        aiFacade.markCandidateApplied(MarkAiCandidateAppliedFacadeRequest.builder()
                .candidateId(pendingCandidate.getCandidateId())
                .resultFormat("STRUCTURED")
                .resultPayload(toJson(candidateApplyPayload(effective, reviewedBy)))
                .appliedAt(Instant.now())
                .build());
    }

    @Override
    public List<TagAliasResult> listTagAliases(TagId tagId) {
        Tag tag = ensureTagExists(tagId);
        return TaxonomyApplicationAssembler.toAliasResultList(tagAliasRepository.listByTagId(tag.getId()));
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
        alias.setId(aliasId);
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

    private void applySelectedTag(TagCandidateApplyItemCommand item, String reviewNote) {
        TagCandidateApplyItemCommand effective = ensureCommand(item, "标签候选项");
        String name = trimText(effective.getName(), "name");
        TagId matchedTagId = parseTagId(effective.getMatchedExistingTagId());
        if (matchedTagId != null) {
            ensureTagExists(matchedTagId);
            return;
        }
        if (tagRepository.countByName(name, null) > 0) {
            return;
        }

        Tag tag = new Tag();
        tag.setId(TagIdCodec.toDomain(idGenerator.nextId().value()));
        tag.setName(name);
        tag.setCategoryId(parseCategoryId(effective.getCategoryId()));
        tag.setDescription(trimOptionalText(effective.getReason()));
        tag.setStatus(TagStatus.ENABLED);
        tag.setSource(TagSource.AI_EXTRACTED);
        tag.setReviewStatus(TagReviewStatus.PENDING);
        tag.setReviewNote(trimOptionalText(reviewNote));
        tag.setCreatedAt(Instant.now());
        tagRepository.insert(tag);
    }

    private TagId parseTagId(String value) {
        String normalized = StringUtils.trimToNull(value);
        return normalized == null ? null : TagIdCodec.toDomain(Long.valueOf(normalized));
    }

    private TagCategoryId parseCategoryId(String value) {
        String normalized = StringUtils.trimToNull(value);
        return normalized == null ? null : TagCategoryIdCodec.toDomain(Long.valueOf(normalized));
    }

    private Map<String, Object> candidateApplyPayload(TagCandidateApplyCommand command, Long reviewedBy) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("aiCandidateId", command.getAiCandidateId());
        payload.put("reviewedBy", reviewedBy);
        payload.put("reviewNote", trimOptionalText(command.getReviewNote()));
        payload.put(
                "selectedTags",
                command.getSelectedTags().stream()
                        .filter(Objects::nonNull)
                        .map(this::candidateApplyItemPayload)
                        .collect(Collectors.toList()));
        return payload;
    }

    private Map<String, Object> candidateApplyItemPayload(TagCandidateApplyItemCommand item) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", item.getName());
        payload.put("categoryId", item.getCategoryId());
        payload.put("categoryName", item.getCategoryName());
        payload.put("confidence", item.getConfidence());
        payload.put("reason", item.getReason());
        payload.put("matchedExistingTagId", item.getMatchedExistingTagId());
        return payload;
    }

    private Map<String, Object> scopePayload(String sourceContentType, Long sourceContentId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("contentType", sourceContentType);
        payload.put("contentIds", List.of(sourceContentId));
        payload.put("includeExistingTags", true);
        return payload;
    }

    private Map<String, Object> promptVariables(int maxTags, boolean allowNewTags) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("locale", "zh-CN");
        variables.put("maxTags", maxTags);
        variables.put("allowNewTags", allowNewTags);
        return variables;
    }

    private Map<String, Object> inputPayload(
            TagExtractionCommand command,
            String sourceContentType,
            Long sourceContentId,
            String contentText,
            int maxTags,
            boolean allowNewTags) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("contentType", sourceContentType);
        payload.put("contentId", sourceContentId);
        payload.put("sourceTitle", trimOptionalText(command.getContentTitle()));
        payload.put("sourceText", contentText);
        payload.put("existingTags", existingTagPayloads());
        payload.put("categories", categoryPayloads());
        payload.put("aliases", aliasPayloads());
        payload.put("constraints", constraintsPayload(maxTags, allowNewTags));
        return payload;
    }

    private List<Map<String, Object>> existingTagPayloads() {
        return tagRepository
                .page(null, null, TagStatus.ENABLED, null, null, 1, TAXONOMY_CONTEXT_PAGE_SIZE)
                .getRecords()
                .stream()
                .filter(Objects::nonNull)
                .map(tag -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put(
                            "tagId",
                            tag.getId() == null
                                    ? null
                                    : String.valueOf(tag.getId().value()));
                    payload.put("name", tag.getName());
                    payload.put(
                            "categoryId",
                            tag.getCategoryId() == null
                                    ? null
                                    : String.valueOf(tag.getCategoryId().value()));
                    payload.put("categoryName", getCategoryName(tag.getCategoryId()));
                    return payload;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> categoryPayloads() {
        return tagCategoryRepository
                .page(null, TagCategoryStatus.ENABLED, 1, TAXONOMY_CONTEXT_PAGE_SIZE)
                .getRecords()
                .stream()
                .filter(Objects::nonNull)
                .map(category -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put(
                            "categoryId",
                            category.getId() == null
                                    ? null
                                    : String.valueOf(category.getId().value()));
                    payload.put("name", category.getName());
                    return payload;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> aliasPayloads() {
        List<Map<String, Object>> aliases = new ArrayList<>();
        for (Tag tag : tagRepository
                .page(null, null, TagStatus.ENABLED, null, null, 1, TAXONOMY_CONTEXT_PAGE_SIZE)
                .getRecords()) {
            if (tag == null || tag.getId() == null) {
                continue;
            }
            for (TagAlias alias : tagAliasRepository.listByTagId(tag.getId())) {
                if (alias == null) {
                    continue;
                }
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("tagId", String.valueOf(tag.getId().value()));
                payload.put("name", alias.getName());
                aliases.add(payload);
            }
        }
        return aliases;
    }

    private Map<String, Object> constraintsPayload(int maxTags, boolean allowNewTags) {
        Map<String, Object> constraints = new LinkedHashMap<>();
        constraints.put("maxTags", maxTags);
        constraints.put("allowNewTags", allowNewTags);
        constraints.put("reviewRequired", true);
        return constraints;
    }

    private String promptMessagesJson() {
        return toJson(List.of(
                Map.of("role", "system", "content", "你是 Knowledge 标签治理助手，只返回符合 schema 的 JSON。"),
                Map.of("role", "user", "content", "请基于内容片段抽取候选标签，优先匹配已有标签。")));
    }

    private String outputSchemaJson() {
        Map<String, Object> name = Map.of("type", "string");
        Map<String, Object> confidence = Map.of("type", "number");
        Map<String, Object> itemProperties = new LinkedHashMap<>();
        itemProperties.put("name", name);
        itemProperties.put("categoryId", name);
        itemProperties.put("categoryName", name);
        itemProperties.put("confidence", confidence);
        itemProperties.put("reason", name);
        itemProperties.put("matchedExistingTagId", name);
        Map<String, Object> tagItem = new LinkedHashMap<>();
        tagItem.put("type", "object");
        tagItem.put("properties", itemProperties);
        tagItem.put("required", List.of("name", "confidence"));
        Map<String, Object> tags = new LinkedHashMap<>();
        tags.put("type", "array");
        tags.put("items", tagItem);
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("tags", tags);
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", List.of("tags"));
        return toJson(schema);
    }

    private String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BizException("标签抽取 JSON 组装失败");
        }
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

    private String getCategoryName(TagCategoryId categoryId) {
        if (categoryId == null) {
            return null;
        }

        TagCategory category = tagCategoryRepository.getByCategoryId(categoryId);
        return category == null ? null : category.getName();
    }

    private List<TagId> normalizeTagIds(List<TagId> tagIds, String field) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new BizException("参数不能为空: " + field);
        }
        LinkedHashSet<TagId> normalized = new LinkedHashSet<>();
        for (TagId tagId : tagIds) {
            if (tagId == null) {
                throw new BizException("参数不能为空: " + field);
            }
            normalized.add(tagId);
        }
        if (normalized.isEmpty()) {
            throw new BizException("参数不能为空: " + field);
        }
        return new ArrayList<>(normalized);
    }

    private List<Tag> getExistingTags(List<TagId> tagIds, String field) {
        List<Tag> tags = tagRepository.listByTagIds(tagIds);
        Map<TagId, Tag> tagById = tags.stream().collect(Collectors.toMap(Tag::getId, tag -> tag));
        for (TagId tagId : tagIds) {
            if (!tagById.containsKey(tagId)) {
                throw new BizException("标签不存在: " + tagId.value());
            }
        }
        return tagIds.stream().map(tagById::get).collect(Collectors.toList());
    }

    private Tag ensureTagUsableForBatchMergeTarget(Tag tag) {
        if (!tag.isUsableForNewBinding()) {
            throw new BizException("目标标签当前不可作为合并目标: " + tag.getId().value());
        }
        return tag;
    }

    private void ensureBatchMergeSourceTags(List<Tag> sourceTags, Tag targetTag) {
        for (Tag sourceTag : sourceTags) {
            if (sourceTag.getId().equals(targetTag.getId())) {
                throw new BizException("源标签不能包含目标标签: " + targetTag.getId().value());
            }
            if (!sourceTag.isUsableForNewBinding()) {
                throw new BizException("源标签当前不可用于合并: " + sourceTag.getId().value());
            }
        }
    }

    private Tag buildReviewedTag(Tag tag, String decision, TagCategoryId categoryId, String reviewNote) {
        Tag reviewed = new Tag();
        reviewed.setId(tag.getId());
        reviewed.setName(tag.getName());
        reviewed.setCategoryId(APPROVE_DECISION.equals(decision) ? categoryId : tag.getCategoryId());
        reviewed.setDescription(tag.getDescription());
        reviewed.setStatus(tag.getStatus());
        reviewed.setSource(tag.getSource());
        reviewed.setCreatedAt(tag.getCreatedAt());
        reviewed.setReviewedAt(Instant.now());
        reviewed.setReviewNote(reviewNote);
        reviewed.setReviewStatus(
                APPROVE_DECISION.equals(decision) ? TagReviewStatus.APPROVED : TagReviewStatus.REJECTED);
        return reviewed;
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
