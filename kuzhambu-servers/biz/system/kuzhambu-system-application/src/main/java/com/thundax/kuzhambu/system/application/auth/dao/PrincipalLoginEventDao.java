package com.thundax.kuzhambu.system.application.auth.dao;

import com.thundax.kuzhambu.system.application.auth.entity.PrincipalLoginEvent;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalLoginEventId;

public interface PrincipalLoginEventDao {

    PrincipalLoginEvent getById(PrincipalLoginEventId id);

    PrincipalLoginEventId insert(PrincipalLoginEvent event);
}
