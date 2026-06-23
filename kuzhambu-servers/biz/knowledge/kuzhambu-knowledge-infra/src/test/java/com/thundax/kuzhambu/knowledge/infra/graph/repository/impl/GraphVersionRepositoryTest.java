package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphVersionDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphVersionMapper;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GraphVersionRepositoryTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findLatestShouldQueryByTaskTypeAndSourceScope() {
        GraphVersionMapper mapper = mock(GraphVersionMapper.class);
        when(mapper.selectOne(any())).thenReturn(new GraphVersionDO());
        GraphVersionRepositoryImpl repository = new GraphVersionRepositoryImpl(mapper);

        repository.findLatest("GRAPH", "SANCAI_ENTRY", 100L);

        ArgumentCaptor<QueryWrapper<GraphVersionDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectOne(captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("task_type"));
        assertTrue(sqlSegment.contains("source_content_type"));
        assertTrue(sqlSegment.contains("source_content_id"));
        assertTrue(sqlSegment.contains("version_no"));
    }

    @Test
    void saveShouldGenerateVersionIdAndMapScopeFields() {
        GraphVersionMapper mapper = mock(GraphVersionMapper.class);
        when(mapper.insert(any())).thenReturn(1);
        GraphVersionRepositoryImpl repository = new GraphVersionRepositoryImpl(mapper);
        GraphVersion version = new GraphVersion();
        version.setTaskId(GraphExtractionTaskId.of(12L));
        version.setCandidateId(34L);
        version.setTaskType("GRAPH");
        version.setScopeType("ENTRY");
        version.setScopeJson("{\"entryIds\":[1]}");
        version.setSourceContentType("SANCAI_ENTRY");
        version.setSourceContentId(100L);
        version.setVersionNo(2);
        version.setStatus("APPLIED");
        version.setAppliedAt(new Date());

        Long versionId = repository.save(version);

        assertNotNull(versionId);
        ArgumentCaptor<GraphVersionDO> captor = ArgumentCaptor.forClass(GraphVersionDO.class);
        verify(mapper).insert(captor.capture());
        assertEquals("ENTRY", captor.getValue().getScopeType());
        assertEquals("{\"entryIds\":[1]}", captor.getValue().getScopeJson());
        assertEquals(12L, captor.getValue().getTaskId());
    }
}
