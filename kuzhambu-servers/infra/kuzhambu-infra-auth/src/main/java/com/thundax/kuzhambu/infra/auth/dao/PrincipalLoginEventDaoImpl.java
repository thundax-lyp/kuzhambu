package com.thundax.kuzhambu.infra.auth.dao;

import com.thundax.kuzhambu.biz.auth.codec.PrincipalLoginEventIdCodec;
import com.thundax.kuzhambu.biz.auth.dao.PrincipalLoginEventDao;
import com.thundax.kuzhambu.biz.auth.entity.PrincipalLoginEvent;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalLoginEventId;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.infra.auth.assembler.PrincipalLoginEventPersistenceAssembler;
import com.thundax.kuzhambu.infra.auth.dataobject.PrincipalLoginEventDO;
import com.thundax.kuzhambu.infra.auth.mapper.PrincipalLoginEventMapper;
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
