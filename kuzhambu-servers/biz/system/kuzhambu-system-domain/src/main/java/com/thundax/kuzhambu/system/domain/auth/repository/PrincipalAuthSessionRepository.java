package com.thundax.kuzhambu.system.domain.auth.repository;

import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAuthSession;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAuthSessionId;
import java.util.Date;

public interface PrincipalAuthSessionRepository {

    PrincipalAuthSession getById(PrincipalAuthSessionId id);

    void insert(PrincipalAuthSession session, int expireSeconds);

    void touch(PrincipalAuthSessionId id, Date accessTime, int expireSeconds);

    void deleteById(PrincipalAuthSessionId id);
}
