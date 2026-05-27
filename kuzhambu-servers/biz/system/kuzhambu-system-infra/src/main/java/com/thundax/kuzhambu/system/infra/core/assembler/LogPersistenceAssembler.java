package com.thundax.kuzhambu.system.infra.core.assembler;

import com.thundax.kuzhambu.system.application.core.entity.Log;
import com.thundax.kuzhambu.system.application.core.entity.enums.LogType;
import com.thundax.kuzhambu.system.domain.core.codec.LogIdCodec;
import com.thundax.kuzhambu.system.infra.core.dataobject.LogDO;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public final class LogPersistenceAssembler {

    private LogPersistenceAssembler() {}

    public static LogDO toDataObject(Log entity) {
        if (entity == null) {
            return null;
        }
        LogDO dataObject = new LogDO();
        dataObject.setId(LogIdCodec.toValue(entity.getId()));
        dataObject.setUserId(StringUtils.isBlank(entity.getUserId()) ? null : Long.valueOf(entity.getUserId()));
        dataObject.setType(typeValue(entity.getType()));
        dataObject.setLogDate(entity.getLogDate());
        dataObject.setTitle(entity.getTitle());
        dataObject.setRemoteAddr(entity.getRemoteAddr());
        dataObject.setUserAgent(entity.getUserAgent());
        dataObject.setMethod(entity.getMethod());
        dataObject.setRequestUri(entity.getRequestUri());
        dataObject.setRequestParams(entity.getRequestParams());
        return dataObject;
    }

    public static Log toEntity(LogDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Log entity = new Log();
        entity.setId(LogIdCodec.toDomain(dataObject.getId()));
        entity.setUserId(dataObject.getUserId() == null ? null : String.valueOf(dataObject.getUserId()));
        entity.setType(typeFrom(dataObject.getType()));
        entity.setLogDate(dataObject.getLogDate());
        entity.setTitle(dataObject.getTitle());
        entity.setRemoteAddr(dataObject.getRemoteAddr());
        entity.setUserAgent(dataObject.getUserAgent());
        entity.setMethod(dataObject.getMethod());
        entity.setRequestUri(dataObject.getRequestUri());
        entity.setRequestParams(dataObject.getRequestParams());
        return entity;
    }

    public static List<Log> toEntityList(List<LogDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Log> entities = new ArrayList<>();
        for (LogDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    public static List<LogDO> toDataObjectList(List<Log> entities) {
        if (entities == null) {
            return null;
        }
        List<LogDO> dataObjects = new ArrayList<>();
        for (Log entity : entities) {
            dataObjects.add(toDataObject(entity));
        }
        return dataObjects;
    }

    private static String typeValue(LogType type) {
        return type == null ? null : type.value();
    }

    private static LogType typeFrom(String type) {
        return type == null ? null : LogType.from(type);
    }
}
