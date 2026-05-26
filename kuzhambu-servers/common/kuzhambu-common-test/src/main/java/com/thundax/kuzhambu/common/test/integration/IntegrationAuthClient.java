package com.thundax.kuzhambu.common.test.integration;

import java.util.LinkedHashMap;
import java.util.Map;

public class IntegrationAuthClient {

    private final IntegrationHttpClient httpClient;

    public IntegrationAuthClient(IntegrationHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public PreAuthSession createAdminPreAuthSession() {
        return preAuthSession("/api/auth/session/pre-auth-session", null);
    }

    public PreAuthSession refreshAdminPreAuthSession(String refreshToken) {
        return preAuthSession("/api/auth/session/pre-auth-session/refresh", request("refreshToken", refreshToken));
    }

    public AuthToken loginAdmin(String loginToken, String userName, String password, String captcha) {
        Map<String, Object> request = request("loginToken", loginToken);
        request.put("userName", userName);
        request.put("password", password);
        request.put("captcha", captcha);
        return authToken("/api/auth/session/login", request);
    }

    public AuthToken refreshAdminToken(String refreshToken) {
        return authToken("/api/auth/session/token/refresh", request("refreshToken", refreshToken));
    }

    public Boolean logoutAdmin(String token) {
        return data("/api/auth/session/logout", request("token", token), Boolean.class);
    }

    public PreAuthSession createFrontPreAuthSession() {
        return preAuthSession("/api/auth/session/pre-auth-session", null);
    }

    public PreAuthSession refreshFrontPreAuthSession(String refreshToken) {
        return preAuthSession("/api/auth/session/pre-auth-session/refresh", request("refreshToken", refreshToken));
    }

    public AuthToken loginFrontAccount(String loginToken, String account, String password, String captcha) {
        Map<String, Object> request = request("loginToken", loginToken);
        request.put("account", account);
        request.put("password", password);
        request.put("captcha", captcha);
        return authToken("/api/auth/session/login", request);
    }

    public AuthToken refreshFrontToken(String refreshToken) {
        return authToken("/api/auth/session/token/refresh", request("refreshToken", refreshToken));
    }

    public Boolean logoutFront(String accessToken) {
        return data("/api/auth/session/logout", request("accessToken", accessToken), Boolean.class);
    }

    private PreAuthSession preAuthSession(String path, Map<String, Object> request) {
        Map<String, Object> data = dataMap(path, request == null ? new LinkedHashMap<String, Object>() : request);
        return new PreAuthSession(
                stringValue(data, "loginToken"), stringValue(data, "refreshToken"), stringValue(data, "publicKey"));
    }

    private AuthToken authToken(String path, Map<String, Object> request) {
        Map<String, Object> data = dataMap(path, request);
        return new AuthToken(
                stringValue(data, "token"), stringValue(data, "refreshToken"), longValue(data, "expireAt"));
    }

    private <T> T data(String path, Map<String, Object> request, Class<T> dataType) {
        Object data = responseData(httpClient.postJson(path, request, Map.class));
        return dataType.cast(data);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> dataMap(String path, Map<String, Object> request) {
        Object data = responseData(httpClient.postJson(path, request, Map.class));
        return data == null ? new LinkedHashMap<String, Object>() : (Map<String, Object>) data;
    }

    private Object responseData(Map<?, ?> response) {
        return response == null ? null : response.get("data");
    }

    private static Map<String, Object> request(String name, Object value) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put(name, value);
        return request;
    }

    private static String stringValue(Map<String, Object> values, String name) {
        Object value = values.get(name);
        return value == null ? null : String.valueOf(value);
    }

    private static Long longValue(Map<String, Object> values, String name) {
        Object value = values.get(name);
        if (value == null) {
            return null;
        }
        return value instanceof Number ? ((Number) value).longValue() : Long.valueOf(String.valueOf(value));
    }

    public static class PreAuthSession {
        private final String loginToken;
        private final String refreshToken;
        private final String publicKey;

        public PreAuthSession(String loginToken, String refreshToken, String publicKey) {
            this.loginToken = loginToken;
            this.refreshToken = refreshToken;
            this.publicKey = publicKey;
        }

        public String getLoginToken() {
            return loginToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public String getPublicKey() {
            return publicKey;
        }
    }

    public static class AuthToken {
        private final String token;
        private final String refreshToken;
        private final Long expireAt;

        public AuthToken(String token, String refreshToken, Long expireAt) {
            this.token = token;
            this.refreshToken = refreshToken;
            this.expireAt = expireAt;
        }

        public String getToken() {
            return token;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public Long getExpireAt() {
            return expireAt;
        }
    }
}
