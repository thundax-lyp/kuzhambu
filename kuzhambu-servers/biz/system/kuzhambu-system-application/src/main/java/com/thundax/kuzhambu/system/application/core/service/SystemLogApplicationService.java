package com.thundax.kuzhambu.system.application.core.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.command.CreateLogCommand;
import com.thundax.kuzhambu.system.application.core.command.DeleteLogCommand;
import com.thundax.kuzhambu.system.application.core.query.GetLogQuery;
import com.thundax.kuzhambu.system.application.core.query.LogQuery;
import com.thundax.kuzhambu.system.domain.core.model.entity.Log;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.LogId;
import java.util.List;

public interface SystemLogApplicationService {

    Log get(GetLogQuery query);

    List<Log> list(LogQuery query);

    PageResult<Log> page(LogQuery query, PageQuery page);

    LogId create(CreateLogCommand command);

    int deleteByCondition(DeleteLogCommand command);
}
