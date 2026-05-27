package com.thundax.kuzhambu.system.application.audit.service.query;

import com.thundax.kuzhambu.system.application.audit.entity.enums.AuditAction;
import com.thundax.kuzhambu.system.application.audit.entity.enums.AuditOperatorType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogQuery {

    private String objectType;
    private String objectId;
    private AuditAction action;
    private AuditOperatorType operatorType;
    private String operatorId;
    private String source;
    private String requestId;
    private Date beginDate;
    private Date endDate;
}
