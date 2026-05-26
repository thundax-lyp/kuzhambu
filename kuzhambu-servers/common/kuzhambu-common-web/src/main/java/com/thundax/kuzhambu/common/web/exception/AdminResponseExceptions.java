package com.thundax.kuzhambu.common.web.exception;

public final class AdminResponseExceptions {

    private AdminResponseExceptions() {}

    public static KuzhambuException invalidParameter(String name) {
        return new KuzhambuException(
                WebErrorCode.BAD_REQUEST, "SYS-00001", "sys.exception.invalid-parameter", "无效的参数: " + name);
    }

    public static KuzhambuException invalidToken() {
        return new KuzhambuException(
                WebErrorCode.BAD_REQUEST, "AUTH-00006", "auth.exception.invalid-token", "token 已失效");
    }

    public static KuzhambuException loginRequestTooMany() {
        return new KuzhambuException(
                WebErrorCode.BAD_REQUEST, "AUTH-00005", "auth.exception.login-request-too-many", "登录请求过多");
    }

    public static KuzhambuException invalidUsernamePassword() {
        return new KuzhambuException(
                WebErrorCode.BAD_REQUEST, "AUTH-00002", "auth.exception.invalid-username-password", "用户名或密码错误");
    }

    public static KuzhambuException bannedAccount() {
        return new KuzhambuException(WebErrorCode.FORBIDDEN, "AUTH-00004", "auth.exception.banned-account", "用户已禁用");
    }

    public static KuzhambuException objectNotFound() {
        return new KuzhambuException(WebErrorCode.NOT_FOUND, "SYS-00002", "sys.exception.object-not-found", "资源不存在");
    }

    public static KuzhambuException objectExists() {
        return new KuzhambuException(WebErrorCode.CONFLICT, "SYS-00003", "sys.exception.object-exists", "资源已存在");
    }

    public static KuzhambuException moveTreeNode() {
        return new KuzhambuException(WebErrorCode.BAD_REQUEST, "SYS-00004", "sys.exception.move-tree-node", "树节点移动失败");
    }

    public static KuzhambuException permissionDenied() {
        return new KuzhambuException(WebErrorCode.FORBIDDEN);
    }

    public static KuzhambuException oauth2AuthorizationNotConfigured() {
        return new KuzhambuException(
                WebErrorCode.SYSTEM_ERROR,
                "AUTH-00007",
                "auth.exception.oauth2-authorization-not-configured",
                "OAuth2 authorization 未配置");
    }

    public static KuzhambuException oauth2GrantTypeUnsupported() {
        return new KuzhambuException(
                WebErrorCode.BAD_REQUEST,
                "AUTH-00008",
                "auth.exception.oauth2-grant-type-unsupported",
                "OAuth2 grant type unsupported");
    }

    public static KuzhambuException oauth2ClientNotConfigured() {
        return new KuzhambuException(
                WebErrorCode.SYSTEM_ERROR,
                "AUTH-00009",
                "auth.exception.oauth2-client-not-configured",
                "OAuth2 client 未配置");
    }

    public static KuzhambuException oauth2ClientSecretInvalid() {
        return new KuzhambuException(
                WebErrorCode.BAD_REQUEST,
                "AUTH-00010",
                "auth.exception.oauth2-client-secret-invalid",
                "OAuth2 client secret invalid");
    }

    public static KuzhambuException oauth2ClientRequestInvalid() {
        return new KuzhambuException(
                WebErrorCode.BAD_REQUEST,
                "AUTH-00011",
                "auth.exception.oauth2-client-request-invalid",
                "OAuth2 client request invalid");
    }

    public static KuzhambuException wecomLoginNotConfigured() {
        return new KuzhambuException(
                WebErrorCode.SYSTEM_ERROR, "AUTH-00012", "auth.exception.wecom-login-not-configured", "企业微信登录未配置");
    }

    public static KuzhambuException githubLoginNotConfigured() {
        return new KuzhambuException(
                WebErrorCode.SYSTEM_ERROR, "AUTH-00013", "auth.exception.github-login-not-configured", "GitHub 登录未配置");
    }

    public static KuzhambuException system(String message) {
        return new SystemException(message);
    }
}
