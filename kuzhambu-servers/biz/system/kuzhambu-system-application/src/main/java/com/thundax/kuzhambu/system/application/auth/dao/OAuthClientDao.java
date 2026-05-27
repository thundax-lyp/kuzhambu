package com.thundax.kuzhambu.system.application.auth.dao;

import com.thundax.kuzhambu.common.core.id.EntityId;
import com.thundax.kuzhambu.system.domain.model.entity.OAuthClient;
import com.thundax.kuzhambu.system.domain.model.enums.OAuthClientStatus;

public interface OAuthClientDao {

    OAuthClient getById(EntityId id);

    OAuthClient getByClientId(String clientId);

    OAuthClient getByClientIdAndStatus(String clientId, OAuthClientStatus status);

    EntityId insert(OAuthClient client);

    int update(OAuthClient client);
}
