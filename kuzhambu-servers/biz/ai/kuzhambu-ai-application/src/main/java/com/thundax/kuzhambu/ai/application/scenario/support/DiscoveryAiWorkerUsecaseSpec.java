package com.thundax.kuzhambu.ai.application.scenario.support;

public record DiscoveryAiWorkerUsecaseSpec(
        String operation, String workerPath, String capability, String workerCapability, boolean stream) {}
