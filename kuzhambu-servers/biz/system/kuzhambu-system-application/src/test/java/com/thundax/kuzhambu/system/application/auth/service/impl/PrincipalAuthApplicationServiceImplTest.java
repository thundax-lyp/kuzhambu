package com.thundax.kuzhambu.system.application.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.system.application.auth.command.AuthenticatePasswordCommand;
import com.thundax.kuzhambu.system.application.auth.command.PrincipalCredentialCommand;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalCredentialQuery;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalCredentialApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.thundax.kuzhambu.system.application.auth.utils.PasswordHelper;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalCredential;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalCredentialId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalIdentityId;
import org.junit.jupiter.api.Test;

class PrincipalAuthApplicationServiceImplTest {

    @Test
    void authenticatePasswordShouldUseLatestFailureCount() {
        PrincipalIdentityApplicationService principalIdentityService = mock(PrincipalIdentityApplicationService.class);
        PrincipalCredentialApplicationService principalCredentialService =
                mock(PrincipalCredentialApplicationService.class);
        PrincipalAuthApplicationServiceImpl service =
                new PrincipalAuthApplicationServiceImpl(principalIdentityService, principalCredentialService);

        PrincipalIdentity identity = new PrincipalIdentity();
        identity.setId(PrincipalIdentityId.of(100L));
        identity.setStatus(PrincipalIdentityStatus.ENABLED);

        PrincipalCredential credential = activePasswordCredential(1);
        PrincipalCredential latestCredential = activePasswordCredential(3);
        when(principalIdentityService.get(any(PrincipalIdentityQuery.class))).thenReturn(identity);
        when(principalCredentialService.get(any(PrincipalCredentialQuery.class)))
                .thenReturn(credential);
        when(principalCredentialService.recordFailure(any(PrincipalCredentialCommand.class)))
                .thenReturn(latestCredential);

        BizException exception = assertThrows(
                BizException.class,
                () -> service.authenticatePassword(new AuthenticatePasswordCommand(
                        PrincipalIdentityType.USER_ACCOUNT,
                        "admin",
                        PrincipalCredentialType.USER_PASSWORD,
                        "wrong-password",
                        new PrincipalPasswordPolicyDTO(true, 5, 60))));

        assertTrue(exception.getMessage().contains("剩余2次"));
        verify(principalCredentialService).recordFailure(any(PrincipalCredentialCommand.class));
    }

    private static PrincipalCredential activePasswordCredential(int failedCount) {
        PrincipalCredential credential = new PrincipalCredential();
        credential.setId(PrincipalCredentialId.of(200L));
        credential.setIdentityId(PrincipalIdentityId.of(100L));
        credential.setCredentialType(PrincipalCredentialType.USER_PASSWORD);
        credential.setCredentialValue(PasswordHelper.encrypt("right-password"));
        credential.setStatus(PrincipalCredentialStatus.ACTIVE);
        credential.setFailedCount(failedCount);
        credential.setFailedLimit(5);
        return credential;
    }
}
