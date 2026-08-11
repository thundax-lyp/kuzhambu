package com.thundax.kuzhambu.operations.infra.health.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.health.codec.HealthAlertIdCodec;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthAlertRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthAlertId;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthAlertRepository;
import com.thundax.kuzhambu.operations.infra.health.persistence.assembler.HealthAlertPersistenceAssembler;
import com.thundax.kuzhambu.operations.infra.health.persistence.dataobject.HealthAlertDO;
import com.thundax.kuzhambu.operations.infra.health.persistence.mapper.HealthAlertMapper;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class HealthAlertRepositoryImpl implements HealthAlertRepository {

    private static final List<String> OPEN_ALERT_STATUSES = List.of("ACTIVE", "ACKED");

    private final HealthAlertMapper mapper;

    public HealthAlertRepositoryImpl(HealthAlertMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public HealthAlertRecord getById(HealthAlertId id) {
        return HealthAlertPersistenceAssembler.toDomain(mapper.selectOne(
                new LambdaQueryWrapper<HealthAlertDO>().eq(HealthAlertDO::getAlertId, HealthAlertIdCodec.toValue(id))));
    }

    @Override
    public HealthAlertRecord findOpenBySource(String sourceRefType, Long sourceRefId, String alertType) {
        LambdaQueryWrapper<HealthAlertDO> wrapper = new LambdaQueryWrapper<HealthAlertDO>()
                .eq(HealthAlertDO::getSourceRefType, sourceRefType)
                .eq(HealthAlertDO::getAlertType, alertType)
                .in(HealthAlertDO::getAlertStatus, OPEN_ALERT_STATUSES)
                .orderByDesc(HealthAlertDO::getLastTriggeredAt)
                .orderByDesc(HealthAlertDO::getAlertId)
                .last("LIMIT 1");
        if (sourceRefId == null) {
            wrapper.isNull(HealthAlertDO::getSourceRefId);
        } else {
            wrapper.eq(HealthAlertDO::getSourceRefId, sourceRefId);
        }
        return HealthAlertPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public PageResult<HealthAlertRecord> page(
            String component,
            String alertLevel,
            String alertStatus,
            String sourceRefType,
            Long sourceRefId,
            Long latestCheckId,
            int pageNo,
            int pageSize) {
        Page<HealthAlertDO> page = new Page<>(pageNo, pageSize);
        IPage<HealthAlertDO> dataObjectPage = mapper.selectPage(
                page, buildPageWrapper(component, alertLevel, alertStatus, sourceRefType, sourceRefId, latestCheckId));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                HealthAlertPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
    }

    @Override
    public List<HealthAlertRecord> listOpenByComponent(String component) {
        return HealthAlertPersistenceAssembler.toDomainList(mapper.selectList(new LambdaQueryWrapper<HealthAlertDO>()
                .eq(HealthAlertDO::getComponent, component)
                .in(HealthAlertDO::getAlertStatus, OPEN_ALERT_STATUSES)
                .orderByDesc(HealthAlertDO::getLastTriggeredAt)
                .orderByDesc(HealthAlertDO::getAlertId)));
    }

    @Override
    public List<HealthAlertRecord> listOpenSummary() {
        return HealthAlertPersistenceAssembler.toDomainList(mapper.selectList(new LambdaQueryWrapper<HealthAlertDO>()
                .in(HealthAlertDO::getAlertStatus, OPEN_ALERT_STATUSES)
                .orderByDesc(HealthAlertDO::getLastTriggeredAt)
                .orderByDesc(HealthAlertDO::getAlertId)));
    }

    @Override
    public HealthAlertId insert(HealthAlertRecord record) {
        HealthAlertDO dataObject = HealthAlertPersistenceAssembler.toObject(record);
        mapper.insert(dataObject);
        return HealthAlertIdCodec.toDomain(dataObject.getAlertId());
    }

    @Override
    public int update(HealthAlertRecord record) {
        HealthAlertDO dataObject = HealthAlertPersistenceAssembler.toObject(record);
        return mapper.update(
                null,
                new LambdaUpdateWrapper<HealthAlertDO>()
                        .eq(HealthAlertDO::getAlertId, dataObject.getAlertId())
                        .set(HealthAlertDO::getComponent, dataObject.getComponent())
                        .set(HealthAlertDO::getAlertType, dataObject.getAlertType())
                        .set(HealthAlertDO::getAlertLevel, dataObject.getAlertLevel())
                        .set(HealthAlertDO::getAlertStatus, dataObject.getAlertStatus())
                        .set(HealthAlertDO::getSourceRefType, dataObject.getSourceRefType())
                        .set(HealthAlertDO::getSourceRefId, dataObject.getSourceRefId())
                        .set(HealthAlertDO::getLatestCheckId, dataObject.getLatestCheckId())
                        .set(HealthAlertDO::getMessage, dataObject.getMessage())
                        .set(HealthAlertDO::getSuggestion, dataObject.getSuggestion())
                        .set(HealthAlertDO::getRecoveryAction, dataObject.getRecoveryAction())
                        .set(HealthAlertDO::getRecoveryTarget, dataObject.getRecoveryTarget())
                        .set(HealthAlertDO::getFirstTriggeredAt, dataObject.getFirstTriggeredAt())
                        .set(HealthAlertDO::getLastTriggeredAt, dataObject.getLastTriggeredAt())
                        .set(HealthAlertDO::getAckedAt, dataObject.getAckedAt())
                        .set(HealthAlertDO::getAckedByUserId, dataObject.getAckedByUserId())
                        .set(HealthAlertDO::getRecoveredAt, dataObject.getRecoveredAt())
                        .set(HealthAlertDO::getFailureReason, dataObject.getFailureReason()));
    }

    private QueryWrapper<HealthAlertDO> buildPageWrapper(
            String component,
            String alertLevel,
            String alertStatus,
            String sourceRefType,
            Long sourceRefId,
            Long latestCheckId) {
        QueryWrapper<HealthAlertDO> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(component)) {
            wrapper.eq("component", component);
        }
        if (StringUtils.isNotBlank(alertLevel)) {
            wrapper.eq("alert_level", alertLevel);
        }
        if (StringUtils.isNotBlank(alertStatus)) {
            wrapper.eq("alert_status", alertStatus);
        }
        if (StringUtils.isNotBlank(sourceRefType)) {
            wrapper.eq("source_ref_type", sourceRefType);
        }
        if (sourceRefId != null) {
            wrapper.eq("source_ref_id", sourceRefId);
        }
        if (latestCheckId != null) {
            wrapper.eq("latest_check_id", latestCheckId);
        }
        wrapper.orderByAsc("alert_status");
        wrapper.orderByDesc("alert_level");
        wrapper.orderByDesc("last_triggered_at");
        wrapper.orderByDesc("alert_id");
        return wrapper;
    }
}
