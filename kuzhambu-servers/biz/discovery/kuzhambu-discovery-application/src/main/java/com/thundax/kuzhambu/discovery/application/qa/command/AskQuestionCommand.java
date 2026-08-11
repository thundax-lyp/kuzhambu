package com.thundax.kuzhambu.discovery.application.qa.command;

public record AskQuestionCommand(
        Long sessionId,
        String question,
        Integer contextTurnCount,
        String operatorType,
        String operatorId,
        String requestId,
        String traceId) {}
