package com.thundax.kuzhambu.system.domain.auth.repository;

import com.thundax.kuzhambu.system.domain.auth.model.entity.PreAuthSession;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionToken;

public interface PreAuthSessionRepository {

    int count();

    PreAuthSession getById(PreAuthSessionId id);

    PreAuthSessionId getByToken(PreAuthSessionToken token);

    PreAuthSessionId getByRefreshToken(PreAuthSessionToken refreshToken);

    void insert(PreAuthSession session);

    void update(PreAuthSession session);

    void deleteById(PreAuthSessionId id);

    default void deleteByToken(PreAuthSessionToken token) {
        PreAuthSessionId id = getByToken(token);
        if (id != null) {
            deleteById(id);
        }
    }
}
