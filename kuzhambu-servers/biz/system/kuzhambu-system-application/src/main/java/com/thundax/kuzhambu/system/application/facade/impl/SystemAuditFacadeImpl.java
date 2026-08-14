package com.thundax.kuzhambu.system.application.facade.impl;

import com.thundax.kuzhambu.system.application.audit.service.AuditTrailApplicationService;
import com.thundax.kuzhambu.system.application.facade.assembler.SystemAuditFacadeAssembler;
import com.thundax.kuzhambu.system.domain.audit.codec.AuditLogIdCodec;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditLog;
import com.thundax.kuzhambu.system.facade.SystemAuditFacade;
import com.thundax.kuzhambu.system.facade.request.SystemAuditFacadeRequest;
import com.thundax.kuzhambu.system.facade.response.SystemAuditFacadeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemAuditFacadeImpl implements SystemAuditFacade {

    private final AuditTrailApplicationService auditTrailApplicationService;
    private final SystemAuditFacadeAssembler systemAuditFacadeAssembler;

    public SystemAuditFacadeImpl(
            AuditTrailApplicationService auditTrailApplicationService,
            SystemAuditFacadeAssembler systemAuditFacadeAssembler) {
        this.auditTrailApplicationService = auditTrailApplicationService;
        this.systemAuditFacadeAssembler = systemAuditFacadeAssembler;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long record(SystemAuditFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return AuditLogIdCodec.toValue(
                auditTrailApplicationService.record(systemAuditFacadeAssembler.toCreateCommand(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public SystemAuditFacadeResponse get(Long auditLogId) {
        AuditLog log = auditTrailApplicationService.getLog(systemAuditFacadeAssembler.toGetQuery(auditLogId));
        if (log == null) {
            return null;
        }
        return systemAuditFacadeAssembler.toResponse(log);
    }
}
