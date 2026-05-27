package com.thundax.kuzhambu.system.application.audit.runtime.sys;

import com.thundax.kuzhambu.system.application.audit.runtime.AuditObjectLoader;
import com.thundax.kuzhambu.system.application.core.service.UserApplicationService;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import org.springframework.stereotype.Component;

@Component
public class UserAuditObjectLoader implements AuditObjectLoader {

    private static final String OBJECT_TYPE = "User";

    private final UserApplicationService userService;

    public UserAuditObjectLoader(UserApplicationService userService) {
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
