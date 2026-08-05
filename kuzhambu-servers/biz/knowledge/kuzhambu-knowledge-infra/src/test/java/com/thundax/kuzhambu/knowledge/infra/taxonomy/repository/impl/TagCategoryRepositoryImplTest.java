package com.thundax.kuzhambu.knowledge.infra.taxonomy.repository.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper.TagCategoryMapper;
import org.junit.jupiter.api.Test;

class TagCategoryRepositoryImplTest {

    @Test
    void getByCategoryIdShouldAllowQueryWithoutStatus() {
        TagCategoryMapper mapper = mock(TagCategoryMapper.class);
        TagCategoryRepositoryImpl repository = new TagCategoryRepositoryImpl(mapper);

        assertDoesNotThrow(() -> repository.getByCategoryId(new TagCategoryId(1001L)));

        verify(mapper).selectOne(any(QueryWrapper.class));
    }
}
