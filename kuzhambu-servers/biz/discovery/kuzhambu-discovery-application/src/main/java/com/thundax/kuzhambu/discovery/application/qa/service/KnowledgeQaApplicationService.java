package com.thundax.kuzhambu.discovery.application.qa.service;

import com.thundax.kuzhambu.discovery.application.qa.command.ChatCompletionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult;

public interface KnowledgeQaApplicationService {
    ChatCompletionResult chatCompletion(ChatCompletionCommand command);
}
