package com.thundax.kuzhambu.ai.infra.config.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.ai.domain.config.codec.AiBusinessConfigIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiBusinessConfigId;
import com.thundax.kuzhambu.ai.domain.config.repository.AiBusinessConfigRepository;
import com.thundax.kuzhambu.ai.infra.config.persistence.assembler.AiBusinessConfigPersistenceAssembler;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.AiBusinessConfigDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.mapper.AiBusinessConfigMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AiBusinessConfigRepositoryImpl implements AiBusinessConfigRepository {

    private final AiBusinessConfigMapper aiBusinessConfigMapper;

    public AiBusinessConfigRepositoryImpl(AiBusinessConfigMapper aiBusinessConfigMapper) {
        this.aiBusinessConfigMapper = aiBusinessConfigMapper;
    }

    @Override
    public AiBusinessConfig getById(AiBusinessConfigId id) {
        return AiBusinessConfigPersistenceAssembler.toDomain(
                aiBusinessConfigMapper.selectById(AiBusinessConfigIdCodec.toValue(id)));
    }

    @Override
    public AiBusinessConfig getByCapability(AiBusinessCapability capability) {
        return AiBusinessConfigPersistenceAssembler.toDomain(
                aiBusinessConfigMapper.selectByCapability(capability == null ? null : capability.value()));
    }

    @Override
    public List<AiBusinessConfig> list(AiBusinessCapability capability, Boolean enabled) {
        return AiBusinessConfigPersistenceAssembler.toDomainList(
                aiBusinessConfigMapper.selectList(new LambdaQueryWrapper<AiBusinessConfigDO>()
                        .eq(
                                capability != null,
                                AiBusinessConfigDO::getCapability,
                                capability == null ? null : capability.value())
                        .eq(enabled != null, AiBusinessConfigDO::getEnabled, enabled)
                        .orderByAsc(AiBusinessConfigDO::getPriority)
                        .orderByAsc(AiBusinessConfigDO::getId)));
    }

    @Override
    public AiBusinessConfigId insert(AiBusinessConfig config) {
        AiBusinessConfigDO dataObject = AiBusinessConfigPersistenceAssembler.toObject(config);
        if (dataObject.getConfiguredAt() == null) {
            dataObject.setConfiguredAt(Instant.now());
        }
        aiBusinessConfigMapper.insert(dataObject);
        return AiBusinessConfigIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(AiBusinessConfig config) {
        AiBusinessConfigDO dataObject = AiBusinessConfigPersistenceAssembler.toObject(config);
        return aiBusinessConfigMapper.update(
                null,
                new LambdaUpdateWrapper<AiBusinessConfigDO>()
                        .eq(AiBusinessConfigDO::getId, dataObject.getId())
                        .set(AiBusinessConfigDO::getCapability, dataObject.getCapability())
                        .set(AiBusinessConfigDO::getPromptTemplateId, dataObject.getPromptTemplateId())
                        .set(AiBusinessConfigDO::getModelId, dataObject.getModelId())
                        .set(AiBusinessConfigDO::getDefaultParamsJson, dataObject.getDefaultParamsJson())
                        .set(AiBusinessConfigDO::getEnabled, dataObject.getEnabled())
                        .set(AiBusinessConfigDO::getPriority, dataObject.getPriority()));
    }

    @Override
    public int maxPriority() {
        List<Object> values =
                aiBusinessConfigMapper.selectObjs(new QueryWrapper<AiBusinessConfigDO>().select("max(priority)"));
        if (values == null || values.isEmpty() || values.get(0) == null) {
            return 0;
        }
        return ((Number) values.get(0)).intValue();
    }

    @Override
    public int delete(AiBusinessConfigId id) {
        return aiBusinessConfigMapper.deleteById(AiBusinessConfigIdCodec.toValue(id));
    }
}
