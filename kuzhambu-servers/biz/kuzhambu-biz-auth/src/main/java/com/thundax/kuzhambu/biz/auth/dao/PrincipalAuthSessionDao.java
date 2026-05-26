package com.thundax.kuzhambu.biz.auth.dao;

import com.thundax.kuzhambu.biz.auth.entity.PrincipalAuthSession;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalAuthSessionId;
import java.util.Date;

public interface PrincipalAuthSessionDao {

    PrincipalAuthSession getById(PrincipalAuthSessionId id);

    void insert(PrincipalAuthSession session, int expireSeconds);

    void touch(PrincipalAuthSessionId id, Date accessTime, int expireSeconds);

    void deleteById(PrincipalAuthSessionId id);
}
