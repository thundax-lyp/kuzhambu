package com.thundax.kuzhambu.system.domain.auth.model.entity;

import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalCredentialId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalCredential {
    private PrincipalCredentialId id;
    private PrincipalKey principalKey;
    private PrincipalIdentityId identityId;
    private PrincipalCredentialType credentialType;
    private String credentialValue;
    private PrincipalCredentialStatus status = PrincipalCredentialStatus.ACTIVE;
    private boolean needChangePassword;
    private int failedCount;
    private int failedLimit;
    private Instant lockedUntil;
    private Instant expiresAt;
    private Instant lastVerifiedAt;

    public boolean isPassword() {
        return credentialType != null && credentialType.isPassword();
    }

    public boolean isActive() {
        return PrincipalCredentialStatus.ACTIVE == status;
    }

    public boolean isLocked(Instant now) {
        if (PrincipalCredentialStatus.LOCKED == status) {
            return lockedUntil == null || now == null || lockedUntil.isAfter(now);
        }
        return lockedUntil != null && now != null && lockedUntil.isAfter(now);
    }

    public boolean isExpired(Instant now) {
        if (PrincipalCredentialStatus.EXPIRED == status) {
            return true;
        }
        return expiresAt != null && now != null && !expiresAt.isAfter(now);
    }

    public void markVerified(Instant verifiedAt) {
        this.status = PrincipalCredentialStatus.ACTIVE;
        this.failedCount = 0;
        this.lockedUntil = null;
        this.lastVerifiedAt = verifiedAt;
    }

    public void markFailed(Instant lockedUntil) {
        this.failedCount += 1;
        if (failedLimit > 0 && failedCount >= failedLimit) {
            lock(lockedUntil);
        }
    }

    public void lock(Instant lockedUntil) {
        this.status = PrincipalCredentialStatus.LOCKED;
        this.lockedUntil = lockedUntil;
    }

    public void unlock() {
        this.status = PrincipalCredentialStatus.ACTIVE;
        this.lockedUntil = null;
    }

    public void expire() {
        this.status = PrincipalCredentialStatus.EXPIRED;
    }

    public void disable() {
        this.status = PrincipalCredentialStatus.DISABLED;
    }
}
