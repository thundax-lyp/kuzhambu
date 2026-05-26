package com.thundax.kuzhambu.biz.auth.dao;

import com.thundax.kuzhambu.biz.auth.entity.OAuthAuthorization;
import com.thundax.kuzhambu.common.core.id.EntityId;

public interface OAuthAuthorizationDao {

    OAuthAuthorization getById(EntityId id);

    OAuthAuthorization getByAuthorizationCode(String authorizationCode);

    EntityId insert(OAuthAuthorization authorization);

    int updateUsed(OAuthAuthorization authorization);

    int deleteByAuthorizationCode(String authorizationCode);
}
