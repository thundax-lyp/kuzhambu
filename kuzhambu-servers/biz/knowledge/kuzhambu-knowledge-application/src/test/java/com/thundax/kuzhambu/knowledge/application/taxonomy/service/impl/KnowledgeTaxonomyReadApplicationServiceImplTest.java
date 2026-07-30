package com.thundax.kuzhambu.knowledge.application.taxonomy.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoverySynonymExpandResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryTagHintResult;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.SynonymIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagAliasIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Synonym;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagAlias;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.SynonymStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.SynonymRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagAliasRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagContentRefRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeTaxonomyReadApplicationServiceImplTest {

    @Test
    void expandSynonymsShouldMergeDirectAndReverseMatches() {
        SynonymRepository synonymRepository = mock(SynonymRepository.class);
        when(synonymRepository.page(eq("礼制"), isNull(), eq(SynonymStatus.ENABLED), eq(1), eq(50)))
                .thenReturn(PageResult.of(
                        1,
                        50,
                        1,
                        List.of(new Synonym(SynonymIdCodec.toDomain(1L), "礼制", "礼学", SynonymStatus.ENABLED))));
        when(synonymRepository.page(isNull(), eq("礼制"), eq(SynonymStatus.ENABLED), eq(1), eq(50)))
                .thenReturn(PageResult.of(
                        1,
                        50,
                        1,
                        List.of(new Synonym(SynonymIdCodec.toDomain(2L), "典礼", "礼制", SynonymStatus.ENABLED))));

        KnowledgeTaxonomyReadApplicationServiceImpl service = new KnowledgeTaxonomyReadApplicationServiceImpl(
                synonymRepository,
                mock(TagRepository.class),
                mock(TagAliasRepository.class),
                mock(TagContentRefRepository.class));

        DiscoverySynonymExpandResult result = service.expandSynonyms("  礼制 ");

        assertEquals("  礼制 ", result.getTerm());
        assertEquals("礼制", result.getNormalizedTerm());
        assertEquals(List.of("礼学", "典礼"), result.getExpandedTerms());
    }

    @Test
    void querySynonymsShouldReturnForwardMatchesOnly() {
        SynonymRepository synonymRepository = mock(SynonymRepository.class);
        when(synonymRepository.page(eq("礼制"), isNull(), eq(SynonymStatus.ENABLED), eq(1), eq(10)))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1,
                        List.of(new Synonym(SynonymIdCodec.toDomain(1L), "礼制", "礼学", SynonymStatus.ENABLED))));
        when(synonymRepository.page(isNull(), eq("礼制"), eq(SynonymStatus.ENABLED), eq(1), eq(10)))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1,
                        List.of(new Synonym(SynonymIdCodec.toDomain(2L), "礼法", "礼制", SynonymStatus.ENABLED))));

        KnowledgeTaxonomyReadApplicationServiceImpl service = new KnowledgeTaxonomyReadApplicationServiceImpl(
                synonymRepository,
                mock(TagRepository.class),
                mock(TagAliasRepository.class),
                mock(TagContentRefRepository.class));

        var result = service.querySynonyms("礼制", "FORWARD", 10);

        assertEquals("礼制", result.getNormalizedTerm());
        assertEquals("FORWARD", result.getDirection());
        assertEquals(10, result.getLimit());
        assertEquals(1, result.getMatches().size());
        assertEquals("礼制", result.getMatches().get(0).getSourceTerm());
        assertEquals("礼学", result.getMatches().get(0).getTargetTerm());
        assertEquals("礼制", result.getMatches().get(0).getMatchedTerm());
        assertEquals("礼学", result.getMatches().get(0).getExpandedTerm());
        assertEquals("FORWARD", result.getMatches().get(0).getDirection());
    }

    @Test
    void querySynonymsShouldReturnReverseMatchesOnly() {
        SynonymRepository synonymRepository = mock(SynonymRepository.class);
        when(synonymRepository.page(isNull(), eq("礼制"), eq(SynonymStatus.ENABLED), eq(1), eq(10)))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1,
                        List.of(new Synonym(SynonymIdCodec.toDomain(1L), "典礼", "礼制", SynonymStatus.ENABLED))));
        when(synonymRepository.page(eq("礼制"), isNull(), eq(SynonymStatus.ENABLED), eq(1), eq(10)))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1,
                        List.of(new Synonym(SynonymIdCodec.toDomain(2L), "礼制", "礼学", SynonymStatus.ENABLED))));

        KnowledgeTaxonomyReadApplicationServiceImpl service = new KnowledgeTaxonomyReadApplicationServiceImpl(
                synonymRepository,
                mock(TagRepository.class),
                mock(TagAliasRepository.class),
                mock(TagContentRefRepository.class));

        var result = service.querySynonyms("礼制", "REVERSE", 10);

        assertEquals("礼制", result.getNormalizedTerm());
        assertEquals("REVERSE", result.getDirection());
        assertEquals(10, result.getLimit());
        assertEquals(1, result.getMatches().size());
        assertEquals("典礼", result.getMatches().get(0).getSourceTerm());
        assertEquals("礼制", result.getMatches().get(0).getTargetTerm());
        assertEquals("礼制", result.getMatches().get(0).getMatchedTerm());
        assertEquals("典礼", result.getMatches().get(0).getExpandedTerm());
        assertEquals("REVERSE", result.getMatches().get(0).getDirection());
    }

    @Test
    void querySynonymsShouldMergeBidirectionalMatchesWithDeduplication() {
        SynonymRepository synonymRepository = mock(SynonymRepository.class);
        when(synonymRepository.page(eq("礼制"), isNull(), eq(SynonymStatus.ENABLED), eq(1), eq(50)))
                .thenReturn(PageResult.of(
                        1,
                        50,
                        3,
                        List.of(
                                new Synonym(SynonymIdCodec.toDomain(1L), "礼制", "礼学", SynonymStatus.ENABLED),
                                new Synonym(SynonymIdCodec.toDomain(2L), "礼制", "典礼", SynonymStatus.ENABLED),
                                new Synonym(SynonymIdCodec.toDomain(3L), "礼法", "礼制", SynonymStatus.ENABLED))));
        when(synonymRepository.page(isNull(), eq("礼制"), eq(SynonymStatus.ENABLED), eq(1), eq(50)))
                .thenReturn(PageResult.of(
                        1,
                        50,
                        2,
                        List.of(
                                new Synonym(SynonymIdCodec.toDomain(4L), "典礼", "礼制", SynonymStatus.ENABLED),
                                new Synonym(SynonymIdCodec.toDomain(5L), "礼法", "礼制", SynonymStatus.ENABLED))));

        KnowledgeTaxonomyReadApplicationServiceImpl service = new KnowledgeTaxonomyReadApplicationServiceImpl(
                synonymRepository,
                mock(TagRepository.class),
                mock(TagAliasRepository.class),
                mock(TagContentRefRepository.class));

        var result = service.querySynonyms("礼制", "BIDIRECTIONAL", null);

        assertEquals("礼制", result.getNormalizedTerm());
        assertEquals("BIDIRECTIONAL", result.getDirection());
        assertEquals(50, result.getLimit());
        assertEquals(3, result.getMatches().size());
        assertEquals("礼学", result.getMatches().get(0).getExpandedTerm());
        assertEquals("典礼", result.getMatches().get(1).getExpandedTerm());
        assertEquals("礼法", result.getMatches().get(2).getExpandedTerm());
        assertEquals("FORWARD", result.getMatches().get(0).getDirection());
        assertEquals("FORWARD", result.getMatches().get(1).getDirection());
        assertEquals("REVERSE", result.getMatches().get(2).getDirection());
    }

    @Test
    void querySynonymsShouldClampLimitAndIgnoreBlankExpandedTerm() {
        SynonymRepository synonymRepository = mock(SynonymRepository.class);
        when(synonymRepository.page(eq("礼制"), isNull(), eq(SynonymStatus.ENABLED), eq(1), eq(50)))
                .thenReturn(PageResult.of(
                        1,
                        50,
                        2,
                        List.of(
                                new Synonym(SynonymIdCodec.toDomain(1L), "礼制", "   ", SynonymStatus.ENABLED),
                                new Synonym(SynonymIdCodec.toDomain(2L), "礼制", "礼学", SynonymStatus.ENABLED))));
        when(synonymRepository.page(isNull(), eq("礼制"), eq(SynonymStatus.ENABLED), eq(1), eq(50)))
                .thenReturn(PageResult.of(
                        1,
                        50,
                        1,
                        List.of(new Synonym(SynonymIdCodec.toDomain(3L), "礼制", "礼制", SynonymStatus.ENABLED))));

        KnowledgeTaxonomyReadApplicationServiceImpl service = new KnowledgeTaxonomyReadApplicationServiceImpl(
                synonymRepository,
                mock(TagRepository.class),
                mock(TagAliasRepository.class),
                mock(TagContentRefRepository.class));

        var result = service.querySynonyms("礼制", "BIDIRECTIONAL", 100);

        assertEquals(50, result.getLimit());
        assertEquals(1, result.getMatches().size());
        assertEquals("礼学", result.getMatches().get(0).getExpandedTerm());

        var blankResult = service.querySynonyms("   ", "BIDIRECTIONAL", 10);
        assertEquals(null, blankResult.getNormalizedTerm());
        assertTrue(blankResult.getMatches().isEmpty());
    }

    @Test
    void getTagHintShouldResolveAliasToTargetTag() {
        TagRepository tagRepository = mock(TagRepository.class);
        TagAliasRepository tagAliasRepository = mock(TagAliasRepository.class);
        TagContentRefRepository tagContentRefRepository = mock(TagContentRefRepository.class);
        when(tagRepository.getByName("节俗")).thenReturn(null);
        when(tagAliasRepository.getByName("节俗"))
                .thenReturn(
                        new TagAlias(TagAliasIdCodec.toDomain(21L), TagIdCodec.toDomain(7L), "节俗", TagSource.MANUAL));
        Tag targetTag = new Tag();
        targetTag.setId(TagIdCodec.toDomain(7L));
        targetTag.setName("民俗");
        when(tagRepository.getByTagId(TagIdCodec.toDomain(7L))).thenReturn(targetTag);
        when(tagContentRefRepository.countByTagId(TagIdCodec.toDomain(7L))).thenReturn(3);

        KnowledgeTaxonomyReadApplicationServiceImpl service = new KnowledgeTaxonomyReadApplicationServiceImpl(
                mock(SynonymRepository.class), tagRepository, tagAliasRepository, tagContentRefRepository);

        DiscoveryTagHintResult result = service.getTagHint("节俗");

        assertEquals("节俗", result.getNormalizedTerm());
        assertEquals("民俗", result.getMatchedTagName());
        assertEquals("节俗", result.getMatchedAliasName());
        assertEquals(3L, result.getContentRefCount());
    }

    @Test
    void listEntityHintsShouldReturnEmptyListForCurrentPhase() {
        KnowledgeTaxonomyReadApplicationServiceImpl service = new KnowledgeTaxonomyReadApplicationServiceImpl(
                mock(SynonymRepository.class),
                mock(TagRepository.class),
                mock(TagAliasRepository.class),
                mock(TagContentRefRepository.class));

        assertTrue(service.listEntityHints("礼制").isEmpty());
    }
}
