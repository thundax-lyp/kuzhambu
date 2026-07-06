package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCandidateApplyCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagAliasResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagContentRefResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagExtractionResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagGovernanceMetricsResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagMergePreviewResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.service.TaxonomyApplicationService;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCandidateApplyRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCandidateApplyRequest.TagCandidateApplyItemRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagDeprecateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagExtractionRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagGovernanceMetricsRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagMergeRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeTaxonomyControllerTest {

    @Test
    void extractTagsShouldMapRequestCommandAndResponse() {
        TaxonomyApplicationService taxonomyService = mock(TaxonomyApplicationService.class);
        KnowledgeTaxonomyController controller = new KnowledgeTaxonomyController(taxonomyService);
        TagExtractionRequest request = new TagExtractionRequest();
        request.setSourceContentType("SANCAI_ENTRY");
        request.setSourceContentId(1001L);
        request.setContentTitle("条目标题");
        request.setContentText("正文片段");
        request.setModelId(401L);
        request.setModelName("gpt-5");
        request.setPromptVersionId(301L);
        request.setMaxTags(8);
        request.setAllowNewTags(true);
        request.setRequestedBy(201L);
        when(taxonomyService.extractTags(any()))
                .thenReturn(
                        new TagExtractionResult(501L, 601L, "SUCCEEDED", "STRUCTURED", "{\"tags\":[]}", null, null));

        var response = controller.extractTags(request);

        ArgumentCaptor<TagExtractionCommand> captor = ArgumentCaptor.forClass(TagExtractionCommand.class);
        verify(taxonomyService).extractTags(captor.capture());
        TagExtractionCommand command = captor.getValue();
        assertEquals("SANCAI_ENTRY", command.getSourceContentType());
        assertEquals(1001L, command.getSourceContentId());
        assertEquals("条目标题", command.getContentTitle());
        assertEquals("正文片段", command.getContentText());
        assertEquals(401L, command.getModelId());
        assertEquals("gpt-5", command.getModelName());
        assertEquals(301L, command.getPromptVersionId());
        assertEquals(8, command.getMaxTags());
        assertEquals(true, command.getAllowNewTags());
        assertEquals(201L, command.getRequestedBy());
        assertEquals(501L, response.getAiCallId());
        assertEquals(601L, response.getAiCandidateId());
        assertEquals("SUCCEEDED", response.getStatus());
        assertEquals("STRUCTURED", response.getResultFormat());
        assertEquals("{\"tags\":[]}", response.getResultPayload());
    }

    @Test
    void applyExtractedTagsShouldMapSelectedCandidates() {
        TaxonomyApplicationService taxonomyService = mock(TaxonomyApplicationService.class);
        KnowledgeTaxonomyController controller = new KnowledgeTaxonomyController(taxonomyService);
        TagCandidateApplyRequest request = new TagCandidateApplyRequest();
        request.setAiCandidateId(601L);
        request.setReviewNote("AI 审核");
        request.setReviewedBy(201L);
        TagCandidateApplyItemRequest item = new TagCandidateApplyItemRequest();
        item.setName("礼制");
        item.setCategoryId("11");
        item.setCategoryName("制度");
        item.setConfidence(new BigDecimal("0.91"));
        item.setReason("匹配既有标签");
        item.setMatchedExistingTagId("21");
        request.setSelectedTags(List.of(item));

        assertTrue(controller.applyExtractedTags(request));

        ArgumentCaptor<TagCandidateApplyCommand> captor = ArgumentCaptor.forClass(TagCandidateApplyCommand.class);
        verify(taxonomyService).applyExtractedTags(captor.capture());
        TagCandidateApplyCommand command = captor.getValue();
        assertEquals(601L, command.getAiCandidateId());
        assertEquals("AI 审核", command.getReviewNote());
        assertEquals(201L, command.getReviewedBy());
        assertEquals(1, command.getSelectedTags().size());
        assertEquals("礼制", command.getSelectedTags().get(0).getName());
        assertEquals("11", command.getSelectedTags().get(0).getCategoryId());
        assertEquals("制度", command.getSelectedTags().get(0).getCategoryName());
        assertEquals(new BigDecimal("0.91"), command.getSelectedTags().get(0).getConfidence());
        assertEquals("匹配既有标签", command.getSelectedTags().get(0).getReason());
        assertEquals("21", command.getSelectedTags().get(0).getMatchedExistingTagId());
    }

    @Test
    void previewTagMergeImpactShouldMapApplicationResult() {
        TaxonomyApplicationService taxonomyService = mock(TaxonomyApplicationService.class);
        KnowledgeTaxonomyController controller = new KnowledgeTaxonomyController(taxonomyService);
        TagMergeRequest request = new TagMergeRequest();
        request.setSourceTagId("1001");
        request.setTargetTagId("1002");
        when(taxonomyService.previewTagMergeImpact(any()))
                .thenReturn(new TagMergePreviewResult(
                        new TagResult("1001", "礼制", "11", "礼学", null, "ENABLED", "MANUAL", "APPROVED", 2, 1L, 2L),
                        new TagResult("1002", "祭祀", "11", "礼学", null, "ENABLED", "AI_EXTRACTED", "APPROVED", 1, 3L, 4L),
                        java.util.List.of(new TagAliasResult("2001", "礼典", "MANUAL")),
                        java.util.List.of(new TagContentRefResult("3001", "CLASSICS", "4001", "周礼", "MANUAL")),
                        1,
                        3));

        var response = controller.previewTagMergeImpact(request);

        verify(taxonomyService).previewTagMergeImpact(any());
        assertEquals("1001", response.getSourceTag().getId());
        assertEquals("1002", response.getTargetTag().getId());
        assertEquals("礼典", response.getAliasesToMerge().get(0).getName());
        assertEquals("周礼", response.getImpactedContentRefs().get(0).getContentTitle());
        assertEquals(1, response.getPendingReviewCount());
        assertEquals(3, response.getGovernedRecordCount());
    }

    @Test
    void applyTagMergeShouldDelegateBusinessAction() {
        TaxonomyApplicationService taxonomyService = mock(TaxonomyApplicationService.class);
        KnowledgeTaxonomyController controller = new KnowledgeTaxonomyController(taxonomyService);
        TagMergeRequest request = new TagMergeRequest();
        request.setSourceTagId("1001");
        request.setTargetTagId("1002");

        assertTrue(controller.applyTagMerge(request));
        verify(taxonomyService).applyTagMerge(any());
    }

    @Test
    void deprecateTagShouldDelegateBusinessAction() {
        TaxonomyApplicationService taxonomyService = mock(TaxonomyApplicationService.class);
        KnowledgeTaxonomyController controller = new KnowledgeTaxonomyController(taxonomyService);
        TagDeprecateRequest request = new TagDeprecateRequest();
        request.setId("1001");

        assertTrue(controller.deprecateTag(request));
        verify(taxonomyService).deprecateTag(any());
    }

    @Test
    void getTagGovernanceMetricsShouldMapAggregatedResult() {
        TaxonomyApplicationService taxonomyService = mock(TaxonomyApplicationService.class);
        KnowledgeTaxonomyController controller = new KnowledgeTaxonomyController(taxonomyService);
        TagGovernanceMetricsRequest request = new TagGovernanceMetricsRequest();
        request.setTopLimit(10);
        request.setRecentMonths(6);
        when(taxonomyService.getTagGovernanceMetrics(any()))
                .thenReturn(new TagGovernanceMetricsResult(
                        java.util.List.of(new TagGovernanceMetricsResult.TagUsageMetric("礼制", 4L)),
                        java.util.List.of(new TagGovernanceMetricsResult.CategoryDistributionMetric("礼学", 2L)),
                        java.util.List.of(new TagGovernanceMetricsResult.SourceRatioMetric(TagSource.MANUAL, 3L)),
                        java.util.List.of(new TagGovernanceMetricsResult.MonthlyNewTagMetric("2025-01", 2L))));

        var response = controller.getTagGovernanceMetrics(request);

        verify(taxonomyService).getTagGovernanceMetrics(any());
        assertEquals("礼制", response.getTopTags().get(0).getTagName());
        assertEquals("礼学", response.getCategoryDistributions().get(0).getCategoryName());
        assertEquals("MANUAL", response.getSourceRatios().get(0).getSource());
        assertEquals("2025-01", response.getMonthlyNewTags().get(0).getMonth());
    }
}
