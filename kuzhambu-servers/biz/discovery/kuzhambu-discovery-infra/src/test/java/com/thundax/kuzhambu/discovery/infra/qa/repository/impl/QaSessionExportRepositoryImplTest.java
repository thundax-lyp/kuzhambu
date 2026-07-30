package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSessionExport;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaSessionExportDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaSessionExportMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class QaSessionExportRepositoryImplTest {

    @Test
    void saveShouldAssignIdentifiersWhenExportIdIsMissing() {
        QaSessionExportMapper mapper = mock(QaSessionExportMapper.class);
        QaSessionExportRepositoryImpl repository = new QaSessionExportRepositoryImpl(mapper);
        QaSessionExport entity =
                new QaSessionExport(null, null, 5001L, "CSV", null, "PROCESSING", null, 1001L, Instant.now(), null);
        doAnswer(invocation -> {
                    invocation.getArgument(0, QaSessionExportDO.class).setId(6001L);
                    return 1;
                })
                .when(mapper)
                .insert(any(QaSessionExportDO.class));

        Long savedId = repository.save(entity);

        assertEquals(6001L, savedId);
        ArgumentCaptor<QaSessionExportDO> dataObjectCaptor = ArgumentCaptor.forClass(QaSessionExportDO.class);
        verify(mapper).insert(dataObjectCaptor.capture());
        assertNotNull(dataObjectCaptor.getValue().getId());
    }

    @Test
    void updateShouldDelegateById() {
        QaSessionExportMapper mapper = mock(QaSessionExportMapper.class);
        QaSessionExportRepositoryImpl repository = new QaSessionExportRepositoryImpl(mapper);
        QaSessionExport entity = new QaSessionExport(
                6001L, 6001L, 5001L, "CSV", 7001L, "SUCCEEDED", null, 1001L, Instant.now(), Instant.now());
        when(mapper.updateById(any(QaSessionExportDO.class))).thenReturn(1);

        int updated = repository.update(entity);

        assertEquals(1, updated);
        verify(mapper).updateById(any(QaSessionExportDO.class));
    }

    @Test
    void getByExportIdShouldReturnDomain() {
        QaSessionExportMapper mapper = mock(QaSessionExportMapper.class);
        QaSessionExportRepositoryImpl repository = new QaSessionExportRepositoryImpl(mapper);
        Instant requestedAt = Instant.ofEpochMilli(1_718_000_000_000L);
        Instant completedAt = Instant.ofEpochMilli(1_718_000_001_000L);
        QaSessionExportDO dataObject =
                new QaSessionExportDO(6001L, 5001L, "CSV", 7001L, "SUCCEEDED", null, 1001L, requestedAt, completedAt);
        when(mapper.selectById(6001L)).thenReturn(dataObject);

        QaSessionExport result = repository.getByExportId(6001L);

        assertEquals(6001L, result.getId());
        assertEquals(7001L, result.getStorageObjectId());
        assertEquals("SUCCEEDED", result.getExportStatus());
        verify(mapper).selectById(6001L);
    }

    @Test
    void getByExportIdShouldReturnNullWhenMissing() {
        QaSessionExportMapper mapper = mock(QaSessionExportMapper.class);
        QaSessionExportRepositoryImpl repository = new QaSessionExportRepositoryImpl(mapper);
        when(mapper.selectById(6001L)).thenReturn(null);

        QaSessionExport result = repository.getByExportId(6001L);

        assertNull(result);
    }
}
