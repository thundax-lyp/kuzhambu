package com.thundax.kuzhambu.biz.auth.dao;

import com.thundax.kuzhambu.biz.auth.entity.PrincipalLoginEvent;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalLoginEventId;

public interface PrincipalLoginEventDao {

    PrincipalLoginEvent getById(PrincipalLoginEventId id);

    PrincipalLoginEventId insert(PrincipalLoginEvent event);
}
