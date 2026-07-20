package com.thundax.kuzhambu.system.application.audit.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.system.application.audit.command.CreateAuditLogCommand;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditLog;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditMeta;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditLogId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditMetaId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.repository.AuditLogRepository;
import com.thundax.kuzhambu.system.domain.audit.repository.AuditMetaRepository;
import org.junit.jupiter.api.Test;

class AuditApplicationServiceImplTest {

    @Test
    void recordShouldRejectConcurrentMetaVersionChange() {
        AuditMetaRepository auditMetaRepository = mock(AuditMetaRepository.class);
        AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
        AuditApplicationServiceImpl service = new AuditApplicationServiceImpl(auditMetaRepository, auditLogRepository);

        AuditMeta meta = new AuditMeta();
        meta.setId(AuditMetaId.of(100L));
        meta.setObjectType("USER");
        meta.setObjectId("1");
        meta.setVersion(7L);

        when(auditMetaRepository.getByObjectRef(any(AuditObjectRef.class))).thenReturn(meta);
        when(auditLogRepository.insert(any(AuditLog.class))).thenReturn(AuditLogId.of(200L));
        when(auditMetaRepository.updateIfVersion(any(AuditMeta.class), eq(7L))).thenReturn(0);

        CreateAuditLogCommand command = new CreateAuditLogCommand();
        command.setObjectType("USER");
        command.setObjectId("1");
        command.setAction(AuditAction.UPDATE);
        command.setRecordWhenUnchanged(true);

        assertThrows(BizException.class, () -> service.record(command));
        verify(auditMetaRepository).updateIfVersion(any(AuditMeta.class), eq(7L));
    }
}
