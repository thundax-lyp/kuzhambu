package com.thundax.kuzhambu.ai.infra.config.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.repository.AiModelRepository;
import com.thundax.kuzhambu.ai.infra.config.persistence.assembler.AiConfigPersistenceAssembler;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.AiModelDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.mapper.AiModelMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AiModelRepositoryImpl implements AiModelRepository {

    private final AiModelMapper aiModelMapper;

    public AiModelRepositoryImpl(AiModelMapper aiModelMapper) {
        this.aiModelMapper = aiModelMapper;
    }

    @Override
    public AiModel get(AiModelId id) {
        return AiConfigPersistenceAssembler.toModelDomain(aiModelMapper.selectById(AiModelIdCodec.toValue(id)));
    }

    @Override
    public List<AiModel> list(String apiSource, Boolean enabled) {
        return AiConfigPersistenceAssembler.toModelDomainList(
                aiModelMapper.selectList(new LambdaQueryWrapper<AiModelDO>()
                        .eq(apiSource != null && !apiSource.isBlank(), AiModelDO::getApiSource, apiSource)
                        .eq(enabled != null, AiModelDO::getEnabled, enabled)));
    }

    @Override
    public AiModelId insert(AiModel model) {
        AiModelDO dataObject = AiConfigPersistenceAssembler.toObject(model);
        if (dataObject.getRegisteredAt() == null) {
            dataObject.setRegisteredAt(Instant.now());
        }
        aiModelMapper.insert(dataObject);
        return AiModelIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(AiModel model) {
        AiModelDO dataObject = AiConfigPersistenceAssembler.toObject(model);
        return aiModelMapper.update(
                null,
                new LambdaUpdateWrapper<AiModelDO>()
                        .eq(AiModelDO::getId, dataObject.getId())
                        .set(AiModelDO::getApiSource, dataObject.getApiSource())
                        .set(AiModelDO::getBaseUrl, dataObject.getBaseUrl())
                        .set(AiModelDO::getEncryptedApiKey, dataObject.getEncryptedApiKey())
                        .set(AiModelDO::getModelName, dataObject.getModelName())
                        .set(AiModelDO::getDisplayName, dataObject.getDisplayName())
                        .set(AiModelDO::getCapabilitiesJson, dataObject.getCapabilitiesJson())
                        .set(AiModelDO::getDefaultParamsJson, dataObject.getDefaultParamsJson())
                        .set(AiModelDO::getDescription, dataObject.getDescription())
                        .set(AiModelDO::getEnabled, dataObject.getEnabled()));
    }

    @Override
    public int delete(AiModelId id) {
        return aiModelMapper.deleteById(AiModelIdCodec.toValue(id));
    }
}
