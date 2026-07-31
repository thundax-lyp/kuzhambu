package com.thundax.kuzhambu.operations.infra.health.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthTrendBucket;
import com.thundax.kuzhambu.operations.infra.health.persistence.dataobject.HealthCheckDO;
import com.thundax.kuzhambu.operations.infra.health.persistence.mapper.HealthCheckMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
        when(mapper.selectOne(any()))
                .thenAnswer(invocation -> dataObject(
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

        PageResult<HealthCheckRecord> result = repository.page(
                "cache",
                "DOWN",
                "HTTP",
                "internal/health",
                Instant.ofEpochMilli(1_718_000_000_000L),
                Instant.ofEpochMilli(1_718_086_400_000L),
                2,
                5);

        assertEquals(2, result.getPageNo());
        assertEquals(5, result.getPageSize());
        assertEquals(6, result.getTotalCount());
        assertEquals(2, result.getTotalPage());
        assertEquals(1, result.getRecords().size());
        assertEquals(9002L, result.getRecords().get(0).getId().value());
        assertEquals("cache", result.getRecords().get(0).getComponent());
        assertEquals("DOWN", result.getRecords().get(0).getHealthStatus());

        ArgumentCaptor<QueryWrapper<HealthCheckDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectPage(any(), captor.capture());
        String sqlSegment = captor.getValue().getCustomSqlSegment();
        assertTrue(sqlSegment.contains("component"));
        assertTrue(sqlSegment.contains("health_status"));
        assertTrue(sqlSegment.contains("probe_source"));
        assertTrue(sqlSegment.contains("probe_target"));
        assertTrue(sqlSegment.contains("checked_at"));
        assertTrue(sqlSegment.contains("ORDER BY checked_at DESC,check_id DESC"));
    }

    @Test
    void listTrendShouldMapHourlyBucketsAndCounts() {
        HealthCheckMapper mapper = mock(HealthCheckMapper.class);
        HealthCheckRepositoryImpl repository = new HealthCheckRepositoryImpl(mapper);
        when(mapper.selectMaps(any()))
                .thenReturn(List.of(Map.of(
                        "bucket",
                        "2026-07-06 10:00:00",
                        "upCount",
                        2L,
                        "degradedCount",
                        1L,
                        "downCount",
                        0L,
                        "avgLatencyMs",
                        new BigDecimal("12"))));

        List<HealthTrendBucket> result = repository.listTrend(
                "admin-server",
                "LOCAL",
                Instant.ofEpochMilli(1_718_000_000_000L),
                Instant.ofEpochMilli(1_718_086_400_000L),
                "HOUR");

        assertEquals(1, result.size());
        HealthTrendBucket bucket = result.get(0);
        assertEquals("2026-07-06 10:00:00", bucket.getBucket());
        assertEquals(2L, bucket.getUpCount());
        assertEquals(1L, bucket.getDegradedCount());
        assertEquals(0L, bucket.getDownCount());
        assertEquals(12L, bucket.getAvgLatencyMs());

        ArgumentCaptor<QueryWrapper<HealthCheckDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectMaps(captor.capture());
        String sqlSegment = captor.getValue().getCustomSqlSegment();
        assertTrue(sqlSegment.contains("component"));
        assertTrue(sqlSegment.contains("probe_source"));
        assertTrue(sqlSegment.contains("checked_at"));
        assertTrue(sqlSegment.contains("DATE_ADD('1970-01-01 00:00:00'"));
        assertTrue(sqlSegment.contains("checked_at DIV 1000"));
        assertTrue(sqlSegment.contains("+ 28800"));
        assertTrue(sqlSegment.contains("DATE_FORMAT("));
        assertTrue(sqlSegment.contains("%Y-%m-%d %H:00:00"));
    }

    @Test
    void listTrendShouldSupportDayBucketWithoutOptionalFilters() {
        HealthCheckMapper mapper = mock(HealthCheckMapper.class);
        HealthCheckRepositoryImpl repository = new HealthCheckRepositoryImpl(mapper);
        when(mapper.selectMaps(any()))
                .thenReturn(List.of(Map.of(
                        "bucket",
                        "2026-07-06",
                        "upCount",
                        1,
                        "degradedCount",
                        0,
                        "downCount",
                        1,
                        "avgLatencyMs",
                        8.4D)));

        List<HealthTrendBucket> result = repository.listTrend(null, null, null, null, "DAY");

        assertEquals(1, result.size());
        assertEquals("2026-07-06", result.get(0).getBucket());
        assertEquals(1L, result.get(0).getUpCount());
        assertEquals(0L, result.get(0).getDegradedCount());
        assertEquals(1L, result.get(0).getDownCount());
        assertEquals(8L, result.get(0).getAvgLatencyMs());

        ArgumentCaptor<QueryWrapper<HealthCheckDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectMaps(captor.capture());
        String sqlSegment = captor.getValue().getCustomSqlSegment();
        assertTrue(sqlSegment.contains("DATE_ADD('1970-01-01 00:00:00'"));
        assertTrue(sqlSegment.contains("checked_at DIV 1000"));
        assertTrue(sqlSegment.contains("+ 28800"));
        assertTrue(sqlSegment.contains("%Y-%m-%d"));
    }

    @Test
    void listExpiredCheckIdsShouldQueryChecksBeforeThreshold() {
        HealthCheckMapper mapper = mock(HealthCheckMapper.class);
        HealthCheckRepositoryImpl repository = new HealthCheckRepositoryImpl(mapper);
        when(mapper.selectObjs(any())).thenReturn(List.of(9001L, 9002L));

        List<com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId> result =
                repository.listExpiredCheckIds(Instant.ofEpochMilli(1_718_086_500_000L), 2);

        assertEquals(2, result.size());
        assertEquals(9001L, result.get(0).value());
        ArgumentCaptor<QueryWrapper<HealthCheckDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectObjs(captor.capture());
        String sqlSegment = captor.getValue().getCustomSqlSegment();
        assertTrue(sqlSegment.contains("checked_at"));
        assertTrue(sqlSegment.contains("LIMIT 2"));
    }

    private static HealthCheckDO dataObject(long checkId, String component, String status) {
        return new HealthCheckDO(
                null,
                checkId,
                component,
                status,
                5,
                "test-health",
                "LOCAL",
                component,
                "{\"component\":\"" + component + "\"}",
                Instant.ofEpochMilli(1_718_000_000_000L));
    }
}
