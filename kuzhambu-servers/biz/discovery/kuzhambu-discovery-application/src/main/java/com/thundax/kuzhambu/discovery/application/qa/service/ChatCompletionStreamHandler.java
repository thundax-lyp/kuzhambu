package com.thundax.kuzhambu.discovery.application.qa.service;

@FunctionalInterface
public interface ChatCompletionStreamHandler {
    void onDelta(String content);
}
