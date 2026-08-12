package com.thundax.kuzhambu.knowledge.infra.taxonomy.repository.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper.TagCategoryMapper;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper.TagMapper;
import org.junit.jupiter.api.Test;

class TagCategoryRepositoryImplTest {

    @Test
    void getByCategoryIdShouldAllowQueryWithoutStatus() {
        TagCategoryMapper mapper = mock(TagCategoryMapper.class);
        TagMapper tagMapper = mock(TagMapper.class);
        TagCategoryRepositoryImpl repository = new TagCategoryRepositoryImpl(mapper, tagMapper);

        assertDoesNotThrow(() -> repository.getByCategoryId(new TagCategoryId(1001L)));

        verify(mapper).selectOne(any(QueryWrapper.class));
        verifyNoInteractions(tagMapper);
    }

    @Test
    void countEnabledByCategoryIdShouldQueryTags() {
        TagCategoryMapper mapper = mock(TagCategoryMapper.class);
        TagMapper tagMapper = mock(TagMapper.class);
        TagCategoryRepositoryImpl repository = new TagCategoryRepositoryImpl(mapper, tagMapper);

        repository.countEnabledByCategoryId(new TagCategoryId(1001L));

        verify(tagMapper).selectCount(any(QueryWrapper.class));
        verifyNoInteractions(mapper);
    }
}
