package com.thundax.kuzhambu.system.application.audit.entity;

import com.thundax.kuzhambu.system.application.audit.entity.enums.AuditAction;
import com.thundax.kuzhambu.system.application.audit.entity.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.model.valueobject.AuditLogId;
import com.thundax.kuzhambu.system.domain.model.valueobject.AuditMetaId;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditMeta {

    private AuditMetaId id;
    private String objectType;
    private String objectId;
    private Long version;
    private AuditLogId lastLogId;
    private AuditAction lastAction;
    private AuditOperatorType lastOperatorType;
    private String lastOperatorId;
    private String lastOperatorName;
    private Date lastOperatedAt;
    private AuditLogId createdLogId;
    private Date createdAt;
}
