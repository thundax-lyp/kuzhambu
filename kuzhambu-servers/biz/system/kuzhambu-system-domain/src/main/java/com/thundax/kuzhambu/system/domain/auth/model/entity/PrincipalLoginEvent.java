package com.thundax.kuzhambu.system.domain.auth.model.entity;

import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalAuthenticationMethod;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalLoginEventType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalLoginEventId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalLoginEvent {
    public static final String REASON_NONE = "NONE";
    public static final String REASON_INVALID_CREDENTIAL = "INVALID_CREDENTIAL";
    public static final String REASON_ACCOUNT_DISABLED = "ACCOUNT_DISABLED";
    public static final String REASON_PRINCIPAL_NOT_FOUND = "PRINCIPAL_NOT_FOUND";
    public static final String REASON_IDENTITY_NOT_FOUND = "IDENTITY_NOT_FOUND";
    public static final String REASON_CAPTCHA_INVALID = "CAPTCHA_INVALID";
    public static final String REASON_PRE_AUTH_SESSION_INVALID = "PRE_AUTH_SESSION_INVALID";
    public static final String REASON_TOKEN_INVALID = "TOKEN_INVALID";
    public static final String REASON_TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String REASON_REFRESH_TOKEN_USED = "REFRESH_TOKEN_USED";
    public static final String REASON_USER_LOGOUT = "USER_LOGOUT";
    public static final String REASON_RELOGIN = "RELOGIN";
    public static final String REASON_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String REASON_KICKED_OUT = "KICKED_OUT";
    public static final String REASON_OAUTH_DENIED = "OAUTH_DENIED";
    public static final String REASON_OAUTH_CLIENT_INVALID = "OAUTH_CLIENT_INVALID";
    public static final String REASON_OAUTH_CODE_INVALID = "OAUTH_CODE_INVALID";
    public static final String REASON_OAUTH_CODE_EXPIRED = "OAUTH_CODE_EXPIRED";
    public static final String REASON_SYSTEM_INVALIDATE = "SYSTEM_INVALIDATE";

    private PrincipalLoginEventId id;
    private PrincipalKey principalKey;
    private String clientId;
    private PrincipalLoginEventType eventType;
    private PrincipalAuthenticationMethod authenticationMethod;
    private PrincipalIdentityType identityType;
    private Date occurredAt;
    private String ip;
    private String userAgent;
    private String reason;
}
