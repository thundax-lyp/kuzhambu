package com.thundax.kuzhambu.system.interfaces.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.AuthController;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.CaptchaController;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request.AuthLoginFormRefreshRequest;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request.AuthLoginRequest;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request.AuthLogoutRequest;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request.CaptchaRefreshRequest;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request.TokenRefreshRequest;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.AuthAccessTokenResponse;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.AuthLoginFormResponse;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.CaptchaRefreshResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.CurrentUserController;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalInfoResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalMenuResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalPermsResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class SystemAuthCurrentUserContractTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void authAndCurrentUserRoutesShouldKeepAdminApiPaths() throws Exception {
        assertRequestMapping(AuthController.class, "/api/auth/session");
        assertPostMapping(AuthController.class, "preAuthSession", "pre-auth-session");
        assertPostMapping(
                AuthController.class,
                "refreshPreAuthSession",
                "pre-auth-session/refresh",
                AuthLoginFormRefreshRequest.class);
        assertPostMapping(AuthController.class, "login", "login", AuthLoginRequest.class);
        assertPostMapping(AuthController.class, "logout", "logout", AuthLogoutRequest.class);
        assertPostMapping(AuthController.class, "refreshToken", "token/refresh", TokenRefreshRequest.class);

        assertRequestMapping(CaptchaController.class, "/api/auth/captcha");
        assertGetMapping(CaptchaController.class, "captcha", HttpServletRequest.class, HttpServletResponse.class);
        assertPostMapping(CaptchaController.class, "refreshCaptcha", "refresh", CaptchaRefreshRequest.class);

        assertRequestMapping(CurrentUserController.class, "/api/sys/current-user");
        assertPostMapping(CurrentUserController.class, "info", "info");
        assertPostMapping(CurrentUserController.class, "menus", "menus");
        assertPostMapping(CurrentUserController.class, "perms", "perms");
    }

    @Test
    void authRequestsShouldKeepJsonFieldNames() throws Exception {
        AuthLoginRequest loginRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "loginToken": "login-token",
                  "userName": "developer",
                  "password": "encrypted-password",
                  "captcha": "1234"
                }
                """,
                AuthLoginRequest.class);

        assertEquals("login-token", loginRequest.getLoginToken());
        assertEquals("developer", loginRequest.getUsername());
        assertEquals("encrypted-password", loginRequest.getPassword());
        assertEquals("1234", loginRequest.getCaptcha());
        assertJsonFields(loginRequest, "loginToken", "userName", "password", "captcha");

        AuthLoginFormRefreshRequest loginFormRefreshRequest =
                OBJECT_MAPPER.readValue("{\"refreshToken\":\"pre-auth-refresh\"}", AuthLoginFormRefreshRequest.class);
        assertEquals("pre-auth-refresh", loginFormRefreshRequest.getRefreshToken());
        assertJsonFields(loginFormRefreshRequest, "refreshToken");

        CaptchaRefreshRequest captchaRefreshRequest =
                OBJECT_MAPPER.readValue("{\"loginToken\":\"login-token\"}", CaptchaRefreshRequest.class);
        assertEquals("login-token", captchaRefreshRequest.getLoginToken());
        assertJsonFields(captchaRefreshRequest, "loginToken");

        TokenRefreshRequest tokenRefreshRequest = OBJECT_MAPPER.readValue(
                "{\"clientId\":\"admin-api\",\"refreshToken\":\"refresh-token\"}", TokenRefreshRequest.class);
        assertEquals("admin-api", tokenRefreshRequest.getClientId());
        assertEquals("refresh-token", tokenRefreshRequest.getRefreshToken());
        assertJsonFields(tokenRefreshRequest, "clientId", "refreshToken");

        AuthLogoutRequest logoutRequest =
                OBJECT_MAPPER.readValue("{\"token\":\"access-token\"}", AuthLogoutRequest.class);
        assertEquals("access-token", logoutRequest.getToken());
        assertJsonFields(logoutRequest, "token");
    }

    @Test
    void authAndCurrentUserResponsesShouldKeepJsonFieldNames() throws Exception {
        assertJsonFields(
                AuthLoginFormResponse.builder()
                        .loginToken("login-token")
                        .refreshToken("pre-auth-refresh")
                        .expiredAt(1778513052155L)
                        .publicKey("public-key")
                        .build(),
                "loginToken",
                "refreshToken",
                "expiredAt",
                "publicKey");

        assertJsonFields(
                AuthAccessTokenResponse.builder()
                        .token("access-token")
                        .refreshToken("refresh-token")
                        .expireAt(1778514052155L)
                        .build(),
                "token",
                "refreshToken",
                "expireAt");

        assertJsonFields(CaptchaRefreshResponse.builder().refreshed(true).build(), "refreshed");

        assertJsonFields(
                PersonalInfoResponse.builder()
                        .id("user-1")
                        .loginName("developer")
                        .ranks(1)
                        .name("Developer")
                        .email("developer@example.com")
                        .mobile("13800000000")
                        .avatar("/avatar.png")
                        .admin(true)
                        .superAdmin(false)
                        .build(),
                "id",
                "loginName",
                "ranks",
                "name",
                "email",
                "mobile",
                "avatar",
                "admin",
                "superAdmin");

        assertJsonFields(
                PersonalMenuResponse.builder()
                        .id("menu-1")
                        .parentId("menu-root")
                        .name("仪表盘")
                        .url("/dashboard")
                        .icon("dashboard")
                        .displayParams("{\"icon\":\"dashboard\"}")
                        .build(),
                "id",
                "parentId",
                "name",
                "url",
                "icon",
                "displayParams");

        assertJsonFields(
                PersonalPermsResponse.builder().perms(Set.of("sys:user:view")).build(), "perms");
    }

    private static void assertRequestMapping(Class<?> controllerType, String expectedPath) {
        RequestMapping mapping = controllerType.getAnnotation(RequestMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private static void assertPostMapping(
            Class<?> controllerType, String methodName, String expectedPath, Class<?>... parameterTypes)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterTypes);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private static void assertGetMapping(Class<?> controllerType, String methodName, Class<?>... parameterTypes)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterTypes);
        assertTrue(method.isAnnotationPresent(GetMapping.class));
    }

    private static void assertJsonFields(Object value, String... expectedFields) throws Exception {
        JsonNode json = OBJECT_MAPPER.valueToTree(value);
        assertEquals(expectedFields.length, json.size(), json::toString);
        for (String expectedField : expectedFields) {
            assertTrue(json.has(expectedField), json::toString);
        }
    }
}
