package com.thundax.kuzhambu.operations.infra.task.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.task.model.entity.LongTaskSnapshot;
import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;
import com.thundax.kuzhambu.operations.infra.task.persistence.dataobject.LongTaskSnapshotDO;
import com.thundax.kuzhambu.operations.infra.task.persistence.mapper.LongTaskSnapshotMapper;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class LongTaskSnapshotRepositoryImplTest {

    @Test
    void getByIdShouldMapIdAndSourceDomain() {
        LongTaskSnapshotMapper mapper = mock(LongTaskSnapshotMapper.class);
        LongTaskSnapshotRepositoryImpl repository = new LongTaskSnapshotRepositoryImpl(mapper);
        when(mapper.selectOne(any())).thenReturn(dataObject(9001L, "catalog", "cleanup", "DONE"));

        LongTaskSnapshot result = repository.getById(LongTaskSnapshotId.of(9001L));

        assertNotNull(result);
        assertEquals(9001L, result.getId().value());
        assertEquals("catalog", result.getSourceDomain());
    }

    @Test
    void pageShouldMapRecordsAndTotalCount() {
        LongTaskSnapshotMapper mapper = mock(LongTaskSnapshotMapper.class);
        LongTaskSnapshotRepositoryImpl repository = new LongTaskSnapshotRepositoryImpl(mapper);
        Page<LongTaskSnapshotDO> dataObjectPage = new Page<>(2, 5);
        dataObjectPage.setTotal(6);
        dataObjectPage.setRecords(List.of(dataObject(9002L, "catalog", "cleanup", "RUNNING")));
        when(mapper.selectPage(any(Page.class), any())).thenReturn(dataObjectPage);

        PageResult<LongTaskSnapshot> result = repository.page("catalog", "cleanup", "RUNNING", 2, 5);

        assertEquals(2, result.getPageNo());
        assertEquals(5, result.getPageSize());
        assertEquals(6, result.getTotalCount());
        assertEquals(2, result.getTotalPage());
        assertEquals(1, result.getRecords().size());
        assertEquals(9002L, result.getRecords().get(0).getId().value());
        assertEquals("catalog", result.getRecords().get(0).getSourceDomain());
        assertEquals("cleanup", result.getRecords().get(0).getTaskType());
    }

    private static LongTaskSnapshotDO dataObject(
            long snapshotId, String sourceDomain, String taskType, String taskStatus) {
        return new LongTaskSnapshotDO(
                null,
                snapshotId,
                sourceDomain,
                taskType,
                "catalog:daily",
                taskStatus,
                120,
                80,
                40,
                null,
                1001L,
                new Date(1_718_000_000_000L),
                new Date(1_718_086_400_000L),
                new Date(1_718_000_100_000L));
    }
}
