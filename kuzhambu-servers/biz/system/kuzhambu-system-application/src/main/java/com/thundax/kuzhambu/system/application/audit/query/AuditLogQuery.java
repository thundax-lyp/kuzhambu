package com.thundax.kuzhambu.system.application.audit.query;

import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogQuery {

    private AuditObjectRef objectRef;
    private AuditAction action;
    private AuditOperatorRef operatorRef;
    private String source;
    private String requestId;
    private Instant beginDate;
    private Instant endDate;
}
