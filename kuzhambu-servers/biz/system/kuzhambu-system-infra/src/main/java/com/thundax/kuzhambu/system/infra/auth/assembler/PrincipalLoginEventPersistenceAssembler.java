package com.thundax.kuzhambu.system.infra.auth.assembler;

import com.thundax.kuzhambu.system.application.auth.codec.PrincipalLoginEventIdCodec;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalLoginEvent;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalAuthenticationMethod;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalLoginEventType;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalType;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.infra.auth.dataobject.PrincipalLoginEventDO;
import java.util.ArrayList;
import java.util.List;

public final class PrincipalLoginEventPersistenceAssembler {

    private PrincipalLoginEventPersistenceAssembler() {}

    public static PrincipalLoginEventDO toDataObject(PrincipalLoginEvent entity) {
        if (entity == null) {
            return null;
        }
        PrincipalLoginEventDO dataObject = new PrincipalLoginEventDO();
        dataObject.setId(PrincipalLoginEventIdCodec.toValue(entity.getId()));
        dataObject.setPrincipalType(principalTypeValue(entity.getPrincipalKey()));
        dataObject.setPrincipalId(principalIdValue(entity.getPrincipalKey()));
        dataObject.setClientId(entity.getClientId());
        dataObject.setEventType(eventTypeValue(entity.getEventType()));
        dataObject.setAuthenticationMethod(authenticationMethodValue(entity.getAuthenticationMethod()));
        dataObject.setIdentityType(identityTypeValue(entity.getIdentityType()));
        dataObject.setOccurredAt(entity.getOccurredAt());
        dataObject.setIp(entity.getIp());
        dataObject.setUserAgent(entity.getUserAgent());
        dataObject.setReason(entity.getReason());
        return dataObject;
    }

    public static PrincipalLoginEvent toEntity(PrincipalLoginEventDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        PrincipalLoginEvent entity = new PrincipalLoginEvent();
        entity.setId(PrincipalLoginEventIdCodec.toDomain(dataObject.getId()));
        entity.setPrincipalKey(principalKeyFrom(dataObject.getPrincipalType(), dataObject.getPrincipalId()));
        entity.setClientId(dataObject.getClientId());
        entity.setEventType(eventTypeFrom(dataObject.getEventType()));
        entity.setAuthenticationMethod(authenticationMethodFrom(dataObject.getAuthenticationMethod()));
        entity.setIdentityType(identityTypeFrom(dataObject.getIdentityType()));
        entity.setOccurredAt(dataObject.getOccurredAt());
        entity.setIp(dataObject.getIp());
        entity.setUserAgent(dataObject.getUserAgent());
        entity.setReason(dataObject.getReason());
        return entity;
    }

    public static List<PrincipalLoginEvent> toEntityList(List<PrincipalLoginEventDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<PrincipalLoginEvent> entities = new ArrayList<>();
        for (PrincipalLoginEventDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static String principalTypeValue(PrincipalKey principalKey) {
        return principalKey == null || principalKey.getPrincipalType() == null
                ? null
                : principalKey.getPrincipalType().value();
    }

    private static Long principalIdValue(PrincipalKey principalKey) {
        return principalKey == null || principalKey.getPrincipalId() == null ? null : principalKey.getPrincipalId();
    }

    private static PrincipalKey principalKeyFrom(String principalType, Long principalId) {
        return principalType == null || principalId == null
                ? null
                : PrincipalKey.of(PrincipalType.from(principalType), principalId);
    }

    private static String eventTypeValue(PrincipalLoginEventType eventType) {
        return eventType == null ? null : eventType.value();
    }

    private static PrincipalLoginEventType eventTypeFrom(String eventType) {
        return eventType == null ? null : PrincipalLoginEventType.from(eventType);
    }

    private static String authenticationMethodValue(PrincipalAuthenticationMethod authenticationMethod) {
        return authenticationMethod == null ? null : authenticationMethod.value();
    }

    private static PrincipalAuthenticationMethod authenticationMethodFrom(String authenticationMethod) {
        return authenticationMethod == null ? null : PrincipalAuthenticationMethod.from(authenticationMethod);
    }

    private static String identityTypeValue(PrincipalIdentityType identityType) {
        return identityType == null ? null : identityType.value();
    }

    private static PrincipalIdentityType identityTypeFrom(String identityType) {
        return identityType == null ? null : PrincipalIdentityType.from(identityType);
    }
}
