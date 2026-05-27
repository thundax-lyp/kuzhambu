package com.thundax.kuzhambu.system.infra.auth.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.application.auth.dao.PrincipalIdentityDao;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.codec.PrincipalIdentityIdCodec;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.infra.auth.assembler.PrincipalIdentityPersistenceAssembler;
import com.thundax.kuzhambu.system.infra.auth.dataobject.PrincipalIdentityDO;
import com.thundax.kuzhambu.system.infra.auth.mapper.PrincipalIdentityMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PrincipalIdentityDaoImpl implements PrincipalIdentityDao {

    private final PrincipalIdentityMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public PrincipalIdentityDaoImpl(PrincipalIdentityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PrincipalIdentity getById(PrincipalIdentityId id) {
        return PrincipalIdentityPersistenceAssembler.toEntity(mapper.selectById(PrincipalIdentityIdCodec.toValue(id)));
    }

    @Override
    public PrincipalIdentity getByIdentity(PrincipalIdentityType identityType, String identityValue) {
        LambdaQueryWrapper<PrincipalIdentityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrincipalIdentityDO::getIdentityType, identityType.value());
        wrapper.eq(PrincipalIdentityDO::getIdentityValue, identityValue);
        return PrincipalIdentityPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public PrincipalIdentity getByPrincipalKeyAndType(PrincipalKey principalKey, PrincipalIdentityType identityType) {
        LambdaQueryWrapper<PrincipalIdentityDO> wrapper = principalKeyWrapper(principalKey);
        wrapper.eq(PrincipalIdentityDO::getIdentityType, identityType.value());
        return PrincipalIdentityPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public List<PrincipalIdentity> listByPrincipalKeyAndStatus(
            PrincipalKey principalKey, PrincipalIdentityStatus status) {
        LambdaQueryWrapper<PrincipalIdentityDO> wrapper = principalKeyWrapper(principalKey);
        if (status != null) {
            wrapper.eq(PrincipalIdentityDO::getStatus, status.value());
        }
        wrapper.orderByDesc(PrincipalIdentityDO::getId);
        return PrincipalIdentityPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public PrincipalIdentityId insert(PrincipalIdentity principalIdentity) {
        PrincipalIdentityDO dataObject = PrincipalIdentityPersistenceAssembler.toDataObject(principalIdentity);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return PrincipalIdentityIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(PrincipalIdentity principalIdentity) {
        PrincipalIdentityDO dataObject = PrincipalIdentityPersistenceAssembler.toDataObject(principalIdentity);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(PrincipalIdentityDO::getPrincipalType, dataObject.getPrincipalType())
                        .set(PrincipalIdentityDO::getPrincipalId, dataObject.getPrincipalId())
                        .set(PrincipalIdentityDO::getIdentityType, dataObject.getIdentityType())
                        .set(PrincipalIdentityDO::getIdentityValue, dataObject.getIdentityValue())
                        .set(PrincipalIdentityDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public int updateStatus(PrincipalIdentity principalIdentity) {
        PrincipalIdentityDO dataObject = PrincipalIdentityPersistenceAssembler.toDataObject(principalIdentity);
        return mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(PrincipalIdentityDO::getStatus, dataObject.getStatus()));
    }

    private LambdaQueryWrapper<PrincipalIdentityDO> principalKeyWrapper(PrincipalKey principalKey) {
        LambdaQueryWrapper<PrincipalIdentityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(
                PrincipalIdentityDO::getPrincipalType,
                principalKey.getPrincipalType().value());
        wrapper.eq(PrincipalIdentityDO::getPrincipalId, principalKey.getPrincipalId());
        return wrapper;
    }

    private LambdaUpdateWrapper<PrincipalIdentityDO> buildIdUpdateWrapper(PrincipalIdentityDO dataObject) {
        LambdaUpdateWrapper<PrincipalIdentityDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PrincipalIdentityDO::getId, dataObject.getId());
        return wrapper;
    }
}
