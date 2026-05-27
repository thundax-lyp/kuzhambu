package com.thundax.kuzhambu.system.application.auth.entity;

import com.thundax.kuzhambu.common.core.id.EntityId;
import com.thundax.kuzhambu.system.application.auth.entity.enums.OAuthClientStatus;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OAuth2 客户端配置。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthClient {
    private EntityId id;
    private String clientId;
    private String clientSecretHash;
    private String clientName;
    private String clientType;
    private Set<String> grantTypes = new LinkedHashSet<>();
    private Set<String> scopes = new LinkedHashSet<>();
    private Set<String> redirectUris = new LinkedHashSet<>();
    private long accessTokenTtlSeconds;
    private long refreshTokenTtlSeconds;
    private OAuthClientStatus status = OAuthClientStatus.ENABLED;
    private String contact;
    private String remark;

    public boolean isEnabled() {
        return OAuthClientStatus.ENABLED == status;
    }

    public boolean supportsGrantType(String grantType) {
        return grantType != null && grantTypes != null && grantTypes.contains(grantType);
    }

    public boolean supportsRedirectUri(String redirectUri) {
        return redirectUri != null && redirectUris != null && redirectUris.contains(redirectUri);
    }

    public boolean supportsScopes(Set<String> requestedScopes) {
        return requestedScopes != null && scopes != null && scopes.containsAll(requestedScopes);
    }
}
