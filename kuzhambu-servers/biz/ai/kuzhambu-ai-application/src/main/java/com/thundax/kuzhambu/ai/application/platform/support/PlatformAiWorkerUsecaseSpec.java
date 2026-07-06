package com.thundax.kuzhambu.ai.application.platform.support;

public record PlatformAiWorkerUsecaseSpec(
        String operation, String workerPath, String capability, boolean defaultCreateCandidate) {}
