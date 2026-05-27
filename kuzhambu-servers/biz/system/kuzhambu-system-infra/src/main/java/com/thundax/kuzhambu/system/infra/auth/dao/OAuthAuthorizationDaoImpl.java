package com.thundax.kuzhambu.system.infra.auth.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.common.core.id.EntityId;
import com.thundax.kuzhambu.common.core.id.EntityIdCodec;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.application.auth.dao.OAuthAuthorizationDao;
import com.thundax.kuzhambu.system.domain.model.entity.OAuthAuthorization;
import com.thundax.kuzhambu.system.infra.auth.assembler.OAuthAuthorizationPersistenceAssembler;
import com.thundax.kuzhambu.system.infra.auth.dataobject.OAuthAuthorizationDO;
import com.thundax.kuzhambu.system.infra.auth.mapper.OAuthAuthorizationMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OAuthAuthorizationDaoImpl implements OAuthAuthorizationDao {

    private final OAuthAuthorizationMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public OAuthAuthorizationDaoImpl(OAuthAuthorizationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public OAuthAuthorization getById(EntityId id) {
        return OAuthAuthorizationPersistenceAssembler.toEntity(mapper.selectById(EntityIdCodec.toValue(id)));
    }

    @Override
    public OAuthAuthorization getByAuthorizationCode(String authorizationCode) {
        LambdaQueryWrapper<OAuthAuthorizationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OAuthAuthorizationDO::getAuthorizationCode, authorizationCode);
        return OAuthAuthorizationPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public EntityId insert(OAuthAuthorization authorization) {
        OAuthAuthorizationDO dataObject = OAuthAuthorizationPersistenceAssembler.toDataObject(authorization);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return EntityIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateUsed(OAuthAuthorization authorization) {
        OAuthAuthorizationDO dataObject = OAuthAuthorizationPersistenceAssembler.toDataObject(authorization);
        return mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(OAuthAuthorizationDO::isUsed, dataObject.isUsed()));
    }

    @Override
    public int deleteByAuthorizationCode(String authorizationCode) {
        LambdaQueryWrapper<OAuthAuthorizationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OAuthAuthorizationDO::getAuthorizationCode, authorizationCode);
        return mapper.delete(wrapper);
    }

    private LambdaUpdateWrapper<OAuthAuthorizationDO> buildIdUpdateWrapper(OAuthAuthorizationDO dataObject) {
        LambdaUpdateWrapper<OAuthAuthorizationDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OAuthAuthorizationDO::getId, dataObject.getId());
        return wrapper;
    }
}
