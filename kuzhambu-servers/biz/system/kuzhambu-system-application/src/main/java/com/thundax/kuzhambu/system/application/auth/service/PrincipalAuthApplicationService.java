package com.thundax.kuzhambu.system.application.auth.service;

import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.system.application.auth.command.AuthenticateIdentityCommand;
import com.thundax.kuzhambu.system.application.auth.command.AuthenticatePasswordCommand;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalIdentity;

public interface PrincipalAuthApplicationService {

    @LayerPublicApi(reason = "统一认证主体非密码登录时解析登录标识的业务入口")
    PrincipalIdentity authenticateIdentity(AuthenticateIdentityCommand command);

    PrincipalIdentity authenticatePassword(AuthenticatePasswordCommand command);
}
