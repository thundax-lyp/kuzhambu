package com.thundax.kuzhambu.system.application.audit.runtime.sys;

import com.thundax.kuzhambu.system.application.audit.runtime.AuditObjectLoader;
import com.thundax.kuzhambu.system.application.core.service.DepartmentService;
import com.thundax.kuzhambu.system.domain.core.codec.DepartmentIdCodec;
import org.springframework.stereotype.Component;

@Component
public class DepartmentAuditObjectLoader implements AuditObjectLoader {

    private final DepartmentService departmentService;

    public DepartmentAuditObjectLoader(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Override
    public String objectType() {
        return "Department";
    }

    @Override
    public Object load(String objectId) {
        return departmentService.get(DepartmentIdCodec.toDomain(Long.valueOf(objectId)));
    }
}
