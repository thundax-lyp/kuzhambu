package com.thundax.kuzhambu.biz.core.service.handler;

import com.thundax.kuzhambu.biz.core.entity.User;

public interface UserDeleteCascadeHandler {

    void beforeDelete(User user);
}
