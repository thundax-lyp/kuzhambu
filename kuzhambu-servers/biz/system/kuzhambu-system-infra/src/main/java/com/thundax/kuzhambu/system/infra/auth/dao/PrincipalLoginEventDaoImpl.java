package com.thundax.kuzhambu.system.infra.auth.dao;

import com.thundax.kuzhambu.system.application.auth.codec.PrincipalLoginEventIdCodec;
import com.thundax.kuzhambu.system.application.auth.dao.PrincipalLoginEventDao;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalLoginEvent;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalLoginEventId;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.infra.auth.assembler.PrincipalLoginEventPersistenceAssembler;
import com.thundax.kuzhambu.system.infra.auth.dataobject.PrincipalLoginEventDO;
import com.thundax.kuzhambu.system.infra.auth.mapper.PrincipalLoginEventMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PrincipalLoginEventDaoImpl implements PrincipalLoginEventDao {

    private final PrincipalLoginEventMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public PrincipalLoginEventDaoImpl(PrincipalLoginEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PrincipalLoginEvent getById(PrincipalLoginEventId id) {
        return PrincipalLoginEventPersistenceAssembler.toEntity(
                mapper.selectById(PrincipalLoginEventIdCodec.toValue(id)));
    }

    @Override
    public PrincipalLoginEventId insert(PrincipalLoginEvent event) {
        PrincipalLoginEventDO dataObject = PrincipalLoginEventPersistenceAssembler.toDataObject(event);
        dataObject.setId(PrincipalLoginEventIdCodec.toValue(PrincipalLoginEventIdCodec.nextId(idGenerator)));
        mapper.insert(dataObject);
        return PrincipalLoginEventIdCodec.toDomain(dataObject.getId());
    }
}
