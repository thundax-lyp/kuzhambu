package com.thundax.kuzhambu.system.application.auth.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalCredential;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.application.auth.exception.InvalidPasswordException;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalAuthService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalCredentialService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityService;
import com.thundax.kuzhambu.system.application.auth.service.command.AuthenticateIdentityCommand;
import com.thundax.kuzhambu.system.application.auth.service.command.AuthenticatePasswordCommand;
import com.thundax.kuzhambu.system.application.auth.service.command.PrincipalCredentialCommand;
import com.thundax.kuzhambu.system.application.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.thundax.kuzhambu.system.application.auth.service.query.PrincipalCredentialQuery;
import com.thundax.kuzhambu.system.application.auth.service.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.auth.utils.PasswordHelper;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalIdentityId;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class PrincipalAuthServiceImpl implements PrincipalAuthService {

    private final PrincipalIdentityService principalIdentityService;
    private final PrincipalCredentialService principalCredentialService;

    public PrincipalAuthServiceImpl(
            PrincipalIdentityService principalIdentityService, PrincipalCredentialService principalCredentialService) {
        this.principalIdentityService = principalIdentityService;
        this.principalCredentialService = principalCredentialService;
    }

    @Override
    public PrincipalIdentity authenticateIdentity(AuthenticateIdentityCommand command) {
        PrincipalIdentity identity = principalIdentityService.get(identityQuery(command));
        if (identity == null || !identity.isEnabled()) {
            throw new InvalidPasswordException();
        }
        return identity;
    }

    @Override
    public PrincipalIdentity authenticatePassword(AuthenticatePasswordCommand command) {
        PrincipalIdentity identity = authenticateIdentity(
                new AuthenticateIdentityCommand(command.getIdentityType(), command.getIdentityValue()));
        PrincipalCredential credential =
                principalCredentialService.get(credentialQuery(identity.getId(), command.getCredentialType()));
        if (credential == null) {
            throw new InvalidPasswordException();
        }
        PrincipalPasswordPolicyDTO passwordPolicy = command.getPasswordPolicy() == null
                ? PrincipalPasswordPolicyDTO.disabled()
                : command.getPasswordPolicy();
        validateCredential(credential, command.getPlainPassword(), passwordPolicy);
        return identity;
    }

    private void validateCredential(
            PrincipalCredential credential, String plainPassword, PrincipalPasswordPolicyDTO passwordPolicy) {
        Date now = new Date();
        if (credential.isLocked(now)) {
            throw new BizException("帐号已被锁定，请等待（" + lockedExpireSeconds(credential, passwordPolicy, now) + "）秒后自动解锁!");
        }
        if (credential.isExpired(now)) {
            throw new BizException("认证凭据已过期");
        }
        if (!credential.isActive()) {
            throw new BizException("认证凭据不可用");
        }

        if (PasswordHelper.validate(plainPassword, credential.getCredentialValue())) {
            credential.markVerified(now);
            principalCredentialService.changeVerifyState(new PrincipalCredentialCommand(credential));
            return;
        }

        if (!passwordPolicy.isLockEnabled()) {
            throw new InvalidPasswordException();
        }

        if (credential.getFailedLimit() <= 0) {
            credential.setFailedLimit(passwordPolicy.getMaxFailedCount());
        }
        Date lockedUntil = new Date(now.getTime() + passwordPolicy.getLockSeconds() * 1000L);
        credential.markFailed(lockedUntil);
        principalCredentialService.changeVerifyState(new PrincipalCredentialCommand(credential));
        if (credential.isLocked(now)) {
            throw new BizException("帐号已被锁定，请等待（" + passwordPolicy.getLockSeconds() + "）秒后自动解锁!");
        }
        throw new BizException("密码输入错误"
                + credential.getFailedLimit()
                + "次后将被锁定，剩余"
                + (credential.getFailedLimit() - credential.getFailedCount())
                + "次");
    }

    private PrincipalIdentityQuery identityQuery(AuthenticateIdentityCommand command) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setIdentityType(command.getIdentityType());
        query.setIdentityValue(command.getIdentityValue());
        return query;
    }

    private PrincipalCredentialQuery credentialQuery(
            PrincipalIdentityId identityId, PrincipalCredentialType credentialType) {
        PrincipalCredentialQuery query = new PrincipalCredentialQuery();
        query.setIdentityId(identityId);
        query.setCredentialType(credentialType);
        return query;
    }

    private long lockedExpireSeconds(
            PrincipalCredential credential, PrincipalPasswordPolicyDTO passwordPolicy, Date now) {
        if (credential.getLockedUntil() == null) {
            return passwordPolicy.getLockSeconds();
        }
        long remaining = (credential.getLockedUntil().getTime() - now.getTime()) / 1000L;
        return Math.max(remaining, 0L);
    }
}
