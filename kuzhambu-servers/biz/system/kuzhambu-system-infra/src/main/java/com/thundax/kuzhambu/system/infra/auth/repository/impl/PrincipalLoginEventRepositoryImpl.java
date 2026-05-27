package com.thundax.kuzhambu.system.infra.auth.repository.impl;

import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.domain.auth.codec.PrincipalLoginEventIdCodec;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalLoginEvent;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalLoginEventId;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalLoginEventRepository;
import com.thundax.kuzhambu.system.infra.auth.persistence.assembler.PrincipalLoginEventPersistenceAssembler;
import com.thundax.kuzhambu.system.infra.auth.persistence.dataobject.PrincipalLoginEventDO;
import com.thundax.kuzhambu.system.infra.auth.persistence.mapper.PrincipalLoginEventMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PrincipalLoginEventRepositoryImpl implements PrincipalLoginEventRepository {

    private final PrincipalLoginEventMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public PrincipalLoginEventRepositoryImpl(PrincipalLoginEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PrincipalLoginEvent getById(PrincipalLoginEventId id) {
        return PrincipalLoginEventPersistenceAssembler.toDomain(
                mapper.selectById(PrincipalLoginEventIdCodec.toValue(id)));
    }

    @Override
    public PrincipalLoginEventId insert(PrincipalLoginEvent event) {
        PrincipalLoginEventDO dataObject = PrincipalLoginEventPersistenceAssembler.toObject(event);
        dataObject.setId(PrincipalLoginEventIdCodec.toValue(PrincipalLoginEventIdCodec.nextId(idGenerator)));
        mapper.insert(dataObject);
        return PrincipalLoginEventIdCodec.toDomain(dataObject.getId());
    }
}
