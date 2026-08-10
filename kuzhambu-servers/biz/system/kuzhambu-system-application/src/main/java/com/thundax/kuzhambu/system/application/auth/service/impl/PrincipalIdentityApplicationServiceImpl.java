package com.thundax.kuzhambu.system.application.auth.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalIdentityCommand;
import com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalIdentityStatusCommand;
import com.thundax.kuzhambu.system.application.auth.command.CreatePrincipalIdentityCommand;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityApplicationService;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalIdentityRepository;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class PrincipalIdentityApplicationServiceImpl implements PrincipalIdentityApplicationService {

    private final PrincipalIdentityRepository principalIdentityRepository;

    public PrincipalIdentityApplicationServiceImpl(PrincipalIdentityRepository principalIdentityRepository) {
        this.principalIdentityRepository = principalIdentityRepository;
    }

    @Override
    public PrincipalIdentity get(PrincipalIdentityQuery query) {
        if (query == null) {
            return null;
        }
        if (query.id() != null) {
            return principalIdentityRepository.getById(query.id());
        }
        if (query.identityType() != null && StringUtils.isNotBlank(query.identityValue())) {
            return principalIdentityRepository.getByIdentity(query.identityType(), query.identityValue());
        }
        if (query.principalKey() != null && query.identityType() != null) {
            return principalIdentityRepository.getByPrincipalKeyAndType(query.principalKey(), query.identityType());
        }
        return null;
    }

    @Override
    public List<PrincipalIdentity> list(PrincipalIdentityQuery query) {
        return principalIdentityRepository.listByPrincipalKeyAndStatus(query.principalKey(), query.status());
    }

    @Override
    public PrincipalIdentityId create(CreatePrincipalIdentityCommand command) {
        PrincipalIdentity principalIdentity = new PrincipalIdentity();
        principalIdentity.setPrincipalKey(command.principalKey());
        principalIdentity.setType(command.type());
        principalIdentity.setIdentityValue(command.identityValue());
        principalIdentity.setStatus(command.status());
        PrincipalIdentityId id = principalIdentityRepository.insert(principalIdentity);
        principalIdentity.setId(id);
        return id;
    }

    @Override
    public void change(ChangePrincipalIdentityCommand command) {
        principalIdentityRepository.update(principalIdentity(command));
    }

    @Override
    public void changeStatus(ChangePrincipalIdentityStatusCommand command) {
        PrincipalIdentity principalIdentity = new PrincipalIdentity();
        principalIdentity.setId(command.id());
        principalIdentity.setStatus(command.status());
        principalIdentityRepository.updateStatus(principalIdentity);
    }

    private PrincipalIdentity principalIdentity(ChangePrincipalIdentityCommand command) {
        PrincipalIdentity principalIdentity = new PrincipalIdentity();
        principalIdentity.setId(command.id());
        principalIdentity.setPrincipalKey(command.principalKey());
        principalIdentity.setType(command.type());
        principalIdentity.setIdentityValue(command.identityValue());
        principalIdentity.setStatus(command.status());
        return principalIdentity;
    }
}
