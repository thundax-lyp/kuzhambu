package com.thundax.kuzhambu.system.interfaces.admin.auth.service.support;

import com.thundax.kuzhambu.common.web.util.RequestIpUtils;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalAuthenticationMethod;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthAccessTokenDTO;
import jakarta.servlet.http.HttpServletRequest;

public final class AdminAuthHelper {

    private AdminAuthHelper() {}

    public static AdminAuthLookup tokenLookup(String token) {
        AdminAuthLookup lookup = new AdminAuthLookup();
        lookup.setToken(token);
        return lookup;
    }

    public static AdminAuthOperation passwordOperation(
            String loginName, String plainPassword, HttpServletRequest request) {
        AdminAuthOperation operation = new AdminAuthOperation();
        operation.setLoginName(loginName);
        operation.setPlainPassword(plainPassword);
        return withRequestContext(operation, request);
    }

    public static AdminAuthOperation accessTokenOperation(UserId userId, String loginName, HttpServletRequest request) {
        AdminAuthOperation operation = new AdminAuthOperation();
        operation.setUserId(userId);
        operation.setLoginName(loginName);
        return withRequestContext(operation, request);
    }

    public static AdminAuthOperation userIdOperation(UserId userId) {
        AdminAuthOperation operation = new AdminAuthOperation();
        operation.setUserId(userId);
        return operation;
    }

    public static AdminAuthOperation mobileOperation(String mobile, HttpServletRequest request) {
        AdminAuthOperation operation = new AdminAuthOperation();
        operation.setMobile(mobile);
        return withRequestContext(operation, request);
    }

    public static AdminAuthOperation codeOperation(String code, HttpServletRequest request) {
        AdminAuthOperation operation = new AdminAuthOperation();
        operation.setCode(code);
        return withRequestContext(operation, request);
    }

    public static AdminAuthOperation accessTokenOperation(AuthAccessTokenDTO accessToken, HttpServletRequest request) {
        AdminAuthOperation operation = accessTokenOperation(accessToken);
        return withRequestContext(operation, request);
    }

    public static AdminAuthOperation accessTokenOperation(AuthAccessTokenDTO accessToken) {
        AdminAuthOperation operation = new AdminAuthOperation();
        operation.setAccessToken(accessToken);
        return operation;
    }

    public static AdminAuthOperation refreshTokenOperation(
            String clientId, String refreshToken, HttpServletRequest request) {
        AdminAuthOperation operation = new AdminAuthOperation();
        operation.setClientId(clientId);
        operation.setRefreshToken(refreshToken);
        return withRequestContext(operation, request);
    }

    public static AdminAuthOperation loginFailedOperation(
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType,
            HttpServletRequest request,
            String reason) {
        AdminAuthOperation operation = new AdminAuthOperation();
        operation.setAuthenticationMethod(authenticationMethod);
        operation.setIdentityType(identityType);
        operation.setReason(reason);
        return withRequestContext(operation, request);
    }

    private static AdminAuthOperation withRequestContext(AdminAuthOperation operation, HttpServletRequest request) {
        operation.setIp(RequestIpUtils.getIpAddr(request));
        if (request != null) {
            operation.setUserAgent(request.getHeader("user-agent"));
        }
        return operation;
    }
}
