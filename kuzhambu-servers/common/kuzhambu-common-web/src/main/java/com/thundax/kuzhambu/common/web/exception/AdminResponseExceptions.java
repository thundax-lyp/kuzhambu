package com.thundax.kuzhambu.common.web.exception;

public final class AdminResponseExceptions {

    private AdminResponseExceptions() {}

    public static ApiException invalidParameter(String name) {
        return new ApiException(
                WebErrorCode.BAD_REQUEST, "SYS-00001", "sys.exception.invalid-parameter", "无效的参数: " + name);
    }

    public static ApiException invalidToken() {
        return new ApiException(WebErrorCode.BAD_REQUEST, "AUTH-00006", "auth.exception.invalid-token", "token 已失效");
    }

    public static ApiException loginRequestTooMany() {
        return new ApiException(
                WebErrorCode.BAD_REQUEST, "AUTH-00005", "auth.exception.login-request-too-many", "登录请求过多");
    }

    public static ApiException invalidUsernamePassword() {
        return new ApiException(
                WebErrorCode.BAD_REQUEST, "AUTH-00002", "auth.exception.invalid-username-password", "用户名或密码错误");
    }

    public static ApiException invalidCaptcha() {
        return new ApiException(WebErrorCode.BAD_REQUEST, "AUTH-00001", "auth.exception.invalid-captcha", "验证码错误");
    }

    public static ApiException bannedAccount() {
        return new ApiException(WebErrorCode.FORBIDDEN, "AUTH-00004", "auth.exception.banned-account", "用户已禁用");
    }

    public static ApiException objectNotFound() {
        return new ApiException(WebErrorCode.NOT_FOUND, "SYS-00002", "sys.exception.object-not-found", "资源不存在");
    }

    public static ApiException objectExists() {
        return new ApiException(WebErrorCode.CONFLICT, "SYS-00003", "sys.exception.object-exists", "资源已存在");
    }

    public static ApiException moveTreeNode() {
        return new ApiException(WebErrorCode.BAD_REQUEST, "SYS-00004", "sys.exception.move-tree-node", "树节点移动失败");
    }

    public static ApiException permissionDenied() {
        return new ApiException(WebErrorCode.FORBIDDEN);
    }

    public static ApiException wecomLoginNotConfigured() {
        return new ApiException(
                WebErrorCode.SYSTEM_ERROR, "AUTH-00012", "auth.exception.wecom-login-not-configured", "企业微信登录未配置");
    }

    public static ApiException githubLoginNotConfigured() {
        return new ApiException(
                WebErrorCode.SYSTEM_ERROR, "AUTH-00013", "auth.exception.github-login-not-configured", "GitHub 登录未配置");
    }

    public static ApiException system(String message) {
        return new ApiException(WebErrorCode.SYSTEM_ERROR, message);
    }
}
