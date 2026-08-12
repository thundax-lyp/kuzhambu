package com.thundax.kuzhambu.operations.infra.backup.repository.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.thundax.kuzhambu.operations.domain.backup.model.entity.BackupRecord;
import com.thundax.kuzhambu.operations.domain.backup.model.enums.BackupStatus;
import com.thundax.kuzhambu.operations.infra.backup.persistence.dataobject.BackupDO;
import com.thundax.kuzhambu.operations.infra.backup.persistence.mapper.BackupMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class BackupRepositoryImplTest {

    @Test
    void insertShouldGenerateBackupIdWhenRecordHasNoId() {
        BackupMapper mapper = mock(BackupMapper.class);
        BackupRepositoryImpl repository = new BackupRepositoryImpl(mapper);

        repository.insert(new BackupRecord(
                null,
                "AUTO",
                BackupStatus.RUNNING.value(),
                null,
                "smoke.sql",
                null,
                null,
                null,
                1001L,
                Instant.ofEpochMilli(1_719_630_400_000L),
                null,
                null));

        ArgumentCaptor<BackupDO> captor = ArgumentCaptor.forClass(BackupDO.class);
        verify(mapper).insert(captor.capture());
        assertNotNull(captor.getValue().getBackupId());
    }
}
