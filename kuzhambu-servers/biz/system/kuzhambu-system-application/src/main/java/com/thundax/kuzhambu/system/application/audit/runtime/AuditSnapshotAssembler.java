package com.thundax.kuzhambu.system.application.audit.runtime;

import com.thundax.kuzhambu.system.domain.model.valueobject.AuditField;
import com.thundax.kuzhambu.system.domain.model.valueobject.AuditSnapshot;
import java.util.List;

public interface AuditSnapshotAssembler {

    String objectType();

    String objectTypeLabel();

    List<AuditField> fields();

    AuditSnapshot assemble(Object object);
}
