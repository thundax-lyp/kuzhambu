package com.thundax.kuzhambu.knowledge.application.taxonomy.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.application.taxonomy.query.DiscoveryEntityHintQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.DiscoveryTagHintQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryTagHintResult;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagAliasIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagAlias;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagAliasRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagContentRefRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import org.junit.jupiter.api.Test;

class KnowledgeTaxonomyReadApplicationServiceImplTest {

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
                tagRepository, tagAliasRepository, tagContentRefRepository);

        DiscoveryTagHintResult result = service.getTagHint(new DiscoveryTagHintQuery("节俗"));

        assertEquals("节俗", result.getNormalizedTerm());
        assertEquals("民俗", result.getMatchedTagName());
        assertEquals("节俗", result.getMatchedAliasName());
        assertEquals(3L, result.getContentRefCount());
    }

    @Test
    void listEntityHintsShouldReturnEmptyListForCurrentPhase() {
        KnowledgeTaxonomyReadApplicationServiceImpl service = new KnowledgeTaxonomyReadApplicationServiceImpl(
                mock(TagRepository.class), mock(TagAliasRepository.class), mock(TagContentRefRepository.class));

        assertTrue(service.listEntityHints(new DiscoveryEntityHintQuery("礼制")).isEmpty());
    }
}
