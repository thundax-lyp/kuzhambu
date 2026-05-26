package com.thundax.kuzhambu.biz.audit.runtime.sys;

import com.thundax.kuzhambu.biz.audit.runtime.AuditObjectLoader;
import com.thundax.kuzhambu.biz.core.entity.valueobject.RoleIdCodec;
import com.thundax.kuzhambu.biz.core.service.RoleService;
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
