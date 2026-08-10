package com.thundax.kuzhambu.system.interfaces.admin.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.crypto.Sm2Crypto;
import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.exception.KuzhambuException;
import com.thundax.kuzhambu.common.web.util.RequestIpUtils;
import com.thundax.kuzhambu.system.application.auth.configure.AuthProperties;
import com.thundax.kuzhambu.system.application.auth.exception.InvalidCaptchaException;
import com.thundax.kuzhambu.system.application.auth.service.PreAuthSessionApplicationService;
import com.thundax.kuzhambu.system.application.auth.utils.PreAuthCodeHelper;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PreAuthSession;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalLoginEvent;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalAuthenticationMethod;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.domain.core.model.entity.Log;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.enums.LogType;
import com.thundax.kuzhambu.system.interfaces.admin.auth.assembler.AuthInterfaceAssembler;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request.AuthLoginFormRefreshRequest;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request.AuthLoginRequest;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request.AuthLogoutRequest;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request.AuthTokenRequest;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request.GithubLoginRequest;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request.SmsLoginRequest;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request.TokenRefreshRequest;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request.WecomLoginRequest;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.AuthAccessTokenResponse;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.AuthLoginFormResponse;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.TokenVerifyResponse;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.AdminAuthService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthAccessTokenDTO;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.support.AdminAuthHelper;
import com.thundax.kuzhambu.system.interfaces.admin.core.service.SysLogMessageService;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Tag(name = "系统模块-认证", description = "认证")
@RequestMapping(value = "/api/auth/session")
@SysLogger(module = {"系统", "登录"})
@WrappedApiController
@PublicApi
public class AuthController {

    private static final String CAPTCHA_ITEM = "CAPTCHA";
    private static final String PUBLIC_KEY_ITEM = "publicKey";
    private static final String PRIVATE_KEY_ITEM = "privateKey";
    private static final String SMS_MOBILE_ITEM = "SMS_MOBILE";
    private static final String SMS_VALIDATE_CODE_ITEM = "SMS_VALIDATE_CODE";
    private static final int CAPTCHA_EXPIRED_SECONDS = 60;
    private static final int REFRESH_TOKEN_GRACE_SECONDS = 60;

    private final AdminAuthService authService;
    private final PreAuthSessionApplicationService preAuthSessionService;
    private final AuthProperties properties;
    private final SysLogMessageService sysLogMessageService;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuthController(
            AdminAuthService authService,
            PreAuthSessionApplicationService preAuthSessionService,
            AuthProperties properties,
            SysLogMessageService sysLogMessageService,
            ObjectMapper objectMapper) {
        this.authService = authService;
        this.preAuthSessionService = preAuthSessionService;
        this.properties = properties;
        this.sysLogMessageService = sysLogMessageService;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "请求预认证会话")
    @ApiImplicitParams({})
    @PostMapping(value = "pre-auth-session")
    @SysLogger(value = "请求预认证会话")
    public AuthLoginFormResponse preAuthSession() {
        return AuthInterfaceAssembler.toLoginFormResponse(createPreAuthSession());
    }

    @Operation(summary = "刷新预认证会话")
    @ApiImplicitParams({})
    @PostMapping(value = "pre-auth-session/refresh")
    @SysLogger(value = "刷新预认证会话")
    public AuthLoginFormResponse refreshPreAuthSession(@Valid @RequestBody AuthLoginFormRefreshRequest request) {
        if (StringUtils.isBlank(request.getRefreshToken())) {
            throw AdminResponseExceptions.invalidParameter("refreshToken");
        }

        return AuthInterfaceAssembler.toLoginFormResponse(refreshPreAuthSession(request.getRefreshToken()));
    }

