package com.thundax.kuzhambu.system.application.audit.runtime;

import com.thundax.kuzhambu.system.application.audit.entity.valueobject.AuditField;
import com.thundax.kuzhambu.system.application.audit.entity.valueobject.AuditSnapshot;
import java.util.List;

public interface AuditSnapshotAssembler {

    String objectType();

    String objectTypeLabel();

    List<AuditField> fields();

    AuditSnapshot assemble(Object object);
}
