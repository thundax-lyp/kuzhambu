package com.thundax.kuzhambu.knowledge.application.refinement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.KnowledgeGraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.application.refinement.command.GenerateQualityReportCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ReextractLowQualityCategoryCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.query.LatestQualityReportQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.ReextractLowQualityCategoryResult;
import com.thundax.kuzhambu.knowledge.application.refinement.service.impl.KnowledgeQualityReportApplicationServiceImpl;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionAiCandidateIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionSourceContentIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.KnowledgeEntityIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphVersionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.KnowledgeConfirmationStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.codec.RefinementTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityAnnotation;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReport;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReportIssue;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReportSourceDetail;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementTask;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.QualityAnnotationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.QualityReportRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementEntityDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementLineageNodeDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementLineageRelationDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementRelationDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementTaskRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeQualityReportApplicationServiceTest {

    @Test
    void generateReportShouldPersistSnapshotIssuesAndSourceDetails() {
        GraphVersionRepository graphVersionRepository = mock(GraphVersionRepository.class);
        KnowledgeEntityRepository entityRepository = mock(KnowledgeEntityRepository.class);
        KnowledgeRelationRepository relationRepository = mock(KnowledgeRelationRepository.class);
        KnowledgeLineageNodeRepository lineageNodeRepository = mock(KnowledgeLineageNodeRepository.class);
        KnowledgeLineageRelationRepository lineageRelationRepository = mock(KnowledgeLineageRelationRepository.class);
        RefinementTaskRepository refinementTaskRepository = mock(RefinementTaskRepository.class);
        QualityAnnotationRepository annotationRepository = mock(QualityAnnotationRepository.class);
        QualityReportRepository reportRepository = mock(QualityReportRepository.class);
        KnowledgeQualityReportApplicationServiceImpl service = new KnowledgeQualityReportApplicationServiceImpl(
                graphVersionRepository,
                mock(KnowledgeGraphExtractionApplicationService.class),
                entityRepository,
                relationRepository,
                lineageNodeRepository,
                lineageRelationRepository,
                refinementTaskRepository,
                mock(RefinementEntityDraftRepository.class),
                mock(RefinementRelationDraftRepository.class),
                mock(RefinementLineageNodeDraftRepository.class),
                mock(RefinementLineageRelationDraftRepository.class),
                annotationRepository,
                reportRepository);
        GraphVersion version = new GraphVersion(
                GraphVersionIdCodec.toDomain(71L),
                null,
                GraphExtractionAiCandidateIdCodec.toDomain(901L),
                GraphExtractionTaskType.GRAPH,
                null,
                null,
                "SANCAI_ENTRY",
                GraphExtractionSourceContentIdCodec.toDomain(1001L),
                "myth",
                "神话",
                2,
                GraphVersionStatus.APPLIED,
                Instant.ofEpochMilli(1_700_000_000_000L));
        when(graphVersionRepository.getByVersionId(GraphVersionIdCodec.toDomain(71L)))
                .thenReturn(version);
        when(entityRepository.listByVersionId(GraphVersionIdCodec.toDomain(71L)))
                .thenReturn(List.of(
                        entity(3001L, "person:huangdi", "黄帝", "PERSON", "始祖", "MANUAL_CONFIRMED", 71L),
                        entity(3002L, "person:fuxi", "伏羲", "PERSON", "始祖", "PENDING", 71L)));
        when(relationRepository.listByVersionId(71L))
                .thenReturn(List.of(new KnowledgeRelation(
                        4001L,
                        "rel:1",
                        "person:huangdi",
                        "person:fuxi",
                        "黄帝",
                        "伏羲",
                        "ANCESTOR",
                        "史料",
                        "MANUAL_CONFIRMED",
                        71L,
                        "[]",
                        null,
                        null,
                        null)));
        when(lineageNodeRepository.listByVersionId(71L))
                .thenReturn(List.of(new KnowledgeLineageNode(
                        5001L,
                        "lineage:huangdi",
                        "黄帝",
                        "PERSON",
                        1,
                        "MALE",
                        "MANUAL_CONFIRMED",
                        71L,
                        "[]",
                        null,
                        null,
                        null)));
        when(lineageRelationRepository.listByVersionId(71L)).thenReturn(List.of());
        when(annotationRepository.listByGraphVersionId(71L))
                .thenReturn(List.of(new QualityAnnotation(
                        1L,
                        6001L,
                        "ENTITY",
                        "person:fuxi",
                        "SANCAI_ENTRY",
                        1001L,
                        71L,
                        "ISSUE",
                        "WRONG_ENTITY",
                        "实体需复核",
                        9L,
                        null,
                        9L,
                        null)));
        when(refinementTaskRepository.findLatestDraft("GRAPH", "SANCAI_ENTRY", 1001L, 71L))
                .thenReturn(null);

        QualityReportDetailResult result = service.generateReport(new GenerateQualityReportCommand(71L, 1L));

        ArgumentCaptor<QualityReport> reportCaptor = ArgumentCaptor.forClass(QualityReport.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<QualityReportIssue>> issueCaptor = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<QualityReportSourceDetail>> sourceCaptor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(reportRepository)
                .save(reportCaptor.capture(), issueCaptor.capture(), sourceCaptor.capture());
        assertNotNull(result.getReport().getReportId());
        assertEquals(new BigDecimal("0.5000"), reportCaptor.getValue().getEntityCoverageRate());
        assertEquals(1L, reportCaptor.getValue().getAnnotationCount());
        assertEquals(2L, reportCaptor.getValue().getIssueCount());
        assertEquals(2, issueCaptor.getValue().size());
        assertEquals("ANNOTATION_ISSUE", issueCaptor.getValue().get(1).getIssueType());
        assertEquals(1, sourceCaptor.getValue().size());
        assertEquals(1L, sourceCaptor.getValue().get(0).getAnnotationCount());
        assertEquals(2L, sourceCaptor.getValue().get(0).getIssueCount());
    }

    @Test
    void reextractLowQualityCategoryShouldCreateSingleGraphTask() {
        ServiceFixture fixture = reextractFixture(List.of(sourceDetail(2001L, "SANCAI_ENTRY", 2L)));
        when(fixture.graphExtractionService.requestGraphExtraction(any()))
                .thenReturn(graphExtractionTask("3001", 4001L));

        ReextractLowQualityCategoryResult result = fixture.service.reextractLowQualityCategory(reextractCommand());

        ArgumentCaptor<RequestGraphExtractionCommand> captor =
                ArgumentCaptor.forClass(RequestGraphExtractionCommand.class);
        verify(fixture.graphExtractionService).requestGraphExtraction(captor.capture());
        assertEquals("QUALITY_REPORT", captor.getValue().triggerSource());
        assertEquals("SANCAI_ENTRY", captor.getValue().sourceContentType());
        assertEquals(2001L, captor.getValue().sourceContentId());
        assertEquals(true, captor.getValue().replaceUnconfirmedOnly());
        assertEquals(3001L, result.getTaskId());
        assertEquals(4001L, result.getBatchJobId());
        assertEquals("QUALITY_REPORT", result.getTriggerSource());
    }

    @Test
    void reextractLowQualityCategoryShouldCreateBatchScope() {
        ServiceFixture fixture = reextractFixture(List.of(
                sourceDetail(3002L, "SANCAI_ENTRY", 1L),
                sourceDetail(3001L, "SANCAI_ENTRY", 2L),
                sourceDetail(3001L, "SANCAI_ENTRY", 3L)));
        when(fixture.graphExtractionService.requestGraphExtraction(any()))
                .thenReturn(graphExtractionTask("5001", 6001L));

        ReextractLowQualityCategoryResult result = fixture.service.reextractLowQualityCategory(reextractCommand());

        ArgumentCaptor<RequestGraphExtractionCommand> captor =
                ArgumentCaptor.forClass(RequestGraphExtractionCommand.class);
        verify(fixture.graphExtractionService).requestGraphExtraction(captor.capture());
        assertEquals(3001L, captor.getValue().sourceContentId());
        assertEquals(
                "{\"triggerSource\":\"QUALITY_REPORT\",\"qualityReportId\":1001,\"graphVersionId\":71,\"sourceCategoryCode\":\"myth\",\"sourceCategoryName\":\"神话\",\"sourceContentType\":\"SANCAI_ENTRY\",\"sourceContentIds\":[3001,3002]}",
                captor.getValue().selectionScopeJson());
        assertEquals(captor.getValue().selectionScopeJson(), result.getSelectionScopeJson());
    }

    @Test
    void reextractLowQualityCategoryShouldRejectNoQualityIssues() {
        ServiceFixture fixture = reextractFixture(List.of(sourceDetail(2001L, "SANCAI_ENTRY", 0L)));

        BizException exception =
                assertThrows(BizException.class, () -> fixture.service.reextractLowQualityCategory(reextractCommand()));

        assertEquals("Knowledge quality report source category has no quality issues", exception.getMessage());
    }

    @Test
    void reextractLowQualityCategoryShouldRejectMixedSourceTypes() {
        ServiceFixture fixture = reextractFixture(
                List.of(sourceDetail(2001L, "SANCAI_ENTRY", 1L), sourceDetail(2002L, "MING_CUSTOMS", 1L)));

        BizException exception =
                assertThrows(BizException.class, () -> fixture.service.reextractLowQualityCategory(reextractCommand()));

        assertEquals("低质量门类包含多个来源类型，请按来源类型拆分重提取", exception.getMessage());
    }

    @Test
    void latestShouldMarkStaleWhenRefinementAppliedAfterReport() {
        RefinementTaskRepository refinementTaskRepository = mock(RefinementTaskRepository.class);
        QualityReportRepository reportRepository = mock(QualityReportRepository.class);
        QualityAnnotationRepository annotationRepository = mock(QualityAnnotationRepository.class);
        KnowledgeQualityReportApplicationServiceImpl service = new KnowledgeQualityReportApplicationServiceImpl(
                mock(GraphVersionRepository.class),
                mock(KnowledgeGraphExtractionApplicationService.class),
                mock(KnowledgeEntityRepository.class),
                mock(KnowledgeRelationRepository.class),
                mock(KnowledgeLineageNodeRepository.class),
                mock(KnowledgeLineageRelationRepository.class),
                refinementTaskRepository,
                mock(RefinementEntityDraftRepository.class),
                mock(RefinementRelationDraftRepository.class),
                mock(RefinementLineageNodeDraftRepository.class),
                mock(RefinementLineageRelationDraftRepository.class),
                annotationRepository,
                reportRepository);
        QualityReport report = report();
        report.setGeneratedAt(Instant.ofEpochMilli(1_719_187_200_000L));
        when(reportRepository.getLatestPublished(71L)).thenReturn(report);
        when(reportRepository.getByReportId(1001L)).thenReturn(report);
        when(reportRepository.listIssuesByReportId(1001L)).thenReturn(List.of());
        when(reportRepository.listSourceDetailsByReportId(1001L)).thenReturn(List.of());
        when(annotationRepository.listByGraphVersionId(71L)).thenReturn(List.of());
        when(refinementTaskRepository.findLatestAppliedByGraphVersionId(71L))
                .thenReturn(new RefinementTask(
                        null,
                        RefinementTaskIdCodec.toDomain(31L),
                        "GRAPH",
                        "SANCAI_ENTRY",
                        2001L,
                        "myth",
                        "神话",
                        71L,
                        "APPLIED",
                        9L,
                        Instant.now(),
                        null,
                        null,
                        19L,
                        Instant.ofEpochMilli(1_719_187_260_000L),
                        null,
                        null));

        QualityReportDetailResult result = service.latest(new LatestQualityReportQuery(71L));

        assertEquals(true, result.getStale());
        assertEquals("REFINEMENT_APPLIED_AFTER_REPORT", result.getStaleReason());
        assertEquals(1_719_187_260_000L, result.getLastRefinementAppliedAt());
    }

    private static ServiceFixture reextractFixture(List<QualityReportSourceDetail> sourceDetails) {
        GraphVersionRepository graphVersionRepository = mock(GraphVersionRepository.class);
        KnowledgeGraphExtractionApplicationService graphExtractionService =
                mock(KnowledgeGraphExtractionApplicationService.class);
        QualityReportRepository reportRepository = mock(QualityReportRepository.class);
        QualityAnnotationRepository annotationRepository = mock(QualityAnnotationRepository.class);
        KnowledgeQualityReportApplicationServiceImpl service = new KnowledgeQualityReportApplicationServiceImpl(
                graphVersionRepository,
                graphExtractionService,
                mock(KnowledgeEntityRepository.class),
                mock(KnowledgeRelationRepository.class),
                mock(KnowledgeLineageNodeRepository.class),
                mock(KnowledgeLineageRelationRepository.class),
                mock(RefinementTaskRepository.class),
                mock(RefinementEntityDraftRepository.class),
                mock(RefinementRelationDraftRepository.class),
                mock(RefinementLineageNodeDraftRepository.class),
                mock(RefinementLineageRelationDraftRepository.class),
                annotationRepository,
                reportRepository);
        when(reportRepository.getByReportId(1001L)).thenReturn(report());
        when(reportRepository.listIssuesByReportId(1001L)).thenReturn(List.of());
        when(reportRepository.listSourceDetailsByReportId(1001L)).thenReturn(sourceDetails);
        when(annotationRepository.listByGraphVersionId(71L)).thenReturn(List.of());
        return new ServiceFixture(service, graphExtractionService);
    }

    private static ReextractLowQualityCategoryCommand reextractCommand() {
        return new ReextractLowQualityCategoryCommand(1001L, "myth", "GRAPH", true, 1L, "gpt-5.5", "[]", "{}", 9L);
    }

    private static GraphExtractionTaskResult graphExtractionTask(String taskId, Long batchJobId) {
        GraphExtractionTaskResult result = new GraphExtractionTaskResult();
        result.setTaskId(taskId);
        result.setBatchJobId(batchJobId);
        result.setTaskType("GRAPH");
        return result;
    }

    private static QualityReport report() {
        return new QualityReport(
                1L,
                1001L,
                "KQR-71",
                71L,
                "SANCAI_ENTRY",
                2001L,
                "myth",
                "神话",
                "PUBLISHED",
                2L,
                1L,
                2L,
                1L,
                0L,
                0L,
                new BigDecimal("0.5000"),
                new BigDecimal("0.5000"),
                BigDecimal.ZERO,
                new BigDecimal("0.5000"),
                1L,
                1L,
                1L,
                null,
                null,
                null,
                null);
    }

    private static QualityReportSourceDetail sourceDetail(
            Long sourceContentId, String sourceContentType, Long issueCount) {
        return new QualityReportSourceDetail(
                null,
                null,
                1001L,
                sourceContentType,
                sourceContentId,
                "myth",
                "神话",
                71L,
                null,
                1L,
                issueCount,
                "APPLIED",
                "/knowledge/atlas",
                null);
    }

    private static KnowledgeEntity entity(
            Long entityId,
            String entityKey,
            String name,
            String entityType,
            String description,
            String confirmationStatus,
            Long latestVersionId) {
        return new KnowledgeEntity(
                KnowledgeEntityIdCodec.toDomain(entityId),
                entityKey,
                name,
                entityType,
                description,
                KnowledgeConfirmationStatus.from(confirmationStatus),
                GraphVersionIdCodec.toDomain(latestVersionId),
                "[]",
                null,
                null,
                null);
    }

    private record ServiceFixture(
            KnowledgeQualityReportApplicationServiceImpl service,
            KnowledgeGraphExtractionApplicationService graphExtractionService) {}
}
