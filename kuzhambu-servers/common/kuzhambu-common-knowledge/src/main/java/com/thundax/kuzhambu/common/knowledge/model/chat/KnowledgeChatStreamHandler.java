package com.thundax.kuzhambu.common.knowledge.model.chat;

@FunctionalInterface
public interface KnowledgeChatStreamHandler {
    void onDelta(String content);
}
