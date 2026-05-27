package com.thundax.kuzhambu.system.infra.core.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.system.application.core.dao.LogDao;
import com.thundax.kuzhambu.system.application.core.entity.Log;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.LogId;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.LogIdCodec;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.infra.core.assembler.LogPersistenceAssembler;
import com.thundax.kuzhambu.system.infra.core.dataobject.LogDO;
import com.thundax.kuzhambu.system.infra.core.mapper.LogMapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class LogDaoImpl implements LogDao {

    private final LogMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public LogDaoImpl(LogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Log getById(LogId id) {
        return LogPersistenceAssembler.toEntity(mapper.selectById(id.value()));
    }

    @Override
    public List<Log> listByIds(List<String> idList) {
        List<Long> longIdList = new ArrayList<>();
        for (String id : idList) {
            longIdList.add(Long.valueOf(id));
        }
        return LogPersistenceAssembler.toEntityList(mapper.selectBatchIds(longIdList));
    }

    @Override
    public List<Log> list(
            String type,
            String remoteAddr,
            String userLoginName,
            String userName,
            String title,
            String requestUri,
            Date beginDate,
            Date endDate) {
        return LogPersistenceAssembler.toEntityList(mapper.selectList(
                buildListWrapper(type, remoteAddr, userLoginName, userName, title, requestUri, beginDate, endDate)));
    }

    @Override
    public Page<Log> page(
            String type,
            String remoteAddr,
            String userLoginName,
            String userName,
            String title,
            String requestUri,
            Date beginDate,
            Date endDate,
            int pageNo,
            int pageSize) {
        Page<LogDO> page = new Page<>(pageNo, pageSize);
        IPage<LogDO> dataObjectPage = mapper.selectPage(
                page,
                buildListWrapper(type, remoteAddr, userLoginName, userName, title, requestUri, beginDate, endDate));
        Page<Log> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(LogPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public LogId insert(Log entity) {
        LogDO dataObject = LogPersistenceAssembler.toDataObject(entity);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return LogIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(Log entity) {
        return mapper.updateById(LogPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int deleteById(LogId id) {
        return mapper.deleteById(id.value());
    }

    @Override
    public List<LogId> batchInsert(List<Log> list) {
        List<LogId> idList = new ArrayList<>();
        for (LogDO dataObject : LogPersistenceAssembler.toDataObjectList(list)) {
            dataObject.setId(idGenerator.nextId().value());
            mapper.insert(dataObject);
            idList.add(LogIdCodec.toDomain(dataObject.getId()));
        }
        return idList;
    }

    @Override
    public int batchDelete(
            String type, String remoteAddr, String title, String requestUri, Date beginDate, Date endDate) {
        return mapper.delete(buildBatchDeleteWrapper(type, remoteAddr, title, requestUri, beginDate, endDate));
    }

    private QueryWrapper<LogDO> buildListWrapper(
            String type,
            String remoteAddr,
            String userLoginName,
            String userName,
            String title,
            String requestUri,
            Date beginDate,
            Date endDate) {
        QueryWrapper<LogDO> wrapper = buildBatchDeleteWrapper(type, remoteAddr, title, requestUri, beginDate, endDate);
        if (StringUtils.isNotBlank(userLoginName)) {
            wrapper.apply(
                    "user_id IN (SELECT principal_id FROM system_auth_principal_identity "
                            + "WHERE principal_type = 'USER' AND identity_type = 'USER_ACCOUNT' "
                            + "AND identity_value LIKE CONCAT('%', {0}, '%'))",
                    userLoginName);
        }
        if (StringUtils.isNotBlank(userName)) {
            wrapper.apply("user_id IN (SELECT id FROM system_user WHERE name LIKE CONCAT('%', {0}, '%'))", userName);
        }
        wrapper.orderByDesc("log_date");
        return wrapper;
    }

    private QueryWrapper<LogDO> buildBatchDeleteWrapper(
            String type, String remoteAddr, String title, String requestUri, Date beginDate, Date endDate) {
        QueryWrapper<LogDO> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(type)) {
            wrapper.eq("type", type);
        }
        if (StringUtils.isNotBlank(remoteAddr)) {
            wrapper.eq("remote_addr", remoteAddr);
        }
        if (StringUtils.isNotBlank(title)) {
            wrapper.like("title", title);
        }
        if (StringUtils.isNotBlank(requestUri)) {
            wrapper.like("request_uri", requestUri);
        }
        if (beginDate != null && endDate != null) {
            wrapper.between("log_date", beginDate, endDate);
        }
        return wrapper;
    }
}
