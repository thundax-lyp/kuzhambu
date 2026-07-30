package com.thundax.kuzhambu.system.domain.auth.model.entity;

import com.thundax.kuzhambu.system.domain.auth.codec.PrincipalClientIdCodec;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalTokenStatus;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalClientId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalRefreshTokenCode;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalRefreshTokenId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalRefreshToken {
    private PrincipalRefreshTokenId id;
    private PrincipalRefreshTokenCode tokenCode;
    private PrincipalAccessTokenId accessTokenId;
    private PrincipalClientId clientId;
    private PrincipalAuthSessionId sessionId;
    private PrincipalKey principalKey;
    private Instant issuedAt;
    private Instant expireAt;
    private PrincipalTokenStatus status = PrincipalTokenStatus.ACTIVE;

    public boolean canRefresh(Instant now) {
        return isActive() && !isExpired(now);
    }

    public void setClientId(String clientId) {
        this.clientId = PrincipalClientIdCodec.toDomain(clientId);
    }

    public void setClientId(PrincipalClientId clientId) {
        this.clientId = clientId;
    }

    public void markUsed() {
        status = PrincipalTokenStatus.USED;
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

    public boolean isExpired(Instant now) {
        return expireAt != null && now != null && !expireAt.isAfter(now);
    }
}
