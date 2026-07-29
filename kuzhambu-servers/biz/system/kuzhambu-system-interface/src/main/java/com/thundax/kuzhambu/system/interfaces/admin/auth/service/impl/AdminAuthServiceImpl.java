package com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.system.application.auth.command.AuthenticateIdentityCommand;
import com.thundax.kuzhambu.system.application.auth.command.AuthenticatePasswordCommand;
import com.thundax.kuzhambu.system.application.auth.command.CreateAdminAccessTokenCommand;
import com.thundax.kuzhambu.system.application.auth.command.DeleteAdminAccessTokenCommand;
import com.thundax.kuzhambu.system.application.auth.command.InvalidateAdminSessionCommand;
import com.thundax.kuzhambu.system.application.auth.command.RecordPrincipalLoginFailureCommand;
import com.thundax.kuzhambu.system.application.auth.command.RefreshAdminAccessTokenCommand;
import com.thundax.kuzhambu.system.application.auth.exception.InvalidPasswordException;
import com.thundax.kuzhambu.system.application.auth.query.AdminAccessTokenQuery;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.auth.result.AdminAccessTokenResult;
import com.thundax.kuzhambu.system.application.auth.result.AdminTokenQueryResult;
import com.thundax.kuzhambu.system.application.auth.result.AdminTokenRefreshResult;
import com.thundax.kuzhambu.system.application.auth.service.AdminTokenApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalAuthApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.thundax.kuzhambu.system.application.core.query.GetUserQuery;
import com.thundax.kuzhambu.system.application.core.service.UserApplicationService;
import com.thundax.kuzhambu.system.domain.auth.codec.PrincipalClientIdCodec;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalLoginEvent;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalAuthenticationMethod;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalRefreshTokenCode;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.AdminAuthService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.PermissionService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.command.AdminAuthCommand;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.provider.GithubLoginProvider;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.provider.WecomLoginProvider;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.query.AdminAuthQuery;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.AuthAccessTokenResult;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.AuthTokenQueryResult;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.AuthTokenRefreshResult;
import com.thundax.kuzhambu.system.interfaces.admin.configure.LoginProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private final LoginProperties loginProperties;
    private final PermissionService permissionService;
    private final AdminTokenApplicationService adminTokenService;
    private final PrincipalAuthApplicationService principalAuthService;
    private final PrincipalIdentityApplicationService principalIdentityService;
    private final UserApplicationService userService;

    @Autowired(required = false)
    private WecomLoginProvider wecomLoginProvider;

    @Autowired(required = false)
    private GithubLoginProvider githubLoginProvider;

    public AdminAuthServiceImpl(
            LoginProperties loginProperties,
            PermissionService permissionService,
            AdminTokenApplicationService adminTokenService,
            PrincipalAuthApplicationService principalAuthService,
            PrincipalIdentityApplicationService principalIdentityService,
            UserApplicationService userService) {
        this.loginProperties = loginProperties;
        this.permissionService = permissionService;
        this.adminTokenService = adminTokenService;
        this.principalAuthService = principalAuthService;
        this.principalIdentityService = principalIdentityService;
        this.userService = userService;
    }

    @Override
    public AuthAccessTokenResult createAccessToken(AdminAuthCommand command) {
        PrincipalAuthenticationMethod authenticationMethod = command.getAuthenticationMethod();
        if (authenticationMethod == null) {
            authenticationMethod = PrincipalAuthenticationMethod.PASSWORD;
        }
        PrincipalIdentityType identityType = command.getIdentityType();
        if (identityType == null) {
            identityType = PrincipalIdentityType.USER_ACCOUNT;
        }
        AuthAccessTokenResult result =
                toInterfaceResult(adminTokenService.createAccessToken(new CreateAdminAccessTokenCommand(
                        command.getUserId(),
                        command.getLoginName(),
                        command.getIp(),
                        command.getUserAgent(),
                        authenticationMethod,
                        identityType)));
        if (result != null) {
            permissionService.createPermissions(result.getToken(), UserIdCodec.toStringValue(command.getUserId()));
        }
        return result;
    }

    @Override
    public AuthAccessTokenResult getAccessToken(AdminAuthQuery query) {
        return toInterfaceResult(adminTokenService.getAccessToken(accessTokenQuery(query.getToken())));
    }

    @Override
    public int deleteAccessTokensByUserId(AdminAuthCommand command) {
        return adminTokenService.deleteAccessTokensByUserId(
                new DeleteAdminAccessTokenCommand(null, command.getUserId(), null, null));
    }

    @Override
    public boolean validateToken(AdminAuthCommand command) {
        return adminTokenService.validateToken(accessTokenQuery(tokenValue(command.getAccessToken())));
    }

    @Override
    public void activeAccessToken(AdminAuthCommand command) {
        adminTokenService.activeAccessToken(accessTokenQuery(tokenValue(command.getAccessToken())));
    }

    @Override
    public void deleteAccessToken(AdminAuthCommand command) {
        adminTokenService.deleteAccessToken(new DeleteAdminAccessTokenCommand(
                accessTokenCode(tokenValue(command.getAccessToken())), null, command.getIp(), command.getUserAgent()));
    }

    @Override
    public AuthTokenQueryResult getTokenInfo(AdminAuthQuery query) {
        return toInterfaceResult(adminTokenService.getTokenInfo(accessTokenQuery(query.getToken())));
    }

    @Override
    public AuthTokenRefreshResult refreshAccessToken(AdminAuthCommand command) {
        try {
            AuthTokenRefreshResult result =
                    toInterfaceResult(adminTokenService.refreshAccessToken(new RefreshAdminAccessTokenCommand(
                            PrincipalClientIdCodec.toDomain(command.getClientId()),
                            PrincipalRefreshTokenCode.ofNullable(command.getRefreshToken()),
                            command.getIp(),
                            command.getUserAgent())));
            if (result != null && result.getAccessToken() != null) {
                permissionService.createPermissions(
                        result.getAccessToken().getToken(),
                        result.getAccessToken().getUserId());
            }
            return result;
        } catch (BizException e) {
            throw AdminResponseExceptions.invalidToken();
        }
    }

    @Override
    public void invalidateSessionByToken(AdminAuthCommand command) {
        adminTokenService.invalidateSessionByToken(
                new InvalidateAdminSessionCommand(accessTokenCode(command.getToken()), null, command.getReason()));
    }

    @Override
    public int invalidateSessionsByUserId(AdminAuthCommand command) {
        return adminTokenService.invalidateSessionsByUserId(
                new InvalidateAdminSessionCommand(null, command.getUserId(), command.getReason()));
    }

    @Override
    public User authenticatePassword(AdminAuthCommand command) {
        return authenticatePassword(
                command.getLoginName(), command.getPlainPassword(), command.getIp(), command.getUserAgent());
    }

    @Override
    public User authenticateSms(AdminAuthCommand command) {
        return authenticateSms(command.getMobile(), command.getIp(), command.getUserAgent());
    }

    @Override
    public User authenticateWecom(AdminAuthCommand command) {
        return authenticateWecom(command.getCode(), command.getIp(), command.getUserAgent());
    }

    @Override
    public User authenticateGithub(AdminAuthCommand command) {
        return authenticateGithub(command.getCode(), command.getIp(), command.getUserAgent());
    }

    @Override
    public void recordLoginFailed(AdminAuthCommand command) {
        adminTokenService.recordLoginFailed(new RecordPrincipalLoginFailureCommand(
                null,
                command.getAuthenticationMethod(),
                command.getIdentityType(),
                command.getIp(),
                command.getUserAgent(),
                command.getReason()));
    }

    @Override
    public void validatePassword(AdminAuthCommand command) {
        validatePassword(command.getUser(), command.getPlainPassword());
    }

    private User authenticatePassword(String loginName, String plainPassword) {
        return authenticatePassword(loginName, plainPassword, null, null);
    }

    private User authenticatePassword(String loginName, String plainPassword, String ip, String userAgent) {
        PrincipalIdentity identity;
        try {
            identity = principalAuthService.authenticatePassword(new AuthenticatePasswordCommand(
                    PrincipalIdentityType.USER_ACCOUNT,
                    loginName,
                    PrincipalCredentialType.USER_PASSWORD,
                    plainPassword,
                    passwordPolicy()));
        } catch (InvalidPasswordException e) {
            adminTokenService.recordLoginFailed(new RecordPrincipalLoginFailureCommand(
                    null,
                    PrincipalAuthenticationMethod.PASSWORD,
                    PrincipalIdentityType.USER_ACCOUNT,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_INVALID_CREDENTIAL));
            throw AdminResponseExceptions.invalidUsernamePassword();
        }

        User user = getUser(UserIdCodec.toDomain(identity.getPrincipalKey().getPrincipalId()));
        if (user == null) {
            adminTokenService.recordLoginFailed(new RecordPrincipalLoginFailureCommand(
                    null,
                    PrincipalAuthenticationMethod.PASSWORD,
                    PrincipalIdentityType.USER_ACCOUNT,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_PRINCIPAL_NOT_FOUND));
            throw AdminResponseExceptions.invalidUsernamePassword();
        }
        if (!user.isEnable()) {
            writeLoginFailed(
                    identity.getPrincipalKey(),
                    PrincipalAuthenticationMethod.PASSWORD,
                    PrincipalIdentityType.USER_ACCOUNT,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_ACCOUNT_DISABLED);
            throw AdminResponseExceptions.bannedAccount();
        }
        return user;
    }

    private User authenticateSms(String mobile) {
        return authenticateSms(mobile, null, null);
    }

    private User authenticateSms(String mobile, String ip, String userAgent) {
        return authenticateIdentity(
                PrincipalIdentityType.USER_MOBILE, mobile, PrincipalAuthenticationMethod.SMS_CODE, ip, userAgent);
    }

    private User authenticateWecom(String code) {
        return authenticateWecom(code, null, null);
    }

    private User authenticateWecom(String code, String ip, String userAgent) {
        if (wecomLoginProvider == null) {
            throw AdminResponseExceptions.wecomLoginNotConfigured();
        }
        return authenticateIdentity(
                PrincipalIdentityType.USER_WECOM,
                wecomLoginProvider.resolveIdentity(code),
                PrincipalAuthenticationMethod.WECOM,
                ip,
                userAgent);
    }

    private User authenticateGithub(String code) {
        return authenticateGithub(code, null, null);
    }

    private User authenticateGithub(String code, String ip, String userAgent) {
        if (githubLoginProvider == null) {
            throw AdminResponseExceptions.githubLoginNotConfigured();
        }
        return authenticateIdentity(
                PrincipalIdentityType.USER_GITHUB,
                githubLoginProvider.resolveIdentity(code),
                PrincipalAuthenticationMethod.GITHUB,
                ip,
                userAgent);
    }

    private void validatePassword(User user, String plainPassword) {
        if (user == null) {
            throw AdminResponseExceptions.invalidUsernamePassword();
        }
        String loginName = getAccountLoginName(user.getId());
        if (StringUtils.isBlank(loginName)) {
            throw AdminResponseExceptions.invalidUsernamePassword();
        }
        authenticatePassword(loginName, plainPassword);
    }

    private User authenticateIdentity(
            PrincipalIdentityType identityType,
            String identityValue,
            PrincipalAuthenticationMethod authenticationMethod,
            String ip,
            String userAgent) {
        PrincipalIdentity identity;
        try {
            identity = principalAuthService.authenticateIdentity(
                    new AuthenticateIdentityCommand(identityType, identityValue));
        } catch (InvalidPasswordException e) {
            adminTokenService.recordLoginFailed(new RecordPrincipalLoginFailureCommand(
                    null,
                    authenticationMethod,
                    identityType,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_IDENTITY_NOT_FOUND));
            throw AdminResponseExceptions.invalidUsernamePassword();
        }
        User user = getUser(UserIdCodec.toDomain(identity.getPrincipalKey().getPrincipalId()));
        if (user == null) {
            adminTokenService.recordLoginFailed(new RecordPrincipalLoginFailureCommand(
                    null,
                    authenticationMethod,
                    identityType,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_PRINCIPAL_NOT_FOUND));
            throw AdminResponseExceptions.invalidUsernamePassword();
        }
        if (!user.isEnable()) {
            writeLoginFailed(
                    identity.getPrincipalKey(),
                    authenticationMethod,
                    identityType,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_ACCOUNT_DISABLED);
            throw AdminResponseExceptions.bannedAccount();
        }
        return user;
    }

    private void writeLoginFailed(
            PrincipalKey principalKey,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType,
            String ip,
            String userAgent,
            String reason) {
        adminTokenService.recordLoginFailed(new RecordPrincipalLoginFailureCommand(
                principalKey, authenticationMethod, identityType, ip, userAgent, reason));
    }

    private User getUser(UserId userId) {
        return userService.get(new GetUserQuery(userId));
    }

    private String getAccountLoginName(UserId userId) {
        if (userId == null) {
            return null;
        }
        PrincipalIdentity identity = principalIdentityService.get(
                identityQuery(PrincipalKey.of(PrincipalType.USER, userId.value()), PrincipalIdentityType.USER_ACCOUNT));
        return identity == null ? null : identity.getIdentityValue();
    }

    private PrincipalIdentityQuery identityQuery(PrincipalKey principalKey, PrincipalIdentityType identityType) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setPrincipalKey(principalKey);
        query.setIdentityType(identityType);
        return query;
    }

    private PrincipalPasswordPolicyDTO passwordPolicy() {
        return new PrincipalPasswordPolicyDTO(
                loginProperties.getEnable(), loginProperties.getMaxFailCount(), loginProperties.getLockTime());
    }

    private AuthAccessTokenResult toInterfaceResult(AdminAccessTokenResult result) {
        if (result == null) {
            return null;
        }
        return new AuthAccessTokenResult(
                result.getToken().asString(),
                result.getRefreshToken() == null
                        ? null
                        : result.getRefreshToken().asString(),
                result.getPrincipalAccessToken());
    }

    private String tokenValue(AuthAccessTokenResult result) {
        if (result == null) {
            return null;
        }
        return result.getToken();
    }

    private AdminAccessTokenQuery accessTokenQuery(String token) {
        return new AdminAccessTokenQuery(accessTokenCode(token));
    }

    private PrincipalAccessTokenCode accessTokenCode(String token) {
        return PrincipalAccessTokenCode.ofNullable(token);
    }

    private AuthTokenQueryResult toInterfaceResult(AdminTokenQueryResult result) {
        if (result == null) {
            return null;
        }
        if (!result.isActive()) {
            return AuthTokenQueryResult.inactive(
                    result.getToken() == null ? null : result.getToken().asString());
        }
        return AuthTokenQueryResult.active(
                result.getToken().asString(), result.getSession(), result.getUser(), result.getUsername());
    }

    private AuthTokenRefreshResult toInterfaceResult(AdminTokenRefreshResult result) {
        if (result == null) {
            return null;
        }
        AuthAccessTokenResult accessToken = toInterfaceResult(result.getAccessToken());
        return new AuthTokenRefreshResult(
                accessToken,
                result.getRefreshToken() == null
                        ? null
                        : result.getRefreshToken().asString());
    }
}
