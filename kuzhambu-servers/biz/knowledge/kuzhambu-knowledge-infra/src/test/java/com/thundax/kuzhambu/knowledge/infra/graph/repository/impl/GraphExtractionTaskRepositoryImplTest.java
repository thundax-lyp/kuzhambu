package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionBatchJobIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionModelIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionModelNameCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionRequestIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionSourceContentIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTraceIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphExtractionTaskDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphExtractionTaskMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphExtractionTaskRepositoryImplTest {

    @Test
    void getByTaskIdShouldMapReplayableSnapshotFields() {
        GraphExtractionTaskMapper mapper = mock(GraphExtractionTaskMapper.class);
        GraphExtractionTaskRepositoryImpl repository = new GraphExtractionTaskRepositoryImpl(mapper);
        when(mapper.selectOne(any())).thenReturn(dataObject(9001L));

        GraphExtractionTask result = repository.getByTaskId(GraphExtractionTaskIdCodec.toDomain(9001L));

        assertNotNull(result);
        assertEquals(9001L, result.getId().value());
        assertEquals(5001L, GraphExtractionModelIdCodec.toValue(result.getModelId()));
        assertEquals("gpt-5.5", GraphExtractionModelNameCodec.toValue(result.getModelName()));
        assertEquals("req-1", GraphExtractionRequestIdCodec.toValue(result.getRequestId()));
        assertEquals("trace-1", GraphExtractionTraceIdCodec.toValue(result.getTraceId()));
        assertEquals("[{\"role\":\"system\",\"content\":\"extract\"}]", result.getPromptMessagesJson());
        assertEquals("{\"content\":\"天地玄黄\"}", result.getInputPayloadJson());
        assertEquals(Boolean.TRUE, result.getForceJson());
    }

    @Test
    void pageShouldMapBatchAndTriggerFields() {
        GraphExtractionTaskMapper mapper = mock(GraphExtractionTaskMapper.class);
        GraphExtractionTaskRepositoryImpl repository = new GraphExtractionTaskRepositoryImpl(mapper);
        Page<GraphExtractionTaskDO> dataObjectPage = new Page<>(1, 10);
        dataObjectPage.setTotal(1);
        dataObjectPage.setRecords(List.of(dataObject(9001L)));
        when(mapper.selectPage(any(Page.class), any())).thenReturn(dataObjectPage);

        PageResult<GraphExtractionTask> result = repository.page(
                "GRAPH",
                GraphExtractionBatchJobIdCodec.toDomain(1001L),
                "QUALITY_REPORT",
                "SUCCEEDED",
                "SANCAI_ENTRY",
                GraphExtractionSourceContentIdCodec.toDomain(1001L),
                1,
                10);

        assertEquals(1, result.getRecords().size());
        assertEquals(
                Long.valueOf(1001L),
                GraphExtractionBatchJobIdCodec.toValue(
                        result.getRecords().get(0).getBatchJobId()));
        assertEquals("QUALITY_REPORT", result.getRecords().get(0).getTriggerSource());
    }

    private static GraphExtractionTaskDO dataObject(long taskId) {
        return new GraphExtractionTaskDO(
                taskId,
                1001L,
                "GRAPH",
                "CLASSICS_ENTRY",
                "{\"entryId\":1001}",
                "QUALITY_REPORT",
                "{\"sourceContentIds\":[1001,1002]}",
                Boolean.TRUE,
                7001L,
                "SANCAI_ENTRY",
                1001L,
                5001L,
                "gpt-5.5",
                61L,
                "req-1",
                "trace-1",
                "[{\"role\":\"system\",\"content\":\"extract\"}]",
                "{\"locale\":\"zh-CN\"}",
                "hash-1",
                "{\"content\":\"天地玄黄\"}",
                "{\"type\":\"object\"}",
                Boolean.TRUE,
                "zh-CN",
                3001L,
                3002L,
                "SUCCEEDED",
                null,
                null,
                99L,
                Instant.ofEpochMilli(1_718_000_000_000L),
                Instant.ofEpochMilli(1_718_000_100_000L),
                null);
    }
}
