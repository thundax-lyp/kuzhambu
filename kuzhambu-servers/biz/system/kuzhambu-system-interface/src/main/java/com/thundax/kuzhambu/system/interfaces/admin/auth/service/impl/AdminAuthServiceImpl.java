package com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.system.application.auth.exception.InvalidPasswordException;
import com.thundax.kuzhambu.system.application.auth.query.AdminAccessTokenQuery;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.auth.result.AdminAccessTokenResult;
import com.thundax.kuzhambu.system.application.auth.result.AdminTokenQueryResult;
import com.thundax.kuzhambu.system.application.auth.result.AdminTokenRefreshResult;
import com.thundax.kuzhambu.system.application.auth.service.AdminSessionTokenApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalAuthenticationApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.thundax.kuzhambu.system.application.core.service.UserManagementApplicationService;
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
import com.thundax.kuzhambu.system.interfaces.admin.auth.assembler.AuthInterfaceAssembler;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.AdminAuthService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.PermissionService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthAccessTokenDTO;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthTokenQueryDTO;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthTokenRefreshDTO;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.provider.GithubLoginProvider;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.provider.WecomLoginProvider;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.support.AdminAuthLookup;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.support.AdminAuthOperation;
import com.thundax.kuzhambu.system.interfaces.admin.configure.LoginProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private final LoginProperties loginProperties;
    private final PermissionService permissionService;
    private final AdminSessionTokenApplicationService adminTokenService;
    private final PrincipalAuthenticationApplicationService principalAuthService;
    private final PrincipalIdentityApplicationService principalIdentityService;
    private final UserManagementApplicationService userService;

    @Autowired(required = false)
    private WecomLoginProvider wecomLoginProvider;

    @Autowired(required = false)
    private GithubLoginProvider githubLoginProvider;

    public AdminAuthServiceImpl(
            LoginProperties loginProperties,
            PermissionService permissionService,
            AdminSessionTokenApplicationService adminTokenService,
            PrincipalAuthenticationApplicationService principalAuthService,
            PrincipalIdentityApplicationService principalIdentityService,
            UserManagementApplicationService userService) {
        this.loginProperties = loginProperties;
        this.permissionService = permissionService;
        this.adminTokenService = adminTokenService;
        this.principalAuthService = principalAuthService;
        this.principalIdentityService = principalIdentityService;
        this.userService = userService;
    }

    @Override
    public AuthAccessTokenDTO createAccessToken(AdminAuthOperation operation) {
        PrincipalAuthenticationMethod authenticationMethod = operation.getAuthenticationMethod();
        if (authenticationMethod == null) {
            authenticationMethod = PrincipalAuthenticationMethod.PASSWORD;
        }
        PrincipalIdentityType identityType = operation.getIdentityType();
        if (identityType == null) {
            identityType = PrincipalIdentityType.USER_ACCOUNT;
        }
        AuthAccessTokenDTO result = toInterfaceResult(
                adminTokenService.createAccessToken(AuthInterfaceAssembler.toCreateAdminAccessTokenCommand(
                        operation.getUserId(),
                        operation.getLoginName(),
                        operation.getIp(),
                        operation.getUserAgent(),
                        authenticationMethod,
                        identityType)));
        if (result != null) {
            permissionService.createPermissions(result.getToken(), UserIdCodec.toStringValue(operation.getUserId()));
        }
        return result;
    }

    @Override
    public AuthAccessTokenDTO getAccessToken(AdminAuthLookup lookup) {
        return toInterfaceResult(adminTokenService.getAccessToken(accessTokenQuery(lookup.getToken())));
    }

    @Override
    public int deleteAccessTokensByUserId(AdminAuthOperation operation) {
        return adminTokenService.deleteAccessTokensByUserId(
                AuthInterfaceAssembler.toDeleteAdminAccessTokenCommand(null, operation.getUserId(), null, null));
    }

    @Override
    public boolean validateToken(AdminAuthOperation operation) {
        return adminTokenService.validateToken(accessTokenQuery(tokenValue(operation.getAccessToken())));
    }

    @Override
    public void activeAccessToken(AdminAuthOperation operation) {
        adminTokenService.activeAccessToken(accessTokenQuery(tokenValue(operation.getAccessToken())));
    }

    @Override
    public void deleteAccessToken(AdminAuthOperation operation) {
        adminTokenService.deleteAccessToken(AuthInterfaceAssembler.toDeleteAdminAccessTokenCommand(
                accessTokenCode(tokenValue(operation.getAccessToken())),
                null,
                operation.getIp(),
                operation.getUserAgent()));
    }

    @Override
    public AuthTokenQueryDTO getTokenInfo(AdminAuthLookup lookup) {
        return toInterfaceResult(adminTokenService.getTokenInfo(accessTokenQuery(lookup.getToken())));
    }

    @Override
    public AuthTokenRefreshDTO refreshAccessToken(AdminAuthOperation operation) {
        try {
            AuthTokenRefreshDTO result = toInterfaceResult(
                    adminTokenService.refreshAccessToken(AuthInterfaceAssembler.toRefreshAdminAccessTokenCommand(
                            PrincipalClientIdCodec.toDomain(operation.getClientId()),
                            PrincipalRefreshTokenCode.ofNullable(blankToNull(operation.getRefreshToken())),
                            operation.getIp(),
                            operation.getUserAgent())));
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
    public void invalidateSessionByToken(AdminAuthOperation operation) {
        adminTokenService.invalidateSessionByToken(AuthInterfaceAssembler.toInvalidateAdminSessionCommand(
                accessTokenCode(operation.getToken()), null, operation.getReason()));
    }

    @Override
    public int invalidateSessionsByUserId(AdminAuthOperation operation) {
        return adminTokenService.invalidateSessionsByUserId(AuthInterfaceAssembler.toInvalidateAdminSessionCommand(
                null, operation.getUserId(), operation.getReason()));
    }

    @Override
    public User authenticatePassword(AdminAuthOperation operation) {
        return authenticatePassword(
                operation.getLoginName(), operation.getPlainPassword(), operation.getIp(), operation.getUserAgent());
    }

    @Override
    public User authenticateSms(AdminAuthOperation operation) {
        return authenticateSms(operation.getMobile(), operation.getIp(), operation.getUserAgent());
    }

    @Override
    public User authenticateWecom(AdminAuthOperation operation) {
        return authenticateWecom(operation.getCode(), operation.getIp(), operation.getUserAgent());
    }

    @Override
    public User authenticateGithub(AdminAuthOperation operation) {
        return authenticateGithub(operation.getCode(), operation.getIp(), operation.getUserAgent());
    }

    @Override
    public void recordLoginFailed(AdminAuthOperation operation) {
        adminTokenService.recordLoginFailed(AuthInterfaceAssembler.toRecordPrincipalLoginFailureCommand(
                null,
                operation.getAuthenticationMethod(),
                operation.getIdentityType(),
                operation.getIp(),
                operation.getUserAgent(),
                operation.getReason()));
    }

    @Override
    public void validatePassword(AdminAuthOperation operation) {
        validatePassword(operation.getUser(), operation.getPlainPassword());
    }

    private User authenticatePassword(String loginName, String plainPassword) {
        return authenticatePassword(loginName, plainPassword, null, null);
    }

    private User authenticatePassword(String loginName, String plainPassword, String ip, String userAgent) {
        PrincipalIdentity identity;
        try {
            identity = principalAuthService.authenticatePassword(AuthInterfaceAssembler.toAuthenticatePasswordCommand(
                    PrincipalIdentityType.USER_ACCOUNT,
                    loginName,
                    PrincipalCredentialType.USER_PASSWORD,
                    plainPassword,
                    passwordPolicy()));
        } catch (InvalidPasswordException e) {
            adminTokenService.recordLoginFailed(AuthInterfaceAssembler.toRecordPrincipalLoginFailureCommand(
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
            adminTokenService.recordLoginFailed(AuthInterfaceAssembler.toRecordPrincipalLoginFailureCommand(
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
                    AuthInterfaceAssembler.toAuthenticateIdentityCommand(identityType, identityValue));
        } catch (InvalidPasswordException e) {
            adminTokenService.recordLoginFailed(AuthInterfaceAssembler.toRecordPrincipalLoginFailureCommand(
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
            adminTokenService.recordLoginFailed(AuthInterfaceAssembler.toRecordPrincipalLoginFailureCommand(
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
        adminTokenService.recordLoginFailed(AuthInterfaceAssembler.toRecordPrincipalLoginFailureCommand(
                principalKey, authenticationMethod, identityType, ip, userAgent, reason));
    }

    private User getUser(UserId userId) {
        return userService.get(userId);
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
        return new PrincipalIdentityQuery(null, identityType, null, principalKey, null);
    }

    private PrincipalPasswordPolicyDTO passwordPolicy() {
        return new PrincipalPasswordPolicyDTO(
                loginProperties.getEnable(), loginProperties.getMaxFailCount(), loginProperties.getLockTime());
    }

    private AuthAccessTokenDTO toInterfaceResult(AdminAccessTokenResult result) {
        if (result == null) {
            return null;
        }
        return new AuthAccessTokenDTO(
                result.getToken().asString(),
                result.getRefreshToken() == null
                        ? null
                        : result.getRefreshToken().asString(),
                result.getPrincipalAccessToken());
    }

    private String tokenValue(AuthAccessTokenDTO result) {
        if (result == null) {
            return null;
        }
        return result.getToken();
    }

    private AdminAccessTokenQuery accessTokenQuery(String token) {
        return AuthInterfaceAssembler.toAdminAccessTokenQuery(accessTokenCode(token));
    }

    private PrincipalAccessTokenCode accessTokenCode(String token) {
        return PrincipalAccessTokenCode.ofNullable(blankToNull(token));
    }

    private String blankToNull(String value) {
        return StringUtils.isBlank(value) ? null : value;
    }

    private AuthTokenQueryDTO toInterfaceResult(AdminTokenQueryResult result) {
        if (result == null) {
            return null;
        }
        if (!result.isActive()) {
            return AuthTokenQueryDTO.inactive(
                    result.getToken() == null ? null : result.getToken().asString());
        }
        return AuthTokenQueryDTO.active(
                result.getToken().asString(), result.getSession(), result.getUser(), result.getUsername());
    }

    private AuthTokenRefreshDTO toInterfaceResult(AdminTokenRefreshResult result) {
        if (result == null) {
            return null;
        }
        AuthAccessTokenDTO accessToken = toInterfaceResult(result.getAccessToken());
        return new AuthTokenRefreshDTO(
                accessToken,
                result.getRefreshToken() == null
                        ? null
                        : result.getRefreshToken().asString());
    }
}
