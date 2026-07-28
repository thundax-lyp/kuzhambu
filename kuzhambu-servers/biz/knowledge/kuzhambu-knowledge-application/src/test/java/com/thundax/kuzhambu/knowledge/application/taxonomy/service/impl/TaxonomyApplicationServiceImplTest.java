package com.thundax.kuzhambu.knowledge.application.taxonomy.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeAiExtractionFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.KnowledgeAiExtractionFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchDeprecateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchMergeCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchReviewCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCandidateApplyCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCandidateApplyCommand.TagCandidateApplyItemCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagDeprecateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagMergeCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagBatchMergePreviewQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagGovernanceMetricsQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagMergePreviewQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagBatchMergePreviewResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagMergePreviewResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.service.TaxonomyApplicationService;
import com.thundax.kuzhambu.knowledge.domain.service.KnowledgeTagBindingDomainService;
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
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.readmodel.TagGovernanceMetrics;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagContentRefId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.SynonymRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagAliasRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagCategoryRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagContentRefRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagGovernanceMetricsRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TaxonomyApplicationServiceImplTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void getTagGovernanceMetricsShouldMapAggregatedReadModel() {
        TagGovernanceMetricsRepository metricsRepository = mock(TagGovernanceMetricsRepository.class);
        TaxonomyApplicationService service = new TaxonomyApplicationServiceImpl(
                mock(AiFacade.class),
                mock(TagCategoryRepository.class),
                mock(TagRepository.class),
                mock(TagAliasRepository.class),
                mock(TagContentRefRepository.class),
                mock(SynonymRepository.class),
                mock(KnowledgeTagBindingDomainService.class),
                metricsRepository);
        when(metricsRepository.getMetrics(10, 6))
                .thenReturn(new TagGovernanceMetrics(
                        List.of(new TagGovernanceMetrics.TagUsageMetric("礼制", 3L)),
                        List.of(new TagGovernanceMetrics.CategoryDistributionMetric("礼学", 2L)),
                        List.of(new TagGovernanceMetrics.SourceRatioMetric(TagSource.MANUAL, 4L)),
                        List.of(new TagGovernanceMetrics.MonthlyNewTagMetric("2026-06", 5L))));

        var result = service.getTagGovernanceMetrics(new TagGovernanceMetricsQuery(10, 6));

        assertEquals("礼制", result.getTopTags().get(0).getTagName());
        assertEquals(3L, result.getTopTags().get(0).getContentRefCount());
        assertEquals("礼学", result.getCategoryDistributions().get(0).getCategoryName());
        assertEquals(2L, result.getCategoryDistributions().get(0).getTagCount());
        assertEquals(TagSource.MANUAL, result.getSourceRatios().get(0).getSource());
        assertEquals(4L, result.getSourceRatios().get(0).getTagCount());
        assertEquals("2026-06", result.getMonthlyNewTags().get(0).getMonth());
        assertEquals(5L, result.getMonthlyNewTags().get(0).getTagCount());
    }

    @Test
    void deprecateTagShouldDisableTagAndRejectSecondDeprecation() {
        TagRepository tagRepository = mock(TagRepository.class);
        TaxonomyApplicationService service = new TaxonomyApplicationServiceImpl(
                mock(AiFacade.class),
                mock(TagCategoryRepository.class),
                tagRepository,
                mock(TagAliasRepository.class),
                mock(TagContentRefRepository.class),
                mock(SynonymRepository.class),
                mock(KnowledgeTagBindingDomainService.class),
                mock(TagGovernanceMetricsRepository.class));
        Tag tag = new Tag();
        tag.setId(TagIdCodec.toDomain(11L));
        tag.setTagId(TagIdCodec.toDomain(1L));
        tag.setStatus(TagStatus.ENABLED);
        when(tagRepository.getByTagId(TagIdCodec.toDomain(1L))).thenReturn(tag);
        when(tagRepository.update(tag)).thenReturn(1);

        service.deprecateTag(new TagDeprecateCommand(TagIdCodec.toDomain(1L)));

        assertEquals(TagStatus.DISABLED, tag.getStatus());
        assertThrows(BizException.class, () -> service.deprecateTag(new TagDeprecateCommand(TagIdCodec.toDomain(1L))));
    }

    @Test
    void applyTagMergeShouldMarkSourceTagAsMergedIntoTarget() {
        TagRepository tagRepository = mock(TagRepository.class);
        TagContentRefRepository contentRefRepository = mock(TagContentRefRepository.class);
        KnowledgeTagBindingDomainService knowledgeTagBindingDomainService =
                mock(KnowledgeTagBindingDomainService.class);
        TaxonomyApplicationService service = new TaxonomyApplicationServiceImpl(
                mock(AiFacade.class),
                mock(TagCategoryRepository.class),
                tagRepository,
                mock(TagAliasRepository.class),
                contentRefRepository,
                mock(SynonymRepository.class),
                knowledgeTagBindingDomainService,
                mock(TagGovernanceMetricsRepository.class));
        Tag sourceTag = new Tag();
        sourceTag.setId(TagIdCodec.toDomain(11L));
        sourceTag.setTagId(TagIdCodec.toDomain(1L));
        sourceTag.setStatus(TagStatus.ENABLED);
        Tag targetTag = new Tag();
        targetTag.setId(TagIdCodec.toDomain(12L));
        targetTag.setTagId(TagIdCodec.toDomain(2L));
        targetTag.setStatus(TagStatus.ENABLED);
        when(tagRepository.getByTagId(TagIdCodec.toDomain(1L))).thenReturn(sourceTag);
        when(tagRepository.getByTagId(TagIdCodec.toDomain(2L))).thenReturn(targetTag);
        when(contentRefRepository.listByTagId(TagIdCodec.toDomain(1L)))
                .thenReturn(List.of(new TagContentRef(
                        TagContentRefIdCodec.toDomain(31L),
                        TagContentRefIdCodec.toDomain(31L),
                        TagIdCodec.toDomain(1L),
                        ContentType.SANCAI_ENTRY,
                        1001L,
                        "内容一",
                        TagSource.AI_EXTRACTED)));
        when(tagRepository.update(sourceTag)).thenReturn(1);

        service.applyTagMerge(new TagMergeCommand(TagIdCodec.toDomain(1L), TagIdCodec.toDomain(2L)));

        assertEquals(TagIdCodec.toDomain(2L), sourceTag.getMergedToTagId());
        assertNull(targetTag.getMergedToTagId());
        verify(tagRepository).update(sourceTag);
        verify(knowledgeTagBindingDomainService, times(1))
                .syncContentTagRef(
                        TagIdCodec.toDomain(2L), ContentType.SANCAI_ENTRY, 1001L, "内容一", TagSource.AI_EXTRACTED);
    }

    @Test
    void previewTagMergeImpactShouldAggregateAliasesContentRefsAndPendingReview() {
        TagCategoryRepository categoryRepository = mock(TagCategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        TagAliasRepository aliasRepository = mock(TagAliasRepository.class);
        TagContentRefRepository contentRefRepository = mock(TagContentRefRepository.class);
        TaxonomyApplicationService service = new TaxonomyApplicationServiceImpl(
                mock(AiFacade.class),
                categoryRepository,
                tagRepository,
                aliasRepository,
                contentRefRepository,
                mock(SynonymRepository.class),
                mock(KnowledgeTagBindingDomainService.class),
                mock(TagGovernanceMetricsRepository.class));

        Tag sourceTag = new Tag(
                TagIdCodec.toDomain(1L),
                TagIdCodec.toDomain(1L),
                "源标签",
                TagCategoryIdCodec.toDomain(11L),
                "source",
                TagStatus.ENABLED,
                TagSource.AI_EXTRACTED,
                TagReviewStatus.PENDING,
                null,
                null,
                null);
        Tag targetTag = new Tag(
                TagIdCodec.toDomain(2L),
                TagIdCodec.toDomain(2L),
                "目标标签",
                TagCategoryIdCodec.toDomain(12L),
                "target",
                TagStatus.ENABLED,
                TagSource.MANUAL,
                TagReviewStatus.APPROVED,
                null,
                null,
                null);
        when(tagRepository.getByTagId(TagIdCodec.toDomain(1L))).thenReturn(sourceTag);
        when(tagRepository.getByTagId(TagIdCodec.toDomain(2L))).thenReturn(targetTag);
        when(categoryRepository.getByCategoryId(TagCategoryIdCodec.toDomain(11L)))
                .thenReturn(new TagCategory(
                        TagCategoryIdCodec.toDomain(11L), TagCategoryIdCodec.toDomain(11L), "源分类", null, 1, null));
        when(categoryRepository.getByCategoryId(TagCategoryIdCodec.toDomain(12L)))
                .thenReturn(new TagCategory(
                        TagCategoryIdCodec.toDomain(12L), TagCategoryIdCodec.toDomain(12L), "目标分类", null, 1, null));
        when(aliasRepository.listByTagId(TagIdCodec.toDomain(1L)))
                .thenReturn(List.of(new TagAlias(
                        TagAliasIdCodec.toDomain(21L),
                        TagAliasIdCodec.toDomain(21L),
                        TagIdCodec.toDomain(1L),
                        "别名一",
                        TagSource.MANUAL)));
        when(contentRefRepository.listByTagId(TagIdCodec.toDomain(1L)))
                .thenReturn(List.of(new TagContentRef(
                        TagContentRefIdCodec.toDomain(31L),
                        TagContentRefIdCodec.toDomain(31L),
                        TagIdCodec.toDomain(1L),
                        ContentType.SANCAI_ENTRY,
                        1001L,
                        "内容一",
                        TagSource.AI_EXTRACTED)));
        when(contentRefRepository.countByTagId(TagIdCodec.toDomain(2L))).thenReturn(2);

        TagMergePreviewResult result = service.previewTagMergeImpact(
                new TagMergePreviewQuery(TagIdCodec.toDomain(1L), TagIdCodec.toDomain(2L)));

        assertEquals("源标签", result.getSourceTag().getName());
        assertEquals("目标标签", result.getTargetTag().getName());
        assertEquals("源分类", result.getSourceTag().getCategoryName());
        assertEquals("目标分类", result.getTargetTag().getCategoryName());
        assertEquals(1, result.getAliasesToMerge().size());
        assertEquals("别名一", result.getAliasesToMerge().get(0).getName());
        assertEquals(1, result.getImpactedContentRefs().size());
        assertEquals("内容一", result.getImpactedContentRefs().get(0).getContentTitle());
        assertEquals(1, result.getPendingReviewCount());
        assertEquals(3, result.getGovernedRecordCount());
    }

    @Test
    void previewTagBatchMergeImpactShouldAggregateMultipleSources() {
        TagCategoryRepository categoryRepository = mock(TagCategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        TagAliasRepository aliasRepository = mock(TagAliasRepository.class);
        TagContentRefRepository contentRefRepository = mock(TagContentRefRepository.class);
        TaxonomyApplicationService service = new TaxonomyApplicationServiceImpl(
                mock(AiFacade.class),
                categoryRepository,
                tagRepository,
                aliasRepository,
                contentRefRepository,
                mock(SynonymRepository.class),
                mock(KnowledgeTagBindingDomainService.class),
                mock(TagGovernanceMetricsRepository.class));

        Tag sourceTag = tag(
                TagIdCodec.toDomain(1L),
                "源标签",
                TagCategoryIdCodec.toDomain(11L),
                TagStatus.ENABLED,
                TagReviewStatus.PENDING);
        Tag secondSourceTag = tag(
                TagIdCodec.toDomain(3L),
                "次源标签",
                TagCategoryIdCodec.toDomain(11L),
                TagStatus.ENABLED,
                TagReviewStatus.APPROVED);
        Tag targetTag = tag(
                TagIdCodec.toDomain(2L),
                "目标标签",
                TagCategoryIdCodec.toDomain(12L),
                TagStatus.ENABLED,
                TagReviewStatus.APPROVED);
        when(tagRepository.getByTagId(TagIdCodec.toDomain(2L))).thenReturn(targetTag);
        when(tagRepository.listByTagIds(List.of(TagIdCodec.toDomain(1L), TagIdCodec.toDomain(3L))))
                .thenReturn(List.of(sourceTag, secondSourceTag));
        when(categoryRepository.getByCategoryId(TagCategoryIdCodec.toDomain(11L)))
                .thenReturn(new TagCategory(
                        TagCategoryIdCodec.toDomain(11L), TagCategoryIdCodec.toDomain(11L), "源分类", null, 1, null));
        when(categoryRepository.getByCategoryId(TagCategoryIdCodec.toDomain(12L)))
                .thenReturn(new TagCategory(
                        TagCategoryIdCodec.toDomain(12L), TagCategoryIdCodec.toDomain(12L), "目标分类", null, 1, null));
        when(aliasRepository.listByTagId(TagIdCodec.toDomain(1L)))
                .thenReturn(List.of(new TagAlias(
                        TagAliasIdCodec.toDomain(21L),
                        TagAliasIdCodec.toDomain(21L),
                        TagIdCodec.toDomain(1L),
                        "别名一",
                        TagSource.MANUAL)));
        when(aliasRepository.listByTagId(TagIdCodec.toDomain(3L))).thenReturn(List.of());
        when(contentRefRepository.listByTagId(TagIdCodec.toDomain(1L)))
                .thenReturn(
                        List.of(contentRef(TagContentRefIdCodec.toDomain(31L), TagIdCodec.toDomain(1L), 1001L, "内容一")));
        when(contentRefRepository.listByTagId(TagIdCodec.toDomain(3L)))
                .thenReturn(
                        List.of(contentRef(TagContentRefIdCodec.toDomain(32L), TagIdCodec.toDomain(3L), 1002L, "内容二")));
        when(contentRefRepository.countByTagId(TagIdCodec.toDomain(1L))).thenReturn(1);
        when(contentRefRepository.countByTagId(TagIdCodec.toDomain(2L))).thenReturn(2);
        when(contentRefRepository.countByTagId(TagIdCodec.toDomain(3L))).thenReturn(1);

        TagBatchMergePreviewResult result = service.previewTagBatchMergeImpact(new TagBatchMergePreviewQuery(
                List.of(TagIdCodec.toDomain(1L), TagIdCodec.toDomain(3L)), TagIdCodec.toDomain(2L)));

        assertEquals(2, result.getSourceTags().size());
        assertEquals("源标签", result.getSourceTags().get(0).getName());
        assertEquals("次源标签", result.getSourceTags().get(1).getName());
        assertEquals("目标标签", result.getTargetTag().getName());
        assertEquals(1, result.getAliasesToMerge().size());
        assertEquals(2, result.getImpactedContentRefs().size());
        assertEquals(1, result.getPendingReviewCount());
        assertEquals(4, result.getGovernedRecordCount());
    }

    @Test
    void applyTagBatchMergeShouldValidateAllSourcesBeforeWriting() {
        TagRepository tagRepository = mock(TagRepository.class);
        KnowledgeTagBindingDomainService knowledgeTagBindingDomainService =
                mock(KnowledgeTagBindingDomainService.class);
        TaxonomyApplicationService service = new TaxonomyApplicationServiceImpl(
                mock(AiFacade.class),
                mock(TagCategoryRepository.class),
                tagRepository,
                mock(TagAliasRepository.class),
                mock(TagContentRefRepository.class),
                mock(SynonymRepository.class),
                knowledgeTagBindingDomainService,
                mock(TagGovernanceMetricsRepository.class));
        Tag enabledSource = tag(
                TagIdCodec.toDomain(1L),
                "源标签",
                TagCategoryIdCodec.toDomain(11L),
                TagStatus.ENABLED,
                TagReviewStatus.APPROVED);
        Tag disabledSource = tag(
                TagIdCodec.toDomain(3L),
                "废弃标签",
                TagCategoryIdCodec.toDomain(11L),
                TagStatus.DISABLED,
                TagReviewStatus.APPROVED);
        Tag targetTag = tag(
                TagIdCodec.toDomain(2L),
                "目标标签",
                TagCategoryIdCodec.toDomain(12L),
                TagStatus.ENABLED,
                TagReviewStatus.APPROVED);
        when(tagRepository.getByTagId(TagIdCodec.toDomain(2L))).thenReturn(targetTag);
        when(tagRepository.listByTagIds(List.of(TagIdCodec.toDomain(1L), TagIdCodec.toDomain(3L))))
                .thenReturn(List.of(enabledSource, disabledSource));

        assertThrows(
                BizException.class,
                () -> service.applyTagBatchMerge(new TagBatchMergeCommand(
                        List.of(TagIdCodec.toDomain(1L), TagIdCodec.toDomain(3L)), TagIdCodec.toDomain(2L))));

        verify(tagRepository, never()).update(any(Tag.class));
        verify(knowledgeTagBindingDomainService, never()).syncContentTagRef(any(), any(), any(), any(), any());
    }

    @Test
    void batchDeprecateTagsShouldValidateAllTagsBeforeWriting() {
        TagRepository tagRepository = mock(TagRepository.class);
        TaxonomyApplicationService service = new TaxonomyApplicationServiceImpl(
                mock(AiFacade.class),
                mock(TagCategoryRepository.class),
                tagRepository,
                mock(TagAliasRepository.class),
                mock(TagContentRefRepository.class),
                mock(SynonymRepository.class),
                mock(KnowledgeTagBindingDomainService.class),
                mock(TagGovernanceMetricsRepository.class));
        Tag enabledTag = tag(
                TagIdCodec.toDomain(1L),
                "可废弃标签",
                TagCategoryIdCodec.toDomain(11L),
                TagStatus.ENABLED,
                TagReviewStatus.APPROVED);
        Tag disabledTag = tag(
                TagIdCodec.toDomain(3L),
                "已废弃标签",
                TagCategoryIdCodec.toDomain(11L),
                TagStatus.DISABLED,
                TagReviewStatus.APPROVED);
        when(tagRepository.listByTagIds(List.of(TagIdCodec.toDomain(1L), TagIdCodec.toDomain(3L))))
                .thenReturn(List.of(enabledTag, disabledTag));

        assertThrows(
                BizException.class,
                () -> service.batchDeprecateTags(
                        new TagBatchDeprecateCommand(List.of(TagIdCodec.toDomain(1L), TagIdCodec.toDomain(3L)))));

        verify(tagRepository, never()).update(any(Tag.class));
    }

    @Test
    void batchReviewTagsShouldApproveAllWithOneCategory() {
        TagCategoryRepository categoryRepository = mock(TagCategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        TaxonomyApplicationService service = new TaxonomyApplicationServiceImpl(
                mock(AiFacade.class),
                categoryRepository,
                tagRepository,
                mock(TagAliasRepository.class),
                mock(TagContentRefRepository.class),
                mock(SynonymRepository.class),
                mock(KnowledgeTagBindingDomainService.class),
                mock(TagGovernanceMetricsRepository.class));
        Tag firstPending = tag(
                TagIdCodec.toDomain(1L),
                "待审一",
                TagCategoryIdCodec.toDomain(11L),
                TagStatus.ENABLED,
                TagReviewStatus.PENDING);
        Tag secondPending = tag(
                TagIdCodec.toDomain(3L),
                "待审二",
                TagCategoryIdCodec.toDomain(11L),
                TagStatus.ENABLED,
                TagReviewStatus.PENDING);
        when(categoryRepository.getByCategoryId(TagCategoryIdCodec.toDomain(12L)))
                .thenReturn(new TagCategory(
                        TagCategoryIdCodec.toDomain(12L),
                        TagCategoryIdCodec.toDomain(12L),
                        "正式分类",
                        null,
                        1,
                        TagCategoryStatus.ENABLED));
        when(tagRepository.listByTagIds(List.of(TagIdCodec.toDomain(1L), TagIdCodec.toDomain(3L))))
                .thenReturn(List.of(firstPending, secondPending));
        when(tagRepository.updateReviewStatus(any(Tag.class))).thenReturn(1);

        service.batchReviewTags(new TagBatchReviewCommand(
                List.of(TagIdCodec.toDomain(1L), TagIdCodec.toDomain(3L)),
                "APPROVE",
                TagCategoryIdCodec.toDomain(12L),
                "批量通过"));

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository, times(2)).updateReviewStatus(captor.capture());
        assertEquals(TagReviewStatus.APPROVED, captor.getAllValues().get(0).getReviewStatus());
        assertEquals(
                TagCategoryIdCodec.toDomain(12L), captor.getAllValues().get(0).getCategoryId());
        assertEquals("批量通过", captor.getAllValues().get(0).getReviewNote());
        assertEquals(TagReviewStatus.APPROVED, captor.getAllValues().get(1).getReviewStatus());
        assertEquals(
                TagCategoryIdCodec.toDomain(12L), captor.getAllValues().get(1).getCategoryId());
    }

    @Test
    void batchReviewTagsShouldValidateAllStatusesBeforeWriting() {
        TagCategoryRepository categoryRepository = mock(TagCategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        TaxonomyApplicationService service = new TaxonomyApplicationServiceImpl(
                mock(AiFacade.class),
                categoryRepository,
                tagRepository,
                mock(TagAliasRepository.class),
                mock(TagContentRefRepository.class),
                mock(SynonymRepository.class),
                mock(KnowledgeTagBindingDomainService.class),
                mock(TagGovernanceMetricsRepository.class));
        Tag firstPending = tag(
                TagIdCodec.toDomain(1L),
                "待审一",
                TagCategoryIdCodec.toDomain(11L),
                TagStatus.ENABLED,
                TagReviewStatus.PENDING);
        Tag approved = tag(
                TagIdCodec.toDomain(3L),
                "已审核",
                TagCategoryIdCodec.toDomain(11L),
                TagStatus.ENABLED,
                TagReviewStatus.APPROVED);
        when(categoryRepository.getByCategoryId(TagCategoryIdCodec.toDomain(12L)))
                .thenReturn(new TagCategory(
                        TagCategoryIdCodec.toDomain(12L),
                        TagCategoryIdCodec.toDomain(12L),
                        "正式分类",
                        null,
                        1,
                        TagCategoryStatus.ENABLED));
        when(tagRepository.listByTagIds(List.of(TagIdCodec.toDomain(1L), TagIdCodec.toDomain(3L))))
                .thenReturn(List.of(firstPending, approved));

        assertThrows(
                BizException.class,
                () -> service.batchReviewTags(new TagBatchReviewCommand(
                        List.of(TagIdCodec.toDomain(1L), TagIdCodec.toDomain(3L)),
                        "APPROVE",
                        TagCategoryIdCodec.toDomain(12L),
                        "批量通过")));

        verify(tagRepository, never()).updateReviewStatus(any(Tag.class));
    }

    @Test
    void extractTagsShouldBuildAiGovernanceRequest() throws Exception {
        AiFacade aiFacade = mock(AiFacade.class);
        TagCategoryRepository categoryRepository = mock(TagCategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        TagAliasRepository aliasRepository = mock(TagAliasRepository.class);
        TaxonomyApplicationService service = new TaxonomyApplicationServiceImpl(
                aiFacade,
                categoryRepository,
                tagRepository,
                aliasRepository,
                mock(TagContentRefRepository.class),
                mock(SynonymRepository.class),
                mock(KnowledgeTagBindingDomainService.class),
                mock(TagGovernanceMetricsRepository.class));
        TagCategory category = new TagCategory(
                TagCategoryIdCodec.toDomain(11L),
                TagCategoryIdCodec.toDomain(11L),
                "制度",
                null,
                1,
                TagCategoryStatus.ENABLED);
        Tag tag = new Tag(
                TagIdCodec.toDomain(21L),
                TagIdCodec.toDomain(21L),
                "礼制",
                TagCategoryIdCodec.toDomain(11L),
                "礼制标签",
                TagStatus.ENABLED,
                TagSource.MANUAL,
                TagReviewStatus.APPROVED,
                null,
                null,
                null);
        when(categoryRepository.page(null, TagCategoryStatus.ENABLED, 1, 200))
                .thenReturn(PageResult.of(1, 200, 1, List.of(category)));
        when(categoryRepository.getByCategoryId(TagCategoryIdCodec.toDomain(11L)))
                .thenReturn(category);
        when(tagRepository.page(null, null, TagStatus.ENABLED, null, null, 1, 200))
                .thenReturn(PageResult.of(1, 200, 1, List.of(tag)));
        when(aliasRepository.listByTagId(TagIdCodec.toDomain(21L)))
                .thenReturn(List.of(new TagAlias(
                        TagAliasIdCodec.toDomain(31L),
                        TagAliasIdCodec.toDomain(31L),
                        TagIdCodec.toDomain(21L),
                        "礼法",
                        TagSource.MANUAL)));
        when(aiFacade.extractKnowledgeTags(any()))
                .thenReturn(KnowledgeAiExtractionFacadeResponse.builder()
                        .callId(501L)
                        .candidateId(601L)
                        .status("SUCCEEDED")
                        .capability("KNOWLEDGE_TAG_EXTRACTION")
                        .resultFormat("STRUCTURED")
                        .resultPayload("{\"tags\":[]}")
                        .build());

        var result = service.extractTags(
                new TagExtractionCommand("SANCAI_ENTRY", 1001L, "条目标题", "正文片段", 401L, "gpt-5", 301L, 8, true, 201L));

        ArgumentCaptor<KnowledgeAiExtractionFacadeRequest> captor =
                ArgumentCaptor.forClass(KnowledgeAiExtractionFacadeRequest.class);
        verify(aiFacade).extractKnowledgeTags(captor.capture());
        KnowledgeAiExtractionFacadeRequest request = captor.getValue();
        assertEquals("TAG", request.getTaskType());
        assertEquals("CONTENT", request.getScopeType());
        assertEquals("SANCAI_ENTRY", request.getSourceContentType());
        assertEquals(1001L, request.getSourceContentId());
        assertEquals(201L, request.getRequestedBy());
        assertEquals("KNOWLEDGE", request.getServiceRole());
        assertEquals(401L, request.getModelId());
        assertEquals("gpt-5", request.getModelName());
        assertEquals(301L, request.getPromptVersionId());
        assertEquals(request.getRequestId(), request.getTraceId());
        assertEquals("zh-CN", request.getLocale());

        JsonNode scope = OBJECT_MAPPER.readTree(request.getScopeJson());
        assertEquals("SANCAI_ENTRY", scope.get("contentType").asText());
        assertEquals(1001L, scope.get("contentIds").get(0).asLong());
        assertEquals(true, scope.get("includeExistingTags").asBoolean());
        JsonNode input = OBJECT_MAPPER.readTree(request.getInputPayloadJson());
        assertEquals("SANCAI_ENTRY", input.get("contentType").asText());
        assertEquals(1001L, input.get("contentId").asLong());
        assertEquals("条目标题", input.get("sourceTitle").asText());
        assertEquals("正文片段", input.get("sourceText").asText());
        assertNull(input.get("contentText"));
        assertEquals("21", input.get("existingTags").get(0).get("tagId").asText());
        assertEquals("礼制", input.get("existingTags").get(0).get("name").asText());
        assertEquals("11", input.get("categories").get(0).get("categoryId").asText());
        assertEquals("制度", input.get("categories").get(0).get("name").asText());
        assertEquals("礼法", input.get("aliases").get(0).get("name").asText());
        assertEquals(8, input.get("constraints").get("maxTags").asInt());
        assertEquals(true, input.get("constraints").get("allowNewTags").asBoolean());
        assertEquals(true, input.get("constraints").get("reviewRequired").asBoolean());
        JsonNode schema = OBJECT_MAPPER.readTree(request.getOutputSchemaJson());
        assertEquals("object", schema.get("type").asText());
        assertEquals("tags", schema.get("required").get(0).asText());
        assertEquals(true, request.isForceJson());
        assertEquals(501L, result.getAiCallId());
        assertEquals(601L, result.getAiCandidateId());
        assertEquals("SUCCEEDED", result.getStatus());
        assertEquals("STRUCTURED", result.getResultFormat());
        assertEquals("{\"tags\":[]}", result.getResultPayload());
    }

    @Test
    void applyExtractedTagsShouldReuseExistingCreatePendingAndMarkCandidateApplied() throws Exception {
        AiFacade aiFacade = mock(AiFacade.class);
        TagRepository tagRepository = mock(TagRepository.class);
        TaxonomyApplicationService service = new TaxonomyApplicationServiceImpl(
                aiFacade,
                mock(TagCategoryRepository.class),
                tagRepository,
                mock(TagAliasRepository.class),
                mock(TagContentRefRepository.class),
                mock(SynonymRepository.class),
                mock(KnowledgeTagBindingDomainService.class),
                mock(TagGovernanceMetricsRepository.class));
        AiCandidateFacadeDto candidate = AiCandidateFacadeDto.builder()
                .candidateId(601L)
                .capability("KNOWLEDGE_TAG_EXTRACTION")
                .contentType("SANCAI_ENTRY")
                .contentId(1001L)
                .resultFormat("STRUCTURED")
                .resultPayload("{\"tags\":[]}")
                .status("PENDING")
                .build();
        Tag existing = new Tag();
        existing.setTagId(TagIdCodec.toDomain(21L));
        existing.setName("礼制");
        when(aiFacade.getCandidate(any())).thenReturn(candidate);
        when(aiFacade.requirePendingCandidate(any())).thenReturn(candidate);
        when(tagRepository.getByTagId(TagIdCodec.toDomain(21L))).thenReturn(existing);
        when(tagRepository.countByName("新礼俗", null)).thenReturn(0);

        service.applyExtractedTags(new TagCandidateApplyCommand(
                601L,
                List.of(
                        new TagCandidateApplyItemCommand("礼制", "11", "制度", new BigDecimal("0.91"), "匹配既有标签", "21"),
                        new TagCandidateApplyItemCommand("新礼俗", "12", "民俗", new BigDecimal("0.82"), "新的候选标签", null)),
                "AI 审核",
                201L));

        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).insert(tagCaptor.capture());
        Tag inserted = tagCaptor.getValue();
        assertEquals("新礼俗", inserted.getName());
        assertEquals(TagCategoryIdCodec.toDomain(12L), inserted.getCategoryId());
        assertEquals(TagSource.AI_EXTRACTED, inserted.getSource());
        assertEquals(TagReviewStatus.PENDING, inserted.getReviewStatus());
        assertEquals("AI 审核", inserted.getReviewNote());
        ArgumentCaptor<MarkAiCandidateAppliedFacadeRequest> markCaptor =
                ArgumentCaptor.forClass(MarkAiCandidateAppliedFacadeRequest.class);
        verify(aiFacade).markCandidateApplied(markCaptor.capture());
        assertEquals(601L, markCaptor.getValue().getCandidateId());
        assertEquals("STRUCTURED", markCaptor.getValue().getResultFormat());
        JsonNode payload = OBJECT_MAPPER.readTree(markCaptor.getValue().getResultPayload());
        assertEquals(201L, payload.get("reviewedBy").asLong());
        assertEquals("AI 审核", payload.get("reviewNote").asText());
        assertEquals(2, payload.get("selectedTags").size());
        assertEquals(
                "21",
                payload.get("selectedTags").get(0).get("matchedExistingTagId").asText());
    }

    private static Tag tag(
            TagId tagId, String name, TagCategoryId categoryId, TagStatus status, TagReviewStatus reviewStatus) {
        return new Tag(
                tagId, tagId, name, categoryId, name + "描述", status, TagSource.MANUAL, reviewStatus, null, null, null);
    }

    private static TagContentRef contentRef(TagContentRefId id, TagId tagId, Long contentId, String title) {
        return new TagContentRef(id, id, tagId, ContentType.SANCAI_ENTRY, contentId, title, TagSource.AI_EXTRACTED);
    }
}
