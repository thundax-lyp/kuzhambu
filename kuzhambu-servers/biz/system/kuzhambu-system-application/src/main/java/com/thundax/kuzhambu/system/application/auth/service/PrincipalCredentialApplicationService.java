package com.thundax.kuzhambu.system.application.auth.service;

import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.system.application.auth.command.PrincipalCredentialCommand;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalCredentialQuery;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalCredential;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalCredentialId;
import java.util.List;

public interface PrincipalCredentialApplicationService {

    @LayerPublicApi(reason = "统一认证主体资料维护时按查询条件读取凭据的业务入口")
    PrincipalCredential get(PrincipalCredentialQuery query);

    @LayerPublicApi(reason = "统一认证主体资料维护时按查询条件列出凭据的业务入口")
    List<PrincipalCredential> list(PrincipalCredentialQuery query);

    @LayerPublicApi(reason = "统一认证主体资料维护时新增凭据的业务入口")
    PrincipalCredentialId create(PrincipalCredentialCommand command);

    @LayerPublicApi(reason = "统一认证主体资料维护时更新凭据的业务入口")
    void change(PrincipalCredentialCommand command);

    @LayerPublicApi(reason = "统一认证主体资料维护时更新凭据状态的业务入口")
    void changeStatus(PrincipalCredentialCommand command);

    @LayerPublicApi(reason = "统一认证主体登录时更新凭据验证状态的业务入口")
    void changeVerifyState(PrincipalCredentialCommand command);
}
