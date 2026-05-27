package com.thundax.kuzhambu.system.domain.audit.model.entity;

import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditLogId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditMetaId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
