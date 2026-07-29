package com.thundax.kuzhambu.system.application.audit.command;

import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.audit.model.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAuditLogCommand {

    private AuditObjectRef objectRef;
    private AuditAction action;
    private String idempotencyKey;
    private AuditOperatorRef operatorRef;
    private String operatorName;
    private String source;
    private String requestId;
    private String traceId;
    private String remoteAddr;
    private String summary;
    private AuditSnapshot beforeSnapshot;
    private AuditSnapshot afterSnapshot;
    private boolean recordWhenUnchanged;
}
