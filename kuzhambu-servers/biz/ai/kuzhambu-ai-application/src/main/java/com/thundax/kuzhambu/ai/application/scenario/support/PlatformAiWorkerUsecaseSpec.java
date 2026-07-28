package com.thundax.kuzhambu.ai.application.scenario.support;

public record PlatformAiWorkerUsecaseSpec(
        String operation,
        String workerPath,
        String capability,
        String workerCapability,
        boolean defaultCreateCandidate) {}
