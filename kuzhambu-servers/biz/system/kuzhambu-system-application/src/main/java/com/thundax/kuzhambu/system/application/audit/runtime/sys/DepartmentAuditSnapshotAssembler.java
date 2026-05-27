package com.thundax.kuzhambu.system.application.audit.runtime.sys;

import com.thundax.kuzhambu.system.application.audit.runtime.AuditSnapshotAssembler;
import com.thundax.kuzhambu.system.application.audit.runtime.AuditSnapshots;
import com.thundax.kuzhambu.system.application.core.entity.Department;
import com.thundax.kuzhambu.system.domain.audit.valueobject.AuditField;
import com.thundax.kuzhambu.system.domain.audit.valueobject.AuditSnapshot;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DepartmentAuditSnapshotAssembler implements AuditSnapshotAssembler {

    @Override
    public String objectType() {
        return "Department";
    }

    @Override
    public String objectTypeLabel() {
        return "部门";
    }

    @Override
    public List<AuditField> fields() {
        return Arrays.asList(
                AuditSnapshots.field("name", "名称", null),
                AuditSnapshots.field("shortName", "简称", null),
                AuditSnapshots.field("parentId", "父级", null));
    }

    @Override
    public AuditSnapshot assemble(Object object) {
        Department department = (Department) object;
        if (department == null) {
            return null;
        }
        return AuditSnapshots.of(
                objectType(),
                department.getId(),
                department.getName(),
                AuditSnapshots.field("name", "名称", department.getName()),
                AuditSnapshots.field("shortName", "简称", department.getShortName()),
                AuditSnapshots.field("parentId", "父级", department.getParentId()));
    }
}
