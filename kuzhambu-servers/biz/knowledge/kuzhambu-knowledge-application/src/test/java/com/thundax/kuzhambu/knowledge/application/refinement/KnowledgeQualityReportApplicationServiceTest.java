package com.thundax.kuzhambu.knowledge.application.refinement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.application.refinement.command.GenerateQualityReportCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.service.impl.KnowledgeQualityReportApplicationServiceImpl;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityAnnotation;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReport;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReportIssue;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReportSourceDetail;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.QualityAnnotationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.QualityReportRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementEntityDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementLineageNodeDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementLineageRelationDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementRelationDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementTaskRepository;
import java.math.BigDecimal;
import java.util.Date;
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
                1L,
                71L,
                null,
                901L,
                "GRAPH",
                null,
                null,
                "SANCAI_ENTRY",
                1001L,
                "myth",
                "神话",
                2,
                "APPLIED",
                new Date(1_700_000_000_000L));
        when(graphVersionRepository.getByVersionId(71L)).thenReturn(version);
        when(entityRepository.listByVersionId(71L))
                .thenReturn(List.of(
                        new KnowledgeEntity(
                                1L,
                                3001L,
                                "person:huangdi",
                                "黄帝",
                                "PERSON",
                                "始祖",
                                "MANUAL_CONFIRMED",
                                71L,
                                "[]",
                                null,
                                null,
                                null),
                        new KnowledgeEntity(
                                2L,
                                3002L,
                                "person:fuxi",
                                "伏羲",
                                "PERSON",
                                "始祖",
                                "PENDING",
                                71L,
                                "[]",
                                null,
                                null,
                                null)));
        when(relationRepository.listByVersionId(71L))
                .thenReturn(List.of(new KnowledgeRelation(
                        1L,
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
                        1L,
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
}
