package com.thundax.kuzhambu.infra.core.assembler;

import com.thundax.kuzhambu.biz.core.entity.Dict;
import com.thundax.kuzhambu.biz.core.entity.valueobject.DictIdCodec;
import com.thundax.kuzhambu.infra.core.dataobject.DictDO;
import java.util.ArrayList;
import java.util.List;

public final class DictPersistenceAssembler {

    private DictPersistenceAssembler() {}

    public static DictDO toDataObject(Dict entity) {
        if (entity == null) {
            return null;
        }
        DictDO dataObject = new DictDO();
        dataObject.setId(DictIdCodec.toValue(entity.getId()));
        dataObject.setType(entity.getType());
        dataObject.setLabel(entity.getLabel());
        dataObject.setValue(entity.getValue());
        dataObject.setPriority(priorityOrDefault(entity.getPriority()));
        dataObject.setRemarks(entity.getRemarks());
        return dataObject;
    }

    public static Dict toEntity(DictDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Dict entity = new Dict();
        entity.setId(DictIdCodec.toDomain(dataObject.getId()));
        entity.setType(dataObject.getType());
        entity.setLabel(dataObject.getLabel());
        entity.setValue(dataObject.getValue());
        entity.setPriority(priorityOrDefault(dataObject.getPriority()));
        entity.setRemarks(dataObject.getRemarks());
        return entity;
    }

    public static List<Dict> toEntityList(List<DictDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Dict> entities = new ArrayList<>();
        for (DictDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static int priorityOrDefault(Integer priority) {
        return priority == null || priority < 0 ? 0 : priority;
    }
}
