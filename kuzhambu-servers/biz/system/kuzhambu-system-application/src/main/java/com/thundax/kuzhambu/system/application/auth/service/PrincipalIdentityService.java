package com.thundax.kuzhambu.system.application.auth.service;

import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.application.auth.service.command.PrincipalIdentityCommand;
import com.thundax.kuzhambu.system.application.auth.service.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalIdentityId;
import java.util.List;

public interface PrincipalIdentityService {

    @LayerPublicApi(reason = "统一认证主体资料维护时按查询条件读取登录标识的业务入口")
    PrincipalIdentity get(PrincipalIdentityQuery query);

    @LayerPublicApi(reason = "统一认证主体资料维护时按查询条件列出登录标识的业务入口")
    List<PrincipalIdentity> list(PrincipalIdentityQuery query);

    @LayerPublicApi(reason = "统一认证主体资料维护时新增登录标识的业务入口")
    PrincipalIdentityId create(PrincipalIdentityCommand command);

    @LayerPublicApi(reason = "统一认证主体资料维护时更新登录标识的业务入口")
    void change(PrincipalIdentityCommand command);

    @LayerPublicApi(reason = "统一认证主体资料维护时启停登录标识的业务入口")
    void changeStatus(PrincipalIdentityCommand command);
}
