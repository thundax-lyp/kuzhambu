package com.thundax.kuzhambu.operations.application.health.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.operations.application.backup.command.OperationsBackupExecuteCommand;
import com.thundax.kuzhambu.operations.application.backup.service.BackupApplicationService;
import com.thundax.kuzhambu.operations.application.health.command.OperationsHealthAlertRecoverCommand;
import com.thundax.kuzhambu.operations.application.health.support.OperationsHealthRecoveryLinkFactory;
import com.thundax.kuzhambu.operations.application.restore.command.OperationsRestoreExecuteCommand;
import com.thundax.kuzhambu.operations.application.restore.service.RestoreApplicationService;
import com.thundax.kuzhambu.operations.domain.health.codec.HealthAlertIdCodec;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthAlertRecord;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthAlertRepository;
import org.junit.jupiter.api.Test;

class HealthAlertApplicationServiceImplTest {

    @Test
    void recoverShouldRunManualBackupActionBeforeMarkingAlertRecovered() {
        HealthAlertRepository repository = mock(HealthAlertRepository.class);
        BackupApplicationService backupApplicationService = mock(BackupApplicationService.class);
        HealthAlertRecord alert = activeAlert();
        alert.setRecoveryAction(OperationsHealthRecoveryLinkFactory.ACTION_RUN_MANUAL_BACKUP);
        when(repository.getById(HealthAlertIdCodec.toDomain(9201L))).thenReturn(alert);
        HealthAlertApplicationServiceImpl service =
                new HealthAlertApplicationServiceImpl(repository, backupApplicationService, null);

        service.recover(new OperationsHealthAlertRecoverCommand(HealthAlertIdCodec.toDomain(9201L), 1001L));

        verify(backupApplicationService)
                .execute(argThat((OperationsBackupExecuteCommand command) ->
                        command != null && Long.valueOf(1001L).equals(command.getRequesterUserId())));
        verify(repository).update(alert);
        assertEquals("RECOVERED", alert.getAlertStatus());
        assertNull(alert.getFailureReason());
    }

    @Test
    void recoverShouldRunRestoreActionFromRecoveryTargetBeforeMarkingAlertRecovered() {
        HealthAlertRepository repository = mock(HealthAlertRepository.class);
        RestoreApplicationService restoreApplicationService = mock(RestoreApplicationService.class);
        HealthAlertRecord alert = activeAlert();
        alert.setRecoveryAction(OperationsHealthRecoveryLinkFactory.ACTION_RUN_RESTORE);
        alert.setRecoveryTarget("{\"route\":\"/operations/backup-restore\",\"backupId\":9001}");
        when(repository.getById(HealthAlertIdCodec.toDomain(9201L))).thenReturn(alert);
        HealthAlertApplicationServiceImpl service =
                new HealthAlertApplicationServiceImpl(repository, null, restoreApplicationService);

        service.recover(new OperationsHealthAlertRecoverCommand(HealthAlertIdCodec.toDomain(9201L), 1001L));

        verify(restoreApplicationService)
                .execute(argThat((OperationsRestoreExecuteCommand command) -> command != null
                        && command.getBackupId().value().equals(9001L)
                        && Long.valueOf(1001L).equals(command.getRequesterUserId())));
        verify(repository).update(alert);
        assertEquals("RECOVERED", alert.getAlertStatus());
    }

    private static HealthAlertRecord activeAlert() {
        HealthAlertRecord alert = new HealthAlertRecord();
        alert.setId(HealthAlertIdCodec.toDomain(9201L));
        alert.setAlertStatus("ACTIVE");
        alert.setFailureReason("previous failure");
        return alert;
    }
}
