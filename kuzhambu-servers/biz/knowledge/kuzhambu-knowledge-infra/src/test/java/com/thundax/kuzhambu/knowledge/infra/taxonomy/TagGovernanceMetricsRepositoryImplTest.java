package com.thundax.kuzhambu.knowledge.infra.taxonomy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.readmodel.TagGovernanceMetrics;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagCategoryDO;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagContentRefDO;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagDO;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper.TagCategoryMapper;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper.TagContentRefMapper;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper.TagMapper;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.repository.impl.TagGovernanceMetricsRepositoryImpl;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class TagGovernanceMetricsRepositoryImplTest {

    @Test
    void getMetricsShouldAggregateActiveTagsAndApprovedTrend() {
        TagMapper tagMapper = mock(TagMapper.class);
        TagCategoryMapper categoryMapper = mock(TagCategoryMapper.class);
        TagContentRefMapper contentRefMapper = mock(TagContentRefMapper.class);
        TagGovernanceMetricsRepositoryImpl repository =
                new TagGovernanceMetricsRepositoryImpl(tagMapper, categoryMapper, contentRefMapper);

        TagDO activeManual = new TagDO();
        activeManual.setId(1L);
        activeManual.setTagId(1001L);
        activeManual.setName("礼制");
        activeManual.setCategoryId(11L);
        activeManual.setStatus("ENABLED");
        activeManual.setSource("MANUAL");
        activeManual.setReviewStatus("APPROVED");
        activeManual.setCreatedAt(Instant.ofEpochMilli(1735689600000L));
        activeManual.setReviewedAt(Instant.ofEpochMilli(1735689600000L));
        TagDO activeAi = new TagDO();
        activeAi.setId(2L);
        activeAi.setTagId(1002L);
        activeAi.setName("祭祀");
        activeAi.setCategoryId(11L);
        activeAi.setStatus("ENABLED");
        activeAi.setSource("AI_EXTRACTED");
        activeAi.setReviewStatus("APPROVED");
        activeAi.setCreatedAt(Instant.ofEpochMilli(1738368000000L));
        activeAi.setReviewedAt(Instant.ofEpochMilli(1738368000000L));
        TagDO deprecated = new TagDO();
        deprecated.setId(3L);
        deprecated.setTagId(1003L);
        deprecated.setName("旧礼");
        deprecated.setCategoryId(12L);
        deprecated.setStatus("DISABLED");
        deprecated.setSource("MANUAL");
        deprecated.setReviewStatus("APPROVED");
        deprecated.setCreatedAt(Instant.ofEpochMilli(1740787200000L));
        deprecated.setReviewedAt(Instant.ofEpochMilli(1740787200000L));
        deprecated.setDeprecatedAt(Instant.ofEpochMilli(1740787200000L));

        when(tagMapper.selectList(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(activeManual, activeAi), List.of(activeManual, activeAi, deprecated));

        TagCategoryDO category = new TagCategoryDO();
        category.setId(11L);
        category.setCategoryId(11L);
        category.setName("礼学");
        when(categoryMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(category));

        TagContentRefDO ref1 = new TagContentRefDO();
        ref1.setId(1L);
        ref1.setRefId(1L);
        ref1.setTagId(1001L);
        TagContentRefDO ref2 = new TagContentRefDO();
        ref2.setId(2L);
        ref2.setRefId(2L);
        ref2.setTagId(1001L);
        TagContentRefDO ref3 = new TagContentRefDO();
        ref3.setId(3L);
        ref3.setRefId(3L);
        ref3.setTagId(1002L);
        when(contentRefMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(ref1, ref2, ref3));

        TagGovernanceMetrics result = repository.getMetrics(10, 6);

        assertEquals("礼制", result.getTopTags().get(0).getTagName());
        assertEquals(2L, result.getTopTags().get(0).getContentRefCount());
        assertEquals("礼学", result.getCategoryDistributions().get(0).getCategoryName());
        assertEquals(2L, result.getCategoryDistributions().get(0).getTagCount());
        assertEquals(TagSource.MANUAL, result.getSourceRatios().get(0).getSource());
        assertEquals("2025-01", result.getMonthlyNewTags().get(0).getMonth());
        assertEquals("2025-02", result.getMonthlyNewTags().get(1).getMonth());
        assertEquals("2025-03", result.getMonthlyNewTags().get(2).getMonth());
    }
}
