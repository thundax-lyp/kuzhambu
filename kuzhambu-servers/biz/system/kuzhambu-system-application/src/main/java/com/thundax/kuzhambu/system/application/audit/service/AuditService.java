package com.thundax.kuzhambu.system.application.audit.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.audit.service.command.CreateAuditLogCommand;
import com.thundax.kuzhambu.system.application.audit.service.query.AuditLogQuery;
import com.thundax.kuzhambu.system.application.audit.service.query.AuditMetaQuery;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditLog;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditMeta;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditLogId;
import java.util.List;

public interface AuditService {

    AuditLogId record(CreateAuditLogCommand command);

    AuditLog getLog(AuditLogId id);

    AuditMeta getMeta(AuditMetaQuery query);

    List<AuditLog> list(AuditMetaQuery query);

    PageResult<AuditLog> page(AuditLogQuery query, PageQuery pageQuery);
}