    @Operation(summary = "用户/密码登录")
    @ApiImplicitParams({})
    @PostMapping(value = "login")
    @SysLogger(value = "用户/密码登录")
    public AuthAccessTokenResponse login(@Valid @RequestBody AuthLoginRequest request) {
        HttpServletRequest currentRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (!validateCaptcha(request.getLoginToken(), request.getCaptcha())) {
            createCaptcha(request.getLoginToken());
            writeLog(currentRequest, "验证码失败", request);
            authService.recordLoginFailed(AdminAuthHelper.loginFailedOperation(
                    PrincipalAuthenticationMethod.PASSWORD,
                    PrincipalIdentityType.USER_ACCOUNT,
                    currentRequest,
                    PrincipalLoginEvent.REASON_CAPTCHA_INVALID));
            throw new InvalidCaptchaException();
        }
        createCaptcha(request.getLoginToken());

        String privateKey = getPrivateKey(request.getLoginToken());
        String password = Sm2Crypto.decrypt(request.getPassword(), privateKey);

        User user;
        try {
            user = authService.authenticatePassword(
                    AdminAuthHelper.passwordOperation(request.getUsername(), password, currentRequest));
        } catch (KuzhambuException e) {
            if (e.getMessage() != null && e.getMessage().contains("锁定")) {
                writeLog(currentRequest, "用户锁定", request);
            } else if (e.getMessage() != null && e.getMessage().contains("密码输入错误")) {
                writeLog(currentRequest, "密码输入错误", request);
            } else if (!"AUTH-00002".equals(e.getCode())) {
                writeLog(currentRequest, "认证失败", request);
            } else {
                writeLog(currentRequest, "用户失败", request);
            }
            throw e;
        }

        releasePreAuthSession(request.getLoginToken());

        authService.deleteAccessTokensByUserId(AdminAuthHelper.userIdOperation(user.getId()));

        return loginSuccess(
                user,
                request.getUsername(),
                "用户/密码登录成功",
                PrincipalAuthenticationMethod.PASSWORD,
                PrincipalIdentityType.USER_ACCOUNT);
    }

    @Operation(summary = "短信登录")
    @ApiImplicitParams({})
    @PostMapping(value = "login/sms")
    @SysLogger(value = "短信登录")
    public AuthAccessTokenResponse loginBySms(@Valid @RequestBody SmsLoginRequest request) {
        HttpServletRequest currentRequest = currentRequest();
        if (!validateSmsValidateCode(request.getLoginToken(), request.getMobile(), request.getValidateCode())) {
            authService.recordLoginFailed(AdminAuthHelper.loginFailedOperation(
                    PrincipalAuthenticationMethod.SMS_CODE,
                    PrincipalIdentityType.USER_MOBILE,
                    currentRequest,
                    PrincipalLoginEvent.REASON_CAPTCHA_INVALID));
            throw new InvalidCaptchaException();
        }
        User user = authService.authenticateSms(AdminAuthHelper.mobileOperation(request.getMobile(), currentRequest));
        return loginSuccess(
                user,
                request.getMobile(),
                "短信登录成功",
                PrincipalAuthenticationMethod.SMS_CODE,
                PrincipalIdentityType.USER_MOBILE);
    }

    @Operation(summary = "企业微信登录")
    @ApiImplicitParams({})
    @PostMapping(value = "login/wecom")
    @SysLogger(value = "企业微信登录")
    public AuthAccessTokenResponse loginByWecom(@Valid @RequestBody WecomLoginRequest request) {
        HttpServletRequest currentRequest = currentRequest();
        User user = authService.authenticateWecom(AdminAuthHelper.codeOperation(request.getCode(), currentRequest));
        return loginSuccess(
                user, "wecom", "企业微信登录成功", PrincipalAuthenticationMethod.WECOM, PrincipalIdentityType.USER_WECOM);
    }

    @Operation(summary = "GitHub 登录")
    @ApiImplicitParams({})
    @PostMapping(value = "login/github")
    @SysLogger(value = "GitHub登录")
    public AuthAccessTokenResponse loginByGithub(@Valid @RequestBody GithubLoginRequest request) {
        HttpServletRequest currentRequest = currentRequest();
        User user = authService.authenticateGithub(AdminAuthHelper.codeOperation(request.getCode(), currentRequest));
        return loginSuccess(
                user, "github", "GitHub登录成功", PrincipalAuthenticationMethod.GITHUB, PrincipalIdentityType.USER_GITHUB);
    }

