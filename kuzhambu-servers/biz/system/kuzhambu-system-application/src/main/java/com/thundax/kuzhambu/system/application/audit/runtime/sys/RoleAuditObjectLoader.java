package com.thundax.kuzhambu.system.application.audit.runtime.sys;

import com.thundax.kuzhambu.system.application.audit.runtime.AuditObjectLoader;
import com.thundax.kuzhambu.system.application.core.service.RoleService;
import com.thundax.kuzhambu.system.domain.core.codec.RoleIdCodec;
import org.springframework.stereotype.Component;

@Component
public class RoleAuditObjectLoader implements AuditObjectLoader {

    private static final String OBJECT_TYPE = "Role";

    private final RoleService roleService;

    public RoleAuditObjectLoader(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public String objectType() {
        return OBJECT_TYPE;
    }

    @Override
    public Object load(String objectId) {
        return roleService.get(RoleIdCodec.toDomain(Long.valueOf(objectId)));
    }
}
