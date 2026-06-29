package com.thundax.kuzhambu.operations.infra.health.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.infra.health.persistence.dataobject.HealthCheckDO;
import com.thundax.kuzhambu.operations.infra.health.persistence.mapper.HealthCheckMapper;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class HealthCheckRepositoryImplTest {

    @Test
    void getByIdShouldMapIdAndComponent() {
        HealthCheckMapper mapper = mock(HealthCheckMapper.class);
        HealthCheckRepositoryImpl repository = new HealthCheckRepositoryImpl(mapper);
        when(mapper.selectOne(any())).thenReturn(dataObject(9001L, "database", "UP"));

        HealthCheckRecord result = repository.getById(null);

        assertNotNull(result);
        assertEquals(9001L, result.getId().value());
        assertEquals("database", result.getComponent());
    }

    @Test
    void listLatestByComponentShouldReturnOneLatestRecordPerComponent() {
        HealthCheckMapper mapper = mock(HealthCheckMapper.class);
        HealthCheckRepositoryImpl repository = new HealthCheckRepositoryImpl(mapper);
        when(mapper.selectObjs(any())).thenReturn(List.of("cache", "database"));

        AtomicInteger invokeIndex = new AtomicInteger(0);
        when(mapper.selectOne(any())).thenAnswer(invocation ->
                dataObject(
                        1001L + invokeIndex.getAndIncrement(),
                        switch (invokeIndex.get()) {
                            case 1 -> "cache";
                            default -> "database";
                        },
                        "UP"));

        List<HealthCheckRecord> result = repository.listLatestByComponent();

        assertEquals(2, result.size());
        assertEquals("cache", result.get(0).getComponent());
        assertEquals(1001L, result.get(0).getId().value());
        assertEquals("database", result.get(1).getComponent());
        assertEquals(1002L, result.get(1).getId().value());
    }

    @Test
    void pageShouldMapRecordsAndTotalCount() {
        HealthCheckMapper mapper = mock(HealthCheckMapper.class);
        HealthCheckRepositoryImpl repository = new HealthCheckRepositoryImpl(mapper);
        Page<HealthCheckDO> dataObjectPage = new Page<>(2, 5);
        dataObjectPage.setTotal(6);
        dataObjectPage.setRecords(List.of(dataObject(9002L, "cache", "DOWN")));
        when(mapper.selectPage(any(), any())).thenReturn(dataObjectPage);

        PageResult<HealthCheckRecord> result = repository.page("cache", "DOWN", 2, 5);

        assertEquals(2, result.getPageNo());
        assertEquals(5, result.getPageSize());
        assertEquals(6, result.getTotalCount());
        assertEquals(2, result.getTotalPage());
        assertEquals(1, result.getRecords().size());
        assertEquals(9002L, result.getRecords().get(0).getId().value());
        assertEquals("cache", result.getRecords().get(0).getComponent());
        assertEquals("DOWN", result.getRecords().get(0).getHealthStatus());
    }

    private static HealthCheckDO dataObject(long checkId, String component, String status) {
        return new HealthCheckDO(
                null,
                checkId,
                component,
                status,
                5,
                "test-health",
                new Date(1_718_000_000_000L));
    }
}
