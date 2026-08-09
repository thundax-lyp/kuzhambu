package com.thundax.kuzhambu.ai.application.invocation.command;

public record AiInvokeCommand(
        AiInvokeContext context,
        AiInvokeWorkerRoute route,
        AiInvokeTarget target,
        AiInvokeModelConfig modelConfig,
        AiInvokeTrace trace,
        AiInvokePrompt prompt,
        AiInvokePayload payload,
        AiInvokeOptions options) {}
