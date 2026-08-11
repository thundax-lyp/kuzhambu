package com.thundax.kuzhambu.knowledge.application.portal.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.lineage.service.KnowledgeLineageReadApplicationService;
import com.thundax.kuzhambu.knowledge.application.portal.query.KnowledgePortalAtlasQuery;
import com.thundax.kuzhambu.knowledge.application.portal.result.KnowledgePortalAtlasResult;
import com.thundax.kuzhambu.knowledge.application.portal.result.KnowledgePortalHomeResult;
import com.thundax.kuzhambu.knowledge.application.portal.result.KnowledgePortalQualityResult;
import com.thundax.kuzhambu.knowledge.application.refinement.query.LatestQualityReportQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.IssueRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.ReportRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.SourceDetailRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.service.KnowledgeQualityReportApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionAiCandidateIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionSourceContentIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.KnowledgeEntityIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphVersionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.KnowledgeConfirmationStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagGovernanceMetricsRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgePortalReadApplicationServiceImplTest {

    @Test
    void getHomeShouldAggregateCountsAndRecentUpdates() {
        TagRepository tagRepository = mock(TagRepository.class);
        GraphVersionRepository graphVersionRepository = mock(GraphVersionRepository.class);
        KnowledgeEntityRepository knowledgeEntityRepository = mock(KnowledgeEntityRepository.class);
        KnowledgeRelationRepository knowledgeRelationRepository = mock(KnowledgeRelationRepository.class);

        when(tagRepository.page(null, null, null, null, null, 1, 1)).thenReturn(PageResult.of(1, 1, 12, List.of()));
        when(graphVersionRepository.page(null, GraphVersionStatus.APPLIED, null, null, 1, 1))
                .thenReturn(PageResult.of(1, 1, 4, List.of()));
        when(knowledgeEntityRepository.page(null, null, null, null, 1, 1))
                .thenReturn(PageResult.of(1, 1, 21, List.of()));
        when(knowledgeRelationRepository.page(null, null, null, null, 1, 1))
                .thenReturn(PageResult.of(1, 1, 34, List.of()));
        when(graphVersionRepository.page(null, GraphVersionStatus.APPLIED, null, null, 1, 3))
                .thenReturn(PageResult.of(
                        1,
                        3,
                        1,
                        List.of(version(
                                71L, 901L, "GRAPH", "SANCAI_ENTRY", 1001L, null, null, 2, 1_700_000_000_000L))));

        KnowledgePortalReadApplicationServiceImpl service = new KnowledgePortalReadApplicationServiceImpl(
                tagRepository,
                graphVersionRepository,
                knowledgeEntityRepository,
                knowledgeRelationRepository,
                mock(TagGovernanceMetricsRepository.class),
                mock(RefinementTaskRepository.class),
                mock(KnowledgeQualityReportApplicationService.class),
                mock(KnowledgeLineageReadApplicationService.class));

        KnowledgePortalHomeResult result = service.getHome();

        assertEquals("古籍知识图谱门户", result.getHeroTitle());
        assertEquals("12", result.getStats().get(0).getValue());
        assertEquals("4", result.getStats().get(1).getValue());
        assertEquals("21", result.getStats().get(2).getValue());
        assertEquals("34", result.getStats().get(3).getValue());
        assertEquals("/knowledge/atlas", result.getQuickLinks().get(0).getHref());
        assertEquals("SANCAI_ENTRY · 版本 2", result.getRecentUpdates().get(0).getTitle());
        assertEquals(
                "/knowledge/atlas?focusType=SANCAI_ENTRY&focusId=1001",
                result.getRecentUpdates().get(0).getHref());
        assertEquals(3, result.getFeatureCollections().size());
    }

    @Test
    void getHomeShouldReturnFallbackRecentUpdateWhenNoAppliedVersionExists() {
        TagRepository tagRepository = mock(TagRepository.class);
        GraphVersionRepository graphVersionRepository = mock(GraphVersionRepository.class);
        KnowledgeEntityRepository knowledgeEntityRepository = mock(KnowledgeEntityRepository.class);
        KnowledgeRelationRepository knowledgeRelationRepository = mock(KnowledgeRelationRepository.class);

        when(tagRepository.page(null, null, null, null, null, 1, 1)).thenReturn(PageResult.of(1, 1, 0, List.of()));
        when(graphVersionRepository.page(null, GraphVersionStatus.APPLIED, null, null, 1, 1))
                .thenReturn(PageResult.of(1, 1, 0, List.of()));
        when(knowledgeEntityRepository.page(null, null, null, null, 1, 1))
                .thenReturn(PageResult.of(1, 1, 0, List.of()));
        when(knowledgeRelationRepository.page(null, null, null, null, 1, 1))
                .thenReturn(PageResult.of(1, 1, 0, List.of()));
        when(graphVersionRepository.page(null, GraphVersionStatus.APPLIED, null, null, 1, 3))
                .thenReturn(PageResult.of(1, 3, 0, List.of()));

        KnowledgePortalReadApplicationServiceImpl service = new KnowledgePortalReadApplicationServiceImpl(
                tagRepository,
                graphVersionRepository,
                knowledgeEntityRepository,
                knowledgeRelationRepository,
                mock(TagGovernanceMetricsRepository.class),
                mock(RefinementTaskRepository.class),
                mock(KnowledgeQualityReportApplicationService.class),
                mock(KnowledgeLineageReadApplicationService.class));

        KnowledgePortalHomeResult result = service.getHome();

        assertEquals(1, result.getRecentUpdates().size());
        assertEquals("等待首批知识版本", result.getRecentUpdates().get(0).getTitle());
        assertNull(result.getRecentUpdates().get(0).getUpdatedAt());
    }

    @Test
    void getAtlasShouldAssembleFocusNodeAndRelationGroupsFromLatestVersion() {
        TagRepository tagRepository = mock(TagRepository.class);
        GraphVersionRepository graphVersionRepository = mock(GraphVersionRepository.class);
        KnowledgeEntityRepository knowledgeEntityRepository = mock(KnowledgeEntityRepository.class);
        KnowledgeRelationRepository knowledgeRelationRepository = mock(KnowledgeRelationRepository.class);

        when(graphVersionRepository.page(null, GraphVersionStatus.APPLIED, null, null, 1, 1))
                .thenReturn(PageResult.of(
                        1,
                        1,
                        1,
                        List.of(version(
                                71L, 901L, "GRAPH", "SANCAI_ENTRY", 1001L, "ANIMALS", "鸟兽", 2, 1_700_000_000_000L))));
        when(knowledgeEntityRepository.getByEntityId(KnowledgeEntityIdCodec.toDomain(3001L)))
                .thenReturn(entity(
                        3001L,
                        "person:huangdi",
                        "黄帝",
                        "PERSON",
                        "上古始祖",
                        "CONFIRMED",
                        71L,
                        "[]",
                        1_700_000_000_000L,
                        1_700_000_100_000L,
                        1_700_000_200_000L));
        when(knowledgeRelationRepository.listByEntityKey("person:huangdi"))
                .thenReturn(List.of(new KnowledgeRelation(
                        4001L,
                        "rel:1",
                        "person:huangdi",
                        "person:shaodian",
                        "黄帝",
                        "少典",
                        "ANCESTOR",
                        "史料证据",
                        "CONFIRMED",
                        71L,
                        "[]",
                        null,
                        null,
                        null)));

        KnowledgePortalReadApplicationServiceImpl service = new KnowledgePortalReadApplicationServiceImpl(
                tagRepository,
                graphVersionRepository,
                knowledgeEntityRepository,
                knowledgeRelationRepository,
                mock(TagGovernanceMetricsRepository.class),
                mock(RefinementTaskRepository.class),
                mock(KnowledgeQualityReportApplicationService.class),
                mock(KnowledgeLineageReadApplicationService.class));

        KnowledgePortalAtlasQuery query = new KnowledgePortalAtlasQuery("detail", null, 3001L, null, null, null, null);
        KnowledgePortalAtlasResult result = service.getAtlas(query);

        assertEquals("detail", result.getCurrentLevel());
        assertEquals("鸟兽", result.getBreadcrumbItems().get(1).getLabel());
        assertEquals("黄帝", result.getBreadcrumbItems().get(2).getLabel());
        assertEquals("3001", result.getDetailView().getFocusNode().getId());
        assertEquals("黄帝", result.getDetailView().getFocusNode().getTitle());
        assertEquals(1, result.getDetailView().getRelationGroups().size());
        assertEquals(
                "ANCESTOR", result.getDetailView().getRelationGroups().get(0).getGroupKey());
        assertEquals(
                "person:shaodian",
                result.getDetailView()
                        .getRelationGroups()
                        .get(0)
                        .getRelations()
                        .get(0)
                        .getTargetId());
        assertEquals(
                "SANCAI_ENTRY", result.getAvailableFilters().getKnowledgeBases().get(0));
        assertEquals("PERSON", result.getAvailableFilters().getEntityTypes().get(0));
        assertEquals(3, result.getDetailView().getTimelineItems().size());
        assertNotNull(result.getCanvasView());
        assertEquals("detail", result.getCanvasView().getMode());
        assertEquals("entity:3001", result.getCanvasView().getFocusNodeId());
        assertEquals("黄帝关系图谱", result.getCanvasView().getTitle());
        assertEquals(2, result.getCanvasView().getNodes().size());
        assertEquals(1, result.getCanvasView().getEdges().size());
    }

    @Test
    void getAtlasShouldAssembleOverviewCategoryCardsFromAppliedVersions() {
        GraphVersionRepository graphVersionRepository = mock(GraphVersionRepository.class);
        KnowledgeEntityRepository knowledgeEntityRepository = mock(KnowledgeEntityRepository.class);
        KnowledgeRelationRepository knowledgeRelationRepository = mock(KnowledgeRelationRepository.class);
        when(graphVersionRepository.listAppliedByCategoryCode(null))
                .thenReturn(List.of(
                        version(71L, 901L, "GRAPH", "SANCAI_ENTRY", 1001L, "ANIMALS", "鸟兽", 3, 1_700_000_000_000L),
                        version(72L, 902L, "GRAPH", "SANCAI_ENTRY", 1002L, "ANIMALS", "鸟兽", 2, 1_699_000_000_000L)));
        when(knowledgeEntityRepository.listByVersionId(GraphVersionIdCodec.toDomain(71L)))
                .thenReturn(List.of(
                        entity(3001L, "bird:luan", "鸾", "CREATURE", "神鸟", "CONFIRMED", 71L),
                        entity(3002L, "bird:feng", "凤", "CREATURE", "瑞鸟", "CONFIRMED", 71L)));
        when(knowledgeRelationRepository.listByVersionId(71L))
                .thenReturn(List.of(new KnowledgeRelation(
                        4001L,
                        "rel:bird",
                        "bird:luan",
                        "bird:feng",
                        "鸾",
                        "凤",
                        "KIN",
                        "图谱证据",
                        "CONFIRMED",
                        71L,
                        "[]",
                        null,
                        null,
                        null)));

        KnowledgePortalReadApplicationServiceImpl service = new KnowledgePortalReadApplicationServiceImpl(
                mock(TagRepository.class),
                graphVersionRepository,
                knowledgeEntityRepository,
                knowledgeRelationRepository,
                mock(TagGovernanceMetricsRepository.class),
                mock(RefinementTaskRepository.class),
                mock(KnowledgeQualityReportApplicationService.class),
                mock(KnowledgeLineageReadApplicationService.class));

        KnowledgePortalAtlasQuery query = new KnowledgePortalAtlasQuery("overview", null, null, null, null, null, null);
        KnowledgePortalAtlasResult result = service.getAtlas(query);

        assertEquals("overview", result.getCurrentLevel());
        assertEquals("图谱总览", result.getBreadcrumbItems().get(0).getLabel());
        assertEquals("十四门类知识鸟瞰", result.getOverviewView().getSummaryTitle());
        assertEquals(14, result.getOverviewView().getCategoryCards().size());
        assertEquals(
                "ASTRONOMY", result.getOverviewView().getCategoryCards().get(0).getCategoryCode());
        assertEquals("天文", result.getOverviewView().getCategoryCards().get(0).getCategoryName());
        assertEquals(0L, result.getOverviewView().getCategoryCards().get(0).getEntityCount());
        assertEquals(0L, result.getOverviewView().getCategoryCards().get(0).getAppliedVersionCount());
        assertEquals(
                "ANIMALS", result.getOverviewView().getCategoryCards().get(12).getCategoryCode());
        assertEquals("鸟兽", result.getOverviewView().getCategoryCards().get(12).getCategoryName());
        assertEquals(2L, result.getOverviewView().getCategoryCards().get(12).getEntityCount());
        assertEquals(1L, result.getOverviewView().getCategoryCards().get(12).getRelationCount());
        assertEquals(2L, result.getOverviewView().getCategoryCards().get(12).getAppliedVersionCount());
        assertNotNull(result.getCanvasView());
        assertEquals("overview", result.getCanvasView().getMode());
        assertEquals("root:sancai", result.getCanvasView().getFocusNodeId());
        assertEquals(15, result.getCanvasView().getNodes().size());
        assertEquals(14, result.getCanvasView().getEdges().size());
        assertTrue(result.getCanvasView().getEdges().stream().anyMatch(edge -> Boolean.TRUE.equals(edge.getDashed())));
    }

    @Test
    void getAtlasShouldAssembleCategoryViewFromCategoryCode() {
        GraphVersionRepository graphVersionRepository = mock(GraphVersionRepository.class);
        KnowledgeEntityRepository knowledgeEntityRepository = mock(KnowledgeEntityRepository.class);
        KnowledgeRelationRepository knowledgeRelationRepository = mock(KnowledgeRelationRepository.class);
        when(graphVersionRepository.getByLatestAppliedCategoryCode("ANIMALS"))
                .thenReturn(version(71L, 901L, "GRAPH", "SANCAI_ENTRY", 1001L, "ANIMALS", "鸟兽", 3, 1_700_000_000_000L));
        when(knowledgeEntityRepository.listByVersionId(GraphVersionIdCodec.toDomain(71L)))
                .thenReturn(List.of(entity(3001L, "bird:luan", "鸾", "CREATURE", "神鸟", "CONFIRMED", 71L)));
        when(knowledgeRelationRepository.listByVersionId(71L))
                .thenReturn(List.of(new KnowledgeRelation(
                        4001L,
                        "rel:bird",
                        "bird:luan",
                        "bird:feng",
                        "鸾",
                        "凤",
                        "KIN",
                        "图谱证据",
                        "CONFIRMED",
                        71L,
                        "[]",
                        null,
                        null,
                        null)));

        KnowledgePortalReadApplicationServiceImpl service = new KnowledgePortalReadApplicationServiceImpl(
                mock(TagRepository.class),
                graphVersionRepository,
                knowledgeEntityRepository,
                knowledgeRelationRepository,
                mock(TagGovernanceMetricsRepository.class),
                mock(RefinementTaskRepository.class),
                mock(KnowledgeQualityReportApplicationService.class),
                mock(KnowledgeLineageReadApplicationService.class));

        KnowledgePortalAtlasQuery query =
                new KnowledgePortalAtlasQuery("category", "ANIMALS", null, null, null, null, null);
        KnowledgePortalAtlasResult result = service.getAtlas(query);

        assertEquals("category", result.getCurrentLevel());
        assertEquals("鸟兽", result.getBreadcrumbItems().get(1).getLabel());
        assertEquals("ANIMALS", result.getCategoryView().getCategoryCode());
        assertEquals("鸟兽", result.getCategoryView().getCategoryName());
        assertEquals(1, result.getCategoryView().getEntityHighlights().size());
        assertEquals("鸾", result.getCategoryView().getEntityHighlights().get(0).getEntityName());
        assertEquals("KIN", result.getCategoryView().getRelationGroups().get(0).getGroupKey());
        assertNotNull(result.getCanvasView());
        assertEquals("category", result.getCanvasView().getMode());
        assertEquals("category:ANIMALS", result.getCanvasView().getFocusNodeId());
        assertEquals(3, result.getCanvasView().getNodes().size());
        assertEquals(2, result.getCanvasView().getEdges().size());
    }

    @Test
    void getAtlasShouldKeepValidEmptyCategoryAtCategoryLevel() {
        GraphVersionRepository graphVersionRepository = mock(GraphVersionRepository.class);
        when(graphVersionRepository.getByLatestAppliedCategoryCode("ASTRONOMY")).thenReturn(null);
        KnowledgePortalReadApplicationServiceImpl service = new KnowledgePortalReadApplicationServiceImpl(
                mock(TagRepository.class),
                graphVersionRepository,
                mock(KnowledgeEntityRepository.class),
                mock(KnowledgeRelationRepository.class),
                mock(TagGovernanceMetricsRepository.class),
                mock(RefinementTaskRepository.class),
                mock(KnowledgeQualityReportApplicationService.class),
                mock(KnowledgeLineageReadApplicationService.class));

        KnowledgePortalAtlasQuery query =
                new KnowledgePortalAtlasQuery("category", "ASTRONOMY", null, null, null, null, null);
        KnowledgePortalAtlasResult result = service.getAtlas(query);

        assertEquals("category", result.getCurrentLevel());
        assertEquals("ASTRONOMY", result.getCategoryView().getCategoryCode());
        assertEquals("天文", result.getCategoryView().getCategoryName());
        assertEquals(0, result.getCategoryView().getEntityHighlights().size());
        assertNotNull(result.getCanvasView());
        assertEquals("category", result.getCanvasView().getMode());
        assertEquals("category:ASTRONOMY", result.getCanvasView().getFocusNodeId());
        assertEquals(Boolean.TRUE, result.getCanvasView().getEmpty());
        assertEquals(1, result.getCanvasView().getNodes().size());
        assertEquals(0, result.getCanvasView().getEdges().size());
    }

    @Test
    void getQualityShouldReadLatestPublishedReportSnapshot() {
        KnowledgeQualityReportApplicationService qualityReportService =
                mock(KnowledgeQualityReportApplicationService.class);
        when(qualityReportService.latest(new LatestQualityReportQuery(null)))
                .thenReturn(new QualityReportDetailResult(
                        new ReportRecord(
                                9001L,
                                "KQR-20260706120000-71",
                                71L,
                                "SANCAI_ENTRY",
                                1001L,
                                "SANCAI",
                                "三才图会",
                                "PUBLISHED",
                                2L,
                                1L,
                                4L,
                                3L,
                                5L,
                                4L,
                                new BigDecimal("0.5000"),
                                new BigDecimal("0.7500"),
                                new BigDecimal("0.8000"),
                                new BigDecimal("0.7273"),
                                8L,
                                1L,
                                1L,
                                Instant.ofEpochMilli(1_700_000_000_000L),
                                Instant.ofEpochMilli(1_700_000_000_000L)),
                        List.of(new IssueRecord(
                                9101L,
                                "LOW_ENTITY_COVERAGE",
                                "medium",
                                null,
                                null,
                                "实体覆盖率偏低",
                                "实体确认率低于阈值。",
                                "继续人工精修。",
                                "/knowledge/quality-report",
                                1)),
                        List.of(new SourceDetailRecord(
                                9201L,
                                "SANCAI_ENTRY",
                                1001L,
                                "SANCAI",
                                "三才图会",
                                71L,
                                Instant.ofEpochMilli(1_700_000_000_000L),
                                8L,
                                1L,
                                "APPLIED",
                                "/knowledge/atlas?level=category&categoryCode=SANCAI")),
                        List.of()));
        KnowledgePortalReadApplicationServiceImpl service = new KnowledgePortalReadApplicationServiceImpl(
                mock(TagRepository.class),
                mock(GraphVersionRepository.class),
                mock(KnowledgeEntityRepository.class),
                mock(KnowledgeRelationRepository.class),
                mock(TagGovernanceMetricsRepository.class),
                mock(RefinementTaskRepository.class),
                qualityReportService,
                mock(KnowledgeLineageReadApplicationService.class));

        KnowledgePortalQualityResult result = service.getQuality();

        assertEquals("50%", result.getQualityStats().get(0).getValue());
        assertEquals("75%", result.getQualityStats().get(1).getValue());
        assertEquals("80%", result.getQualityStats().get(2).getValue());
        assertEquals("73%", result.getQualityStats().get(3).getValue());
        assertEquals(0, result.getTrendSeries().size());
        assertEquals("SANCAI", result.getSourceBreakdowns().get(0).getSourceKey());
        assertEquals(8L, result.getSourceBreakdowns().get(0).getValue());
        assertEquals("三才图会", result.getSourceDetails().get(0).getSourceTitle());
        assertEquals("实体覆盖率偏低", result.getFocusIssues().get(0).getTitle());
    }

    @Test
    void getQualityShouldReturnEmptyStateWhenNoQualityReportExists() {
        KnowledgeQualityReportApplicationService qualityReportService =
                mock(KnowledgeQualityReportApplicationService.class);
        when(qualityReportService.latest(new LatestQualityReportQuery(null)))
                .thenReturn(new QualityReportDetailResult(null, List.of(), List.of(), List.of()));
        KnowledgePortalReadApplicationServiceImpl service = new KnowledgePortalReadApplicationServiceImpl(
                mock(TagRepository.class),
                mock(GraphVersionRepository.class),
                mock(KnowledgeEntityRepository.class),
                mock(KnowledgeRelationRepository.class),
                mock(TagGovernanceMetricsRepository.class),
                mock(RefinementTaskRepository.class),
                qualityReportService,
                mock(KnowledgeLineageReadApplicationService.class));

        KnowledgePortalQualityResult result = service.getQuality();

        assertEquals(0, result.getQualityStats().size());
        assertEquals(0, result.getTrendSeries().size());
        assertEquals(0, result.getSourceBreakdowns().size());
        assertEquals(0, result.getSourceDetails().size());
        assertEquals("尚未生成质量报告", result.getFocusIssues().get(0).getTitle());
        assertEquals("high", result.getFocusIssues().get(0).getSeverity());
        assertEquals("/knowledge/quality", result.getFocusIssues().get(0).getHref());
    }

    private static GraphVersion version(
            Long versionId,
            Long candidateId,
            String taskType,
            String sourceContentType,
            Long sourceContentId,
            String sourceCategoryCode,
            String sourceCategoryName,
            Integer versionNo,
            Long appliedAt) {
        return new GraphVersion(
                GraphVersionIdCodec.toDomain(versionId),
                null,
                GraphExtractionAiCandidateIdCodec.toDomain(candidateId),
                GraphExtractionTaskType.from(taskType),
                null,
                null,
                sourceContentType,
                GraphExtractionSourceContentIdCodec.toDomain(sourceContentId),
                sourceCategoryCode,
                sourceCategoryName,
                versionNo,
                GraphVersionStatus.APPLIED,
                Instant.ofEpochMilli(appliedAt));
    }

    private static KnowledgeEntity entity(
            Long entityId,
            String entityKey,
            String name,
            String entityType,
            String description,
            String confirmationStatus,
            Long latestVersionId) {
        return entity(
                entityId,
                entityKey,
                name,
                entityType,
                description,
                confirmationStatus,
                latestVersionId,
                "[]",
                null,
                null,
                null);
    }

    private static KnowledgeEntity entity(
            Long entityId,
            String entityKey,
            String name,
            String entityType,
            String description,
            String confirmationStatus,
            Long latestVersionId,
            String sourceRefsJson,
            Long firstExtractedAt,
            Long lastExtractedAt,
            Long confirmedAt) {
        return new KnowledgeEntity(
                KnowledgeEntityIdCodec.toDomain(entityId),
                entityKey,
                name,
                entityType,
                description,
                KnowledgeConfirmationStatus.from(confirmationStatus),
                GraphVersionIdCodec.toDomain(latestVersionId),
                sourceRefsJson,
                instant(firstExtractedAt),
                instant(lastExtractedAt),
                instant(confirmedAt));
    }

    private static Instant instant(Long epochMillis) {
        return epochMillis == null ? null : Instant.ofEpochMilli(epochMillis);
    }
}
