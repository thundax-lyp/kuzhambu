package com.thundax.kuzhambu.infra.auth.persistence.assembler;

import com.thundax.kuzhambu.biz.auth.entity.PrincipalIdentity;
import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalType;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalIdentityIdCodec;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalKey;
import com.thundax.kuzhambu.infra.auth.persistence.dataobject.PrincipalIdentityDO;
import java.util.ArrayList;
import java.util.List;

public final class PrincipalIdentityPersistenceAssembler {

    private PrincipalIdentityPersistenceAssembler() {}

    public static PrincipalIdentityDO toDataObject(PrincipalIdentity entity) {
        if (entity == null) {
            return null;
        }
        PrincipalIdentityDO dataObject = new PrincipalIdentityDO();
        dataObject.setId(PrincipalIdentityIdCodec.toValue(entity.getId()));
        dataObject.setPrincipalType(principalTypeValue(entity.getPrincipalKey()));
        dataObject.setPrincipalId(principalIdValue(entity.getPrincipalKey()));
        dataObject.setIdentityType(identityTypeValue(entity.getType()));
        dataObject.setIdentityValue(entity.getIdentityValue());
        dataObject.setStatus(statusValue(entity.getStatus()));
        return dataObject;
    }

    public static PrincipalIdentity toEntity(PrincipalIdentityDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        PrincipalIdentity entity = new PrincipalIdentity();
        entity.setId(PrincipalIdentityIdCodec.toDomain(dataObject.getId()));
        entity.setPrincipalKey(
                PrincipalKey.of(principalTypeFrom(dataObject.getPrincipalType()), dataObject.getPrincipalId()));
        entity.setType(identityTypeFrom(dataObject.getIdentityType()));
        entity.setIdentityValue(dataObject.getIdentityValue());
        entity.setStatus(statusFrom(dataObject.getStatus()));
        return entity;
    }

    public static List<PrincipalIdentity> toEntityList(List<PrincipalIdentityDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<PrincipalIdentity> entities = new ArrayList<>();
        for (PrincipalIdentityDO dataObject : dataObjects) {
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

    private static PrincipalType principalTypeFrom(String principalType) {
        return principalType == null ? null : PrincipalType.from(principalType);
    }

    private static String identityTypeValue(PrincipalIdentityType identityType) {
        return identityType == null ? null : identityType.value();
    }

    private static PrincipalIdentityType identityTypeFrom(String identityType) {
        return identityType == null ? null : PrincipalIdentityType.from(identityType);
    }

    private static String statusValue(PrincipalIdentityStatus status) {
        return status == null ? null : status.value();
    }

    private static PrincipalIdentityStatus statusFrom(String status) {
        return status == null ? null : PrincipalIdentityStatus.from(status);
    }
}
