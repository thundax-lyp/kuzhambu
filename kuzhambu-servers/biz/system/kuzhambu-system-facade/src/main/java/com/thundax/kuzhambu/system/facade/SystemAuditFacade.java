package com.thundax.kuzhambu.system.facade;

import com.thundax.kuzhambu.system.facade.request.SystemAuditFacadeRequest;
import com.thundax.kuzhambu.system.facade.response.SystemAuditFacadeResponse;

public interface SystemAuditFacade {

    Long record(SystemAuditFacadeRequest request);

    SystemAuditFacadeResponse get(Long auditLogId);
}
