package com.thundax.kuzhambu.system.application.auth.entity;

import com.thundax.kuzhambu.common.core.id.EntityId;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalKey;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OAuth2 授权请求和授权码事实。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAuthorization {
    private EntityId id;
    private String authorizationCode;
    private String clientId;
    private PrincipalKey principalKey;
    private String redirectUri;
    private Set<String> scopes = new LinkedHashSet<>();
    private String state;
    private String codeChallenge;
    private String codeChallengeMethod;
    private Date issuedAt;
    private Date expireAt;
    private boolean used;

    public boolean isExpired(Date now) {
        return expireAt != null && now != null && !expireAt.after(now);
    }

    public boolean canConsume(Date now) {
        return !used && !isExpired(now);
    }

    public void markUsed(Date updateTime) {
        this.used = true;
    }
}
