package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionAiCandidateIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionSourceContentIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphVersionStatus;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphVersionDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphVersionMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GraphVersionRepositoryTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void pageShouldQueryByReadableFilters() {
        GraphVersionMapper mapper = mock(GraphVersionMapper.class);
        Page<GraphVersionDO> dataObjectPage = new Page<>(1, 10, 1);
        dataObjectPage.setRecords(List.of(new GraphVersionDO()));
        when(mapper.selectPage(any(Page.class), any())).thenReturn(dataObjectPage);
        GraphVersionRepositoryImpl repository = new GraphVersionRepositoryImpl(mapper);

        PageResult<GraphVersion> page = repository.page(
                GraphExtractionTaskType.GRAPH,
                GraphVersionStatus.APPLIED,
                "SANCAI_ENTRY",
                GraphExtractionSourceContentIdCodec.toDomain(100L),
                1,
                10);

        ArgumentCaptor<QueryWrapper<GraphVersionDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectPage(any(Page.class), captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("task_type"));
        assertTrue(sqlSegment.contains("status"));
        assertTrue(sqlSegment.contains("source_content_type"));
        assertTrue(sqlSegment.contains("source_content_id"));
        assertEquals(1, page.getRecords().size());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getByVersionIdShouldQueryPrimaryKey() {
        GraphVersionMapper mapper = mock(GraphVersionMapper.class);
        when(mapper.selectOne(any())).thenReturn(new GraphVersionDO());
        GraphVersionRepositoryImpl repository = new GraphVersionRepositoryImpl(mapper);

        repository.getByVersionId(GraphVersionIdCodec.toDomain(66L));

        ArgumentCaptor<QueryWrapper<GraphVersionDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectOne(captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("id"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findLatestShouldQueryByTaskTypeAndSourceScope() {
        GraphVersionMapper mapper = mock(GraphVersionMapper.class);
        when(mapper.selectOne(any())).thenReturn(new GraphVersionDO());
        GraphVersionRepositoryImpl repository = new GraphVersionRepositoryImpl(mapper);

        repository.getByLatestSource(
                GraphExtractionTaskType.GRAPH, "SANCAI_ENTRY", GraphExtractionSourceContentIdCodec.toDomain(100L));

        ArgumentCaptor<QueryWrapper<GraphVersionDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectOne(captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("task_type"));
        assertTrue(sqlSegment.contains("source_content_type"));
        assertTrue(sqlSegment.contains("source_content_id"));
        assertTrue(sqlSegment.contains("version_no"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getByLatestAppliedCategoryCodeShouldQueryAppliedCategoryScope() {
        GraphVersionMapper mapper = mock(GraphVersionMapper.class);
        when(mapper.selectOne(any())).thenReturn(new GraphVersionDO());
        GraphVersionRepositoryImpl repository = new GraphVersionRepositoryImpl(mapper);

        repository.getByLatestAppliedCategoryCode("BIRDS");

        ArgumentCaptor<QueryWrapper<GraphVersionDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper, times(1)).selectOne(captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("status"));
        assertTrue(sqlSegment.contains("source_category_code"));
        assertTrue(sqlSegment.contains("version_no"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void listAppliedByCategoryCodeShouldQueryAppliedList() {
        GraphVersionMapper mapper = mock(GraphVersionMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of(new GraphVersionDO()));
        GraphVersionRepositoryImpl repository = new GraphVersionRepositoryImpl(mapper);

        List<GraphVersion> result = repository.listAppliedByCategoryCode("BIRDS");

        ArgumentCaptor<QueryWrapper<GraphVersionDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectList(captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("status"));
        assertTrue(sqlSegment.contains("source_category_code"));
        assertEquals(1, result.size());
    }

    @Test
    void saveShouldGenerateVersionIdAndMapScopeFields() {
        GraphVersionMapper mapper = mock(GraphVersionMapper.class);
        when(mapper.insert(any(GraphVersionDO.class))).thenAnswer(invocation -> {
            GraphVersionDO dataObject = invocation.getArgument(0);
            dataObject.setId(1001L);
            return 1;
        });
        GraphVersionRepositoryImpl repository = new GraphVersionRepositoryImpl(mapper);
        GraphVersion version = new GraphVersion();
        version.setTaskId(GraphExtractionTaskIdCodec.toDomain(12L));
        version.setCandidateId(GraphExtractionAiCandidateIdCodec.toDomain(34L));
        version.setTaskType(GraphExtractionTaskType.GRAPH);
        version.setScopeType("ENTRY");
        version.setScopeJson("{\"entryIds\":[1]}");
        version.setSourceContentType("SANCAI_ENTRY");
        version.setSourceContentId(GraphExtractionSourceContentIdCodec.toDomain(100L));
        version.setSourceCategoryCode("BIRDS");
        version.setSourceCategoryName("羽族");
        version.setVersionNo(2);
        version.setStatus(GraphVersionStatus.APPLIED);
        version.setAppliedAt(Instant.now());

        Long versionId = GraphVersionIdCodec.toValue(repository.save(version));

        assertNotNull(versionId);
        ArgumentCaptor<GraphVersionDO> captor = ArgumentCaptor.forClass(GraphVersionDO.class);
        verify(mapper).insert(captor.capture());
        assertEquals("ENTRY", captor.getValue().getScopeType());
        assertEquals("{\"entryIds\":[1]}", captor.getValue().getScopeJson());
        assertEquals(12L, captor.getValue().getTaskId());
        assertEquals("BIRDS", captor.getValue().getSourceCategoryCode());
        assertEquals("羽族", captor.getValue().getSourceCategoryName());
    }
}
