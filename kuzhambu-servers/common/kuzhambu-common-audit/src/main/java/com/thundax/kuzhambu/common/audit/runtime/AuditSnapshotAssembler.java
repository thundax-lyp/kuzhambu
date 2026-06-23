package com.thundax.kuzhambu.common.audit.runtime;

import com.thundax.kuzhambu.common.audit.model.valueobject.AuditField;
import com.thundax.kuzhambu.common.audit.model.valueobject.AuditSnapshot;
import java.util.List;

public interface AuditSnapshotAssembler {

    String objectType();

    String objectTypeLabel();

    List<AuditField> fields();

    AuditSnapshot assemble(Object object);
}
