package com.thundax.kuzhambu.system.application.auth.entity;

import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalTokenStatus;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalAccessTokenId;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalAuthSessionId;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalKey;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalAccessToken {
    private PrincipalAccessTokenId id;
    private PrincipalAccessTokenCode tokenCode;
    private String clientId;
    private PrincipalAuthSessionId sessionId;
    private PrincipalKey principalKey;
    private Set<String> scopes = new LinkedHashSet<>();
    private Date issuedAt;
    private Date expireAt;
    private PrincipalTokenStatus status = PrincipalTokenStatus.ACTIVE;

    public boolean canAccess(Date now) {
        return isActive() && !isExpired(now);
    }

    public void revoke() {
        status = PrincipalTokenStatus.REVOKED;
    }

    public void expire() {
        status = PrincipalTokenStatus.EXPIRED;
    }

    public boolean isActive() {
        return status == PrincipalTokenStatus.ACTIVE;
    }

    public boolean isExpired(Date now) {
        return expireAt != null && now != null && !expireAt.after(now);
    }
}
