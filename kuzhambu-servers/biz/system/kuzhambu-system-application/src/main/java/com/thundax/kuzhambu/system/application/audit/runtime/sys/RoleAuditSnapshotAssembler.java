package com.thundax.kuzhambu.system.application.audit.runtime.sys;

import com.thundax.kuzhambu.system.application.audit.runtime.AuditSnapshotAssembler;
import com.thundax.kuzhambu.system.application.audit.runtime.AuditSnapshots;
import com.thundax.kuzhambu.system.domain.model.entity.Role;
import com.thundax.kuzhambu.system.domain.model.valueobject.AuditField;
import com.thundax.kuzhambu.system.domain.model.valueobject.AuditSnapshot;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RoleAuditSnapshotAssembler implements AuditSnapshotAssembler {

    private static final String OBJECT_TYPE = "Role";

    @Override
    public String objectType() {
        return OBJECT_TYPE;
    }

    @Override
    public String objectTypeLabel() {
        return "角色";
    }

    @Override
    public List<AuditField> fields() {
        return Arrays.asList(
                AuditSnapshots.field("name", "名称", null),
                AuditSnapshots.field("status", "状态", null),
                AuditSnapshots.field("privilege", "权限", null));
    }

    @Override
    public AuditSnapshot assemble(Object object) {
        Role role = (Role) object;
        if (role == null) {
            return null;
        }
        return AuditSnapshots.of(
                objectType(),
                role.getId(),
                role.getName(),
                AuditSnapshots.field("name", "名称", role.getName()),
                AuditSnapshots.field("status", "状态", role.getStatus()),
                AuditSnapshots.field("privilege", "权限", role.getPrivilege()));
    }
}
