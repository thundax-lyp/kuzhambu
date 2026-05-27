package com.thundax.kuzhambu.system.application.auth.dao;

import com.thundax.kuzhambu.common.core.id.EntityId;
import com.thundax.kuzhambu.system.application.auth.entity.OAuthAuthorization;

public interface OAuthAuthorizationDao {

    OAuthAuthorization getById(EntityId id);

    OAuthAuthorization getByAuthorizationCode(String authorizationCode);

    EntityId insert(OAuthAuthorization authorization);

    int updateUsed(OAuthAuthorization authorization);

    int deleteByAuthorizationCode(String authorizationCode);
}
