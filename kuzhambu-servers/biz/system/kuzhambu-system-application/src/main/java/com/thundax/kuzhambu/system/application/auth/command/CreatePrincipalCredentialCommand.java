package com.thundax.kuzhambu.system.application.auth.command;

import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePrincipalCredentialCommand {
    private PrincipalKey principalKey;
    private PrincipalIdentityId identityId;
    private PrincipalCredentialType credentialType;
    private String credentialValue;
    private PrincipalCredentialStatus status;
    private boolean needChangePassword;
    private int failedCount;
    private int failedLimit;
    private Date lockedUntil;
    private Date expiresAt;
    private Date lastVerifiedAt;
}
