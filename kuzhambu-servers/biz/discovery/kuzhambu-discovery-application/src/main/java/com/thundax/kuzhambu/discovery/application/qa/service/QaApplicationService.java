package com.thundax.kuzhambu.discovery.application.qa.service;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.application.qa.command.DeleteQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.ExportQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.OpenQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.query.QaSessionPageQuery;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionExportResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSourceResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaTraceResult;
import java.util.List;

public interface QaApplicationService {

    QaSessionResult openSession(OpenQaSessionCommand command);

    void deleteSession(DeleteQaSessionCommand command);

    QaSessionExportResult exportSession(ExportQaSessionCommand command);

    List<QaSessionResult> listPortalSessions(String ownerType, String ownerId, Integer limit);

    PageResult<QaSessionResult> pageSessions(QaSessionPageQuery query);

    QaSessionDetailResult getPortalSessionDetail(Long sessionId, String ownerType, String ownerId);

    QaSessionDetailResult getSessionDetail(Long sessionId);

    List<QaSourceResult> listSourcesByMessageId(Long messageId);

    QaTraceResult getTraceByTraceId(Long traceId);
}
