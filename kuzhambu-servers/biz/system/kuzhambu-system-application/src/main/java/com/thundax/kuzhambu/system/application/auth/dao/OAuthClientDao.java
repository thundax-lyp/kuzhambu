package com.thundax.kuzhambu.system.application.auth.dao;

import com.thundax.kuzhambu.system.application.auth.entity.OAuthClient;
import com.thundax.kuzhambu.system.application.auth.entity.enums.OAuthClientStatus;
import com.thundax.kuzhambu.common.core.id.EntityId;

public interface OAuthClientDao {

    OAuthClient getById(EntityId id);

    OAuthClient getByClientId(String clientId);

    OAuthClient getByClientIdAndStatus(String clientId, OAuthClientStatus status);

    EntityId insert(OAuthClient client);

    int update(OAuthClient client);
}
