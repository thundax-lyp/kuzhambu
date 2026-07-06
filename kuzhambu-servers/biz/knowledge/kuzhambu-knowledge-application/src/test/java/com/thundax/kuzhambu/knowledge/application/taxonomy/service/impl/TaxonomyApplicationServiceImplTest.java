package com.thundax.kuzhambu.knowledge.application.taxonomy.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeAiExtractionFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.KnowledgeAiExtractionFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagDeprecateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagMergeCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagGovernanceMetricsQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagMergePreviewQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagMergePreviewResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.service.TaxonomyApplicationService;
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
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.readmodel.TagGovernanceMetrics;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagAliasId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagContentRefId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.SynonymRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagAliasRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagCategoryRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagContentRefRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagGovernanceMetricsRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
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
        tag.setId(TagId.of(11L));
        tag.setTagId(TagId.of(1L));
        tag.setStatus(TagStatus.ENABLED);
        when(tagRepository.getByTagId(TagId.of(1L))).thenReturn(tag);
        when(tagRepository.update(tag)).thenReturn(1);

        service.deprecateTag(new TagDeprecateCommand(TagId.of(1L)));

        assertEquals(TagStatus.DISABLED, tag.getStatus());
        assertThrows(BizException.class, () -> service.deprecateTag(new TagDeprecateCommand(TagId.of(1L))));
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
        sourceTag.setId(TagId.of(11L));
        sourceTag.setTagId(TagId.of(1L));
        sourceTag.setStatus(TagStatus.ENABLED);
        Tag targetTag = new Tag();
        targetTag.setId(TagId.of(12L));
        targetTag.setTagId(TagId.of(2L));
        targetTag.setStatus(TagStatus.ENABLED);
        when(tagRepository.getByTagId(TagId.of(1L))).thenReturn(sourceTag);
        when(tagRepository.getByTagId(TagId.of(2L))).thenReturn(targetTag);
        when(contentRefRepository.listByTagId(TagId.of(1L)))
                .thenReturn(List.of(new TagContentRef(
                        TagContentRefId.of(31L),
                        TagContentRefId.of(31L),
                        TagId.of(1L),
                        ContentType.SANCAI_ENTRY,
                        1001L,
                        "内容一",
                        TagSource.AI_EXTRACTED)));
        when(tagRepository.update(sourceTag)).thenReturn(1);

        service.applyTagMerge(new TagMergeCommand(TagId.of(1L), TagId.of(2L)));

        assertEquals(TagId.of(2L), sourceTag.getMergedToTagId());
        assertNull(targetTag.getMergedToTagId());
        verify(tagRepository).update(sourceTag);
        verify(knowledgeTagBindingDomainService, times(1))
                .syncContentTagRef(TagId.of(2L), ContentType.SANCAI_ENTRY, 1001L, "内容一", TagSource.AI_EXTRACTED);
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
                TagId.of(1L),
                TagId.of(1L),
                "源标签",
                TagCategoryId.of(11L),
                "source",
                TagStatus.ENABLED,
                TagSource.AI_EXTRACTED,
                TagReviewStatus.PENDING,
                null,
                null,
                null);
        Tag targetTag = new Tag(
                TagId.of(2L),
                TagId.of(2L),
                "目标标签",
                TagCategoryId.of(12L),
                "target",
                TagStatus.ENABLED,
                TagSource.MANUAL,
                TagReviewStatus.APPROVED,
                null,
                null,
                null);
        when(tagRepository.getByTagId(TagId.of(1L))).thenReturn(sourceTag);
        when(tagRepository.getByTagId(TagId.of(2L))).thenReturn(targetTag);
        when(categoryRepository.getByCategoryId(TagCategoryId.of(11L)))
                .thenReturn(new TagCategory(TagCategoryId.of(11L), TagCategoryId.of(11L), "源分类", null, 1, null));
        when(categoryRepository.getByCategoryId(TagCategoryId.of(12L)))
                .thenReturn(new TagCategory(TagCategoryId.of(12L), TagCategoryId.of(12L), "目标分类", null, 1, null));
        when(aliasRepository.listByTagId(TagId.of(1L)))
                .thenReturn(List.of(
                        new TagAlias(TagAliasId.of(21L), TagAliasId.of(21L), TagId.of(1L), "别名一", TagSource.MANUAL)));
        when(contentRefRepository.listByTagId(TagId.of(1L)))
                .thenReturn(List.of(new TagContentRef(
                        TagContentRefId.of(31L),
                        TagContentRefId.of(31L),
                        TagId.of(1L),
                        ContentType.SANCAI_ENTRY,
                        1001L,
                        "内容一",
                        TagSource.AI_EXTRACTED)));
        when(contentRefRepository.countByTagId(TagId.of(2L))).thenReturn(2);

        TagMergePreviewResult result =
                service.previewTagMergeImpact(new TagMergePreviewQuery(TagId.of(1L), TagId.of(2L)));

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
        TagCategory category =
                new TagCategory(TagCategoryId.of(11L), TagCategoryId.of(11L), "制度", null, 1, TagCategoryStatus.ENABLED);
        Tag tag = new Tag(
                TagId.of(21L),
                TagId.of(21L),
                "礼制",
                TagCategoryId.of(11L),
                "礼制标签",
                TagStatus.ENABLED,
                TagSource.MANUAL,
                TagReviewStatus.APPROVED,
                null,
                null,
                null);
        when(categoryRepository.page(null, TagCategoryStatus.ENABLED, 1, 200))
                .thenReturn(PageResult.of(1, 200, 1, List.of(category)));
        when(categoryRepository.getByCategoryId(TagCategoryId.of(11L))).thenReturn(category);
        when(tagRepository.page(null, null, TagStatus.ENABLED, null, null, 1, 200))
                .thenReturn(PageResult.of(1, 200, 1, List.of(tag)));
        when(aliasRepository.listByTagId(TagId.of(21L)))
                .thenReturn(List.of(
                        new TagAlias(TagAliasId.of(31L), TagAliasId.of(31L), TagId.of(21L), "礼法", TagSource.MANUAL)));
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
        assertEquals("条目标题", input.get("contentTitle").asText());
        assertEquals("正文片段", input.get("contentText").asText());
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
}
