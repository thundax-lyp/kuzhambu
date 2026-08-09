package com.thundax.kuzhambu.ai.application.invocation.command;

public record AiInvokeOptions(
        boolean stream, boolean forceJson, String locale, boolean allowFallback, boolean createCandidate) {}
