package com.thundax.kuzhambu.infra.auth.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.biz.auth.dao.OAuthClientDao;
import com.thundax.kuzhambu.biz.auth.entity.OAuthClient;
import com.thundax.kuzhambu.biz.auth.entity.enums.OAuthClientStatus;
import com.thundax.kuzhambu.common.core.id.EntityId;
import com.thundax.kuzhambu.common.core.id.EntityIdCodec;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.infra.auth.persistence.assembler.OAuthClientPersistenceAssembler;
import com.thundax.kuzhambu.infra.auth.persistence.dataobject.OAuthClientDO;
import com.thundax.kuzhambu.infra.auth.persistence.mapper.OAuthClientMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OAuthClientDaoImpl implements OAuthClientDao {

    private final OAuthClientMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public OAuthClientDaoImpl(OAuthClientMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public OAuthClient getById(EntityId id) {
        return OAuthClientPersistenceAssembler.toEntity(mapper.selectById(EntityIdCodec.toValue(id)));
    }

    @Override
    public OAuthClient getByClientId(String clientId) {
        LambdaQueryWrapper<OAuthClientDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OAuthClientDO::getClientId, clientId);
        return OAuthClientPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public OAuthClient getByClientIdAndStatus(String clientId, OAuthClientStatus status) {
        LambdaQueryWrapper<OAuthClientDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OAuthClientDO::getClientId, clientId);
        if (status != null) {
            wrapper.eq(OAuthClientDO::getStatus, status.value());
        }
        return OAuthClientPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public EntityId insert(OAuthClient client) {
        OAuthClientDO dataObject = OAuthClientPersistenceAssembler.toDataObject(client);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return EntityIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(OAuthClient client) {
        OAuthClientDO dataObject = OAuthClientPersistenceAssembler.toDataObject(client);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(OAuthClientDO::getClientId, dataObject.getClientId())
                        .set(OAuthClientDO::getClientSecretHash, dataObject.getClientSecretHash())
                        .set(OAuthClientDO::getClientName, dataObject.getClientName())
                        .set(OAuthClientDO::getClientType, dataObject.getClientType())
                        .set(OAuthClientDO::getGrantTypes, dataObject.getGrantTypes())
                        .set(OAuthClientDO::getScopes, dataObject.getScopes())
                        .set(OAuthClientDO::getRedirectUris, dataObject.getRedirectUris())
                        .set(OAuthClientDO::getAccessTokenTtlSeconds, dataObject.getAccessTokenTtlSeconds())
                        .set(OAuthClientDO::getRefreshTokenTtlSeconds, dataObject.getRefreshTokenTtlSeconds())
                        .set(OAuthClientDO::getStatus, dataObject.getStatus())
                        .set(OAuthClientDO::getContact, dataObject.getContact())
                        .set(OAuthClientDO::getRemark, dataObject.getRemark()));
    }

    private LambdaUpdateWrapper<OAuthClientDO> buildIdUpdateWrapper(OAuthClientDO dataObject) {
        LambdaUpdateWrapper<OAuthClientDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OAuthClientDO::getId, dataObject.getId());
        return wrapper;
    }
}
