package com.thundax.kuzhambu.operations.infra.health.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.health.codec.HealthAlertIdCodec;
import com.thundax.kuzhambu.operations.domain.health.codec.HealthCheckIdCodec;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthAlertRecord;
import com.thundax.kuzhambu.operations.infra.health.persistence.dataobject.HealthAlertDO;
import com.thundax.kuzhambu.operations.infra.health.persistence.mapper.HealthAlertMapper;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class HealthAlertRepositoryImplTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), HealthAlertDO.class);
    }

    @Test
    void findOpenBySourceShouldMatchOpenAlertAndLatestRecord() {
        HealthAlertMapper mapper = mock(HealthAlertMapper.class);
        HealthAlertRepositoryImpl repository = new HealthAlertRepositoryImpl(mapper);
        when(mapper.selectOne(any())).thenReturn(dataObject(9201L, "ACTIVE"));

        HealthAlertRecord result = repository.findOpenBySource("BACKUP", 9001L, "BACKUP_FAILED");

        assertNotNull(result);
        assertEquals(9201L, result.getId().value());
        assertEquals("database", result.getComponent());
        assertEquals("BACKUP_FAILED", result.getAlertType());
        ArgumentCaptor<LambdaQueryWrapper<HealthAlertDO>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(mapper).selectOne(captor.capture());
        String sqlSegment = captor.getValue().getCustomSqlSegment();
        assertTrue(sqlSegment.contains("source_ref_type"));
        assertTrue(sqlSegment.contains("source_ref_id"));
        assertTrue(sqlSegment.contains("alert_type"));
        assertTrue(sqlSegment.contains("alert_status"));
        assertTrue(sqlSegment.contains("last_triggered_at"));
        assertTrue(sqlSegment.contains("alert_id"));
    }

    @Test
    void findOpenBySourceShouldSupportNullSourceRefId() {
        HealthAlertMapper mapper = mock(HealthAlertMapper.class);
        HealthAlertRepositoryImpl repository = new HealthAlertRepositoryImpl(mapper);
        when(mapper.selectOne(any())).thenReturn(dataObject(9202L, "ACKED"));

        HealthAlertRecord result = repository.findOpenBySource("HEALTH", null, "HEALTH_STALE");

        assertNotNull(result);
        assertEquals("ACKED", result.getAlertStatus());
        ArgumentCaptor<LambdaQueryWrapper<HealthAlertDO>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(mapper).selectOne(captor.capture());
        assertTrue(captor.getValue().getCustomSqlSegment().contains("source_ref_id IS NULL"));
    }

    @Test
    void pageShouldMapRecordsAndApplyFiltersAndSorting() {
        HealthAlertMapper mapper = mock(HealthAlertMapper.class);
        HealthAlertRepositoryImpl repository = new HealthAlertRepositoryImpl(mapper);
        Page<HealthAlertDO> dataObjectPage = new Page<>(2, 5);
        dataObjectPage.setTotal(6);
        dataObjectPage.setRecords(List.of(dataObject(9203L, "ACTIVE")));
        when(mapper.selectPage(any(Page.class), any())).thenReturn(dataObjectPage);

        PageResult<HealthAlertRecord> result =
                repository.page("database", "CRITICAL", "ACTIVE", "BACKUP", 9001L, 9101L, 2, 5);

        assertEquals(2, result.getPageNo());
        assertEquals(5, result.getPageSize());
        assertEquals(6, result.getTotalCount());
        assertEquals(2, result.getTotalPage());
        assertEquals(1, result.getRecords().size());
        assertEquals(9203L, result.getRecords().get(0).getId().value());
        ArgumentCaptor<QueryWrapper<HealthAlertDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectPage(any(Page.class), captor.capture());
        String sqlSegment = captor.getValue().getCustomSqlSegment();
        assertTrue(sqlSegment.contains("component"));
        assertTrue(sqlSegment.contains("alert_level"));
        assertTrue(sqlSegment.contains("alert_status"));
        assertTrue(sqlSegment.contains("source_ref_type"));
        assertTrue(sqlSegment.contains("source_ref_id"));
        assertTrue(sqlSegment.contains("latest_check_id"));
        assertTrue(
                sqlSegment.contains("ORDER BY alert_status ASC,alert_level DESC,last_triggered_at DESC,alert_id DESC"));
    }

    @Test
    void insertShouldPersistAlertFields() {
        HealthAlertMapper mapper = mock(HealthAlertMapper.class);
        HealthAlertRepositoryImpl repository = new HealthAlertRepositoryImpl(mapper);

        repository.insert(domainRecord(9204L, "ACTIVE"));

        ArgumentCaptor<HealthAlertDO> captor = ArgumentCaptor.forClass(HealthAlertDO.class);
        verify(mapper).insert(captor.capture());
        HealthAlertDO dataObject = captor.getValue();
        assertEquals(9204L, dataObject.getAlertId());
        assertEquals("database", dataObject.getComponent());
        assertEquals("BACKUP_FAILED", dataObject.getAlertType());
        assertEquals("CRITICAL", dataObject.getAlertLevel());
        assertEquals("ACTIVE", dataObject.getAlertStatus());
        assertEquals(9101L, dataObject.getLatestCheckId());
        assertEquals("OPEN_BACKUP", dataObject.getRecoveryAction());
        assertEquals("/operations/backup-restore?backupId=9001", dataObject.getRecoveryTarget());
    }

    @Test
    void updateShouldPersistMutableAlertFields() {
        HealthAlertMapper mapper = mock(HealthAlertMapper.class);
        HealthAlertRepositoryImpl repository = new HealthAlertRepositoryImpl(mapper);

        repository.update(domainRecord(9205L, "RECOVERED"));

        ArgumentCaptor<LambdaUpdateWrapper<HealthAlertDO>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(mapper).update(isNull(), captor.capture());
        String sqlSet = captor.getValue().getSqlSet();
        assertTrue(sqlSet.contains("component"));
        assertTrue(sqlSet.contains("alert_type"));
        assertTrue(sqlSet.contains("alert_level"));
        assertTrue(sqlSet.contains("alert_status"));
        assertTrue(sqlSet.contains("source_ref_type"));
        assertTrue(sqlSet.contains("source_ref_id"));
        assertTrue(sqlSet.contains("latest_check_id"));
        assertTrue(sqlSet.contains("message"));
        assertTrue(sqlSet.contains("suggestion"));
        assertTrue(sqlSet.contains("recovery_action"));
        assertTrue(sqlSet.contains("recovery_target"));
        assertTrue(sqlSet.contains("acked_at"));
        assertTrue(sqlSet.contains("acked_by_user_id"));
        assertTrue(sqlSet.contains("recovered_at"));
        assertTrue(sqlSet.contains("failure_reason"));
    }

    private static HealthAlertRecord domainRecord(long alertId, String alertStatus) {
        return new HealthAlertRecord(
                HealthAlertIdCodec.toDomain(alertId),
                "database",
                "BACKUP_FAILED",
                "CRITICAL",
                alertStatus,
                "BACKUP",
                9001L,
                HealthCheckIdCodec.toDomain(9101L),
                "backup failed",
                "retry backup",
                "OPEN_BACKUP",
                "/operations/backup-restore?backupId=9001",
                Instant.ofEpochMilli(1_719_630_400_000L),
                Instant.ofEpochMilli(1_719_630_500_000L),
                Instant.ofEpochMilli(1_719_630_600_000L),
                1001L,
                Instant.ofEpochMilli(1_719_630_700_000L),
                "network timeout");
    }

    private static HealthAlertDO dataObject(long alertId, String alertStatus) {
        return new HealthAlertDO(
                null,
                alertId,
                "database",
                "BACKUP_FAILED",
                "CRITICAL",
                alertStatus,
                "BACKUP",
                9001L,
                9101L,
                "backup failed",
                "retry backup",
                "OPEN_BACKUP",
                "/operations/backup-restore?backupId=9001",
                Instant.ofEpochMilli(1_719_630_400_000L),
                Instant.ofEpochMilli(1_719_630_500_000L),
                null,
                null,
                null,
                "network timeout");
    }
}
