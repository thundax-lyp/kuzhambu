package com.thundax.kuzhambu.biz.audit.service;

import com.thundax.kuzhambu.biz.audit.entity.AuditLog;
import com.thundax.kuzhambu.biz.audit.entity.AuditMeta;
import com.thundax.kuzhambu.biz.audit.entity.valueobject.AuditLogId;
import com.thundax.kuzhambu.biz.audit.service.command.CreateAuditLogCommand;
import com.thundax.kuzhambu.biz.audit.service.query.AuditLogQuery;
import com.thundax.kuzhambu.biz.audit.service.query.AuditMetaQuery;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface AuditService {

    AuditLogId record(CreateAuditLogCommand command);

    AuditLog getLog(AuditLogId id);

    AuditMeta getMeta(AuditMetaQuery query);

    List<AuditLog> list(AuditMetaQuery query);

    PageResult<AuditLog> page(AuditLogQuery query, PageQuery pageQuery);
}
