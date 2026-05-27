package com.thundax.kuzhambu.system.application.audit.service.command;

import com.thundax.kuzhambu.system.application.audit.entity.enums.AuditAction;
import com.thundax.kuzhambu.system.application.audit.entity.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.audit.valueobject.AuditSnapshot;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAuditLogCommand {

    private String objectType;
    private String objectId;
    private AuditAction action;
    private String idempotencyKey;
    private AuditOperatorType operatorType;
    private String operatorId;
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
