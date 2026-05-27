package com.thundax.kuzhambu.system.domain.auth.repository;

import com.thundax.kuzhambu.common.core.id.EntityId;
import com.thundax.kuzhambu.system.domain.auth.model.entity.OAuthClient;
import com.thundax.kuzhambu.system.domain.auth.model.enums.OAuthClientStatus;

public interface OAuthClientRepository {

    OAuthClient getById(EntityId id);

    OAuthClient getByClientId(String clientId);

    OAuthClient getByClientIdAndStatus(String clientId, OAuthClientStatus status);

    EntityId insert(OAuthClient client);

    int update(OAuthClient client);
}
