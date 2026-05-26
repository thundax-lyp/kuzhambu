package com.thundax.kuzhambu.biz.audit.runtime.sys;

import com.thundax.kuzhambu.biz.audit.runtime.AuditObjectLoader;
import com.thundax.kuzhambu.biz.core.entity.valueobject.UserIdCodec;
import com.thundax.kuzhambu.biz.core.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class UserAuditObjectLoader implements AuditObjectLoader {

    private static final String OBJECT_TYPE = "User";

    private final UserService userService;

    public UserAuditObjectLoader(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String objectType() {
        return OBJECT_TYPE;
    }

    @Override
    public Object load(String objectId) {
        return userService.get(UserIdCodec.toDomain(Long.valueOf(objectId)));
    }
}
