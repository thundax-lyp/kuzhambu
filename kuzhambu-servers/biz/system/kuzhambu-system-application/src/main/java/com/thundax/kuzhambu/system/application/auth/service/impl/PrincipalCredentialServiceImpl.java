package com.thundax.kuzhambu.system.application.auth.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalCredentialService;
import com.thundax.kuzhambu.system.application.auth.service.command.PrincipalCredentialCommand;
import com.thundax.kuzhambu.system.application.auth.service.query.PrincipalCredentialQuery;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalCredential;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalCredentialId;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalCredentialRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class PrincipalCredentialServiceImpl implements PrincipalCredentialService {

    private final PrincipalCredentialRepository principalCredentialRepository;

    public PrincipalCredentialServiceImpl(PrincipalCredentialRepository principalCredentialRepository) {
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
    public PrincipalCredentialId create(PrincipalCredentialCommand command) {
        PrincipalCredential principalCredential = command.getPrincipalCredential();
        PrincipalCredentialId id = principalCredentialRepository.insert(principalCredential);
        principalCredential.setId(id);
        return id;
    }

    @Override
    public void change(PrincipalCredentialCommand command) {
        principalCredentialRepository.update(command.getPrincipalCredential());
    }

    @Override
    public void changeStatus(PrincipalCredentialCommand command) {
        principalCredentialRepository.updateStatus(command.getPrincipalCredential());
    }

    @Override
    public void changeVerifyState(PrincipalCredentialCommand command) {
        principalCredentialRepository.updateVerifyState(command.getPrincipalCredential());
    }
}
