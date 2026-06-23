package com.thundax.kuzhambu.knowledge.infra.refinement.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementTask;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementTaskDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.RefinementTaskMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RefinementTaskRepositoryTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void pageShouldQueryByTaskTypeCategoryAndStatus() {
        RefinementTaskMapper mapper = mock(RefinementTaskMapper.class);
        Page<RefinementTaskDO> dataObjectPage = new Page<>(1, 10, 1);
        dataObjectPage.setRecords(List.of(new RefinementTaskDO()));
        when(mapper.selectPage(any(Page.class), any())).thenReturn(dataObjectPage);
        RefinementTaskRepositoryImpl repository = new RefinementTaskRepositoryImpl(mapper);

        PageResult<RefinementTask> page = repository.page("GRAPH", "SANCAI_ENTRY", null, "BIRDS", "DRAFT", 1, 10);

        ArgumentCaptor<QueryWrapper<RefinementTaskDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectPage(any(Page.class), captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("task_type"));
        assertTrue(sqlSegment.contains("source_content_type"));
        assertTrue(sqlSegment.contains("source_category_code"));
        assertTrue(sqlSegment.contains("status"));
        assertEquals(1, page.getRecords().size());
    }
}
