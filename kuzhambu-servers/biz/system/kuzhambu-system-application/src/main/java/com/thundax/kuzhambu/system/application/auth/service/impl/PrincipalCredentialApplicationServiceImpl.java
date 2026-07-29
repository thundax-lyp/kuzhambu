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
        if (query.getId() != null) {
            return principalCredentialRepository.getById(query.getId());
        }
        if (query.getIdentityId() != null && query.getCredentialType() != null) {
            return principalCredentialRepository.getByIdentityIdAndType(
                    query.getIdentityId(), query.getCredentialType());
        }
        if (query.getPrincipalKey() != null && query.getCredentialType() != null) {
            return principalCredentialRepository.getByPrincipalKeyAndType(
                    query.getPrincipalKey(), query.getCredentialType());
        }
        return null;
    }

    @Override
    public List<PrincipalCredential> list(PrincipalCredentialQuery query) {
        return principalCredentialRepository.listByPrincipalKeyAndStatus(query.getPrincipalKey(), query.getStatus());
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
        principalCredential.setId(command.getId());
        principalCredential.setStatus(command.getStatus());
        principalCredentialRepository.updateStatus(principalCredential);
    }

    @Override
    public void changeVerifyState(ChangePrincipalCredentialVerifyStateCommand command) {
        PrincipalCredential principalCredential = new PrincipalCredential();
        principalCredential.setId(command.getId());
        principalCredential.setStatus(command.getStatus());
        principalCredential.setFailedCount(command.getFailedCount());
        principalCredential.setLockedUntil(command.getLockedUntil());
        principalCredential.setLastVerifiedAt(command.getLastVerifiedAt());
        principalCredentialRepository.updateVerifyState(principalCredential);
    }

    @Override
    public PrincipalCredential recordFailure(RecordPrincipalCredentialFailureCommand command) {
        PrincipalCredential credential = new PrincipalCredential();
        credential.setId(command.getId());
        credential.setFailedLimit(command.getFailedLimit());
        credential.setLockedUntil(command.getLockedUntil());
        principalCredentialRepository.recordFailure(credential);
        return principalCredentialRepository.getById(credential.getId());
    }

    private PrincipalCredential principalCredential(CreatePrincipalCredentialCommand command) {
        PrincipalCredential principalCredential = new PrincipalCredential();
        principalCredential.setPrincipalKey(command.getPrincipalKey());
        principalCredential.setIdentityId(command.getIdentityId());
        principalCredential.setCredentialType(command.getCredentialType());
        principalCredential.setCredentialValue(command.getCredentialValue());
        principalCredential.setStatus(command.getStatus());
        principalCredential.setNeedChangePassword(command.isNeedChangePassword());
        principalCredential.setFailedCount(command.getFailedCount());
        principalCredential.setFailedLimit(command.getFailedLimit());
        principalCredential.setLockedUntil(command.getLockedUntil());
        principalCredential.setExpiresAt(command.getExpiresAt());
        principalCredential.setLastVerifiedAt(command.getLastVerifiedAt());
        return principalCredential;
    }

    private PrincipalCredential principalCredential(ChangePrincipalCredentialCommand command) {
        PrincipalCredential principalCredential = principalCredential(new CreatePrincipalCredentialCommand(
                command.getPrincipalKey(),
                command.getIdentityId(),
                command.getCredentialType(),
                command.getCredentialValue(),
                command.getStatus(),
                command.isNeedChangePassword(),
                command.getFailedCount(),
                command.getFailedLimit(),
                command.getLockedUntil(),
                command.getExpiresAt(),
                command.getLastVerifiedAt()));
        principalCredential.setId(command.getId());
        return principalCredential;
    }
}
