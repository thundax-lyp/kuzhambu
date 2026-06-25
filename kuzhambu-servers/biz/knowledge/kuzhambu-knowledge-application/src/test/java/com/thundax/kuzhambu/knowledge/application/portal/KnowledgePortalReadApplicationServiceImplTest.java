package com.thundax.kuzhambu.knowledge.application.portal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.readmodel.TagGovernanceMetrics;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagGovernanceMetricsRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import java.util.Date;
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
        when(graphVersionRepository.page(null, "APPLIED", null, null, 1, 1))
                .thenReturn(PageResult.of(1, 1, 4, List.of()));
        when(knowledgeEntityRepository.page(null, null, null, null, 1, 1))
                .thenReturn(PageResult.of(1, 1, 21, List.of()));
        when(knowledgeRelationRepository.page(null, null, null, null, 1, 1))
                .thenReturn(PageResult.of(1, 1, 34, List.of()));
        when(graphVersionRepository.page(null, "APPLIED", null, null, 1, 3))
                .thenReturn(PageResult.of(
                        1,
                        3,
                        1,
                        List.of(new GraphVersion(
                                1L,
                                71L,
                                null,
                                901L,
                                "GRAPH",
                                null,
                                null,
                                "SANCAI_ENTRY",
                                1001L,
                                null,
                                null,
                                2,
                                "APPLIED",
                                new Date(1_700_000_000_000L)))));

        KnowledgePortalReadApplicationServiceImpl service = new KnowledgePortalReadApplicationServiceImpl(
                tagRepository,
                graphVersionRepository,
                knowledgeEntityRepository,
                knowledgeRelationRepository,
                mock(TagGovernanceMetricsRepository.class),
                mock(RefinementTaskRepository.class));

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
        when(graphVersionRepository.page(null, "APPLIED", null, null, 1, 1))
                .thenReturn(PageResult.of(1, 1, 0, List.of()));
        when(knowledgeEntityRepository.page(null, null, null, null, 1, 1))
                .thenReturn(PageResult.of(1, 1, 0, List.of()));
        when(knowledgeRelationRepository.page(null, null, null, null, 1, 1))
                .thenReturn(PageResult.of(1, 1, 0, List.of()));
        when(graphVersionRepository.page(null, "APPLIED", null, null, 1, 3))
                .thenReturn(PageResult.of(1, 3, 0, List.of()));

        KnowledgePortalReadApplicationServiceImpl service = new KnowledgePortalReadApplicationServiceImpl(
                tagRepository,
                graphVersionRepository,
                knowledgeEntityRepository,
                knowledgeRelationRepository,
                mock(TagGovernanceMetricsRepository.class),
                mock(RefinementTaskRepository.class));

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

        when(graphVersionRepository.page(null, "APPLIED", null, null, 1, 1))
                .thenReturn(PageResult.of(
                        1,
                        1,
                        1,
                        List.of(new GraphVersion(
                                1L,
                                71L,
                                null,
                                901L,
                                "GRAPH",
                                null,
                                null,
                                "SANCAI_ENTRY",
                                1001L,
                                "SANCAI",
                                "三才图会",
                                2,
                                "APPLIED",
                                new Date(1_700_000_000_000L)))));
        when(knowledgeEntityRepository.listByVersionId(71L))
                .thenReturn(List.of(
                        new KnowledgeEntity(
                                1L,
                                3001L,
                                "person:huangdi",
                                "黄帝",
                                "PERSON",
                                "上古始祖",
                                "CONFIRMED",
                                71L,
                                "[]",
                                new Date(1_700_000_000_000L),
                                new Date(1_700_000_100_000L),
                                new Date(1_700_000_200_000L)),
                        new KnowledgeEntity(
                                2L,
                                3002L,
                                "person:shaodian",
                                "少典",
                                "PERSON",
                                "黄帝之父",
                                "CONFIRMED",
                                71L,
                                "[]",
                                null,
                                null,
                                null)));
        when(knowledgeRelationRepository.listByVersionId(71L))
                .thenReturn(List.of(new KnowledgeRelation(
                        1L,
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
                mock(RefinementTaskRepository.class));

        KnowledgePortalAtlasQuery query = new KnowledgePortalAtlasQuery();
        query.setLevel("detail");
        KnowledgePortalAtlasResult result = service.getAtlas(query);

        assertEquals("detail", result.getCurrentLevel());
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
    }

    @Test
    void getAtlasShouldAssembleOverviewCategoryCardsFromAppliedVersions() {
        GraphVersionRepository graphVersionRepository = mock(GraphVersionRepository.class);
        KnowledgeEntityRepository knowledgeEntityRepository = mock(KnowledgeEntityRepository.class);
        KnowledgeRelationRepository knowledgeRelationRepository = mock(KnowledgeRelationRepository.class);
        when(graphVersionRepository.listAppliedByCategoryCode(null))
                .thenReturn(List.of(
                        new GraphVersion(
                                1L,
                                71L,
                                null,
                                901L,
                                "GRAPH",
                                null,
                                null,
                                "SANCAI_ENTRY",
                                1001L,
                                "BIRDS",
                                "羽族",
                                3,
                                "APPLIED",
                                new Date(1_700_000_000_000L)),
                        new GraphVersion(
                                2L,
                                72L,
                                null,
                                902L,
                                "GRAPH",
                                null,
                                null,
                                "SANCAI_ENTRY",
                                1002L,
                                "BIRDS",
                                "羽族",
                                2,
                                "APPLIED",
                                new Date(1_699_000_000_000L))));
        when(knowledgeEntityRepository.listByVersionId(71L))
                .thenReturn(List.of(
                        new KnowledgeEntity(
                                1L,
                                3001L,
                                "bird:luan",
                                "鸾",
                                "CREATURE",
                                "神鸟",
                                "CONFIRMED",
                                71L,
                                "[]",
                                null,
                                null,
                                null),
                        new KnowledgeEntity(
                                2L,
                                3002L,
                                "bird:feng",
                                "凤",
                                "CREATURE",
                                "瑞鸟",
                                "CONFIRMED",
                                71L,
                                "[]",
                                null,
                                null,
                                null)));
        when(knowledgeRelationRepository.listByVersionId(71L))
                .thenReturn(List.of(new KnowledgeRelation(
                        1L,
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
                mock(RefinementTaskRepository.class));

        KnowledgePortalAtlasQuery query = new KnowledgePortalAtlasQuery();
        query.setLevel("overview");
        KnowledgePortalAtlasResult result = service.getAtlas(query);

        assertEquals("overview", result.getCurrentLevel());
        assertEquals("图谱总览", result.getBreadcrumbItems().get(0).getLabel());
        assertEquals("十四门类知识鸟瞰", result.getOverviewView().getSummaryTitle());
        assertEquals(1, result.getOverviewView().getCategoryCards().size());
        assertEquals("BIRDS", result.getOverviewView().getCategoryCards().get(0).getCategoryCode());
        assertEquals("羽族", result.getOverviewView().getCategoryCards().get(0).getCategoryName());
        assertEquals(2L, result.getOverviewView().getCategoryCards().get(0).getEntityCount());
        assertEquals(1L, result.getOverviewView().getCategoryCards().get(0).getRelationCount());
        assertEquals(2L, result.getOverviewView().getCategoryCards().get(0).getAppliedVersionCount());
    }

    @Test
    void getAtlasShouldAssembleCategoryViewFromCategoryCode() {
        GraphVersionRepository graphVersionRepository = mock(GraphVersionRepository.class);
        KnowledgeEntityRepository knowledgeEntityRepository = mock(KnowledgeEntityRepository.class);
        KnowledgeRelationRepository knowledgeRelationRepository = mock(KnowledgeRelationRepository.class);
        when(graphVersionRepository.findLatestAppliedByCategoryCode("BIRDS"))
                .thenReturn(new GraphVersion(
                        1L,
                        71L,
                        null,
                        901L,
                        "GRAPH",
                        null,
                        null,
                        "SANCAI_ENTRY",
                        1001L,
                        "BIRDS",
                        "羽族",
                        3,
                        "APPLIED",
                        new Date(1_700_000_000_000L)));
        when(knowledgeEntityRepository.listByVersionId(71L))
                .thenReturn(List.of(new KnowledgeEntity(
                        1L, 3001L, "bird:luan", "鸾", "CREATURE", "神鸟", "CONFIRMED", 71L, "[]", null, null, null)));
        when(knowledgeRelationRepository.listByVersionId(71L))
                .thenReturn(List.of(new KnowledgeRelation(
                        1L,
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
                mock(RefinementTaskRepository.class));

        KnowledgePortalAtlasQuery query = new KnowledgePortalAtlasQuery();
        query.setLevel("category");
        query.setCategoryCode("BIRDS");
        KnowledgePortalAtlasResult result = service.getAtlas(query);

        assertEquals("category", result.getCurrentLevel());
        assertEquals("羽族", result.getBreadcrumbItems().get(1).getLabel());
        assertEquals("BIRDS", result.getCategoryView().getCategoryCode());
        assertEquals("羽族", result.getCategoryView().getCategoryName());
        assertEquals(1, result.getCategoryView().getEntityHighlights().size());
        assertEquals("鸾", result.getCategoryView().getEntityHighlights().get(0).getEntityName());
        assertEquals("KIN", result.getCategoryView().getRelationGroups().get(0).getGroupKey());
    }

    @Test
    void getQualityShouldAggregateRatiosMetricsAndRecentSources() {
        GraphVersionRepository graphVersionRepository = mock(GraphVersionRepository.class);
        KnowledgeEntityRepository knowledgeEntityRepository = mock(KnowledgeEntityRepository.class);
        KnowledgeRelationRepository knowledgeRelationRepository = mock(KnowledgeRelationRepository.class);
        TagGovernanceMetricsRepository tagGovernanceMetricsRepository = mock(TagGovernanceMetricsRepository.class);
        RefinementTaskRepository refinementTaskRepository = mock(RefinementTaskRepository.class);

        GraphVersion latestVersion = new GraphVersion(
                1L,
                71L,
                null,
                901L,
                "GRAPH",
                null,
                null,
                "SANCAI_ENTRY",
                1001L,
                "SANCAI",
                "三才图会",
                2,
                "APPLIED",
                new Date(1_700_000_000_000L));
        when(graphVersionRepository.page(null, "APPLIED", null, null, 1, 1))
                .thenReturn(PageResult.of(1, 1, 2, List.of(latestVersion)));
        when(graphVersionRepository.page(null, "APPLIED", null, null, 1, 3))
                .thenReturn(PageResult.of(1, 3, 2, List.of(latestVersion)));
        when(knowledgeEntityRepository.listByVersionId(71L))
                .thenReturn(List.of(
                        new KnowledgeEntity(
                                1L,
                                3001L,
                                "person:huangdi",
                                "黄帝",
                                "PERSON",
                                "上古始祖",
                                "CONFIRMED",
                                71L,
                                "[]",
                                null,
                                null,
                                null),
                        new KnowledgeEntity(
                                2L,
                                3002L,
                                "person:shaodian",
                                "少典",
                                "PERSON",
                                "黄帝之父",
                                "PENDING",
                                71L,
                                "[]",
                                null,
                                null,
                                null)));
        when(knowledgeRelationRepository.listByVersionId(71L))
                .thenReturn(List.of(
                        new KnowledgeRelation(
                                1L,
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
                                null),
                        new KnowledgeRelation(
                                2L,
                                4002L,
                                "rel:2",
                                "person:huangdi",
                                "person:leizu",
                                "黄帝",
                                "嫘祖",
                                "SPOUSE",
                                "史料证据",
                                "PENDING",
                                71L,
                                "[]",
                                null,
                                null,
                                null)));
        when(tagGovernanceMetricsRepository.getMetrics(5, 6))
                .thenReturn(new TagGovernanceMetrics(
                        List.of(),
                        List.of(),
                        List.of(new TagGovernanceMetrics.SourceRatioMetric(TagSource.MANUAL, 8L)),
                        List.of(new TagGovernanceMetrics.MonthlyNewTagMetric("2026-05", 3L))));
        when(refinementTaskRepository.page(null, null, null, null, "DRAFT", 1, 1))
                .thenReturn(PageResult.of(1, 1, 2, List.of()));

        KnowledgePortalReadApplicationServiceImpl service = new KnowledgePortalReadApplicationServiceImpl(
                mock(TagRepository.class),
                graphVersionRepository,
                knowledgeEntityRepository,
                knowledgeRelationRepository,
                tagGovernanceMetricsRepository,
                refinementTaskRepository);

        KnowledgePortalQualityResult result = service.getQuality();

        assertEquals("50%", result.getQualityStats().get(0).getValue());
        assertEquals("50%", result.getQualityStats().get(1).getValue());
        assertEquals("2", result.getQualityStats().get(2).getValue());
        assertEquals("2", result.getQualityStats().get(3).getValue());
        assertEquals(
                "2026-05", result.getTrendSeries().get(0).getPoints().get(0).getLabel());
        assertEquals("MANUAL", result.getSourceBreakdowns().get(0).getSourceKey());
        assertEquals("三才图会", result.getSourceDetails().get(0).getSourceTitle());
        assertEquals(3, result.getFocusIssues().size());
    }
}
