package com.thundax.kuzhambu.biz.auth.dao;

import com.thundax.kuzhambu.biz.auth.entity.OAuthClient;
import com.thundax.kuzhambu.biz.auth.entity.enums.OAuthClientStatus;
import com.thundax.kuzhambu.common.core.id.EntityId;

public interface OAuthClientDao {

    OAuthClient getById(EntityId id);

    OAuthClient getByClientId(String clientId);

    OAuthClient getByClientIdAndStatus(String clientId, OAuthClientStatus status);

    EntityId insert(OAuthClient client);

    int update(OAuthClient client);
}
