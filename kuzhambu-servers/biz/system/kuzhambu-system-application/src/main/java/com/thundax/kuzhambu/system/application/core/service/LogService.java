package com.thundax.kuzhambu.system.application.core.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.service.command.CreateLogCommand;
import com.thundax.kuzhambu.system.application.core.service.query.LogQuery;
import com.thundax.kuzhambu.system.domain.model.entity.Log;
import com.thundax.kuzhambu.system.domain.model.valueobject.LogId;
import java.util.List;

public interface LogService {

    Log get(LogId id);

    List<Log> list(LogQuery query);

    PageResult<Log> page(LogQuery query, PageQuery page);

    LogId create(CreateLogCommand command);

    int deleteByCondition(LogQuery query);
}
