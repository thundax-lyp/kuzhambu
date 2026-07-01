package com.thundax.kuzhambu.classics.application.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.application.content.result.AiCandidateApplyContentResult;
import com.thundax.kuzhambu.classics.application.content.service.impl.ClassicsContentApplicationServiceImpl;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsTagBindingSupport;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiAssetApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsContentApplicationServiceAiCandidateTest {

    @Test
    void applyAiCandidateTranslateShouldUpdateSancaiAndGenerateAiAppliedVersion() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(11L));
        entry.setTranslationText("old translation");
        entry.setContentUpdatedAt(new Date(1L));
        repository.sancaiEntryForAiApply = entry;

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("SANCAI_ENTRY", request.getContentType());
                    assertEquals(11L, request.getContentId());
                    assertEquals("translate", request.getCapability());
                    return pendingCandidate();
                },
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("TEXT", request.getResultFormat());
                    assertEquals("new translation", request.getResultPayload());
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command =
                applyCommand(11L, ClassicsContentType.SANCAI_ENTRY, 11L, "translate", "new translation");

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.SANCAI_ENTRY, result.getContentType());
        assertEquals(11L, result.getContentId());
        assertEquals(1L, result.getVersionId());
        assertEquals(1, result.getVersionNo());
        assertEquals("new translation", entry.getTranslationText());
        assertEquals(ClassicsContentChangeType.AI_APPLIED, repository.lastInsertedVersion.getChangeType());
        assertEquals("AI 应用：译文", repository.lastInsertedVersion.getChangeSummary());
        assertEquals(1, repository.insertVersionCount);
        assertEquals(1, repository.updateSancaiEntryAiCount);
        verify(aiFacade).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidateSummaryShouldUpdateSancaiAndGenerateAiAppliedVersion() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(11L));
        entry.setSummary("old summary");
        entry.setContentUpdatedAt(new Date(1L));
        repository.sancaiEntryForAiApply = entry;

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("SANCAI_ENTRY", request.getContentType());
                    assertEquals(11L, request.getContentId());
                    assertEquals("summary", request.getCapability());
                    return pendingCandidate();
                },
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("TEXT", request.getResultFormat());
                    assertEquals("new summary", request.getResultPayload());
                    assertEquals(Instant.class, request.getAppliedAt().getClass());
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command =
                applyCommand(11L, ClassicsContentType.SANCAI_ENTRY, 11L, "summary", "new summary");

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.SANCAI_ENTRY, result.getContentType());
        assertEquals(11L, result.getContentId());
        assertEquals(1L, result.getVersionId());
        assertEquals(1, result.getVersionNo());
        assertEquals("new summary", entry.getSummary());
        assertEquals(ClassicsContentChangeType.AI_APPLIED, repository.lastInsertedVersion.getChangeType());
        assertEquals("AI 应用：摘要", repository.lastInsertedVersion.getChangeSummary());
        assertEquals(1, repository.insertVersionCount);
        assertEquals(1, repository.updateSancaiEntryAiCount);
        verify(aiFacade).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidateImageAnalysisShouldUpdateTargetVisualAssetAndSkipVersion() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(11L));
        entry.setContentUpdatedAt(new Date(1L));
        repository.sancaiEntryForAiApply = entry;

        SancaiVisualAsset visualAsset = new SancaiVisualAsset();
        visualAsset.setId(SancaiVisualAssetId.of(111L));

        SancaiAssetApplicationService assetService = org.mockito.Mockito.mock(SancaiAssetApplicationService.class);
        when(assetService.listVisualAssets(SancaiEntryId.of(11L))).thenReturn(List.of(visualAsset));
        when(assetService.updateVisualAsset(visualAsset)).thenReturn(SancaiVisualAssetId.of(111L));

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("SANCAI_ENTRY", request.getContentType());
                    assertEquals(11L, request.getContentId());
                    assertEquals("image_analysis", request.getCapability());
                    return pendingCandidate();
                },
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("TEXT", request.getResultFormat());
                    assertEquals("分析结果", request.getResultPayload());
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade, assetService);
        AiCandidateApplyContentCommand command =
                applyCommand(11L, ClassicsContentType.SANCAI_ENTRY, 11L, "image_analysis", "分析结果", 111L);

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.SANCAI_ENTRY, result.getContentType());
        assertEquals(11L, result.getContentId());
        assertEquals(null, result.getVersionId());
        assertEquals(null, result.getVersionNo());
        assertEquals("分析结果", visualAsset.getImageAnalysisMarkdown());
        assertEquals(null, visualAsset.getFusionDescription());
        assertEquals(null, visualAsset.getVisualDescription());
        assertEquals(0, repository.insertVersionCount);
        assertEquals(0, repository.updateSancaiEntryAiCount);
        verify(aiFacade).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
        verify(assetService).updateVisualAsset(visualAsset);
    }

    @Test
    void applyAiCandidateVisualShouldUpdateTargetVisualAssetAndSkipVersion() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(11L));
        entry.setContentUpdatedAt(new Date(1L));
        repository.sancaiEntryForAiApply = entry;

        SancaiVisualAsset visualAsset = new SancaiVisualAsset();
        visualAsset.setId(SancaiVisualAssetId.of(111L));
        visualAsset.setImageAnalysisMarkdown("old image analysis");
        visualAsset.setFusionDescription("old fusion");

        SancaiAssetApplicationService assetService = org.mockito.Mockito.mock(SancaiAssetApplicationService.class);
        when(assetService.listVisualAssets(SancaiEntryId.of(11L))).thenReturn(List.of(visualAsset));
        when(assetService.updateVisualAsset(visualAsset)).thenReturn(SancaiVisualAssetId.of(111L));

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("SANCAI_ENTRY", request.getContentType());
                    assertEquals(11L, request.getContentId());
                    assertEquals("visual", request.getCapability());
                    return pendingCandidate();
                },
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("TEXT", request.getResultFormat());
                    assertEquals("视觉描述", request.getResultPayload());
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade, assetService);
        AiCandidateApplyContentCommand command =
                applyCommand(11L, ClassicsContentType.SANCAI_ENTRY, 11L, "visual", "视觉描述", 111L);

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.SANCAI_ENTRY, result.getContentType());
        assertEquals(11L, result.getContentId());
        assertEquals(null, result.getVersionId());
        assertEquals(null, result.getVersionNo());
        assertEquals("视觉描述", visualAsset.getVisualDescription());
        assertEquals("old image analysis", visualAsset.getImageAnalysisMarkdown());
        assertEquals("old fusion", visualAsset.getFusionDescription());
        assertEquals(0, repository.insertVersionCount);
        assertEquals(0, repository.updateSancaiEntryAiCount);
        verify(aiFacade).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
        verify(assetService).updateVisualAsset(visualAsset);
    }

    @Test
    void applyAiCandidateImageAnalysisShouldFailWhenObjectIdMissing() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(11L));
        entry.setContentUpdatedAt(new Date(1L));
        repository.sancaiEntryForAiApply = entry;

        AiFacade aiFacade = mockAiFacade(request -> pendingCandidate(), request -> {
            throw new IllegalStateException("markApplied should not be called");
        });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command =
                applyCommand(11L, ClassicsContentType.SANCAI_ENTRY, 11L, "image_analysis", "分析结果", null);

        BizException exception = assertThrows(BizException.class, () -> service.applyAiCandidate(command));
        assertEquals("AI候选应用参数不完整", exception.getMessage());
        assertEquals(0, repository.insertVersionCount);
        assertEquals(0, repository.updateSancaiEntryAiCount);
        verify(aiFacade).requirePendingCandidate(any(RequirePendingAiCandidateFacadeRequest.class));
        verify(aiFacade, never()).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidateImageAnalysisShouldFailWhenVisualAssetNotFound() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(11L));
        repository.sancaiEntryForAiApply = entry;

        SancaiVisualAsset visualAsset = new SancaiVisualAsset();
        visualAsset.setId(SancaiVisualAssetId.of(111L));

        SancaiAssetApplicationService assetService = org.mockito.Mockito.mock(SancaiAssetApplicationService.class);
        when(assetService.listVisualAssets(SancaiEntryId.of(11L))).thenReturn(List.of(visualAsset));
        when(assetService.updateVisualAsset(visualAsset)).thenReturn(SancaiVisualAssetId.of(111L));

        AiFacade aiFacade = mockAiFacade(request -> pendingCandidate(), request -> {
            throw new IllegalStateException("markApplied should not be called");
        });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade, assetService);
        AiCandidateApplyContentCommand command =
                applyCommand(11L, ClassicsContentType.SANCAI_ENTRY, 11L, "image_analysis", "分析结果", 112L);

        BizException exception = assertThrows(BizException.class, () -> service.applyAiCandidate(command));
        assertEquals("三才视觉资产不存在: 112", exception.getMessage());
        assertEquals(0, repository.insertVersionCount);
        assertEquals(0, repository.updateSancaiEntryAiCount);
        verify(aiFacade).requirePendingCandidate(any(RequirePendingAiCandidateFacadeRequest.class));
        verify(assetService, never()).updateVisualAsset(any());
        verify(aiFacade, never()).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidateSummaryShouldUpdateWangqiAndGenerateAiAppliedVersion() {
        FakeRepository repository = new FakeRepository();
        WangqiDocument document = new WangqiDocument(
                WangqiDocumentId.of(22L),
                "title",
                "old summary",
                WangqiContentFormat.HTML,
                "content",
                new Date(),
                null,
                WangqiDocumentVisibility.PUBLIC);
        repository.wangqiDocumentForAiApply = document;

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(22L, request.getCandidateId());
                    assertEquals("WANGQI_DOCUMENT", request.getContentType());
                    assertEquals(22L, request.getContentId());
                    assertEquals("summary", request.getCapability());
                    return pendingCandidate();
                },
                request -> {
                    assertEquals(22L, request.getCandidateId());
                    assertEquals("TEXT", request.getResultFormat());
                    assertEquals("new summary", request.getResultPayload());
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command =
                applyCommand(22L, ClassicsContentType.WANGQI_DOCUMENT, 22L, "summary", "new summary");

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.WANGQI_DOCUMENT, result.getContentType());
        assertEquals(22L, result.getContentId());
        assertEquals(1L, result.getVersionId());
        assertEquals(1, result.getVersionNo());
        assertEquals("new summary", document.getSummary());
        assertEquals(ClassicsContentChangeType.AI_APPLIED, repository.lastInsertedVersion.getChangeType());
        assertEquals("AI 应用：摘要", repository.lastInsertedVersion.getChangeSummary());
        assertEquals(1, repository.insertVersionCount);
        assertEquals(1, repository.updateWangqiDocumentAiCount);
        verify(aiFacade).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidateSummaryShouldUpdateMingCustomsAndGenerateAiAppliedVersion() {
        FakeRepository repository = new FakeRepository();
        MingCustomsEntry entry = new MingCustomsEntry();
        entry.setId(MingCustomsEntryId.of(33L));
        entry.setSummary("old summary");
        repository.mingCustomsEntryForAiApply = entry;

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(33L, request.getCandidateId());
                    assertEquals("MING_CUSTOMS", request.getContentType());
                    assertEquals(33L, request.getContentId());
                    assertEquals("summary", request.getCapability());
                    return pendingCandidate();
                },
                request -> {
                    assertEquals(33L, request.getCandidateId());
                    assertEquals("TEXT", request.getResultFormat());
                    assertEquals("new summary", request.getResultPayload());
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command =
                applyCommand(33L, ClassicsContentType.MING_CUSTOMS, 33L, "summary", "new summary");

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.MING_CUSTOMS, result.getContentType());
        assertEquals(33L, result.getContentId());
        assertEquals(1L, result.getVersionId());
        assertEquals(1, result.getVersionNo());
        assertEquals("new summary", entry.getSummary());
        assertEquals(ClassicsContentChangeType.AI_APPLIED, repository.lastInsertedVersion.getChangeType());
        assertEquals("AI 应用：摘要", repository.lastInsertedVersion.getChangeSummary());
        assertEquals(1, repository.insertVersionCount);
        assertEquals(1, repository.updateMingCustomsEntryAiCount);
        verify(aiFacade).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidateTagsShouldOnlyReplaceAiTagsAndCreateAiAppliedVersion() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(11L));
        repository.sancaiEntryForAiApply = entry;
        repository.tags.add(manualTag(1L, 11L, "manual-tag"));
        repository.tags.add(aiTag(2L, 11L, "old-ai-tag"));

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("SANCAI_ENTRY", request.getContentType());
                    assertEquals(11L, request.getContentId());
                    assertEquals("tags", request.getCapability());
                    return pendingCandidate();
                },
                request -> candidateApplied());

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command = applyCommand(
                11L,
                ClassicsContentType.SANCAI_ENTRY,
                11L,
                "tags",
                "{\"tags\":[\"ai-one\",\"ai-two\",\"ai-one\",\"\"]}");

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.SANCAI_ENTRY, result.getContentType());
        assertEquals(11L, result.getContentId());
        assertEquals(ClassicsContentChangeType.AI_APPLIED, repository.lastInsertedVersion.getChangeType());
        assertEquals("AI 应用：标签", repository.lastInsertedVersion.getChangeSummary());
        assertEquals(1, repository.deleteAiTagsCount);
        assertEquals(2, repository.insertTagCount);
        assertEquals(1, repository.insertVersionCount);
        assertEquals(3, repository.tags.size());
        assertEquals(
                1,
                repository.tags.stream()
                        .filter(tag -> tag.getSource() == ClassicsContentSource.MANUAL)
                        .count());
        assertEquals(
                2,
                repository.tags.stream()
                        .filter(tag -> tag.getSource() == ClassicsContentSource.AI)
                        .count());
        verify(aiFacade).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidateTagsShouldSyncKnowledgeRefsWhenBindingSupportPresent() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(11L));
        repository.sancaiEntryForAiApply = entry;
        ClassicsContentTag oldAiTag = aiTag(2L, 11L, "old-ai-tag");
        repository.tags.add(manualTag(1L, 11L, "manual-tag"));
        repository.tags.add(oldAiTag);

        AiFacade aiFacade = mockAiFacade(request -> pendingCandidate(), request -> candidateApplied());
        ClassicsTagBindingSupport tagBindingSupport = org.mockito.Mockito.mock(ClassicsTagBindingSupport.class);
        when(tagBindingSupport.bindAiTag(any(ContentTagCommand.class), any())).thenAnswer(invocation -> {
            ContentTagCommand command = invocation.getArgument(0);
            return command.toEntity();
        });

        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, null, null, aiFacade, tagBindingSupport, null);

        service.applyAiCandidate(
                applyCommand(11L, ClassicsContentType.SANCAI_ENTRY, 11L, "tags", "{\"tags\":[\"ai-one\",\"ai-two\"]}"));

        verify(tagBindingSupport).removeTagRef(oldAiTag);
        verify(tagBindingSupport, times(2)).bindAiTag(any(ContentTagCommand.class), any());
        verify(tagBindingSupport, times(2)).syncTagRef(any(ClassicsContentTag.class));
    }

    @Test
    void applyAiCandidateQaShouldOnlyReplaceAiQaPairsAndCreateAiAppliedVersion() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(11L));
        repository.sancaiEntryForAiApply = entry;
        repository.qaPairs.add(manualQaPair(1L, 11L, "manual-q", "manual-a"));
        repository.qaPairs.add(aiQaPair(2L, 11L, "old-q", "old-a"));

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("SANCAI_ENTRY", request.getContentType());
                    assertEquals(11L, request.getContentId());
                    assertEquals("qa", request.getCapability());
                    return pendingCandidate();
                },
                request -> candidateApplied());

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command = applyCommand(
                11L,
                ClassicsContentType.SANCAI_ENTRY,
                11L,
                "qa",
                "{\"qaPairs\":[{\"question\":\"q1\",\"answer\":\"a\"},{\"question\":\"q2\",\"answer\":\"b\"},{\"question\":\"q1\",\"answer\":\"a\"}]}");

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.SANCAI_ENTRY, result.getContentType());
        assertEquals(11L, result.getContentId());
        assertEquals(ClassicsContentChangeType.AI_APPLIED, repository.lastInsertedVersion.getChangeType());
        assertEquals("AI 应用：问答对", repository.lastInsertedVersion.getChangeSummary());
        assertEquals(1, repository.deleteAiQaPairsCount);
        assertEquals(2, repository.insertQaPairCount);
        assertEquals(3, repository.qaPairs.size());
        assertEquals(
                1,
                repository.qaPairs.stream()
                        .filter(pair -> pair.getSource() == ClassicsContentSource.MANUAL)
                        .count());
        assertEquals(
                2,
                repository.qaPairs.stream()
                        .filter(pair -> pair.getSource() == ClassicsContentSource.AI)
                        .count());
        verify(aiFacade).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidateShouldFailWhenAiCandidateNotPending() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(11L));
        entry.setSummary("old summary");
        repository.sancaiEntryForAiApply = entry;

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    throw new DomainException(
                            "AI-INVOCATION-409",
                            "ai.candidate.not-pending",
                            "AI candidate is not pending: " + request.getCandidateId());
                },
                request -> {
                    throw new IllegalStateException("markApplied should not be called");
                });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command =
                applyCommand(11L, ClassicsContentType.SANCAI_ENTRY, 11L, "summary", "new summary");

        assertThrows(DomainException.class, () -> service.applyAiCandidate(command));

        assertEquals("old summary", entry.getSummary());
        assertEquals(0, repository.insertVersionCount);
        assertEquals(0, repository.updateSancaiEntryAiCount);
        verify(aiFacade, never()).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    private static ClassicsContentApplicationServiceImpl serviceWithAiFacade(
            ClassicsContentRepository repository, AiFacade aiFacade) {
        return serviceWithAiFacade(repository, aiFacade, null);
    }

    private static ClassicsContentApplicationServiceImpl serviceWithAiFacade(
            ClassicsContentRepository repository, AiFacade aiFacade, SancaiAssetApplicationService assetService) {
        return new ClassicsContentApplicationServiceImpl(
                repository, null, null, assetService, null, null, aiFacade, null, null);
    }

    private static AiCandidateApplyContentCommand applyCommand(
            Long candidateId, ClassicsContentType contentType, Long contentId, String capability, String payload) {
        return applyCommand(candidateId, contentType, contentId, capability, payload, null);
    }

    private static AiCandidateApplyContentCommand applyCommand(
            Long candidateId,
            ClassicsContentType contentType,
            Long contentId,
            String capability,
            String payload,
            Long objectId) {
        AiCandidateApplyContentCommand command = new AiCandidateApplyContentCommand();
        command.setCandidateId(candidateId);
        command.setContentType(contentType);
        command.setContentId(contentId);
        command.setObjectId(objectId);
        command.setCapability(capability);
        command.setResultFormat("TEXT");
        command.setResultPayload(payload);
        return command;
    }

    private static AiCandidateFacadeDto pendingCandidate() {
        return AiCandidateFacadeDto.builder().candidateId(11L).status("PENDING").build();
    }

    private static AiCandidateFacadeDto candidateApplied() {
        return AiCandidateFacadeDto.builder()
                .candidateId(11L)
                .status("APPLIED")
                .appliedAt(Instant.now())
                .build();
    }

    private static AiFacade mockAiFacade(
            java.util.function.Function<RequirePendingAiCandidateFacadeRequest, AiCandidateFacadeDto> requirePending,
            java.util.function.Function<MarkAiCandidateAppliedFacadeRequest, AiCandidateFacadeDto> markApplied) {
        AiFacade aiFacade = org.mockito.Mockito.mock(AiFacade.class);
        when(aiFacade.requirePendingCandidate(any(RequirePendingAiCandidateFacadeRequest.class)))
                .thenAnswer(invocation -> {
                    RequirePendingAiCandidateFacadeRequest check = invocation.getArgument(0);
                    return requirePending.apply(check);
                });
        when(aiFacade.markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class)))
                .thenAnswer(invocation -> {
                    MarkAiCandidateAppliedFacadeRequest request = invocation.getArgument(0);
                    return markApplied.apply(request);
                });
        return aiFacade;
    }

    private static ClassicsContentTag manualTag(Long id, Long contentId, String tagName) {
        ClassicsContentTag tag = new ClassicsContentTag();
        tag.setId(ClassicsContentTagId.of(id));
        tag.setContentType(ClassicsContentType.SANCAI_ENTRY);
        tag.setContentId(ClassicsContentId.of(contentId));
        tag.setTagNameSnapshot(tagName);
        tag.setSource(ClassicsContentSource.MANUAL);
        tag.setStatus(ClassicsContentTagStatus.ACTIVE);
        return tag;
    }

    private static ClassicsContentTag aiTag(Long id, Long contentId, String tagName) {
        ClassicsContentTag tag = manualTag(id, contentId, tagName);
        tag.setSource(ClassicsContentSource.AI);
        return tag;
    }

    private static ClassicsContentQaPair manualQaPair(Long id, Long contentId, String question, String answer) {
        ClassicsContentQaPair qaPair = new ClassicsContentQaPair();
        qaPair.setId(ClassicsContentQaPairId.of(id));
        qaPair.setContentType(ClassicsContentType.SANCAI_ENTRY);
        qaPair.setContentId(ClassicsContentId.of(contentId));
        qaPair.setQuestion(question);
        qaPair.setAnswer(answer);
        qaPair.setSource(ClassicsContentSource.MANUAL);
        return qaPair;
    }

    private static ClassicsContentQaPair aiQaPair(Long id, Long contentId, String question, String answer) {
        ClassicsContentQaPair qaPair = manualQaPair(id, contentId, question, answer);
        qaPair.setSource(ClassicsContentSource.AI);
        return qaPair;
    }

    private static final class FakeRepository implements ClassicsContentRepository {
        private final List<ClassicsContentVersion> versions = new ArrayList<>();
        private final List<ClassicsContentTag> tags = new ArrayList<>();
        private final List<ClassicsContentQaPair> qaPairs = new ArrayList<>();
        private ClassicsContentVersion lastInsertedVersion;
        private int insertVersionCount;
        private int updateSancaiEntryAiCount;
        private int updateWangqiDocumentAiCount;
        private int updateMingCustomsEntryAiCount;
        private int deleteAiTagsCount;
        private int deleteAiQaPairsCount;
        private int insertTagCount;
        private int insertQaPairCount;
        private SancaiEntry sancaiEntryForAiApply;
        private WangqiDocument wangqiDocumentForAiApply;
        private MingCustomsEntry mingCustomsEntryForAiApply;

        @Override
        public List<ClassicsContentVersion> listVersions(String contentType, ClassicsContentId contentId) {
            return versions;
        }

        @Override
        public ClassicsContentVersionId insertVersion(ClassicsContentVersion version) {
            ClassicsContentVersionId id = ClassicsContentVersionId.of(versions.size() + 1L);
            version.setId(id);
            version.setVersionNo(versions.size() + 1);
            versions.add(version);
            insertVersionCount++;
            lastInsertedVersion = version;
            return id;
        }

        @Override
        public List<ClassicsContentTag> listTags(
                String contentType,
                ClassicsContentId contentId,
                com.thundax.kuzhambu.common.core.sort.SortDirection sortDirection) {
            return tags;
        }

        @Override
        public int maxTagPriority(String contentType, ClassicsContentId contentId) {
            return tags.size();
        }

        @Override
        public ClassicsContentTagId insertTag(ClassicsContentTag tag) {
            tags.add(tag);
            insertTagCount++;
            return ClassicsContentTagId.of((long) tags.size());
        }

        @Override
        public ClassicsContentTag getTagById(ClassicsContentTagId id) {
            return null;
        }

        @Override
        public int updateTagPriority(ClassicsContentTag tag) {
            return 1;
        }

        @Override
        public int updateTag(ClassicsContentTag tag) {
            return 1;
        }

        @Override
        public int deleteTagById(String contentType, ClassicsContentId contentId, ClassicsContentTagId id) {
            return 1;
        }

        @Override
        public List<ClassicsContentQaPair> listQaPairs(
                String contentType,
                ClassicsContentId contentId,
                com.thundax.kuzhambu.common.core.sort.SortDirection sortDirection) {
            return qaPairs;
        }

        @Override
        public List<ClassicsContentQaPair> listQaPairs(
                com.thundax.kuzhambu.common.core.sort.SortDirection sortDirection) {
            return qaPairs;
        }

        @Override
        public int maxQaPairPriority() {
            return qaPairs.size();
        }

        @Override
        public ClassicsContentQaPairId insertQaPair(ClassicsContentQaPair qaPair) {
            qaPairs.add(qaPair);
            insertQaPairCount++;
            return ClassicsContentQaPairId.of((long) qaPairs.size());
        }

        @Override
        public ClassicsContentQaPair getQaPairById(ClassicsContentQaPairId id) {
            return null;
        }

        @Override
        public int updateQaPairPriority(ClassicsContentQaPair qaPair) {
            return 1;
        }

        @Override
        public int updateQaPair(ClassicsContentQaPair qaPair) {
            return 1;
        }

        @Override
        public int deleteQaPairById(ClassicsContentQaPairId id) {
            return 1;
        }

        @Override
        public ClassicsContentVersion getVersionById(ClassicsContentVersionId id) {
            return versions.stream()
                    .filter(version -> version.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public int deleteVersions(String contentType, ClassicsContentId contentId) {
            return 1;
        }

        @Override
        public SancaiEntry getSancaiEntryForAiApply(ClassicsContentId contentId) {
            return sancaiEntryForAiApply;
        }

        @Override
        public int updateSancaiEntryAiFields(SancaiEntry entry) {
            updateSancaiEntryAiCount++;
            return 1;
        }

        @Override
        public WangqiDocument getWangqiDocumentForAiApply(ClassicsContentId contentId) {
            return wangqiDocumentForAiApply;
        }

        @Override
        public int updateWangqiDocumentAiFields(WangqiDocument document) {
            updateWangqiDocumentAiCount++;
            return 1;
        }

        @Override
        public MingCustomsEntry getMingCustomsEntryForAiApply(ClassicsContentId contentId) {
            return mingCustomsEntryForAiApply;
        }

        @Override
        public int updateMingCustomsEntryAiFields(MingCustomsEntry entry) {
            updateMingCustomsEntryAiCount++;
            return 1;
        }

        @Override
        public int deleteAiTags(String contentType, ClassicsContentId contentId) {
            deleteAiTagsCount++;
            tags.removeIf(tag -> tag.getContentType() != null
                    && tag.getContentType().value().equals(contentType)
                    && tag.getContentId() != null
                    && tag.getContentId().equals(contentId)
                    && tag.getSource() == ClassicsContentSource.AI);
            return 1;
        }

        @Override
        public int deleteAiQaPairs(String contentType, ClassicsContentId contentId) {
            deleteAiQaPairsCount++;
            qaPairs.removeIf(pair -> pair.getContentType() != null
                    && pair.getContentType().value().equals(contentType)
                    && pair.getContentId() != null
                    && pair.getContentId().equals(contentId)
                    && pair.getSource() == ClassicsContentSource.AI);
            return 1;
        }

        @Override
        public ClassicsContentExportJobId insertExportJob(ClassicsContentExportJob exportJob) {
            return null;
        }

        @Override
        public ClassicsContentExportJob getExportJobById(ClassicsContentExportJobId id) {
            return null;
        }

        @Override
        public int updateExportJob(ClassicsContentExportJob exportJob) {
            return 1;
        }

        @Override
        public int markExportJobCompleted(
                ClassicsContentExportJobId id,
                com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId storageObjectId,
                Date expiresAt,
                int itemCount,
                int assetCount) {
            return 1;
        }

        @Override
        public int markExportJobFailed(ClassicsContentExportJobId id) {
            return 1;
        }

        @Override
        public int markExportJobExpired(ClassicsContentExportJobId id) {
            return 1;
        }

        @Override
        public PageResult<ClassicsContentExportJob> pageExportJobs(
                String contentType, String exportKind, String status, int pageNo, int pageSize) {
            return new PageResult<>();
        }
    }
}
