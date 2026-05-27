package com.thundax.kuzhambu.system.application.core.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.system.application.core.entity.Log;
import com.thundax.kuzhambu.system.domain.core.valueobject.LogId;
import java.util.Date;
import java.util.List;

public interface LogDao {

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

    Page<Log> page(
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
