package com.thundax.kuzhambu.operations.infra.health.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.health.codec.HealthCheckIdCodec;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthCheckRepository;
import com.thundax.kuzhambu.operations.infra.health.persistence.assembler.HealthCheckPersistenceAssembler;
import com.thundax.kuzhambu.operations.infra.health.persistence.dataobject.HealthCheckDO;
import com.thundax.kuzhambu.operations.infra.health.persistence.mapper.HealthCheckMapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class HealthCheckRepositoryImpl implements HealthCheckRepository {

    private final HealthCheckMapper mapper;

    public HealthCheckRepositoryImpl(HealthCheckMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public HealthCheckRecord getById(HealthCheckId id) {
        return HealthCheckPersistenceAssembler.toDomain(mapper.selectOne(
                new LambdaQueryWrapper<HealthCheckDO>().eq(HealthCheckDO::getCheckId, HealthCheckIdCodec.toValue(id))));
    }

    @Override
    public List<HealthCheckRecord> listLatestByComponent() {
        List<Object> components = mapper.selectObjs(
                new QueryWrapper<HealthCheckDO>().select("component").groupBy("component"));
        if (components == null || components.isEmpty()) {
            return List.of();
        }
        List<HealthCheckRecord> records = new ArrayList<>();
        for (Object componentObj : components) {
            if (componentObj == null) {
                continue;
            }
            String component = componentObj.toString();
            HealthCheckDO latest = mapper.selectOne(new LambdaQueryWrapper<HealthCheckDO>()
                    .eq(HealthCheckDO::getComponent, component)
                    .orderByDesc(HealthCheckDO::getCheckedAt)
                    .orderByDesc(HealthCheckDO::getCheckId)
                    .last("LIMIT 1"));
            HealthCheckRecord record = HealthCheckPersistenceAssembler.toDomain(latest);
            if (record != null) {
                records.add(record);
            }
        }
        return records;
    }

    @Override
    public PageResult<HealthCheckRecord> page(String component, String healthStatus, int pageNo, int pageSize) {
        Page<HealthCheckDO> page = new Page<>(pageNo, pageSize);
        IPage<HealthCheckDO> dataObjectPage = mapper.selectPage(page, buildPageWrapper(component, healthStatus));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                HealthCheckPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
    }

    @Override
    public HealthCheckId insert(HealthCheckRecord record) {
        HealthCheckDO dataObject = HealthCheckPersistenceAssembler.toObject(record);
        mapper.insert(dataObject);
        return HealthCheckIdCodec.toDomain(dataObject.getCheckId());
    }

    @Override
    public int update(HealthCheckRecord record) {
        HealthCheckDO dataObject = HealthCheckPersistenceAssembler.toObject(record);
        return mapper.update(
                null,
                new LambdaUpdateWrapper<HealthCheckDO>()
                        .eq(HealthCheckDO::getCheckId, dataObject.getCheckId())
                        .set(HealthCheckDO::getComponent, dataObject.getComponent())
                        .set(HealthCheckDO::getHealthStatus, dataObject.getHealthStatus())
                        .set(HealthCheckDO::getLatencyMs, dataObject.getLatencyMs())
                        .set(HealthCheckDO::getMessage, dataObject.getMessage())
                        .set(HealthCheckDO::getCheckedAt, dataObject.getCheckedAt()));
    }

    @Override
    public int deleteById(HealthCheckId id) {
        return mapper.delete(
                new LambdaQueryWrapper<HealthCheckDO>().eq(HealthCheckDO::getCheckId, HealthCheckIdCodec.toValue(id)));
    }

    private QueryWrapper<HealthCheckDO> buildPageWrapper(String component, String healthStatus) {
        QueryWrapper<HealthCheckDO> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(component)) {
            wrapper.eq("component", component);
        }
        if (StringUtils.isNotBlank(healthStatus)) {
            wrapper.eq("health_status", healthStatus);
        }
        wrapper.orderByDesc("checked_at");
        wrapper.orderByDesc("check_id");
        return wrapper;
    }
}
