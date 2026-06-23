package com.thundax.kuzhambu.system.application.audit.command;

import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.audit.model.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditOperatorType;
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
