package com.thundax.kuzhambu.ai.application.scenario.service;

import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.scenario.command.PlatformAiInvokeCommand;

public interface PlatformAiApplicationService {

    AiInvokeResult buildPromptSuggestion(PlatformAiInvokeCommand command);

    AiInvokeResult summarizeVersion(PlatformAiInvokeCommand command);
}
