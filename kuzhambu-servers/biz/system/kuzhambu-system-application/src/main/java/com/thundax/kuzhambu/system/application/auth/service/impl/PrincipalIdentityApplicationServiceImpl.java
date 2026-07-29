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
        if (query.getId() != null) {
            return principalIdentityRepository.getById(query.getId());
        }
        if (query.getIdentityType() != null && StringUtils.isNotBlank(query.getIdentityValue())) {
            return principalIdentityRepository.getByIdentity(query.getIdentityType(), query.getIdentityValue());
        }
        if (query.getPrincipalKey() != null && query.getIdentityType() != null) {
            return principalIdentityRepository.getByPrincipalKeyAndType(
                    query.getPrincipalKey(), query.getIdentityType());
        }
        return null;
    }

    @Override
    public List<PrincipalIdentity> list(PrincipalIdentityQuery query) {
        return principalIdentityRepository.listByPrincipalKeyAndStatus(query.getPrincipalKey(), query.getStatus());
    }

    @Override
    public PrincipalIdentityId create(CreatePrincipalIdentityCommand command) {
        PrincipalIdentity principalIdentity = new PrincipalIdentity();
        principalIdentity.setPrincipalKey(command.getPrincipalKey());
        principalIdentity.setType(command.getType());
        principalIdentity.setIdentityValue(command.getIdentityValue());
        principalIdentity.setStatus(command.getStatus());
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
        principalIdentity.setId(command.getId());
        principalIdentity.setStatus(command.getStatus());
        principalIdentityRepository.updateStatus(principalIdentity);
    }

    private PrincipalIdentity principalIdentity(ChangePrincipalIdentityCommand command) {
        PrincipalIdentity principalIdentity = new PrincipalIdentity();
        principalIdentity.setId(command.getId());
        principalIdentity.setPrincipalKey(command.getPrincipalKey());
        principalIdentity.setType(command.getType());
        principalIdentity.setIdentityValue(command.getIdentityValue());
        principalIdentity.setStatus(command.getStatus());
        return principalIdentity;
    }
}
