package com.thundax.kuzhambu.system.infra.auth.assembler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.id.EntityIdCodec;
import com.thundax.kuzhambu.system.application.auth.entity.OAuthAuthorization;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalType;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.infra.auth.dataobject.OAuthAuthorizationDO;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public final class OAuthAuthorizationPersistenceAssembler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<LinkedHashSet<String>> STRING_SET_TYPE =
            new TypeReference<LinkedHashSet<String>>() {};

    private OAuthAuthorizationPersistenceAssembler() {}

    public static OAuthAuthorizationDO toDataObject(OAuthAuthorization entity) {
        if (entity == null) {
            return null;
        }
        OAuthAuthorizationDO dataObject = new OAuthAuthorizationDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setAuthorizationCode(entity.getAuthorizationCode());
        dataObject.setClientId(entity.getClientId());
        dataObject.setPrincipalType(entity.getPrincipalKey().getPrincipalType().value());
        dataObject.setPrincipalId(entity.getPrincipalKey().getPrincipalId());
        dataObject.setRedirectUri(entity.getRedirectUri());
        dataObject.setScopes(writeStringSet(entity.getScopes()));
        dataObject.setState(entity.getState());
        dataObject.setCodeChallenge(entity.getCodeChallenge());
        dataObject.setCodeChallengeMethod(entity.getCodeChallengeMethod());
        dataObject.setIssuedAt(entity.getIssuedAt());
        dataObject.setExpireAt(entity.getExpireAt());
        dataObject.setUsed(entity.isUsed());
        return dataObject;
    }

    public static OAuthAuthorization toEntity(OAuthAuthorizationDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        OAuthAuthorization entity = new OAuthAuthorization();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setAuthorizationCode(dataObject.getAuthorizationCode());
        entity.setClientId(dataObject.getClientId());
        entity.setPrincipalKey(
                PrincipalKey.of(PrincipalType.from(dataObject.getPrincipalType()), dataObject.getPrincipalId()));
        entity.setRedirectUri(dataObject.getRedirectUri());
        entity.setScopes(readStringSet(dataObject.getScopes()));
        entity.setState(dataObject.getState());
        entity.setCodeChallenge(dataObject.getCodeChallenge());
        entity.setCodeChallengeMethod(dataObject.getCodeChallengeMethod());
        entity.setIssuedAt(dataObject.getIssuedAt());
        entity.setExpireAt(dataObject.getExpireAt());
        entity.setUsed(dataObject.isUsed());
        return entity;
    }

    public static List<OAuthAuthorization> toEntityList(List<OAuthAuthorizationDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<OAuthAuthorization> entities = new ArrayList<>();
        for (OAuthAuthorizationDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static String writeStringSet(Set<String> values) {
        if (values == null) {
            return "[]";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(values);
        } catch (Exception e) {
            throw new IllegalStateException("failed to write oauth authorization string set", e);
        }
    }

    private static LinkedHashSet<String> readStringSet(String value) {
        if (StringUtils.isBlank(value)) {
            return new LinkedHashSet<>();
        }
        try {
            LinkedHashSet<String> values = OBJECT_MAPPER.readValue(value, STRING_SET_TYPE);
            return values == null ? new LinkedHashSet<>() : values;
        } catch (Exception e) {
            throw new IllegalStateException("failed to read oauth authorization string set", e);
        }
    }
}
