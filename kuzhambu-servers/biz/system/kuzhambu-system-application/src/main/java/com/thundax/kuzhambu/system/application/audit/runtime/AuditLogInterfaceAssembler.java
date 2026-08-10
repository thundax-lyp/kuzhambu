package com.thundax.kuzhambu.system.application.audit.runtime;

import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.audit.model.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.system.application.audit.command.CreateAuditLogCommand;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;

final class AuditLogInterfaceAssembler {

    private AuditLogInterfaceAssembler() {}

    static CreateAuditLogCommand toCreateCommand(
            AuditObjectRef objectRef,
            AuditAction action,
            AuditOperatorRef operatorRef,
            String operatorName,
            String summary,
            AuditSnapshot beforeSnapshot,
            AuditSnapshot afterSnapshot,
            boolean recordWhenUnchanged) {
        return new CreateAuditLogCommand(
                objectRef,
                action,
                null,
                operatorRef,
                operatorName,
                null,
                null,
                null,
                null,
                summary,
                beforeSnapshot,
                afterSnapshot,
                recordWhenUnchanged);
    }
}
