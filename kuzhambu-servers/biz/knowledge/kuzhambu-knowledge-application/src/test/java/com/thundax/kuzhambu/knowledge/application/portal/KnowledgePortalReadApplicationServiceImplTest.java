package com.thundax.kuzhambu.knowledge.application.portal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
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
                tagRepository, graphVersionRepository, knowledgeEntityRepository, knowledgeRelationRepository);

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
                tagRepository, graphVersionRepository, knowledgeEntityRepository, knowledgeRelationRepository);

        KnowledgePortalHomeResult result = service.getHome();

        assertEquals(1, result.getRecentUpdates().size());
        assertEquals("等待首批知识版本", result.getRecentUpdates().get(0).getTitle());
        assertNull(result.getRecentUpdates().get(0).getUpdatedAt());
    }
}
