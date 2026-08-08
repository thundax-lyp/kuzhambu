package com.thundax.kuzhambu.system.interfaces.admin.auth.service;

import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthAccessTokenDTO;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthTokenQueryDTO;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthTokenRefreshDTO;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.support.AdminAuthLookup;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.support.AdminAuthOperation;
import org.springframework.lang.NonNull;

public interface AdminAuthService {

    @NonNull
    AuthAccessTokenDTO createAccessToken(AdminAuthOperation operation);

    AuthAccessTokenDTO getAccessToken(AdminAuthLookup lookup);

    int deleteAccessTokensByUserId(AdminAuthOperation operation);

    boolean validateToken(AdminAuthOperation operation);

    void activeAccessToken(AdminAuthOperation operation);

    void deleteAccessToken(AdminAuthOperation operation);

    AuthTokenQueryDTO getTokenInfo(AdminAuthLookup lookup);

    AuthTokenRefreshDTO refreshAccessToken(AdminAuthOperation operation);

    void invalidateSessionByToken(AdminAuthOperation operation);

    @LayerPublicApi(reason = "账号状态变化时按用户维度失效在线会话的业务入口")
    int invalidateSessionsByUserId(AdminAuthOperation operation);

    User authenticatePassword(AdminAuthOperation operation);

    User authenticateSms(AdminAuthOperation operation);

    User authenticateWecom(AdminAuthOperation operation);

    User authenticateGithub(AdminAuthOperation operation);

    void recordLoginFailed(AdminAuthOperation operation);

    void validatePassword(AdminAuthOperation operation);
}
