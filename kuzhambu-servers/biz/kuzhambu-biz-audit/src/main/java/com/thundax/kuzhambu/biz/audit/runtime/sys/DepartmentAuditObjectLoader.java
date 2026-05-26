package com.thundax.kuzhambu.biz.audit.runtime.sys;

import com.thundax.kuzhambu.biz.audit.runtime.AuditObjectLoader;
import com.thundax.kuzhambu.biz.core.entity.valueobject.DepartmentIdCodec;
import com.thundax.kuzhambu.biz.core.service.DepartmentService;
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