    @Operation(summary = "登出")
    @ApiImplicitParams({})
    @PostMapping(value = "logout")
    @SysLogger(value = "登出")
    public Boolean logout(@Valid @RequestBody AuthLogoutRequest request) {
        if (StringUtils.isEmpty(request.getToken())) {
            throw AdminResponseExceptions.invalidToken();
        }

        AuthAccessTokenDTO accessToken = authService.getAccessToken(AdminAuthHelper.tokenLookup(request.getToken()));
        if (accessToken == null) {
            throw AdminResponseExceptions.invalidToken();
        }

        HttpServletRequest currentRequest = currentRequest();
        authService.deleteAccessToken(AdminAuthHelper.accessTokenOperation(accessToken, currentRequest));

        return true;
    }

    @Operation(summary = "校验 token")
    @ApiImplicitParams({})
    @PostMapping(value = "token/verify")
    @IgnoreSysLogger
    public TokenVerifyResponse verifyToken(@Valid @RequestBody AuthTokenRequest request) {
        return AuthInterfaceAssembler.toTokenVerifyResponse(
                authService.getTokenInfo(AdminAuthHelper.tokenLookup(request.getToken())));
    }

    @Operation(summary = "刷新 token")
    @ApiImplicitParams({})
    @PostMapping(value = "token/refresh")
    @IgnoreSysLogger
    public AuthAccessTokenResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return AuthInterfaceAssembler.toAccessTokenResponse(
                authService.refreshAccessToken(AdminAuthHelper.refreshTokenOperation(
                        request.getClientId(), request.getRefreshToken(), currentRequest())));
    }

    private PreAuthSession createPreAuthSession() {
        if (preAuthSessionService.summaryActiveSessionCount() > properties.getMaxLoginCount()) {
            throw AdminResponseExceptions.loginRequestTooMany();
        }
        PreAuthSession session = preAuthSessionService.create(
                AuthInterfaceAssembler.toCreatePreAuthSessionCommand(properties.getLoginExpiredSeconds()));
        writeCaptcha(session.getId(), PreAuthCodeHelper.generateCaptcha());
        Sm2Crypto.StringKeyPair keyPair = Sm2Crypto.generateKeyPair();
        if (keyPair != null) {
            preAuthSessionService.upsertValue(AuthInterfaceAssembler.toUpsertPreAuthSessionValueCommand(
                    session.getId(), PUBLIC_KEY_ITEM, keyPair.getPublicKey(), session.getExpiredAt()));
            preAuthSessionService.upsertValue(AuthInterfaceAssembler.toUpsertPreAuthSessionValueCommand(
                    session.getId(), PRIVATE_KEY_ITEM, keyPair.getPrivateKey(), session.getExpiredAt()));
        }
        return preAuthSessionService.get(AuthInterfaceAssembler.toPreAuthSessionQuery(session.getId()));
    }

    private PreAuthSession refreshPreAuthSession(String refreshToken) {
        PreAuthSession session = preAuthSessionService.refresh(AuthInterfaceAssembler.toRefreshPreAuthSessionCommand(
                requireSessionIdByRefreshToken(refreshToken),
                properties.getLoginExpiredSeconds(),
                REFRESH_TOKEN_GRACE_SECONDS));
        writeCaptcha(session.getId(), PreAuthCodeHelper.generateCaptcha());
        return session;
    }

    private void releasePreAuthSession(String loginToken) {
        PreAuthSessionId sessionId =
                preAuthSessionService.getIdByToken(AuthInterfaceAssembler.toPreAuthSessionTokenQuery(loginToken));
        if (sessionId != null) {
            preAuthSessionService.release(AuthInterfaceAssembler.toReleasePreAuthSessionCommand(sessionId));
        }
    }

    private String createCaptcha(String loginToken) {
        String captcha = PreAuthCodeHelper.generateCaptcha();
        writeCaptcha(requireSessionIdByToken(loginToken), captcha);
        return captcha;
    }

    private boolean validateCaptcha(String loginToken, String captcha) {
        return preAuthSessionService.existsValidatedValue(AuthInterfaceAssembler.toPreAuthSessionValueValidateQuery(
                requireSessionIdByToken(loginToken), CAPTCHA_ITEM, captcha));
    }

    private boolean validateSmsValidateCode(String loginToken, String mobile, String validateCode) {
        PreAuthSessionId sessionId = requireSessionIdByToken(loginToken);
        return preAuthSessionService.existsValidatedValue(AuthInterfaceAssembler.toPreAuthSessionValueValidateQuery(
                sessionId, SMS_VALIDATE_CODE_ITEM, validateCode, SMS_MOBILE_ITEM, mobile));
    }

    private String getPrivateKey(String loginToken) {
        String privateKey = preAuthSessionService.getValue(AuthInterfaceAssembler.toPreAuthSessionValueQuery(
                requireSessionIdByToken(loginToken), PRIVATE_KEY_ITEM));
        if (StringUtils.isBlank(privateKey)) {
            throw AdminResponseExceptions.invalidToken();
        }
        return privateKey;
    }

    private void writeCaptcha(PreAuthSessionId sessionId, String captcha) {
        preAuthSessionService.upsertValue(AuthInterfaceAssembler.toUpsertPreAuthSessionValueCommand(
                sessionId, CAPTCHA_ITEM, captcha, System.currentTimeMillis() + CAPTCHA_EXPIRED_SECONDS * 1000L));
    }

    private PreAuthSessionId requireSessionIdByToken(String token) {
        PreAuthSessionId sessionId =
                preAuthSessionService.getIdByToken(AuthInterfaceAssembler.toPreAuthSessionTokenQuery(token));
        if (sessionId == null) {
            throw AdminResponseExceptions.invalidToken();
        }
        return sessionId;
    }

    private PreAuthSessionId requireSessionIdByRefreshToken(String refreshToken) {
        PreAuthSessionId sessionId = preAuthSessionService.getIdByRefreshToken(
                AuthInterfaceAssembler.toPreAuthSessionRefreshTokenQuery(refreshToken));
        if (sessionId == null) {
            throw AdminResponseExceptions.invalidToken();
        }
        return sessionId;
    }

    private void writeLog(HttpServletRequest currentRequest, String title, AuthLoginRequest request) {
        Log log = new Log();
        log.setTitle("系统-登录-" + title);
        log.setLogDate(Instant.now());
        log.setRemoteAddr(RequestIpUtils.getIpAddr(currentRequest));
        log.setUserAgent(currentRequest.getHeader("user-agent"));
        log.setRequestUri(currentRequest.getRequestURI());
        log.setMethod(currentRequest.getMethod());
        log.setType(LogType.ACCESS);
        log.setRequestParams(toLogJson(request));
        sysLogMessageService.saveLog(log);
    }

    private void writeLog(HttpServletRequest currentRequest, String title, User user, String loginName) {
        Log log = new Log();
        log.setUserId(user.getId());
        log.setTitle("系统-登录-" + title);
        log.setLogDate(Instant.now());
        log.setRemoteAddr(RequestIpUtils.getIpAddr(currentRequest));
        log.setUserAgent(currentRequest.getHeader("user-agent"));
        log.setRequestUri(currentRequest.getRequestURI());
        log.setMethod(currentRequest.getMethod());
        log.setType(LogType.ACCESS);
        log.setRequestParams(toLogJson(loginName));
        sysLogMessageService.saveLog(log);
    }

    private String toLogJson(AuthLoginRequest request) {
        if (request == null) {
            return null;
        }
        AuthLoginRequest maskedRequest = new AuthLoginRequest();
        maskedRequest.setLoginToken(request.getLoginToken());
        maskedRequest.setUsername(request.getUsername());
        maskedRequest.setPassword("******");
        maskedRequest.setCaptcha(request.getCaptcha());
        return toJson(maskedRequest);
    }

    private String toLogJson(String loginName) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("loginName", loginName);
        return toJson(request);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }

    private AuthAccessTokenResponse loginSuccess(
            User user,
            String loginName,
            String logTitle,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType) {
        authService.deleteAccessTokensByUserId(AdminAuthHelper.userIdOperation(user.getId()));
        HttpServletRequest currentRequest = currentRequest();
        writeLog(currentRequest, logTitle, user, loginName);
        var operation = AdminAuthHelper.accessTokenOperation(user.getId(), loginName, currentRequest);
        operation.setAuthenticationMethod(authenticationMethod);
        operation.setIdentityType(identityType);
        return AuthInterfaceAssembler.toAccessTokenResponse(authService.createAccessToken(operation));
    }

    private HttpServletRequest currentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}
