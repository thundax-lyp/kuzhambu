package com.thundax.kuzhambu.discovery.application.qa.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.application.qa.command.DeleteQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.ExportQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.OpenQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.query.PortalQaSessionDetailQuery;
import com.thundax.kuzhambu.discovery.application.qa.query.PortalQaSessionQuery;
import com.thundax.kuzhambu.discovery.application.qa.query.QaMessageSourcesQuery;
import com.thundax.kuzhambu.discovery.application.qa.query.QaRetrievalTraceQuery;
import com.thundax.kuzhambu.discovery.application.qa.query.QaSessionDetailQuery;
import com.thundax.kuzhambu.discovery.application.qa.query.QaSessionQuery;
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

    List<QaSessionResult> listPortalSessions(PortalQaSessionQuery query, PageQuery pageQuery);

    PageResult<QaSessionResult> pageSessions(QaSessionQuery query, PageQuery pageQuery);

    QaSessionDetailResult getPortalSessionDetail(PortalQaSessionDetailQuery query);

    QaSessionDetailResult getSessionDetail(QaSessionDetailQuery query);

    List<QaSourceResult> listSourcesByMessageId(QaMessageSourcesQuery query);

    QaTraceResult getTraceByTraceId(QaRetrievalTraceQuery query);
}
