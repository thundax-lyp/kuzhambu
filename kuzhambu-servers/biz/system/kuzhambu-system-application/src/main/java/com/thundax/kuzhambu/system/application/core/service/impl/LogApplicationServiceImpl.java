package com.thundax.kuzhambu.system.application.core.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.command.CreateLogCommand;
import com.thundax.kuzhambu.system.application.core.query.LogQuery;
import com.thundax.kuzhambu.system.application.core.service.LogApplicationService;
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
public class LogApplicationServiceImpl implements LogApplicationService {

    private final LogRepository dao;

    public LogApplicationServiceImpl(LogRepository dao) {
        this.dao = dao;
    }

    @Override
    public Log get(LogId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<Log> list(LogQuery query) {
        return dao.list(
                query == null ? null : typeValue(query.getType()),
                query == null ? null : query.getRemoteAddr(),
                query == null ? null : query.getUserLoginName(),
                query == null ? null : query.getUserName(),
                query == null ? null : query.getTitle(),
                query == null ? null : query.getRequestUri(),
                query == null ? null : query.getBeginDate(),
                query == null ? null : query.getEndDate());
    }

    @Override
    public PageResult<Log> page(LogQuery query, PageQuery page) {
        return dao.page(
                query == null ? null : typeValue(query.getType()),
                query == null ? null : query.getRemoteAddr(),
                query == null ? null : query.getUserLoginName(),
                query == null ? null : query.getUserName(),
                query == null ? null : query.getTitle(),
                query == null ? null : query.getRequestUri(),
                query == null ? null : query.getBeginDate(),
                query == null ? null : query.getEndDate(),
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
    public int deleteByCondition(LogQuery query) {
        return dao.batchDelete(
                query == null ? null : typeValue(query.getType()),
                query == null ? null : query.getRemoteAddr(),
                query == null ? null : query.getTitle(),
                query == null ? null : query.getRequestUri(),
                query == null ? null : query.getBeginDate(),
                query == null ? null : query.getEndDate());
    }

    private String typeValue(LogType type) {
        return type == null ? null : type.value();
    }

    private Log toLog(CreateLogCommand command) {
        Log log = new Log();
        log.setId(command.getId());
        log.setUserId(command.getUserId());
        log.setType(command.getType());
        log.setLogDate(command.getLogDate());
        log.setTitle(command.getTitle());
        log.setRemoteAddr(command.getRemoteAddr());
        log.setUserAgent(command.getUserAgent());
        log.setMethod(command.getMethod());
        log.setRequestUri(command.getRequestUri());
        log.setRequestParams(command.getRequestParams());
        log.setRemarks(command.getRemarks());
        return log;
    }
}
