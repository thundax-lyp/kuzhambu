package com.thundax.kuzhambu.system.domain.auth.repository;

import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAuthSession;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAuthSessionId;
import java.time.Instant;

public interface PrincipalAuthSessionRepository {

    PrincipalAuthSession getById(PrincipalAuthSessionId id);

    void insert(PrincipalAuthSession session, int expireSeconds);

    void touch(PrincipalAuthSessionId id, Instant accessTime, int expireSeconds);

    void deleteById(PrincipalAuthSessionId id);
}
