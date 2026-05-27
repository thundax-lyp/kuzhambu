package com.thundax.kuzhambu.system.domain.core.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.domain.core.model.entity.Log;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.LogId;
import java.util.Date;
import java.util.List;

public interface LogRepository {

    Log getById(LogId id);

    List<Log> listByIds(List<String> idList);

    List<Log> list(
            String type,
            String remoteAddr,
            String userLoginName,
            String userName,
            String title,
            String requestUri,
            Date beginDate,
            Date endDate);

    PageResult<Log> page(
            String type,
            String remoteAddr,
            String userLoginName,
            String userName,
            String title,
            String requestUri,
            Date beginDate,
            Date endDate,
            int pageNo,
            int pageSize);

    LogId insert(Log log);

    int update(Log log);

    int deleteById(LogId id);

    List<LogId> batchInsert(List<Log> list);

    int batchDelete(String type, String remoteAddr, String title, String requestUri, Date beginDate, Date endDate);
}
