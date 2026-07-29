package com.thundax.kuzhambu.system.application.audit.query;

import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditMetaQuery {

    private AuditObjectRef objectRef;
}
