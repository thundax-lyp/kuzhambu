package com.thundax.kuzhambu.system.application.core.service.handler;

import com.thundax.kuzhambu.system.domain.core.model.entity.User;

public interface UserDeleteCascadeHandler {

    void beforeDelete(User user);
}
