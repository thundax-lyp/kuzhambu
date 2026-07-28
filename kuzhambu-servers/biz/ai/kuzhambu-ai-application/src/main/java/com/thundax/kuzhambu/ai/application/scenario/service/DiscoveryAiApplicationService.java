package com.thundax.kuzhambu.ai.application.scenario.service;

import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.scenario.command.DiscoveryAiCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.DiscoveryAiInvokeResult;
import java.util.function.Consumer;

public interface DiscoveryAiApplicationService {

    DiscoveryAiInvokeResult understandQuery(DiscoveryAiCommand command);

    DiscoveryAiInvokeResult rewriteQuery(DiscoveryAiCommand command);

    DiscoveryAiInvokeResult generateAnswer(DiscoveryAiCommand command);

    DiscoveryAiInvokeResult streamAnswer(DiscoveryAiCommand command);

    DiscoveryAiInvokeResult streamAnswer(DiscoveryAiCommand command, Consumer<AiStreamEventResult> eventConsumer);
}
