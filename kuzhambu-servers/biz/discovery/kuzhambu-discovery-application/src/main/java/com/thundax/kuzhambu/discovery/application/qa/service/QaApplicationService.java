package com.thundax.kuzhambu.discovery.application.qa.service;

import com.thundax.kuzhambu.discovery.application.qa.command.AskQuestionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.OpenQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.QaAnswerResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;

public interface QaApplicationService {

    QaSessionResult openSession(OpenQaSessionCommand command);

    QaAnswerResult askQuestion(AskQuestionCommand command);
}
