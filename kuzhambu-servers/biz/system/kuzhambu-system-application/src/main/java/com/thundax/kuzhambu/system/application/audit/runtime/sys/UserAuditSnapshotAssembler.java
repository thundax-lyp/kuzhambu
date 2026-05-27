package com.thundax.kuzhambu.system.application.audit.runtime.sys;

import com.thundax.kuzhambu.system.application.audit.runtime.AuditSnapshotAssembler;
import com.thundax.kuzhambu.system.application.audit.runtime.AuditSnapshots;
import com.thundax.kuzhambu.system.application.core.entity.User;
import com.thundax.kuzhambu.system.domain.audit.valueobject.AuditField;
import com.thundax.kuzhambu.system.domain.audit.valueobject.AuditSnapshot;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UserAuditSnapshotAssembler implements AuditSnapshotAssembler {

    private static final String OBJECT_TYPE = "User";

    @Override
    public String objectType() {
        return OBJECT_TYPE;
    }

    @Override
    public String objectTypeLabel() {
        return "后台用户";
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
        User user = (User) object;
        if (user == null) {
            return null;
        }
        return AuditSnapshots.of(
                objectType(),
                user.getId(),
                user.getName(),
                AuditSnapshots.field("name", "名称", user.getName()),
                AuditSnapshots.field("status", "状态", user.getStatus()),
                AuditSnapshots.field("privilege", "权限", user.getPrivilege()));
    }
}
