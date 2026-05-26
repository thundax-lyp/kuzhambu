package com.thundax.kuzhambu.interfaces.admin.auth.service.command;

import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalAuthenticationMethod;
import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.biz.core.entity.User;
import com.thundax.kuzhambu.biz.core.entity.valueobject.UserId;
import com.thundax.kuzhambu.interfaces.admin.auth.service.result.AuthAccessTokenResult;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminAuthCommand {
    private UserId userId;
    private String loginName;
    private String plainPassword;
    private String mobile;
    private String code;
    private String token;
    private String reason;
    private String ip;
    private String userAgent;
    private PrincipalAuthenticationMethod authenticationMethod;
    private PrincipalIdentityType identityType;
    private AuthAccessTokenResult accessToken;
    private User user;
    private String clientId;
    private String clientSecret;
    private String grantType;
    private String redirectUri;
    private String authorizationCode;
    private String codeVerifier;
    private String refreshToken;
    private String state;
    private String codeChallenge;
    private String codeChallengeMethod;
    private List<String> scopes;
    private boolean approved;
}
