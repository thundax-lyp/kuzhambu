package com.thundax.kuzhambu.biz.audit.entity;

import com.thundax.kuzhambu.biz.audit.entity.enums.AuditAction;
import com.thundax.kuzhambu.biz.audit.entity.enums.AuditOperatorType;
import com.thundax.kuzhambu.biz.audit.entity.valueobject.AuditLogId;
import com.thundax.kuzhambu.biz.audit.entity.valueobject.AuditMetaId;
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
