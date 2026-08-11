package com.thundax.kuzhambu.system.application.core.service.impl;

import com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalCredentialCommand;
import com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalIdentityCommand;
import com.thundax.kuzhambu.system.application.auth.command.CreatePrincipalCredentialCommand;
import com.thundax.kuzhambu.system.application.auth.command.CreatePrincipalIdentityCommand;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalCredentialQuery;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalCredentialApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityApplicationService;
import com.thundax.kuzhambu.system.application.core.command.ChangeUserAccountCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateUserAccountCommand;
import com.thundax.kuzhambu.system.application.core.service.UserAccountApplicationService;
import com.thundax.kuzhambu.system.application.core.service.UserManagementApplicationService;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalCredential;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountApplicationServiceImpl implements UserAccountApplicationService {
    private static final int DEFAULT_PASSWORD_FAILED_LIMIT = 0;
    private final UserManagementApplicationService userService;
    private final PrincipalIdentityApplicationService identityService;
    private final PrincipalCredentialApplicationService credentialService;

    public UserAccountApplicationServiceImpl(
            UserManagementApplicationService userService,
            PrincipalIdentityApplicationService identityService,
            PrincipalCredentialApplicationService credentialService) {
        this.userService = userService;
        this.identityService = identityService;
        this.credentialService = credentialService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserId create(CreateUserAccountCommand command) {
        UserId userId = userService.create(command.userCommand());
        PrincipalIdentity identity = upsertIdentity(userId, command.loginName());
        upsertPassword(userId, identity.getId(), command.encryptedPassword());
        return userId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void change(ChangeUserAccountCommand command) {
        userService.changeInfo(command.userCommand());
        PrincipalIdentity identity = upsertIdentity(command.userId(), command.loginName());
        command.encryptedPassword().ifPresent(password -> upsertPassword(command.userId(), identity.getId(), password));
    }

    private PrincipalIdentity upsertIdentity(UserId userId, String loginName) {
        PrincipalKey key = PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(userId));
        PrincipalIdentity identity = identityService.get(
                new PrincipalIdentityQuery(null, PrincipalIdentityType.USER_ACCOUNT, null, key, null));
        if (identity == null) {
            PrincipalIdentityId id = identityService.create(new CreatePrincipalIdentityCommand(
                    key, PrincipalIdentityType.USER_ACCOUNT, loginName, PrincipalIdentityStatus.ENABLED));
            identity = new PrincipalIdentity();
            identity.setId(id);
        } else {
            identityService.change(new ChangePrincipalIdentityCommand(
                    identity.getId(),
                    key,
                    PrincipalIdentityType.USER_ACCOUNT,
                    loginName,
                    PrincipalIdentityStatus.ENABLED));
        }
        return identity;
    }

    private void upsertPassword(UserId userId, PrincipalIdentityId identityId, String encryptedPassword) {
        PrincipalKey key = PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(userId));
        PrincipalCredential credential = credentialService.get(
                new PrincipalCredentialQuery(null, identityId, PrincipalCredentialType.USER_PASSWORD, null, null));
        if (credential == null) {
            credentialService.create(new CreatePrincipalCredentialCommand(
                    key,
                    identityId,
                    PrincipalCredentialType.USER_PASSWORD,
                    encryptedPassword,
                    PrincipalCredentialStatus.ACTIVE,
                    false,
                    0,
                    DEFAULT_PASSWORD_FAILED_LIMIT,
                    null,
                    null,
                    null));
            return;
        }
        credentialService.change(new ChangePrincipalCredentialCommand(
                credential.getId(),
                key,
                identityId,
                PrincipalCredentialType.USER_PASSWORD,
                encryptedPassword,
                PrincipalCredentialStatus.ACTIVE,
                false,
                0,
                DEFAULT_PASSWORD_FAILED_LIMIT,
                null,
                credential.getExpiresAt(),
                null));
    }
}
