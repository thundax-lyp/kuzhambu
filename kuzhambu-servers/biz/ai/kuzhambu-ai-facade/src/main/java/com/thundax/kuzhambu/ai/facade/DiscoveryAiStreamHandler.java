package com.thundax.kuzhambu.ai.facade;

@FunctionalInterface
public interface DiscoveryAiStreamHandler {
    void onDelta(String content);
}
