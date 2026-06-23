package com.thundax.kuzhambu.knowledge.domain.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagAlias;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagCategory;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagContentRef;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.ContentType;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagCategoryStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagReviewStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagAliasId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagContentRefId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagAliasRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagCategoryRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagContentRefRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeTagBindingDomainServiceTest {

    @Test
    void resolveTagByNameOrAliasShouldReturnAliasTarget() {
        TagRepository tagRepository = mock(TagRepository.class);
        TagAliasRepository tagAliasRepository = mock(TagAliasRepository.class);
        KnowledgeTagBindingDomainServiceImpl service = service(tagRepository, tagAliasRepository, null, null);
        TagAlias alias = new TagAlias();
        alias.setId(TagAliasId.of(1L));
        alias.setTagId(TagId.of(1001L));
        alias.setName("礼制");
        Tag tag = enabledTag(1001L, "礼制");
        when(tagRepository.getByName("礼制")).thenReturn(null);
        when(tagAliasRepository.getByName("礼制")).thenReturn(alias);
        when(tagRepository.getByTagId(TagId.of(1001L))).thenReturn(tag);

        Tag result = service.resolveTagByNameOrAlias("  礼制 ");

        assertSame(tag, result);
    }

    @Test
    void resolveTagByNameOrAliasShouldRejectDisabledTag() {
        TagRepository tagRepository = mock(TagRepository.class);
        TagAliasRepository tagAliasRepository = mock(TagAliasRepository.class);
        KnowledgeTagBindingDomainServiceImpl service = service(tagRepository, tagAliasRepository, null, null);
        Tag tag = enabledTag(1001L, "礼制");
        tag.setStatus(TagStatus.DISABLED);
        when(tagRepository.getByName("礼制")).thenReturn(tag);

        assertThrows(DomainException.class, () -> service.resolveTagByNameOrAlias("礼制"));
    }

    @Test
    void resolveTagByNameOrAliasShouldFollowMergedDirectTag() {
        TagRepository tagRepository = mock(TagRepository.class);
        TagAliasRepository tagAliasRepository = mock(TagAliasRepository.class);
        KnowledgeTagBindingDomainServiceImpl service = service(tagRepository, tagAliasRepository, null, null);
        Tag mergedSource = enabledTag(1001L, "礼制");
        mergedSource.setMergedToTagId(TagId.of(1002L));
        Tag mergedTarget = enabledTag(1002L, "吉礼");
        when(tagRepository.getByName("礼制")).thenReturn(mergedSource);
        when(tagRepository.getByTagId(TagId.of(1002L))).thenReturn(mergedTarget);

        Tag result = service.resolveTagByNameOrAlias("礼制");

        assertSame(mergedTarget, result);
    }

    @Test
    void resolveTagByNameOrAliasShouldFollowMergedAliasTarget() {
        TagRepository tagRepository = mock(TagRepository.class);
        TagAliasRepository tagAliasRepository = mock(TagAliasRepository.class);
        KnowledgeTagBindingDomainServiceImpl service = service(tagRepository, tagAliasRepository, null, null);
        TagAlias alias = new TagAlias();
        alias.setId(TagAliasId.of(2L));
        alias.setTagId(TagId.of(1001L));
        alias.setName("旧别名");
        Tag mergedSource = enabledTag(1001L, "旧礼制");
        mergedSource.setMergedToTagId(TagId.of(1002L));
        Tag mergedTarget = enabledTag(1002L, "新礼制");
        when(tagRepository.getByName("旧别名")).thenReturn(null);
        when(tagAliasRepository.getByName("旧别名")).thenReturn(alias);
        when(tagRepository.getByTagId(TagId.of(1001L))).thenReturn(mergedSource);
        when(tagRepository.getByTagId(TagId.of(1002L))).thenReturn(mergedTarget);

        Tag result = service.resolveTagByNameOrAlias("旧别名");

        assertSame(mergedTarget, result);
    }

    @Test
    void resolveOrCreateManualTagShouldCreateApprovedManualTagInDefaultCategory() {
        TagRepository tagRepository = mock(TagRepository.class);
        TagCategoryRepository tagCategoryRepository = mock(TagCategoryRepository.class);
        KnowledgeTagBindingDomainServiceImpl service =
                service(tagRepository, mock(TagAliasRepository.class), tagCategoryRepository, null);
        when(tagRepository.getByName("礼制")).thenReturn(null);
        when(tagCategoryRepository.getByCategoryId(TagCategoryId.of(1999L))).thenReturn(enabledCategory());
        when(tagRepository.insert(any(Tag.class))).thenReturn(TagId.of(9001L));
        Tag created = enabledTag(9001L, "礼制");
        created.setSource(TagSource.MANUAL);
        created.setReviewStatus(TagReviewStatus.APPROVED);
        when(tagRepository.getByTagId(TagId.of(9001L))).thenReturn(created);

        Tag result = service.resolveOrCreateManualTag(" 礼制 ");

        assertSame(created, result);
        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).insert(captor.capture());
        Tag inserted = captor.getValue();
        assertEquals("礼制", inserted.getName());
        assertEquals(TagCategoryId.of(1999L), inserted.getCategoryId());
        assertEquals(TagStatus.ENABLED, inserted.getStatus());
        assertEquals(TagSource.MANUAL, inserted.getSource());
        assertEquals(TagReviewStatus.APPROVED, inserted.getReviewStatus());
        assertNotNull(inserted.getReviewedAt());
    }

    @Test
    void resolveOrCreateAiTagShouldCreatePendingAiExtractedTag() {
        TagRepository tagRepository = mock(TagRepository.class);
        TagCategoryRepository tagCategoryRepository = mock(TagCategoryRepository.class);
        KnowledgeTagBindingDomainServiceImpl service =
                service(tagRepository, mock(TagAliasRepository.class), tagCategoryRepository, null);
        when(tagRepository.getByName("祭祀")).thenReturn(null);
        when(tagCategoryRepository.getByCategoryId(TagCategoryId.of(1999L))).thenReturn(enabledCategory());
        when(tagRepository.insert(any(Tag.class))).thenReturn(TagId.of(9002L));
        Tag created = enabledTag(9002L, "祭祀");
        created.setSource(TagSource.AI_EXTRACTED);
        created.setReviewStatus(TagReviewStatus.PENDING);
        when(tagRepository.getByTagId(TagId.of(9002L))).thenReturn(created);

        Tag result = service.resolveOrCreateAiTag("祭祀");

        assertSame(created, result);
        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).insert(captor.capture());
        Tag inserted = captor.getValue();
        assertEquals(TagSource.AI_EXTRACTED, inserted.getSource());
        assertEquals(TagReviewStatus.PENDING, inserted.getReviewStatus());
        assertEquals(null, inserted.getReviewedAt());
    }

    @Test
    void syncContentTagRefShouldInsertProjectedRefWithFallbackTitleAndDefaultSource() {
        TagContentRefRepository tagContentRefRepository = mock(TagContentRefRepository.class);
        KnowledgeTagBindingDomainServiceImpl service = service(
                mock(TagRepository.class),
                mock(TagAliasRepository.class),
                mock(TagCategoryRepository.class),
                tagContentRefRepository);
        when(tagContentRefRepository.countByTagAndContentTypeAndContentId(
                        TagId.of(1001L), ContentType.SANCAI_ENTRY, 88L, null))
                .thenReturn(0);

        service.syncContentTagRef(TagId.of(1001L), ContentType.SANCAI_ENTRY, 88L, "  ", null);

        ArgumentCaptor<TagContentRef> captor = ArgumentCaptor.forClass(TagContentRef.class);
        verify(tagContentRefRepository).insert(captor.capture());
        TagContentRef inserted = captor.getValue();
        assertNotNull(inserted.getRefId());
        assertEquals(TagId.of(1001L), inserted.getTagId());
        assertEquals(ContentType.SANCAI_ENTRY, inserted.getContentType());
        assertEquals(88L, inserted.getContentId());
        assertEquals("SANCAI_ENTRY:88", inserted.getContentTitle());
        assertEquals(TagSource.MANUAL, inserted.getSource());
    }

    @Test
    void removeContentTagRefShouldDeleteOnlyMatchingProjectedRefs() {
        TagContentRefRepository tagContentRefRepository = mock(TagContentRefRepository.class);
        KnowledgeTagBindingDomainServiceImpl service = service(
                mock(TagRepository.class),
                mock(TagAliasRepository.class),
                mock(TagCategoryRepository.class),
                tagContentRefRepository);
        TagContentRef matched = new TagContentRef();
        matched.setId(TagContentRefId.of(1L));
        matched.setTagId(TagId.of(1001L));
        matched.setContentType(ContentType.SANCAI_ENTRY);
        matched.setContentId(88L);
        TagContentRef differentContent = new TagContentRef();
        differentContent.setId(TagContentRefId.of(2L));
        differentContent.setTagId(TagId.of(1001L));
        differentContent.setContentType(ContentType.WANGQI_DOCUMENT);
        differentContent.setContentId(88L);
        TagContentRef missingId = new TagContentRef();
        missingId.setId(null);
        missingId.setTagId(TagId.of(1001L));
        missingId.setContentType(ContentType.SANCAI_ENTRY);
        missingId.setContentId(88L);
        when(tagContentRefRepository.listByTagId(TagId.of(1001L)))
                .thenReturn(List.of(matched, differentContent, missingId));

        service.removeContentTagRef(TagId.of(1001L), ContentType.SANCAI_ENTRY, 88L);

        verify(tagContentRefRepository).deleteById(TagContentRefId.of(1L));
        verify(tagContentRefRepository, never()).deleteById(TagContentRefId.of(2L));
    }

    private static KnowledgeTagBindingDomainServiceImpl service(
            TagRepository tagRepository,
            TagAliasRepository tagAliasRepository,
            TagCategoryRepository tagCategoryRepository,
            TagContentRefRepository tagContentRefRepository) {
        return new KnowledgeTagBindingDomainServiceImpl(
                tagRepository,
                tagAliasRepository == null ? mock(TagAliasRepository.class) : tagAliasRepository,
                tagCategoryRepository == null ? mock(TagCategoryRepository.class) : tagCategoryRepository,
                tagContentRefRepository == null ? mock(TagContentRefRepository.class) : tagContentRefRepository);
    }

    private static Tag enabledTag(Long tagId, String name) {
        Tag tag = new Tag();
        tag.setTagId(TagId.of(tagId));
        tag.setName(name);
        tag.setStatus(TagStatus.ENABLED);
        return tag;
    }

    private static TagCategory enabledCategory() {
        TagCategory category = new TagCategory();
        category.setCategoryId(TagCategoryId.of(1999L));
        category.setStatus(TagCategoryStatus.ENABLED);
        return category;
    }
}
