package com.thundax.kuzhambu.system.application.auth.service;

import com.thundax.kuzhambu.system.application.auth.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.application.auth.service.command.AuthenticateIdentityCommand;
import com.thundax.kuzhambu.system.application.auth.service.command.AuthenticatePasswordCommand;
import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;

public interface PrincipalAuthService {

    @LayerPublicApi(reason = "统一认证主体非密码登录时解析登录标识的业务入口")
    PrincipalIdentity authenticateIdentity(AuthenticateIdentityCommand command);

    PrincipalIdentity authenticatePassword(AuthenticatePasswordCommand command);
}
