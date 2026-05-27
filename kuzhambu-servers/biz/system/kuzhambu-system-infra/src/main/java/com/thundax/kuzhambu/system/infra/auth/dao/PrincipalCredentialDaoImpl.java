package com.thundax.kuzhambu.system.infra.auth.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.system.application.auth.dao.PrincipalCredentialDao;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalCredential;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalCredentialId;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalCredentialIdCodec;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalIdentityIdCodec;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalKey;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.infra.auth.assembler.PrincipalCredentialPersistenceAssembler;
import com.thundax.kuzhambu.system.infra.auth.dataobject.PrincipalCredentialDO;
import com.thundax.kuzhambu.system.infra.auth.mapper.PrincipalCredentialMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PrincipalCredentialDaoImpl implements PrincipalCredentialDao {

    private final PrincipalCredentialMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public PrincipalCredentialDaoImpl(PrincipalCredentialMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PrincipalCredential getById(PrincipalCredentialId id) {
        return PrincipalCredentialPersistenceAssembler.toEntity(
                mapper.selectById(PrincipalCredentialIdCodec.toValue(id)));
    }

    @Override
    public PrincipalCredential getByIdentityIdAndType(
            PrincipalIdentityId identityId, PrincipalCredentialType credentialType) {
        LambdaQueryWrapper<PrincipalCredentialDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrincipalCredentialDO::getIdentityId, PrincipalIdentityIdCodec.toValue(identityId));
        wrapper.eq(PrincipalCredentialDO::getCredentialType, credentialType.value());
        return PrincipalCredentialPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public PrincipalCredential getByPrincipalKeyAndType(
            PrincipalKey principalKey, PrincipalCredentialType credentialType) {
        LambdaQueryWrapper<PrincipalCredentialDO> wrapper = principalKeyWrapper(principalKey);
        wrapper.eq(PrincipalCredentialDO::getCredentialType, credentialType.value());
        return PrincipalCredentialPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public List<PrincipalCredential> listByPrincipalKeyAndStatus(
            PrincipalKey principalKey, PrincipalCredentialStatus status) {
        LambdaQueryWrapper<PrincipalCredentialDO> wrapper = principalKeyWrapper(principalKey);
        if (status != null) {
            wrapper.eq(PrincipalCredentialDO::getStatus, status.value());
        }
        wrapper.orderByDesc(PrincipalCredentialDO::getId);
        return PrincipalCredentialPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public PrincipalCredentialId insert(PrincipalCredential principalCredential) {
        PrincipalCredentialDO dataObject = PrincipalCredentialPersistenceAssembler.toDataObject(principalCredential);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return PrincipalCredentialIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(PrincipalCredential principalCredential) {
        PrincipalCredentialDO dataObject = PrincipalCredentialPersistenceAssembler.toDataObject(principalCredential);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(PrincipalCredentialDO::getPrincipalType, dataObject.getPrincipalType())
                        .set(PrincipalCredentialDO::getPrincipalId, dataObject.getPrincipalId())
                        .set(PrincipalCredentialDO::getIdentityId, dataObject.getIdentityId())
                        .set(PrincipalCredentialDO::getCredentialType, dataObject.getCredentialType())
                        .set(PrincipalCredentialDO::getCredentialValue, dataObject.getCredentialValue())
                        .set(PrincipalCredentialDO::getStatus, dataObject.getStatus())
                        .set(PrincipalCredentialDO::getNeedChangePassword, dataObject.getNeedChangePassword())
                        .set(PrincipalCredentialDO::getFailedCount, dataObject.getFailedCount())
                        .set(PrincipalCredentialDO::getFailedLimit, dataObject.getFailedLimit())
                        .set(PrincipalCredentialDO::getLockedUntil, dataObject.getLockedUntil())
                        .set(PrincipalCredentialDO::getExpiresAt, dataObject.getExpiresAt())
                        .set(PrincipalCredentialDO::getLastVerifiedAt, dataObject.getLastVerifiedAt()));
    }

    @Override
    public int updateStatus(PrincipalCredential principalCredential) {
        PrincipalCredentialDO dataObject = PrincipalCredentialPersistenceAssembler.toDataObject(principalCredential);
        return mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(PrincipalCredentialDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public int updateVerifyState(PrincipalCredential principalCredential) {
        PrincipalCredentialDO dataObject = PrincipalCredentialPersistenceAssembler.toDataObject(principalCredential);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(PrincipalCredentialDO::getStatus, dataObject.getStatus())
                        .set(PrincipalCredentialDO::getFailedCount, dataObject.getFailedCount())
                        .set(PrincipalCredentialDO::getLockedUntil, dataObject.getLockedUntil())
                        .set(PrincipalCredentialDO::getLastVerifiedAt, dataObject.getLastVerifiedAt()));
    }

    private LambdaQueryWrapper<PrincipalCredentialDO> principalKeyWrapper(PrincipalKey principalKey) {
        LambdaQueryWrapper<PrincipalCredentialDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(
                PrincipalCredentialDO::getPrincipalType,
                principalKey.getPrincipalType().value());
        wrapper.eq(PrincipalCredentialDO::getPrincipalId, principalKey.getPrincipalId());
        return wrapper;
    }

    private LambdaUpdateWrapper<PrincipalCredentialDO> buildIdUpdateWrapper(PrincipalCredentialDO dataObject) {
        LambdaUpdateWrapper<PrincipalCredentialDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PrincipalCredentialDO::getId, dataObject.getId());
        return wrapper;
    }
}
