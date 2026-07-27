package com.thundax.kuzhambu.ai.application.discovery.service;

import com.thundax.kuzhambu.ai.application.discovery.command.DiscoveryAiCommand;
import com.thundax.kuzhambu.ai.application.discovery.result.DiscoveryAiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import java.util.function.Consumer;

public interface DiscoveryAiApplicationService {

    DiscoveryAiInvokeResult understandQuery(DiscoveryAiCommand command);

    DiscoveryAiInvokeResult rewriteQuery(DiscoveryAiCommand command);

    DiscoveryAiInvokeResult generateAnswer(DiscoveryAiCommand command);

    DiscoveryAiInvokeResult streamAnswer(DiscoveryAiCommand command);

    DiscoveryAiInvokeResult streamAnswer(DiscoveryAiCommand command, Consumer<AiStreamEventResult> eventConsumer);
}
