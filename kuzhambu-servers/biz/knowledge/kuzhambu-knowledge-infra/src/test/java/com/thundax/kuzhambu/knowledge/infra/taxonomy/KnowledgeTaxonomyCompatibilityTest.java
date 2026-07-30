package com.thundax.kuzhambu.knowledge.infra.taxonomy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagCategoryIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.ContentType;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagReviewStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagDO;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper.TagMapper;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.repository.impl.TagRepositoryImpl;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeTaxonomyCompatibilityTest {

    @Test
    void enumsShouldAcceptLegacyClassicsValues() {
        assertEquals(ContentType.MING_CUSTOM, ContentType.from("MING_CUSTOMS"));
        assertEquals(TagSource.AI_EXTRACTED, TagSource.from("AI"));
    }

    @Test
    void insertShouldNormalizeLegacyAiSourceBeforePersist() {
        TagMapper mapper = mock(TagMapper.class);
        TagRepositoryImpl repository = new TagRepositoryImpl(mapper);
        Tag tag = new Tag();
        tag.setId(TagIdCodec.toDomain(100L));
        tag.setId(TagIdCodec.toDomain(1001L));
        tag.setName("祭祀");
        tag.setCategoryId(TagCategoryIdCodec.toDomain(1999L));
        tag.setStatus(TagStatus.ENABLED);
        tag.setSource(TagSource.from("AI"));
        tag.setReviewStatus(TagReviewStatus.PENDING);

        repository.insert(tag);

        ArgumentCaptor<TagDO> captor = ArgumentCaptor.forClass(TagDO.class);
        verify(mapper).insert(captor.capture());
        assertEquals("AI_EXTRACTED", captor.getValue().getSource());
    }

    @Test
    void getByNameShouldAllowEmptyOptionalFilters() {
        TagMapper mapper = mock(TagMapper.class);
        TagRepositoryImpl repository = new TagRepositoryImpl(mapper);

        Tag tag = repository.getByName("礼制");

        assertNull(tag);
        verify(mapper).selectOne(any());
    }

    @Test
    void normalizeSourceValueShouldMapLegacyAiString() throws Exception {
        TagRepositoryImpl repository = new TagRepositoryImpl(mock(TagMapper.class));
        Method method = TagRepositoryImpl.class.getDeclaredMethod("normalizeSourceValue", String.class);
        method.setAccessible(true);

        assertEquals("AI_EXTRACTED", method.invoke(repository, "AI"));
        assertEquals("MANUAL", method.invoke(repository, "MANUAL"));
    }
}
