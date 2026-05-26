package com.thundax.kuzhambu.biz.audit.runtime;

import com.thundax.kuzhambu.biz.audit.entity.valueobject.AuditField;
import com.thundax.kuzhambu.biz.audit.entity.valueobject.AuditSnapshot;
import java.util.List;

public interface AuditSnapshotAssembler {

    String objectType();

    String objectTypeLabel();

    List<AuditField> fields();

    AuditSnapshot assemble(Object object);
}
