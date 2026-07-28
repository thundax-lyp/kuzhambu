package com.thundax.kuzhambu.ai.application.invocation.service;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import java.util.function.Consumer;

public interface AiWorkerInvocationApplicationService {

    AiInvokeResult invoke(AiInvokeCommand command);

    AiInvokeResult stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer);
}
