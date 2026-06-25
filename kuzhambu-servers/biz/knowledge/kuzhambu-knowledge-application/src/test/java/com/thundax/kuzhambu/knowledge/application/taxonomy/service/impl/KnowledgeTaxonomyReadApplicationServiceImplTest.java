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
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Synonym;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagAlias;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.SynonymStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.SynonymId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagAliasId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
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
                        List.of(new Synonym(SynonymId.of(1L), SynonymId.of(1L), "礼制", "礼学", SynonymStatus.ENABLED))));
        when(synonymRepository.page(isNull(), eq("礼制"), eq(SynonymStatus.ENABLED), eq(1), eq(50)))
                .thenReturn(PageResult.of(
                        1,
                        50,
                        1,
                        List.of(new Synonym(SynonymId.of(2L), SynonymId.of(2L), "典礼", "礼制", SynonymStatus.ENABLED))));

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
    void getTagHintShouldResolveAliasToTargetTag() {
        TagRepository tagRepository = mock(TagRepository.class);
        TagAliasRepository tagAliasRepository = mock(TagAliasRepository.class);
        TagContentRefRepository tagContentRefRepository = mock(TagContentRefRepository.class);
        when(tagRepository.getByName("节俗")).thenReturn(null);
        when(tagAliasRepository.getByName("节俗"))
                .thenReturn(new TagAlias(TagAliasId.of(21L), TagAliasId.of(21L), TagId.of(7L), "节俗", TagSource.MANUAL));
        Tag targetTag = new Tag();
        targetTag.setTagId(TagId.of(7L));
        targetTag.setName("民俗");
        when(tagRepository.getByTagId(TagId.of(7L))).thenReturn(targetTag);
        when(tagContentRefRepository.countByTagId(TagId.of(7L))).thenReturn(3);

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
