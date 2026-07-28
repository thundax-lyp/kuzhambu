package com.thundax.kuzhambu.operations.infra.restore.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.restore.codec.RestoreIdCodec;
import com.thundax.kuzhambu.operations.domain.restore.model.entity.RestoreRecord;
import com.thundax.kuzhambu.operations.domain.restore.model.enums.RestoreMode;
import com.thundax.kuzhambu.operations.domain.restore.model.enums.RestoreStatus;
import com.thundax.kuzhambu.operations.infra.restore.persistence.dataobject.RestoreDO;
import com.thundax.kuzhambu.operations.infra.restore.persistence.mapper.RestoreMapper;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RestoreRepositoryImplTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), RestoreDO.class);
    }

    @Test
    void getByIdShouldMapRestoreModeAndWriteBlockTimes() {
        RestoreMapper mapper = mock(RestoreMapper.class);
        RestoreRepositoryImpl repository = new RestoreRepositoryImpl(mapper);
        when(mapper.selectOne(any())).thenReturn(dataObject(9101L, RestoreMode.DRILL.value()));

        RestoreRecord result = repository.getById(RestoreIdCodec.toDomain(9101L));

        assertNotNull(result);
        assertEquals(9101L, result.getId().value());
        assertEquals(RestoreMode.DRILL.value(), result.getRestoreMode());
        assertEquals(new Date(1_719_630_410_000L), result.getWriteBlockStartedAt());
        assertEquals(new Date(1_719_630_490_000L), result.getWriteBlockReleasedAt());
    }

    @Test
    void insertShouldPersistRestoreModeAndWriteBlockTimes() {
        RestoreMapper mapper = mock(RestoreMapper.class);
        RestoreRepositoryImpl repository = new RestoreRepositoryImpl(mapper);
        RestoreRecord record = domainRecord(RestoreMode.DRILL.value());

        repository.insert(record);

        ArgumentCaptor<RestoreDO> captor = ArgumentCaptor.forClass(RestoreDO.class);
        verify(mapper).insert(captor.capture());
        RestoreDO dataObject = captor.getValue();
        assertEquals(RestoreMode.DRILL.value(), dataObject.getRestoreMode());
        assertEquals(record.getWriteBlockStartedAt(), dataObject.getWriteBlockStartedAt());
        assertEquals(record.getWriteBlockReleasedAt(), dataObject.getWriteBlockReleasedAt());
    }

    @Test
    void updateShouldPersistRestoreModeAndWriteBlockTimes() {
        RestoreMapper mapper = mock(RestoreMapper.class);
        RestoreRepositoryImpl repository = new RestoreRepositoryImpl(mapper);

        repository.update(domainRecord(RestoreMode.REAL.value()));

        ArgumentCaptor<LambdaUpdateWrapper<RestoreDO>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(mapper).update(isNull(), captor.capture());
        String sqlSet = captor.getValue().getSqlSet();
        assertTrue(sqlSet.contains("restore_mode"));
        assertTrue(sqlSet.contains("write_block_started_at"));
        assertTrue(sqlSet.contains("write_block_released_at"));
    }

    @Test
    void pageShouldFilterByRestoreModeAndMapRecords() {
        RestoreMapper mapper = mock(RestoreMapper.class);
        RestoreRepositoryImpl repository = new RestoreRepositoryImpl(mapper);
        Page<RestoreDO> dataObjectPage = new Page<>(1, 10);
        dataObjectPage.setTotal(1);
        dataObjectPage.setRecords(List.of(dataObject(9101L, RestoreMode.DRILL.value())));
        when(mapper.selectPage(any(Page.class), any())).thenReturn(dataObjectPage);

        PageResult<RestoreRecord> result =
                repository.page(9001L, RestoreMode.DRILL.value(), RestoreStatus.SUCCEEDED.value(), 1001L, 1, 10);

        assertEquals(1, result.getTotalCount());
        assertEquals(RestoreMode.DRILL.value(), result.getRecords().get(0).getRestoreMode());
        ArgumentCaptor<QueryWrapper<RestoreDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectPage(any(Page.class), captor.capture());
        String sqlSegment = captor.getValue().getCustomSqlSegment();
        assertTrue(sqlSegment.contains("backup_id"));
        assertTrue(sqlSegment.contains("restore_mode"));
        assertTrue(sqlSegment.contains("restore_status"));
        assertTrue(sqlSegment.contains("requester_user_id"));
    }

    private static RestoreRecord domainRecord(String restoreMode) {
        return new RestoreRecord(
                RestoreIdCodec.toDomain(9101L),
                9001L,
                9201L,
                restoreMode,
                RestoreStatus.SUCCEEDED.value(),
                true,
                new Date(1_719_630_410_000L),
                new Date(1_719_630_490_000L),
                null,
                1001L,
                new Date(1_719_630_400_000L),
                new Date(1_719_630_500_000L));
    }

    private static RestoreDO dataObject(long restoreId, String restoreMode) {
        return new RestoreDO(
                null,
                restoreId,
                9001L,
                9201L,
                restoreMode,
                RestoreStatus.SUCCEEDED.value(),
                true,
                new Date(1_719_630_410_000L),
                new Date(1_719_630_490_000L),
                null,
                1001L,
                new Date(1_719_630_400_000L),
                new Date(1_719_630_500_000L));
    }
}
