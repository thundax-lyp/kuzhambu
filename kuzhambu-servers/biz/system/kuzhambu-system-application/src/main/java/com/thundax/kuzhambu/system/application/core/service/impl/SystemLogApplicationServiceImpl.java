package com.thundax.kuzhambu.system.application.core.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.command.CreateLogCommand;
import com.thundax.kuzhambu.system.application.core.command.DeleteLogCommand;
import com.thundax.kuzhambu.system.application.core.query.GetLogQuery;
import com.thundax.kuzhambu.system.application.core.query.LogQuery;
import com.thundax.kuzhambu.system.application.core.service.SystemLogApplicationService;
import com.thundax.kuzhambu.system.domain.core.model.entity.Log;
import com.thundax.kuzhambu.system.domain.core.model.enums.LogType;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.LogId;
import com.thundax.kuzhambu.system.domain.core.repository.LogRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class SystemLogApplicationServiceImpl implements SystemLogApplicationService {

    private final LogRepository dao;

    public SystemLogApplicationServiceImpl(LogRepository dao) {
        this.dao = dao;
    }

    @Override
    public Log get(GetLogQuery query) {
        LogId id = query == null ? null : query.id();
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<Log> list(LogQuery query) {
        return dao.list(
                query == null ? null : typeValue(query.type()),
                query == null ? null : query.remoteAddr(),
                query == null ? null : query.userLoginName(),
                query == null ? null : query.userName(),
                query == null ? null : query.title(),
                query == null ? null : query.requestUri(),
                query == null ? null : query.beginDate(),
                query == null ? null : query.endDate());
    }

    @Override
    public PageResult<Log> page(LogQuery query, PageQuery page) {
        return dao.page(
                query == null ? null : typeValue(query.type()),
                query == null ? null : query.remoteAddr(),
                query == null ? null : query.userLoginName(),
                query == null ? null : query.userName(),
                query == null ? null : query.title(),
                query == null ? null : query.requestUri(),
                query == null ? null : query.beginDate(),
                query == null ? null : query.endDate(),
                page.getPageNo(),
                page.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LogId create(CreateLogCommand command) {
        Log log = toLog(command);
        log.setId(dao.insert(log));
        return log.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByCondition(DeleteLogCommand command) {
        LogQuery query = command == null ? null : command.query();
        return dao.batchDelete(
                query == null ? null : typeValue(query.type()),
                query == null ? null : query.remoteAddr(),
                query == null ? null : query.title(),
                query == null ? null : query.requestUri(),
                query == null ? null : query.beginDate(),
                query == null ? null : query.endDate());
    }

    private String typeValue(LogType type) {
        return type == null ? null : type.value();
    }

    private Log toLog(CreateLogCommand command) {
        Log log = new Log();
        log.setId(command.id());
        log.setUserId(command.userId());
        log.setType(command.type());
        log.setLogDate(command.logDate());
        log.setTitle(command.title());
        log.setRemoteAddr(command.remoteAddr());
        log.setUserAgent(command.userAgent());
        log.setMethod(command.method());
        log.setRequestUri(command.requestUri());
        log.setRequestParams(command.requestParams());
        log.setRemarks(command.remarks());
        return log;
    }
}
