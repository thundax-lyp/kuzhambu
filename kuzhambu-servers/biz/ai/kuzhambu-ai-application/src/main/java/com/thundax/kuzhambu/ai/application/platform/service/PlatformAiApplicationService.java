package com.thundax.kuzhambu.ai.application.platform.service;

import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.platform.command.PlatformAiInvokeCommand;

public interface PlatformAiApplicationService {

    AiInvokeResult buildPromptSuggestion(PlatformAiInvokeCommand command);

    AiInvokeResult summarizeVersion(PlatformAiInvokeCommand command);
}
