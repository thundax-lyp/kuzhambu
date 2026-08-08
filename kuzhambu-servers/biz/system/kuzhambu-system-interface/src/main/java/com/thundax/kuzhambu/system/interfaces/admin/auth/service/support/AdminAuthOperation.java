package com.thundax.kuzhambu.system.interfaces.admin.auth.service.support;

import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalAuthenticationMethod;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthAccessTokenDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminAuthOperation {
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
    private AuthAccessTokenDTO accessToken;
    private User user;
    private String clientId;
    private String refreshToken;
}
