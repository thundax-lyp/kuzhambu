package com.thundax.kuzhambu.classics.application.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RejectAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import com.thundax.kuzhambu.classics.application.content.assembler.ClassicsContentApplicationAssembler;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchRejectContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchRejectContentItemCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.application.content.result.AiCandidateApplyContentResult;
import com.thundax.kuzhambu.classics.application.content.service.impl.ClassicsContentApplicationServiceImpl;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsTagBindingSupport;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationWriteGuard;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationWriteOperation;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationItemResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVisualAssetCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVisualAssetFusionCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVisualAssetVersionCommand;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiAssetApplicationService;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentQaPairIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentTagIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
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
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationContent;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVisualAssetIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubject;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ClassicsContentApplicationServiceAiCandidateTest {

    private static final String AI_CAPABILITY_CLASSICS_TRANSLATE = "CLASSICS_TRANSLATE";
    private static final String AI_CAPABILITY_CLASSICS_SUMMARY = "CLASSICS_SUMMARY";
    private static final String AI_CAPABILITY_CLASSICS_TAGS = "CLASSICS_TAG_EXTRACT";
    private static final String AI_CAPABILITY_CLASSICS_QA = "CLASSICS_QA";
    private static final String AI_CAPABILITY_CLASSICS_IMAGE_DESCRIBE = "CLASSICS_IMAGE_DESCRIBE";
    private static final String AI_CAPABILITY_CLASSICS_IMAGE_PROMPT_FUSION = "CLASSICS_IMAGE_PROMPT_FUSION";
    private static final String AI_CAPABILITY_CLASSICS_VISUAL_DESCRIBE = "CLASSICS_VISUAL_DESCRIBE";
    private static final String AI_CAPABILITY_CLASSICS_IMAGE_GENERATE = "CLASSICS_IMAGE_GENERATE";

    @Test
    void applyAiCandidateShouldCheckPublicationStateBeforeCallingAiFacade() {
        FakeRepository repository = new FakeRepository();
        AiFacade aiFacade = mock(AiFacade.class);
        ClassicsPublicationWriteGuard writeGuard = mock(ClassicsPublicationWriteGuard.class);
        when(writeGuard.requireWritable(
                        ClassicsContentType.SANCAI_ENTRY,
                        ClassicsContentIdCodec.toDomain(11L),
                        ClassicsPublicationWriteOperation.EDIT))
                .thenThrow(new BizException("TRANSITION_ACTIVE"));
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, null, null, aiFacade, null, null, writeGuard);

        assertThrows(
                BizException.class,
                () -> service.applyAiCandidate(applyCommand(
                        11L, ClassicsContentType.SANCAI_ENTRY, 11L, AI_CAPABILITY_CLASSICS_SUMMARY, "摘要")));

        verify(aiFacade, never()).requirePendingCandidate(any());
    }

    @Test
    void applyAiCandidateTranslateShouldUpdateSancaiAndGenerateAiAppliedVersion() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        entry.setTranslationText("old translation");
        entry.setContentUpdatedAt(Instant.ofEpochMilli(1L));
        repository.sancaiEntryForAiApply = entry;

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("SANCAI_ENTRY", request.getContentType());
                    assertEquals(11L, request.getContentId());
                    assertEquals(AI_CAPABILITY_CLASSICS_TRANSLATE, request.getCapability());
                    return pendingCandidate();
                },
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("TEXT", request.getResultFormat());
                    assertEquals("new translation", request.getResultPayload());
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command = applyCommand(
                11L, ClassicsContentType.SANCAI_ENTRY, 11L, AI_CAPABILITY_CLASSICS_TRANSLATE, "new translation");

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
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        entry.setSummary("old summary");
        entry.setContentUpdatedAt(Instant.ofEpochMilli(1L));
        repository.sancaiEntryForAiApply = entry;

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("SANCAI_ENTRY", request.getContentType());
                    assertEquals(11L, request.getContentId());
                    assertEquals(AI_CAPABILITY_CLASSICS_SUMMARY, request.getCapability());
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
                applyCommand(11L, ClassicsContentType.SANCAI_ENTRY, 11L, AI_CAPABILITY_CLASSICS_SUMMARY, "new summary");

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
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        entry.setContentUpdatedAt(Instant.ofEpochMilli(1L));
        repository.sancaiEntryForAiApply = entry;

        SancaiVisualAsset visualAsset = new SancaiVisualAsset();
        visualAsset.setId(SancaiVisualAssetIdCodec.toDomain(111L));

        SancaiAssetApplicationService assetService = org.mockito.Mockito.mock(SancaiAssetApplicationService.class);
        when(assetService.listVisualAssets(SancaiEntryIdCodec.toDomain(11L))).thenReturn(List.of(visualAsset));

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("SANCAI_ENTRY", request.getContentType());
                    assertEquals(11L, request.getContentId());
                    assertEquals(AI_CAPABILITY_CLASSICS_IMAGE_DESCRIBE, request.getCapability());
                    return pendingCandidate();
                },
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("TEXT", request.getResultFormat());
                    assertEquals("分析结果", request.getResultPayload());
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade, assetService);
        AiCandidateApplyContentCommand command = applyCommand(
                11L, ClassicsContentType.SANCAI_ENTRY, 11L, AI_CAPABILITY_CLASSICS_IMAGE_DESCRIBE, "分析结果", 111L);

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
        verify(assetService).updateVisualAsset(any(SancaiVisualAssetCommand.class));
    }

    @Test
    void applyAiCandidateVisualShouldUpdateTargetVisualAssetAndSkipVersion() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        entry.setContentUpdatedAt(Instant.ofEpochMilli(1L));
        repository.sancaiEntryForAiApply = entry;

        SancaiVisualAsset visualAsset = new SancaiVisualAsset();
        visualAsset.setId(SancaiVisualAssetIdCodec.toDomain(111L));
        visualAsset.setImageAnalysisMarkdown("old image analysis");
        visualAsset.setFusionDescription("old fusion");

        SancaiAssetApplicationService assetService = org.mockito.Mockito.mock(SancaiAssetApplicationService.class);
        when(assetService.listVisualAssets(SancaiEntryIdCodec.toDomain(11L))).thenReturn(List.of(visualAsset));
        when(assetService.updateVisualAsset(any(SancaiVisualAssetCommand.class)))
                .thenReturn(SancaiVisualAssetIdCodec.toDomain(111L));

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("SANCAI_ENTRY", request.getContentType());
                    assertEquals(11L, request.getContentId());
                    assertEquals(AI_CAPABILITY_CLASSICS_VISUAL_DESCRIBE, request.getCapability());
                    return pendingCandidate();
                },
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("TEXT", request.getResultFormat());
                    assertEquals("视觉描述", request.getResultPayload());
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade, assetService);
        AiCandidateApplyContentCommand command = applyCommand(
                11L, ClassicsContentType.SANCAI_ENTRY, 11L, AI_CAPABILITY_CLASSICS_VISUAL_DESCRIBE, "视觉描述", 111L);

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
        verify(assetService).updateVisualAsset(any(SancaiVisualAssetCommand.class));
    }

    @Test
    void applyAiCandidateFusionShouldUpdateTargetVisualAssetAndSkipVersion() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        entry.setContentUpdatedAt(Instant.ofEpochMilli(1L));
        repository.sancaiEntryForAiApply = entry;

        SancaiVisualAsset visualAsset = new SancaiVisualAsset();
        visualAsset.setId(SancaiVisualAssetIdCodec.toDomain(111L));
        visualAsset.setImageAnalysisMarkdown("old image analysis");
        visualAsset.setVisualDescription("old visual");

        SancaiAssetApplicationService assetService = org.mockito.Mockito.mock(SancaiAssetApplicationService.class);
        when(assetService.listVisualAssets(SancaiEntryIdCodec.toDomain(11L))).thenReturn(List.of(visualAsset));
        when(assetService.updateVisualAsset(any(SancaiVisualAssetCommand.class)))
                .thenReturn(SancaiVisualAssetIdCodec.toDomain(111L));

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("SANCAI_ENTRY", request.getContentType());
                    assertEquals(11L, request.getContentId());
                    assertEquals(AI_CAPABILITY_CLASSICS_IMAGE_PROMPT_FUSION, request.getCapability());
                    return pendingCandidate();
                },
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("TEXT", request.getResultFormat());
                    assertEquals("融合说明", request.getResultPayload());
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade, assetService);
        AiCandidateApplyContentCommand command = applyCommand(
                11L, ClassicsContentType.SANCAI_ENTRY, 11L, AI_CAPABILITY_CLASSICS_IMAGE_PROMPT_FUSION, "融合说明", 111L);

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.SANCAI_ENTRY, result.getContentType());
        assertEquals(11L, result.getContentId());
        assertEquals(null, result.getVersionId());
        assertEquals(null, result.getVersionNo());
        assertEquals(0, repository.insertVersionCount);
        assertEquals(0, repository.updateSancaiEntryAiCount);
        verify(aiFacade).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
        verify(assetService)
                .applyFusionDescription(new SancaiVisualAssetFusionCommand(
                        SancaiEntryIdCodec.toDomain(11L), SancaiVisualAssetIdCodec.toDomain(111L), "融合说明"));
    }

    @Test
    void applyAiCandidateImageGenShouldCreateGeneratedVisualAssetVersion() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        repository.sancaiEntryForAiApply = entry;

        SancaiVisualAsset visualAsset = new SancaiVisualAsset();
        visualAsset.setId(SancaiVisualAssetIdCodec.toDomain(111L));
        visualAsset.setEntryId(SancaiEntryIdCodec.toDomain(11L));

        SancaiVisualAsset generatedAsset = new SancaiVisualAsset();
        generatedAsset.setId(SancaiVisualAssetIdCodec.toDomain(112L));
        generatedAsset.setEntryId(SancaiEntryIdCodec.toDomain(11L));
        generatedAsset.setVersionNo(3);

        SancaiAssetApplicationService assetService = org.mockito.Mockito.mock(SancaiAssetApplicationService.class);
        when(assetService.listVisualAssets(SancaiEntryIdCodec.toDomain(11L))).thenReturn(List.of(visualAsset));
        when(assetService.createGeneratedVisualAssetVersion(new SancaiVisualAssetVersionCommand(
                        SancaiEntryIdCodec.toDomain(11L),
                        SancaiVisualAssetIdCodec.toDomain(111L),
                        StorageObjectIdCodec.toDomain(7101L))))
                .thenReturn(generatedAsset);

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("SANCAI_ENTRY", request.getContentType());
                    assertEquals(11L, request.getContentId());
                    assertEquals(AI_CAPABILITY_CLASSICS_IMAGE_GENERATE, request.getCapability());
                    return pendingCandidate();
                },
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("TEXT", request.getResultFormat());
                    assertEquals(
                            "{\"storageObjectId\":7101,\"contentType\":\"image/png\"}", request.getResultPayload());
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade, assetService);
        AiCandidateApplyContentCommand command = applyCommand(
                11L,
                ClassicsContentType.SANCAI_ENTRY,
                11L,
                AI_CAPABILITY_CLASSICS_IMAGE_GENERATE,
                "{\"storageObjectId\":7101,\"contentType\":\"image/png\"}",
                111L);

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.SANCAI_ENTRY, result.getContentType());
        assertEquals(11L, result.getContentId());
        assertEquals(112L, result.getVersionId());
        assertEquals(3, result.getVersionNo());
        assertEquals(0, repository.insertVersionCount);
        verify(assetService)
                .createGeneratedVisualAssetVersion(new SancaiVisualAssetVersionCommand(
                        SancaiEntryIdCodec.toDomain(11L),
                        SancaiVisualAssetIdCodec.toDomain(111L),
                        StorageObjectIdCodec.toDomain(7101L)));
        verify(aiFacade).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidateImageGenShouldFailWhenStorageObjectIdMissingInPayload() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        repository.sancaiEntryForAiApply = entry;

        SancaiVisualAsset visualAsset = new SancaiVisualAsset();
        visualAsset.setId(SancaiVisualAssetIdCodec.toDomain(111L));
        visualAsset.setEntryId(SancaiEntryIdCodec.toDomain(11L));

        SancaiAssetApplicationService assetService = org.mockito.Mockito.mock(SancaiAssetApplicationService.class);
        when(assetService.listVisualAssets(SancaiEntryIdCodec.toDomain(11L))).thenReturn(List.of(visualAsset));

        AiFacade aiFacade = mockAiFacade(request -> pendingCandidate(), request -> {
            throw new IllegalStateException("markApplied should not be called");
        });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade, assetService);
        AiCandidateApplyContentCommand command = applyCommand(
                11L,
                ClassicsContentType.SANCAI_ENTRY,
                11L,
                AI_CAPABILITY_CLASSICS_IMAGE_GENERATE,
                "{\"contentType\":\"image/png\"}",
                111L);

        BizException exception = assertThrows(BizException.class, () -> service.applyAiCandidate(command));

        assertEquals("AI候选生图结果不可用: AI候选生图结果缺少 storageObjectId", exception.getMessage());
        verify(aiFacade, never()).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
        verify(assetService, never()).createGeneratedVisualAssetVersion(any(SancaiVisualAssetVersionCommand.class));
    }

    @Test
    void applyAiCandidateImageAnalysisShouldFailWhenObjectIdMissing() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        entry.setContentUpdatedAt(Instant.ofEpochMilli(1L));
        repository.sancaiEntryForAiApply = entry;

        AiFacade aiFacade = mockAiFacade(request -> pendingCandidate(), request -> {
            throw new IllegalStateException("markApplied should not be called");
        });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command = applyCommand(
                11L, ClassicsContentType.SANCAI_ENTRY, 11L, AI_CAPABILITY_CLASSICS_IMAGE_DESCRIBE, "分析结果", null);

        BizException exception = assertThrows(BizException.class, () -> service.applyAiCandidate(command));
        assertEquals("三才视觉资产候选应用参数不完整", exception.getMessage());
        assertEquals(0, repository.insertVersionCount);
        assertEquals(0, repository.updateSancaiEntryAiCount);
        verify(aiFacade).requirePendingCandidate(any(RequirePendingAiCandidateFacadeRequest.class));
        verify(aiFacade, never()).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidateImageAnalysisShouldFailWhenVisualAssetNotFound() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        repository.sancaiEntryForAiApply = entry;

        SancaiVisualAsset visualAsset = new SancaiVisualAsset();
        visualAsset.setId(SancaiVisualAssetIdCodec.toDomain(111L));

        SancaiAssetApplicationService assetService = org.mockito.Mockito.mock(SancaiAssetApplicationService.class);
        when(assetService.listVisualAssets(SancaiEntryIdCodec.toDomain(11L))).thenReturn(List.of(visualAsset));
        when(assetService.updateVisualAsset(any(SancaiVisualAssetCommand.class)))
                .thenReturn(SancaiVisualAssetIdCodec.toDomain(111L));

        AiFacade aiFacade = mockAiFacade(request -> pendingCandidate(), request -> {
            throw new IllegalStateException("markApplied should not be called");
        });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade, assetService);
        AiCandidateApplyContentCommand command = applyCommand(
                11L, ClassicsContentType.SANCAI_ENTRY, 11L, AI_CAPABILITY_CLASSICS_IMAGE_DESCRIBE, "分析结果", 112L);

        BizException exception = assertThrows(BizException.class, () -> service.applyAiCandidate(command));
        assertEquals("三才视觉资产不存在: 112", exception.getMessage());
        assertEquals(0, repository.insertVersionCount);
        assertEquals(0, repository.updateSancaiEntryAiCount);
        verify(aiFacade).requirePendingCandidate(any(RequirePendingAiCandidateFacadeRequest.class));
        verify(assetService, never()).updateVisualAsset(any(SancaiVisualAssetCommand.class));
        verify(aiFacade, never()).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidateSummaryShouldUpdateWangqiAndGenerateAiAppliedVersion() {
        FakeRepository repository = new FakeRepository();
        WangqiDocument document = new WangqiDocument(
                WangqiDocumentIdCodec.toDomain(22L),
                "title",
                "old summary",
                WangqiContentFormat.HTML,
                "content",
                Instant.now(),
                null);
        repository.wangqiDocumentForAiApply = document;

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(22L, request.getCandidateId());
                    assertEquals("WANGQI_DOCUMENT", request.getContentType());
                    assertEquals(22L, request.getContentId());
                    assertEquals(AI_CAPABILITY_CLASSICS_SUMMARY, request.getCapability());
                    return pendingCandidate();
                },
                request -> {
                    assertEquals(22L, request.getCandidateId());
                    assertEquals("TEXT", request.getResultFormat());
                    assertEquals("new summary", request.getResultPayload());
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command = applyCommand(
                22L, ClassicsContentType.WANGQI_DOCUMENT, 22L, AI_CAPABILITY_CLASSICS_SUMMARY, "new summary");

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
        entry.setId(MingCustomsEntryIdCodec.toDomain(33L));
        entry.setSummary("old summary");
        repository.mingCustomsEntryForAiApply = entry;

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(33L, request.getCandidateId());
                    assertEquals("MING_CUSTOMS", request.getContentType());
                    assertEquals(33L, request.getContentId());
                    assertEquals(AI_CAPABILITY_CLASSICS_SUMMARY, request.getCapability());
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
                applyCommand(33L, ClassicsContentType.MING_CUSTOMS, 33L, AI_CAPABILITY_CLASSICS_SUMMARY, "new summary");

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
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        repository.sancaiEntryForAiApply = entry;
        repository.tags.add(manualTag(1L, 11L, "manual-tag"));
        repository.tags.add(aiTag(2L, 11L, "old-ai-tag"));

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("SANCAI_ENTRY", request.getContentType());
                    assertEquals(11L, request.getContentId());
                    assertEquals(AI_CAPABILITY_CLASSICS_TAGS, request.getCapability());
                    return pendingCandidate();
                },
                request -> candidateApplied());

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command = applyCommand(
                11L,
                ClassicsContentType.SANCAI_ENTRY,
                11L,
                AI_CAPABILITY_CLASSICS_TAGS,
                "{\"tags\":[\"ai-one\",\"ai-two\",\"ai-one\",\"\"]}");

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.SANCAI_ENTRY, result.getContentType());
        assertEquals(11L, result.getContentId());
        assertEquals(ClassicsContentChangeType.AI_APPLIED, repository.lastInsertedVersion.getChangeType());
        assertEquals("AI 应用：标签", repository.lastInsertedVersion.getChangeSummary());
        assertEquals(1, repository.deleteByAiTagsCount);
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
    void applyAiCandidateTagsShouldAppendMissingTagsWithoutDeletingExistingAiTags() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        repository.sancaiEntryForAiApply = entry;
        repository.tags.add(manualTag(1L, 11L, "manual-tag"));
        repository.tags.add(aiTag(2L, 11L, "old-ai-tag"));

        AiFacade aiFacade = mockAiFacade(request -> pendingCandidate(), request -> candidateApplied());
        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command = applyCommand(
                11L,
                ClassicsContentType.SANCAI_ENTRY,
                11L,
                AI_CAPABILITY_CLASSICS_TAGS,
                "{\"tags\":[\"old-ai-tag\",\"new-ai-tag\"]}",
                null,
                "APPEND");

        service.applyAiCandidate(command);

        assertEquals(0, repository.deleteByAiTagsCount);
        assertEquals(1, repository.insertTagCount);
        assertEquals(3, repository.tags.size());
        assertEquals(
                1,
                repository.tags.stream()
                        .filter(tag -> "new-ai-tag".equals(tag.getTagNameSnapshot()))
                        .count());
        assertEquals(
                1,
                repository.tags.stream()
                        .filter(tag -> "old-ai-tag".equals(tag.getTagNameSnapshot()))
                        .count());
        verify(aiFacade).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidateTagsShouldIgnoreRemovedTagsWhenAppending() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        repository.sancaiEntryForAiApply = entry;
        repository.tags.add(removedAiTag(2L, 11L, "old-ai-tag"));

        AiFacade aiFacade = mockAiFacade(request -> pendingCandidate(), request -> candidateApplied());
        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command = applyCommand(
                11L,
                ClassicsContentType.SANCAI_ENTRY,
                11L,
                AI_CAPABILITY_CLASSICS_TAGS,
                "{\"tags\":[\"old-ai-tag\"]}",
                null,
                "APPEND");

        service.applyAiCandidate(command);

        assertEquals(0, repository.deleteByAiTagsCount);
        assertEquals(1, repository.insertTagCount);
        assertEquals(2, repository.tags.size());
        assertEquals(
                1,
                repository.tags.stream()
                        .filter(tag -> "old-ai-tag".equals(tag.getTagNameSnapshot()))
                        .filter(tag -> tag.getStatus() == ClassicsContentTagStatus.ACTIVE)
                        .count());
        verify(aiFacade).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidateTagsShouldCoverAllCurrentTagsInSingleApply() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        repository.sancaiEntryForAiApply = entry;
        repository.tags.add(manualTag(1L, 11L, "manual-tag"));
        repository.tags.add(aiTag(2L, 11L, "old-ai-tag"));

        AiFacade aiFacade = mockAiFacade(request -> pendingCandidate(), request -> candidateApplied());
        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command = applyCommand(
                11L,
                ClassicsContentType.SANCAI_ENTRY,
                11L,
                AI_CAPABILITY_CLASSICS_TAGS,
                "{\"tags\":[\"new-one\",\"new-two\"]}",
                null,
                "COVER");

        service.applyAiCandidate(command);

        assertEquals(0, repository.deleteByAiTagsCount);
        assertEquals(2, repository.deleteByTagIdCount);
        assertEquals(2, repository.insertTagCount);
        assertEquals(
                List.of("new-one", "new-two"),
                repository.tags.stream()
                        .map(ClassicsContentTag::getTagNameSnapshot)
                        .toList());
        verify(aiFacade).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidateTagsShouldSyncKnowledgeRefsWhenBindingSupportPresent() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        repository.sancaiEntryForAiApply = entry;
        ClassicsContentTag oldAiTag = aiTag(2L, 11L, "old-ai-tag");
        repository.tags.add(manualTag(1L, 11L, "manual-tag"));
        repository.tags.add(oldAiTag);

        AiFacade aiFacade = mockAiFacade(request -> pendingCandidate(), request -> candidateApplied());
        ClassicsTagBindingSupport tagBindingSupport = org.mockito.Mockito.mock(ClassicsTagBindingSupport.class);
        when(tagBindingSupport.bindAiTag(any(ContentTagCommand.class), any())).thenAnswer(invocation -> {
            ContentTagCommand command = invocation.getArgument(0);
            return ClassicsContentApplicationAssembler.toTag(command);
        });

        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository,
                null,
                null,
                null,
                null,
                null,
                aiFacade,
                tagBindingSupport,
                null,
                mock(ClassicsPublicationWriteGuard.class));

        service.applyAiCandidate(applyCommand(
                11L,
                ClassicsContentType.SANCAI_ENTRY,
                11L,
                AI_CAPABILITY_CLASSICS_TAGS,
                "{\"tags\":[\"ai-one\",\"ai-two\"]}"));

        verify(tagBindingSupport).removeTagRef(oldAiTag);
        verify(tagBindingSupport, times(2)).bindAiTag(any(ContentTagCommand.class), any());
        verify(tagBindingSupport, times(2)).syncTagRef(any(ClassicsContentTag.class));
    }

    @Test
    void applyAiCandidateSummaryShouldApplySummaryTagsAndQaAndGenerateSingleVersion() {
        FakeRepository repository = new FakeRepository();
        WangqiDocument document = new WangqiDocument(
                WangqiDocumentIdCodec.toDomain(22L),
                "title",
                "old summary",
                WangqiContentFormat.HTML,
                "content",
                Instant.now(),
                null);
        repository.wangqiDocumentForAiApply = document;

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(22L, request.getCandidateId());
                    assertEquals("WANGQI_DOCUMENT", request.getContentType());
                    assertEquals(22L, request.getContentId());
                    assertEquals(AI_CAPABILITY_CLASSICS_SUMMARY, request.getCapability());
                    return pendingCandidate();
                },
                request -> {
                    assertEquals(22L, request.getCandidateId());
                    assertEquals("TEXT", request.getResultFormat());
                    assertEquals(
                            "{\"summary\":\"ok-summary\",\"tags\":[\"t1\",\"t2\"],\"qaPairs\":[{\"question\":\"q1\",\"answer\":\"a1\"}]}",
                            request.getResultPayload());
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command = applyCommand(
                22L,
                ClassicsContentType.WANGQI_DOCUMENT,
                22L,
                AI_CAPABILITY_CLASSICS_SUMMARY,
                "{\"summary\":\"ok-summary\",\"tags\":[\"t1\",\"t2\"],\"qaPairs\":[{\"question\":\"q1\",\"answer\":\"a1\"}]}");

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.WANGQI_DOCUMENT, result.getContentType());
        assertEquals(22L, result.getContentId());
        assertEquals(1L, result.getVersionId());
        assertEquals(1, result.getVersionNo());
        assertEquals("ok-summary", document.getSummary());
        assertEquals(ClassicsContentChangeType.AI_APPLIED, repository.lastInsertedVersion.getChangeType());
        assertEquals("AI 应用：摘要", repository.lastInsertedVersion.getChangeSummary());
        assertEquals(1, repository.insertVersionCount);
        assertEquals(1, repository.updateWangqiDocumentAiCount);
        assertEquals(2, repository.tags.size());
        assertEquals(1, repository.qaPairs.size());
    }

    @Test
    void applyAiCandidateQaShouldAppendMissingQaPairsAndCreateAiAppliedVersion() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        repository.sancaiEntryForAiApply = entry;
        repository.qaPairs.add(manualQaPair(1L, 11L, "manual-q", "manual-a"));
        repository.qaPairs.add(aiQaPair(2L, 11L, "old-q", "old-a"));

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    assertEquals(11L, request.getCandidateId());
                    assertEquals("SANCAI_ENTRY", request.getContentType());
                    assertEquals(11L, request.getContentId());
                    assertEquals("CLASSICS_QA", request.getCapability());
                    return pendingCandidate();
                },
                request -> candidateApplied());

        ClassicsContentApplicationServiceImpl service = serviceWithAiFacade(repository, aiFacade);
        AiCandidateApplyContentCommand command = applyCommand(
                11L,
                ClassicsContentType.SANCAI_ENTRY,
                11L,
                "CLASSICS_QA",
                "{\"qaPairs\":[{\"question\":\"old-q\",\"answer\":\"old-a\"},{\"question\":\"q1\",\"answer\":\"a\"},{\"question\":\"q2\",\"answer\":\"b\"},{\"question\":\"q1\",\"answer\":\"a\"}]}");

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.SANCAI_ENTRY, result.getContentType());
        assertEquals(11L, result.getContentId());
        assertEquals(ClassicsContentChangeType.AI_APPLIED, repository.lastInsertedVersion.getChangeType());
        assertEquals("AI 应用：问答对", repository.lastInsertedVersion.getChangeSummary());
        assertEquals(0, repository.deleteByAiQaPairsCount);
        assertEquals(2, repository.insertQaPairCount);
        assertEquals(4, repository.qaPairs.size());
        assertEquals(
                1,
                repository.qaPairs.stream()
                        .filter(pair -> pair.getSource() == ClassicsContentSource.MANUAL)
                        .count());
        assertEquals(
                3,
                repository.qaPairs.stream()
                        .filter(pair -> pair.getSource() == ClassicsContentSource.AI)
                        .count());
        verify(aiFacade).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidateShouldFailWhenAiCandidateNotPending() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
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
                applyCommand(11L, ClassicsContentType.SANCAI_ENTRY, 11L, AI_CAPABILITY_CLASSICS_SUMMARY, "new summary");

        assertThrows(DomainException.class, () -> service.applyAiCandidate(command));

        assertEquals("old summary", entry.getSummary());
        assertEquals(0, repository.insertVersionCount);
        assertEquals(0, repository.updateSancaiEntryAiCount);
        verify(aiFacade, never()).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    @Test
    void applyAiCandidatesShouldProcessPartialSuccessAndFailByPermissionAndTargetMismatch() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        entry.setTranslationText("old translation");
        entry.setContentUpdatedAt(Instant.ofEpochMilli(1L));
        repository.sancaiEntryForAiApply = entry;

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    if (request.getCandidateId().equals(11L)
                            || request.getCandidateId().equals(33L)) {
                        assertEquals("SANCAI_ENTRY", request.getContentType());
                        assertEquals(AI_CAPABILITY_CLASSICS_SUMMARY, request.getCapability());
                        if (request.getCandidateId().equals(33L)) {
                            throw new DomainException(
                                    "AI-INVOCATION-409",
                                    "ai.candidate.target-mismatch",
                                    "AI candidate target mismatch");
                        }
                        return pendingCandidateWithId(request.getCandidateId());
                    }
                    throw new IllegalStateException("unexpected candidate: " + request.getCandidateId());
                },
                request -> candidateApplied());

        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository,
                null,
                null,
                null,
                null,
                null,
                aiFacade,
                null,
                null,
                mock(ClassicsPublicationWriteGuard.class));

        setPermissions(Set.of("classics:sancai:edit"));
        try {
            ClassicsBatchOperationResult result =
                    service.applyAiCandidates(new AiCandidateBatchApplyContentCommand(List.of(
                            applyCommand(
                                    11L, ClassicsContentType.SANCAI_ENTRY, 11L, AI_CAPABILITY_CLASSICS_SUMMARY, "ok"),
                            applyCommand(
                                    22L,
                                    ClassicsContentType.WANGQI_DOCUMENT,
                                    22L,
                                    AI_CAPABILITY_CLASSICS_SUMMARY,
                                    "ok"),
                            applyCommand(
                                    33L,
                                    ClassicsContentType.SANCAI_ENTRY,
                                    33L,
                                    AI_CAPABILITY_CLASSICS_SUMMARY,
                                    "ok"))));

            assertEquals(1, result.getSuccessCount());
            assertEquals(2, result.getFailureCount());

            ClassicsBatchOperationItemResult success = result.getSuccesses().get(0);
            assertEquals(11L, success.getCandidateId());
            assertEquals("APPLIED", success.getStatus());

            assertEquals("PERMISSION_DENIED", result.getFailures().get(0).getFailureCode());
            assertEquals(
                    "CANDIDATE_TARGET_MISMATCH", result.getFailures().get(1).getFailureCode());
            verify(aiFacade, times(1)).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
        } finally {
            clearPermissions();
        }
    }

    @Test
    void rejectAiCandidatesShouldRejectAndReportNonPendingFailure() {
        AiFacade aiFacade = org.mockito.Mockito.mock(AiFacade.class);
        when(aiFacade.requirePendingCandidate(any(RequirePendingAiCandidateFacadeRequest.class)))
                .thenAnswer(invocation -> {
                    RequirePendingAiCandidateFacadeRequest request = invocation.getArgument(0);
                    if (request.getCandidateId().equals(11L)) {
                        return pendingCandidateWithId(11L);
                    }
                    throw new DomainException(
                            "AI-INVOCATION-409", "ai.candidate.not-pending", "AI candidate is not pending");
                });
        when(aiFacade.rejectCandidate(any(RejectAiCandidateFacadeRequest.class)))
                .thenAnswer(invocation -> {
                    RejectAiCandidateFacadeRequest request = invocation.getArgument(0);
                    return AiCandidateFacadeDto.builder()
                            .candidateId(request.getCandidateId())
                            .status("REJECTED")
                            .rejectedAt(Instant.now())
                            .build();
                });

        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                new FakeRepository(),
                null,
                null,
                null,
                null,
                null,
                aiFacade,
                null,
                null,
                mock(ClassicsPublicationWriteGuard.class));

        setPermissions(Set.of("classics:sancai:edit"));
        try {
            ClassicsBatchOperationResult result = service.rejectAiCandidates(new AiCandidateBatchRejectContentCommand(
                    List.of(
                            rejectItem(11L, ClassicsContentType.SANCAI_ENTRY, 11L, AI_CAPABILITY_CLASSICS_SUMMARY),
                            rejectItem(12L, ClassicsContentType.SANCAI_ENTRY, 12L, AI_CAPABILITY_CLASSICS_SUMMARY)),
                    null,
                    null));

            assertEquals(1, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
            assertEquals("REJECTED", result.getSuccesses().get(0).getStatus());
            assertEquals("CANDIDATE_NOT_PENDING", result.getFailures().get(0).getFailureCode());

            verify(aiFacade).rejectCandidate(any(RejectAiCandidateFacadeRequest.class));
            verify(aiFacade, times(2)).requirePendingCandidate(any(RequirePendingAiCandidateFacadeRequest.class));
        } finally {
            clearPermissions();
        }
    }

    @Test
    void rejectAiCandidatesShouldNotCreateVersion() {
        FakeRepository repository = new FakeRepository();
        AiFacade aiFacade = org.mockito.Mockito.mock(AiFacade.class);
        when(aiFacade.requirePendingCandidate(any(RequirePendingAiCandidateFacadeRequest.class)))
                .thenReturn(pendingCandidate());
        when(aiFacade.rejectCandidate(any(RejectAiCandidateFacadeRequest.class)))
                .thenReturn(candidateRejected());

        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository,
                null,
                null,
                null,
                null,
                null,
                aiFacade,
                null,
                null,
                mock(ClassicsPublicationWriteGuard.class));

        setPermissions(Set.of("classics:sancai:edit"));
        try {
            service.rejectAiCandidates(new AiCandidateBatchRejectContentCommand(
                    List.of(rejectItem(11L, ClassicsContentType.SANCAI_ENTRY, 11L, AI_CAPABILITY_CLASSICS_SUMMARY)),
                    null,
                    null));
            assertEquals(0, repository.insertVersionCount);
            assertEquals(0, repository.updateSancaiEntryAiCount);
        } finally {
            clearPermissions();
        }
    }

    @Test
    void applyAiCandidatesShouldFailObjectMismatchWhenImageAnalysisObjectDoesNotMatch() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(11L));
        entry.setContentUpdatedAt(Instant.ofEpochMilli(1L));
        repository.sancaiEntryForAiApply = entry;

        SancaiAssetApplicationService assetService = org.mockito.Mockito.mock(SancaiAssetApplicationService.class);
        when(assetService.listVisualAssets(SancaiEntryIdCodec.toDomain(11L))).thenReturn(List.of(visualAsset(111L)));

        AiFacade aiFacade = mockAiFacade(
                request -> {
                    if (Long.valueOf(999L).equals(request.getObjectId())) {
                        throw new DomainException(
                                "AI-INVOCATION-409", "ai.candidate.target-mismatch", "AI candidate target mismatch");
                    }
                    return pendingCandidateWithObjectId(request.getCandidateId(), request.getObjectId());
                },
                request -> candidateApplied());

        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository,
                null,
                null,
                assetService,
                null,
                null,
                aiFacade,
                null,
                null,
                mock(ClassicsPublicationWriteGuard.class));

        setPermissions(Set.of("classics:sancai:edit"));
        try {
            ClassicsBatchOperationResult result =
                    service.applyAiCandidates(new AiCandidateBatchApplyContentCommand(List.of(
                            applyCommand(
                                    11L,
                                    ClassicsContentType.SANCAI_ENTRY,
                                    11L,
                                    AI_CAPABILITY_CLASSICS_IMAGE_DESCRIBE,
                                    "text",
                                    111L),
                            applyCommand(
                                    22L,
                                    ClassicsContentType.SANCAI_ENTRY,
                                    11L,
                                    AI_CAPABILITY_CLASSICS_IMAGE_DESCRIBE,
                                    "text",
                                    999L))));

            assertEquals(1, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
            assertEquals(
                    "CANDIDATE_TARGET_MISMATCH", result.getFailures().get(0).getFailureCode());
            assertNotNull(result.getFailures().get(0).getFailureReason());
        } finally {
            clearPermissions();
        }
    }

    private static ClassicsContentApplicationServiceImpl serviceWithAiFacade(
            ClassicsContentRepository repository, AiFacade aiFacade) {
        return serviceWithAiFacade(repository, aiFacade, null);
    }

    private static ClassicsContentApplicationServiceImpl serviceWithAiFacade(
            ClassicsContentRepository repository, AiFacade aiFacade, SancaiAssetApplicationService assetService) {
        return new ClassicsContentApplicationServiceImpl(
                repository,
                null,
                null,
                assetService,
                null,
                null,
                aiFacade,
                null,
                null,
                mock(ClassicsPublicationWriteGuard.class));
    }

    private static AiCandidateBatchRejectContentItemCommand rejectItem(
            Long candidateId, ClassicsContentType contentType, Long contentId, String capability) {
        return new AiCandidateBatchRejectContentItemCommand(candidateId, contentType, contentId, null, capability);
    }

    private static SancaiVisualAsset visualAsset(Long objectId) {
        SancaiVisualAsset visualAsset = new SancaiVisualAsset();
        visualAsset.setId(SancaiVisualAssetIdCodec.toDomain(objectId));
        return visualAsset;
    }

    private static AiCandidateFacadeDto pendingCandidateWithId(Long candidateId) {
        return AiCandidateFacadeDto.builder()
                .candidateId(candidateId)
                .contentType("SANCAI_ENTRY")
                .contentId(candidateId)
                .capability(AI_CAPABILITY_CLASSICS_SUMMARY)
                .status("PENDING")
                .build();
    }

    private static AiCandidateFacadeDto pendingCandidateWithObjectId(Long candidateId, Long objectId) {
        return AiCandidateFacadeDto.builder()
                .candidateId(candidateId)
                .contentType("SANCAI_ENTRY")
                .contentId(11L)
                .objectId(objectId)
                .capability(AI_CAPABILITY_CLASSICS_IMAGE_DESCRIBE)
                .status("PENDING")
                .build();
    }

    private static void setPermissions(Set<String> permissions) {
        KuzhambuContextHolder.setSubject(
                new KuzhambuSubject("operator", KuzhambuSubjectType.ADMIN_USER, "operator", null, permissions));
    }

    private static void clearPermissions() {
        KuzhambuContextHolder.clear();
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
        return applyCommand(candidateId, contentType, contentId, capability, payload, objectId, null);
    }

    private static AiCandidateApplyContentCommand applyCommand(
            Long candidateId,
            ClassicsContentType contentType,
            Long contentId,
            String capability,
            String payload,
            Long objectId,
            String tagApplyMode) {
        return new AiCandidateApplyContentCommand(
                candidateId, contentType, contentId, objectId, capability, "TEXT", payload, null, tagApplyMode);
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

    private static AiCandidateFacadeDto candidateRejected() {
        return AiCandidateFacadeDto.builder()
                .candidateId(11L)
                .status("REJECTED")
                .rejectedAt(Instant.now())
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
        tag.setId(ClassicsContentTagIdCodec.toDomain(id));
        tag.setContentType(ClassicsContentType.SANCAI_ENTRY);
        tag.setContentId(ClassicsContentIdCodec.toDomain(contentId));
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

    private static ClassicsContentTag removedAiTag(Long id, Long contentId, String tagName) {
        ClassicsContentTag tag = aiTag(id, contentId, tagName);
        tag.setStatus(ClassicsContentTagStatus.REMOVED);
        return tag;
    }

    private static ClassicsContentQaPair manualQaPair(Long id, Long contentId, String question, String answer) {
        ClassicsContentQaPair qaPair = new ClassicsContentQaPair();
        qaPair.setId(ClassicsContentQaPairIdCodec.toDomain(id));
        qaPair.setContentType(ClassicsContentType.SANCAI_ENTRY);
        qaPair.setContentId(ClassicsContentIdCodec.toDomain(contentId));
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
        private int deleteByAiTagsCount;
        private int deleteByAiQaPairsCount;
        private int deleteByTagIdCount;
        private int insertTagCount;
        private int insertQaPairCount;
        private SancaiEntry sancaiEntryForAiApply;
        private WangqiDocument wangqiDocumentForAiApply;
        private MingCustomsEntry mingCustomsEntryForAiApply;

        @Override
        public ClassicsPublicationContent getByPublicationContentForLock(
                ClassicsContentType contentType, ClassicsContentId contentId) {
            return null;
        }

        @Override
        public int updatePublicationContentState(
                ClassicsPublicationContent expectedState, ClassicsPublicationContent targetState) {
            return 0;
        }

        @Override
        public List<ClassicsContentVersion> listVersions(String contentType, ClassicsContentId contentId) {
            return versions;
        }

        @Override
        public ClassicsContentVersionId insertVersion(ClassicsContentVersion version) {
            ClassicsContentVersionId id = ClassicsContentVersionIdCodec.toDomain(versions.size() + 1L);
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
        public List<ClassicsContentTag> listTags(com.thundax.kuzhambu.common.core.sort.SortDirection sortDirection) {
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
            return ClassicsContentTagIdCodec.toDomain((long) tags.size());
        }

        @Override
        public ClassicsContentTag getByTagId(ClassicsContentTagId id) {
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
        public int deleteByTagId(String contentType, ClassicsContentId contentId, ClassicsContentTagId id) {
            deleteByTagIdCount++;
            tags.removeIf(tag -> tag.getId() != null && tag.getId().equals(id));
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
            return ClassicsContentQaPairIdCodec.toDomain((long) qaPairs.size());
        }

        @Override
        public ClassicsContentQaPair getByQaPairId(ClassicsContentQaPairId id) {
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
        public int deleteByQaPairId(ClassicsContentQaPairId id) {
            return 1;
        }

        @Override
        public ClassicsContentVersion getByVersionId(ClassicsContentVersionId id) {
            return versions.stream()
                    .filter(version -> version.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public int deleteByVersions(String contentType, ClassicsContentId contentId) {
            return 1;
        }

        @Override
        public SancaiEntry getBySancaiEntryForAiApply(ClassicsContentId contentId) {
            return sancaiEntryForAiApply;
        }

        @Override
        public int updateSancaiEntryAiFields(SancaiEntry entry) {
            updateSancaiEntryAiCount++;
            return 1;
        }

        @Override
        public WangqiDocument getByWangqiDocumentForAiApply(ClassicsContentId contentId) {
            return wangqiDocumentForAiApply;
        }

        @Override
        public int updateWangqiDocumentAiFields(WangqiDocument document) {
            updateWangqiDocumentAiCount++;
            return 1;
        }

        @Override
        public MingCustomsEntry getByMingCustomsEntryForAiApply(ClassicsContentId contentId) {
            return mingCustomsEntryForAiApply;
        }

        @Override
        public int updateMingCustomsEntryAiFields(MingCustomsEntry entry) {
            updateMingCustomsEntryAiCount++;
            return 1;
        }

        @Override
        public int deleteByAiTags(String contentType, ClassicsContentId contentId) {
            deleteByAiTagsCount++;
            tags.removeIf(tag -> tag.getContentType() != null
                    && tag.getContentType().value().equals(contentType)
                    && tag.getContentId() != null
                    && tag.getContentId().equals(contentId)
                    && tag.getSource() == ClassicsContentSource.AI);
            return 1;
        }

        @Override
        public int deleteByAiQaPairs(String contentType, ClassicsContentId contentId) {
            deleteByAiQaPairsCount++;
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
        public ClassicsContentExportJob getByExportJobId(ClassicsContentExportJobId id) {
            return null;
        }

        @Override
        public int updateExportJob(ClassicsContentExportJob exportJob) {
            return 1;
        }

        @Override
        public int updateExportJobCompleted(
                ClassicsContentExportJobId id,
                com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId storageObjectId,
                Instant expiresAt,
                int itemCount,
                int assetCount) {
            return 1;
        }

        @Override
        public int updateExportJobFailed(ClassicsContentExportJobId id) {
            return 1;
        }

        @Override
        public int updateExportJobExpired(ClassicsContentExportJobId id) {
            return 1;
        }

        @Override
        public int deleteByExportJobId(ClassicsContentExportJobId id) {
            return 1;
        }

        @Override
        public PageResult<ClassicsContentExportJob> page(
                String contentType, String exportKind, String status, int pageNo, int pageSize) {
            return new PageResult<>();
        }
    }
}
