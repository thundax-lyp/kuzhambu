package com.thundax.kuzhambu.system.application.auth.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalCredentialCommand;
import com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalCredentialStatusCommand;
import com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalCredentialVerifyStateCommand;
import com.thundax.kuzhambu.system.application.auth.command.CreatePrincipalCredentialCommand;
import com.thundax.kuzhambu.system.application.auth.command.RecordPrincipalCredentialFailureCommand;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalCredentialQuery;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalCredentialApplicationService;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalCredential;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalCredentialId;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalCredentialRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class PrincipalCredentialApplicationServiceImpl implements PrincipalCredentialApplicationService {

    private final PrincipalCredentialRepository principalCredentialRepository;

    public PrincipalCredentialApplicationServiceImpl(PrincipalCredentialRepository principalCredentialRepository) {
        this.principalCredentialRepository = principalCredentialRepository;
    }

    @Override
    public PrincipalCredential get(PrincipalCredentialQuery query) {
        if (query == null) {
            return null;
        }
        if (query.id() != null) {
            return principalCredentialRepository.getById(query.id());
        }
        if (query.identityId() != null && query.credentialType() != null) {
            return principalCredentialRepository.getByIdentityIdAndType(query.identityId(), query.credentialType());
        }
        if (query.principalKey() != null && query.credentialType() != null) {
            return principalCredentialRepository.getByPrincipalKeyAndType(query.principalKey(), query.credentialType());
        }
        return null;
    }

    @Override
    public List<PrincipalCredential> list(PrincipalCredentialQuery query) {
        return principalCredentialRepository.listByPrincipalKeyAndStatus(query.principalKey(), query.status());
    }

    @Override
    public PrincipalCredentialId create(CreatePrincipalCredentialCommand command) {
        PrincipalCredential principalCredential = principalCredential(command);
        PrincipalCredentialId id = principalCredentialRepository.insert(principalCredential);
        principalCredential.setId(id);
        return id;
    }

    @Override
    public void change(ChangePrincipalCredentialCommand command) {
        principalCredentialRepository.update(principalCredential(command));
    }

    @Override
    public void changeStatus(ChangePrincipalCredentialStatusCommand command) {
        PrincipalCredential principalCredential = new PrincipalCredential();
        principalCredential.setId(command.id());
        principalCredential.setStatus(command.status());
        principalCredentialRepository.updateStatus(principalCredential);
    }

    @Override
    public void changeVerifyState(ChangePrincipalCredentialVerifyStateCommand command) {
        PrincipalCredential principalCredential = new PrincipalCredential();
        principalCredential.setId(command.id());
        principalCredential.setStatus(command.status());
        principalCredential.setFailedCount(command.failedCount());
        principalCredential.setLockedUntil(command.lockedUntil());
        principalCredential.setLastVerifiedAt(command.lastVerifiedAt());
        principalCredentialRepository.updateVerifyState(principalCredential);
    }

    @Override
    public PrincipalCredential recordFailure(RecordPrincipalCredentialFailureCommand command) {
        PrincipalCredential credential = new PrincipalCredential();
        credential.setId(command.id());
        credential.setFailedLimit(command.failedLimit());
        credential.setLockedUntil(command.lockedUntil());
        principalCredentialRepository.updateFailure(credential);
        return principalCredentialRepository.getById(credential.getId());
    }

    private PrincipalCredential principalCredential(CreatePrincipalCredentialCommand command) {
        PrincipalCredential principalCredential = new PrincipalCredential();
        principalCredential.setPrincipalKey(command.principalKey());
        principalCredential.setIdentityId(command.identityId());
        principalCredential.setCredentialType(command.credentialType());
        principalCredential.setCredentialValue(command.credentialValue());
        principalCredential.setStatus(command.status());
        principalCredential.setNeedChangePassword(command.needChangePassword());
        principalCredential.setFailedCount(command.failedCount());
        principalCredential.setFailedLimit(command.failedLimit());
        principalCredential.setLockedUntil(command.lockedUntil());
        principalCredential.setExpiresAt(command.expiresAt());
        principalCredential.setLastVerifiedAt(command.lastVerifiedAt());
        return principalCredential;
    }

    private PrincipalCredential principalCredential(ChangePrincipalCredentialCommand command) {
        PrincipalCredential principalCredential = principalCredential(new CreatePrincipalCredentialCommand(
                command.principalKey(),
                command.identityId(),
                command.credentialType(),
                command.credentialValue(),
                command.status(),
                command.needChangePassword(),
                command.failedCount(),
                command.failedLimit(),
                command.lockedUntil(),
                command.expiresAt(),
                command.lastVerifiedAt()));
        principalCredential.setId(command.id());
        return principalCredential;
    }
}
