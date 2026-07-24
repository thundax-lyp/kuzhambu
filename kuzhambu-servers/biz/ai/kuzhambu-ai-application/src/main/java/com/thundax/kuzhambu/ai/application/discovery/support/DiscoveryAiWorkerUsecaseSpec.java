package com.thundax.kuzhambu.ai.application.discovery.support;

public record DiscoveryAiWorkerUsecaseSpec(
        String operation, String workerPath, String capability, String workerCapability, boolean stream) {}
