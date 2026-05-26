package com.thundax.kuzhambu.infra.core.persistence.assembler;

import com.thundax.kuzhambu.biz.core.entity.Department;
import com.thundax.kuzhambu.biz.core.entity.valueobject.DepartmentIdCodec;
import com.thundax.kuzhambu.infra.core.persistence.dataobject.DepartmentDO;
import java.util.ArrayList;
import java.util.List;

public final class DepartmentPersistenceAssembler {

    private DepartmentPersistenceAssembler() {}

    public static DepartmentDO toDataObject(Department entity) {
        if (entity == null) {
            return null;
        }
        DepartmentDO dataObject = new DepartmentDO();
        dataObject.setId(DepartmentIdCodec.toValue(entity.getId()));
        dataObject.setParentId(DepartmentIdCodec.toValue(entity.getParentId()));
        dataObject.setName(entity.getName());
        dataObject.setShortName(entity.getShortName());
        dataObject.setRemarks(entity.getRemarks());
        return dataObject;
    }

    public static Department toEntity(DepartmentDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Department entity = new Department();
        entity.setId(DepartmentIdCodec.toDomain(dataObject.getId()));
        entity.setParentId(DepartmentIdCodec.toDomain(dataObject.getParentId()));
        entity.setName(dataObject.getName());
        entity.setShortName(dataObject.getShortName());
        entity.setRemarks(dataObject.getRemarks());
        return entity;
    }

    public static List<Department> toEntityList(List<DepartmentDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Department> entities = new ArrayList<>();
        for (DepartmentDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    public static DepartmentDO toParentUpdateDataObject(Long id, Long parentId) {
        DepartmentDO dataObject = new DepartmentDO();
        dataObject.setId(id);
        dataObject.setParentId(parentId);
        return dataObject;
    }
}
