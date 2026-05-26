package com.thundax.kuzhambu.infra.auth.persistence.assembler;

import com.thundax.kuzhambu.biz.auth.entity.PrincipalCredential;
import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalType;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalCredentialIdCodec;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalIdentityIdCodec;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalKey;
import com.thundax.kuzhambu.infra.auth.persistence.dataobject.PrincipalCredentialDO;
import java.util.ArrayList;
import java.util.List;

public final class PrincipalCredentialPersistenceAssembler {

    private PrincipalCredentialPersistenceAssembler() {}

    public static PrincipalCredentialDO toDataObject(PrincipalCredential entity) {
        if (entity == null) {
            return null;
        }
        PrincipalCredentialDO dataObject = new PrincipalCredentialDO();
        dataObject.setId(PrincipalCredentialIdCodec.toValue(entity.getId()));
        dataObject.setPrincipalType(principalTypeValue(entity.getPrincipalKey()));
        dataObject.setPrincipalId(principalIdValue(entity.getPrincipalKey()));
        dataObject.setIdentityId(PrincipalIdentityIdCodec.toValue(entity.getIdentityId()));
        dataObject.setCredentialType(credentialTypeValue(entity.getCredentialType()));
        dataObject.setCredentialValue(entity.getCredentialValue());
        dataObject.setStatus(statusValue(entity.getStatus()));
        dataObject.setNeedChangePassword(entity.isNeedChangePassword());
        dataObject.setFailedCount(entity.getFailedCount());
        dataObject.setFailedLimit(entity.getFailedLimit());
        dataObject.setLockedUntil(entity.getLockedUntil());
        dataObject.setExpiresAt(entity.getExpiresAt());
        dataObject.setLastVerifiedAt(entity.getLastVerifiedAt());
        return dataObject;
    }

    public static PrincipalCredential toEntity(PrincipalCredentialDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        PrincipalCredential entity = new PrincipalCredential();
        entity.setId(PrincipalCredentialIdCodec.toDomain(dataObject.getId()));
        entity.setPrincipalKey(
                PrincipalKey.of(principalTypeFrom(dataObject.getPrincipalType()), dataObject.getPrincipalId()));
        entity.setIdentityId(PrincipalIdentityIdCodec.toDomain(dataObject.getIdentityId()));
        entity.setCredentialType(credentialTypeFrom(dataObject.getCredentialType()));
        entity.setCredentialValue(dataObject.getCredentialValue());
        entity.setStatus(statusFrom(dataObject.getStatus()));
        entity.setNeedChangePassword(booleanOrFalse(dataObject.getNeedChangePassword()));
        entity.setFailedCount(intOrZero(dataObject.getFailedCount()));
        entity.setFailedLimit(intOrZero(dataObject.getFailedLimit()));
        entity.setLockedUntil(dataObject.getLockedUntil());
        entity.setExpiresAt(dataObject.getExpiresAt());
        entity.setLastVerifiedAt(dataObject.getLastVerifiedAt());
        return entity;
    }

    public static List<PrincipalCredential> toEntityList(List<PrincipalCredentialDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<PrincipalCredential> entities = new ArrayList<>();
        for (PrincipalCredentialDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static int intOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static boolean booleanOrFalse(Boolean value) {
        return value != null && value;
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

    private static String credentialTypeValue(PrincipalCredentialType credentialType) {
        return credentialType == null ? null : credentialType.value();
    }

    private static PrincipalCredentialType credentialTypeFrom(String credentialType) {
        return credentialType == null ? null : PrincipalCredentialType.from(credentialType);
    }

    private static String statusValue(PrincipalCredentialStatus status) {
        return status == null ? null : status.value();
    }

    private static PrincipalCredentialStatus statusFrom(String status) {
        return status == null ? null : PrincipalCredentialStatus.from(status);
    }
}
