package com.thundax.kuzhambu.system.application.auth.command;

import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalCredentialId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import java.time.Instant;

public record ChangePrincipalCredentialCommand(
        PrincipalCredentialId id,
        PrincipalKey principalKey,
        PrincipalIdentityId identityId,
        PrincipalCredentialType credentialType,
        String credentialValue,
        PrincipalCredentialStatus status,
        boolean needChangePassword,
        int failedCount,
        int failedLimit,
        Instant lockedUntil,
        Instant expiresAt,
        Instant lastVerifiedAt) {}
