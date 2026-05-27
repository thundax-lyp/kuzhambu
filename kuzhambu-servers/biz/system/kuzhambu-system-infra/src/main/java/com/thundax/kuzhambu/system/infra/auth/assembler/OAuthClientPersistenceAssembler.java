package com.thundax.kuzhambu.system.infra.auth.assembler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.id.EntityIdCodec;
import com.thundax.kuzhambu.system.application.auth.entity.OAuthClient;
import com.thundax.kuzhambu.system.application.auth.entity.enums.OAuthClientStatus;
import com.thundax.kuzhambu.system.infra.auth.dataobject.OAuthClientDO;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public final class OAuthClientPersistenceAssembler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<LinkedHashSet<String>> STRING_SET_TYPE =
            new TypeReference<LinkedHashSet<String>>() {};

    private OAuthClientPersistenceAssembler() {}

    public static OAuthClientDO toDataObject(OAuthClient entity) {
        if (entity == null) {
            return null;
        }
        OAuthClientDO dataObject = new OAuthClientDO();
        dataObject.setId(EntityIdCodec.toValue(entity.getId()));
        dataObject.setClientId(entity.getClientId());
        dataObject.setClientSecretHash(entity.getClientSecretHash());
        dataObject.setClientName(entity.getClientName());
        dataObject.setClientType(entity.getClientType());
        dataObject.setGrantTypes(writeStringSet(entity.getGrantTypes()));
        dataObject.setScopes(writeStringSet(entity.getScopes()));
        dataObject.setRedirectUris(writeStringSet(entity.getRedirectUris()));
        dataObject.setAccessTokenTtlSeconds(entity.getAccessTokenTtlSeconds());
        dataObject.setRefreshTokenTtlSeconds(entity.getRefreshTokenTtlSeconds());
        dataObject.setStatus(statusValue(entity.getStatus()));
        dataObject.setContact(entity.getContact());
        dataObject.setRemark(entity.getRemark());
        return dataObject;
    }

    public static OAuthClient toEntity(OAuthClientDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        OAuthClient entity = new OAuthClient();
        entity.setId(EntityIdCodec.toDomain(dataObject.getId()));
        entity.setClientId(dataObject.getClientId());
        entity.setClientSecretHash(dataObject.getClientSecretHash());
        entity.setClientName(dataObject.getClientName());
        entity.setClientType(dataObject.getClientType());
        entity.setGrantTypes(readStringSet(dataObject.getGrantTypes()));
        entity.setScopes(readStringSet(dataObject.getScopes()));
        entity.setRedirectUris(readStringSet(dataObject.getRedirectUris()));
        entity.setAccessTokenTtlSeconds(dataObject.getAccessTokenTtlSeconds());
        entity.setRefreshTokenTtlSeconds(dataObject.getRefreshTokenTtlSeconds());
        entity.setStatus(statusFrom(dataObject.getStatus()));
        entity.setContact(dataObject.getContact());
        entity.setRemark(dataObject.getRemark());
        return entity;
    }

    public static List<OAuthClient> toEntityList(List<OAuthClientDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<OAuthClient> entities = new ArrayList<>();
        for (OAuthClientDO dataObject : dataObjects) {
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
            throw new IllegalStateException("failed to write oauth client string set", e);
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
            throw new IllegalStateException("failed to read oauth client string set", e);
        }
    }

    private static String statusValue(OAuthClientStatus status) {
        return status == null ? null : status.value();
    }

    private static OAuthClientStatus statusFrom(String status) {
        return status == null ? null : OAuthClientStatus.from(status);
    }
}
