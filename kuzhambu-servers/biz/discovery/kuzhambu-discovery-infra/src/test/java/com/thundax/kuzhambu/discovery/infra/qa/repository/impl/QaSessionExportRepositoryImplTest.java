package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSessionExport;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaSessionExportDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaSessionExportMapper;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class QaSessionExportRepositoryImplTest {

    @Test
    void saveShouldAssignIdentifiersWhenExportIdIsMissing() {
        QaSessionExportMapper mapper = mock(QaSessionExportMapper.class);
        QaSessionExportRepositoryImpl repository = new QaSessionExportRepositoryImpl(mapper);
        QaSessionExport entity =
                new QaSessionExport(null, null, 5001L, "CSV", null, "PROCESSING", null, 1001L, new Date(), null);

        Long savedId = repository.save(entity);

        assertNotNull(savedId);
        ArgumentCaptor<QaSessionExportDO> dataObjectCaptor = ArgumentCaptor.forClass(QaSessionExportDO.class);
        verify(mapper).insert(dataObjectCaptor.capture());
        assertNotNull(dataObjectCaptor.getValue().getId());
        assertNotNull(dataObjectCaptor.getValue().getExportId());
    }

    @Test
    void updateShouldDelegateById() {
        QaSessionExportMapper mapper = mock(QaSessionExportMapper.class);
        QaSessionExportRepositoryImpl repository = new QaSessionExportRepositoryImpl(mapper);
        QaSessionExport entity =
                new QaSessionExport(1L, 6001L, 5001L, "CSV", 7001L, "SUCCEEDED", null, 1001L, new Date(), new Date());
        when(mapper.updateById(any(QaSessionExportDO.class))).thenReturn(1);

        int updated = repository.update(entity);

        assertEquals(1, updated);
        verify(mapper).updateById(any(QaSessionExportDO.class));
    }

    @Test
    void getByExportIdShouldReturnDomain() {
        QaSessionExportMapper mapper = mock(QaSessionExportMapper.class);
        QaSessionExportRepositoryImpl repository = new QaSessionExportRepositoryImpl(mapper);
        Date requestedAt = new Date(1_718_000_000_000L);
        Date completedAt = new Date(1_718_000_001_000L);
        QaSessionExportDO dataObject = new QaSessionExportDO(
                1L, 6001L, 5001L, "CSV", 7001L, "SUCCEEDED", null, 1001L, requestedAt, completedAt);
        when(mapper.selectOne(any())).thenReturn(dataObject);

        QaSessionExport result = repository.getByExportId(6001L);

        assertEquals(6001L, result.getExportId());
        assertEquals(7001L, result.getStorageObjectId());
        assertEquals("SUCCEEDED", result.getExportStatus());
        ArgumentCaptor<QueryWrapper<QaSessionExportDO>> wrapperCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectOne(wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getSqlSegment().contains("export_id"));
        assertTrue(wrapperCaptor.getValue().getSqlSegment().contains("limit 1"));
    }
}
