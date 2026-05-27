package com.thundax.kuzhambu.system.domain.auth.repository;

import com.thundax.kuzhambu.common.core.id.EntityId;
import com.thundax.kuzhambu.system.domain.auth.model.entity.OAuthAuthorization;

public interface OAuthAuthorizationRepository {

    OAuthAuthorization getById(EntityId id);

    OAuthAuthorization getByAuthorizationCode(String authorizationCode);

    EntityId insert(OAuthAuthorization authorization);

    int updateUsed(OAuthAuthorization authorization);

    int deleteByAuthorizationCode(String authorizationCode);
}
