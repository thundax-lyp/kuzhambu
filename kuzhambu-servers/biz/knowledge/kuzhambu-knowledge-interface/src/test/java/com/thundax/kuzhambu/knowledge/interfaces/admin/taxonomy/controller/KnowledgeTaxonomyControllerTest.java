package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchDeprecateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchMergeCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchReviewCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCandidateApplyCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagBatchMergePreviewQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagAliasResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagBatchMergePreviewResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagContentRefResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagExtractionResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagGovernanceMetricsResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagMergePreviewResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.service.TaxonomyApplicationService;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagBatchDeprecateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagBatchMergeRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagBatchReviewRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCandidateApplyRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCandidateApplyRequest.TagCandidateApplyItemRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagDeprecateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagExtractionRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagGovernanceMetricsRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagMergeRequest;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.bind.annotation.PostMapping;

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
        assertEquals("SANCAI_ENTRY", command.sourceContentType());
        assertEquals(1001L, command.sourceContentId());
        assertEquals("条目标题", command.contentTitle());
        assertEquals("正文片段", command.contentText());
        assertEquals(401L, command.modelId());
        assertEquals("gpt-5", command.modelName());
        assertEquals(301L, command.promptVersionId());
        assertEquals(8, command.maxTags());
        assertEquals(true, command.allowNewTags());
        assertEquals(201L, command.requestedBy());
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
        assertEquals(601L, command.aiCandidateId());
        assertEquals("AI 审核", command.reviewNote());
        assertEquals(201L, command.reviewedBy());
        assertEquals(1, command.selectedTags().size());
        assertEquals("礼制", command.selectedTags().get(0).name());
        assertEquals("11", command.selectedTags().get(0).categoryId());
        assertEquals("制度", command.selectedTags().get(0).categoryName());
        assertEquals(new BigDecimal("0.91"), command.selectedTags().get(0).confidence());
        assertEquals("匹配既有标签", command.selectedTags().get(0).reason());
        assertEquals("21", command.selectedTags().get(0).matchedExistingTagId());
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
    void previewTagBatchMergeImpactShouldMapApplicationResult() {
        TaxonomyApplicationService taxonomyService = mock(TaxonomyApplicationService.class);
        KnowledgeTaxonomyController controller = new KnowledgeTaxonomyController(taxonomyService);
        TagBatchMergeRequest request = new TagBatchMergeRequest();
        request.setSourceTagIds(List.of("1001", "1003"));
        request.setTargetTagId("1002");
        when(taxonomyService.previewTagBatchMergeImpact(any()))
                .thenReturn(new TagBatchMergePreviewResult(
                        List.of(
                                new TagResult(
                                        "1001", "礼制", "11", "礼学", null, "ENABLED", "MANUAL", "APPROVED", 2, 1L, 2L),
                                new TagResult(
                                        "1003",
                                        "礼典",
                                        "11",
                                        "礼学",
                                        null,
                                        "ENABLED",
                                        "AI_EXTRACTED",
                                        "PENDING",
                                        1,
                                        3L,
                                        4L)),
                        new TagResult("1002", "祭祀", "11", "礼学", null, "ENABLED", "MANUAL", "APPROVED", 3, 5L, 6L),
                        List.of(new TagAliasResult("2001", "礼法", "MANUAL")),
                        List.of(new TagContentRefResult("3001", "CLASSICS", "4001", "周礼", "MANUAL")),
                        1,
                        3));

        var response = controller.previewTagBatchMergeImpact(request);

        ArgumentCaptor<TagBatchMergePreviewQuery> captor = ArgumentCaptor.forClass(TagBatchMergePreviewQuery.class);
        verify(taxonomyService).previewTagBatchMergeImpact(captor.capture());
        assertEquals(2, captor.getValue().sourceTagIds().size());
        assertEquals(1001L, captor.getValue().sourceTagIds().get(0).value());
        assertEquals(1002L, captor.getValue().targetTagId().value());
        assertEquals(2, response.getSourceTags().size());
        assertEquals("祭祀", response.getTargetTag().getName());
        assertEquals("礼法", response.getAliasesToMerge().get(0).getName());
        assertEquals("周礼", response.getImpactedContentRefs().get(0).getContentTitle());
        assertEquals(1, response.getPendingReviewCount());
        assertEquals(3, response.getGovernedRecordCount());
    }

    @Test
    void applyTagBatchMergeShouldDelegateBusinessAction() {
        TaxonomyApplicationService taxonomyService = mock(TaxonomyApplicationService.class);
        KnowledgeTaxonomyController controller = new KnowledgeTaxonomyController(taxonomyService);
        TagBatchMergeRequest request = new TagBatchMergeRequest();
        request.setSourceTagIds(List.of("1001", "1003"));
        request.setTargetTagId("1002");

        assertTrue(controller.applyTagBatchMerge(request));

        ArgumentCaptor<TagBatchMergeCommand> captor = ArgumentCaptor.forClass(TagBatchMergeCommand.class);
        verify(taxonomyService).applyTagBatchMerge(captor.capture());
        assertEquals(2, captor.getValue().sourceTagIds().size());
        assertEquals(1003L, captor.getValue().sourceTagIds().get(1).value());
        assertEquals(1002L, captor.getValue().targetTagId().value());
    }

    @Test
    void batchDeprecateTagsShouldDelegateBusinessAction() {
        TaxonomyApplicationService taxonomyService = mock(TaxonomyApplicationService.class);
        KnowledgeTaxonomyController controller = new KnowledgeTaxonomyController(taxonomyService);
        TagBatchDeprecateRequest request = new TagBatchDeprecateRequest();
        request.setTagIds(List.of("1001", "1003"));

        assertTrue(controller.batchDeprecateTags(request));

        ArgumentCaptor<TagBatchDeprecateCommand> captor = ArgumentCaptor.forClass(TagBatchDeprecateCommand.class);
        verify(taxonomyService).batchDeprecateTags(captor.capture());
        assertEquals(2, captor.getValue().tagIds().size());
        assertEquals(1001L, captor.getValue().tagIds().get(0).value());
    }

    @Test
    void batchReviewTagsShouldDelegateBusinessAction() {
        TaxonomyApplicationService taxonomyService = mock(TaxonomyApplicationService.class);
        KnowledgeTaxonomyController controller = new KnowledgeTaxonomyController(taxonomyService);
        TagBatchReviewRequest request = new TagBatchReviewRequest();
        request.setTagIds(List.of("1001", "1003"));
        request.setDecision("APPROVE");
        request.setCategoryId("11");
        request.setReviewNote("批量通过");

        assertTrue(controller.batchReviewTags(request));

        ArgumentCaptor<TagBatchReviewCommand> captor = ArgumentCaptor.forClass(TagBatchReviewCommand.class);
        verify(taxonomyService).batchReviewTags(captor.capture());
        assertEquals(2, captor.getValue().tagIds().size());
        assertEquals("APPROVE", captor.getValue().decision());
        assertEquals(11L, captor.getValue().categoryId().value());
        assertEquals("批量通过", captor.getValue().reviewNote());
    }

    @Test
    void batchEndpointsShouldKeepExpectedRoutesPermissionsAndAuditText() throws Exception {
        assertEndpoint(
                "previewTagBatchMergeImpact",
                "tag/merge/batch-preview",
                "knowledge:taxonomy:view",
                "批量预览标签合并",
                TagBatchMergeRequest.class);
        assertEndpoint(
                "applyTagBatchMerge",
                "tag/merge/batch-apply",
                "knowledge:taxonomy:edit",
                "批量执行标签合并",
                TagBatchMergeRequest.class);
        assertEndpoint(
                "batchDeprecateTags",
                "tag/deprecate/batch",
                "knowledge:taxonomy:edit",
                "批量废弃标签",
                TagBatchDeprecateRequest.class);
        assertEndpoint(
                "batchReviewTags",
                "tag/review/batch",
                "knowledge:taxonomy:review",
                "批量审核标签",
                TagBatchReviewRequest.class);
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

    private static void assertEndpoint(
            String methodName, String path, String permission, String auditText, Class<?> requestType)
            throws Exception {
        Method method = KnowledgeTaxonomyController.class.getDeclaredMethod(methodName, requestType);
        assertEquals(path, method.getAnnotation(PostMapping.class).value()[0]);
        assertArrayEquals(
                new String[] {permission},
                method.getAnnotation(HasPermission.class).value());
        assertEquals(auditText, method.getAnnotation(SysLogger.class).value());
    }
}
