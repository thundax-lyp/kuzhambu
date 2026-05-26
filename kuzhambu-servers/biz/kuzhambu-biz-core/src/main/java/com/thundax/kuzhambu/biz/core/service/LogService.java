package com.thundax.kuzhambu.biz.core.service;

import com.thundax.kuzhambu.biz.core.entity.Log;
import com.thundax.kuzhambu.biz.core.entity.valueobject.LogId;
import com.thundax.kuzhambu.biz.core.service.command.CreateLogCommand;
import com.thundax.kuzhambu.biz.core.service.query.LogQuery;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface LogService {

    Log get(LogId id);

    List<Log> list(LogQuery query);

    PageResult<Log> page(LogQuery query, PageQuery page);

    LogId create(CreateLogCommand command);

    int deleteByCondition(LogQuery query);
}
