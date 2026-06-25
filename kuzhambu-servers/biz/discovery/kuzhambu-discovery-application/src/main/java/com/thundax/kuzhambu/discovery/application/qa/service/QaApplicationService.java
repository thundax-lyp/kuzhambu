package com.thundax.kuzhambu.discovery.application.qa.service;

import com.thundax.kuzhambu.discovery.application.qa.command.AskQuestionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.OpenQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.QaAnswerResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSourceResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaTraceResult;
import java.util.List;

public interface QaApplicationService {

    QaSessionResult openSession(OpenQaSessionCommand command);

    QaAnswerResult askQuestion(AskQuestionCommand command);

    QaSessionDetailResult getSessionDetail(Long sessionId);

    List<QaSourceResult> listSourcesByMessageId(Long messageId);

    QaTraceResult getTraceByTraceId(Long traceId);
}
