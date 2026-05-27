package com.thundax.kuzhambu.system.application.auth.entity;

import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalCredentialId;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalKey;
import java.util.Date;
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
    private Date lockedUntil;
    private Date expiresAt;
    private Date lastVerifiedAt;

    public boolean isPassword() {
        return credentialType != null && credentialType.isPassword();
    }

    public boolean isActive() {
        return PrincipalCredentialStatus.ACTIVE == status;
    }

    public boolean isLocked(Date now) {
        if (PrincipalCredentialStatus.LOCKED == status) {
            return lockedUntil == null || now == null || lockedUntil.after(now);
        }
        return lockedUntil != null && now != null && lockedUntil.after(now);
    }

    public boolean isExpired(Date now) {
        if (PrincipalCredentialStatus.EXPIRED == status) {
            return true;
        }
        return expiresAt != null && now != null && !expiresAt.after(now);
    }

    public void markVerified(Date verifiedAt) {
        this.status = PrincipalCredentialStatus.ACTIVE;
        this.failedCount = 0;
        this.lockedUntil = null;
        this.lastVerifiedAt = verifiedAt;
    }

    public void markFailed(Date lockedUntil) {
        this.failedCount += 1;
        if (failedLimit > 0 && failedCount >= failedLimit) {
            lock(lockedUntil);
        }
    }

    public void lock(Date lockedUntil) {
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
