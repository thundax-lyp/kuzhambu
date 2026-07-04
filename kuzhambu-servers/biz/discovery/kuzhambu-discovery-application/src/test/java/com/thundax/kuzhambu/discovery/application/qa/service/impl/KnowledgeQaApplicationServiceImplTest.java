package com.thundax.kuzhambu.discovery.application.qa.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.knowledge.client.KnowledgeBaseClient;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatRequest;
import com.thundax.kuzhambu.discovery.application.qa.command.ChatCompletionCommand;
import com.thundax.kuzhambu.discovery.application.qa.support.QaSourceAssembler;
import com.thundax.kuzhambu.discovery.application.qa.support.QaTraceAssembler;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaMessageRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaRetrievalTraceRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSourceRepository;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeQaApplicationServiceImplTest {

    @Test
    void chatCompletionShouldRejectRemovedSession() {
        KnowledgeBaseClient knowledgeBaseClient = mock(KnowledgeBaseClient.class);
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        KnowledgeQaApplicationServiceImpl service = new KnowledgeQaApplicationServiceImpl(
                knowledgeBaseClient,
                sessionRepository,
                mock(QaMessageRepository.class),
                mock(QaSourceRepository.class),
                mock(QaRetrievalTraceRepository.class),
                new QaSourceAssembler(),
                new QaTraceAssembler());
        QaSession session = openSession();
        session.markRemoved(new Date());
        when(sessionRepository.getBySessionId(5001L)).thenReturn(session);

        BizException exception = assertThrows(BizException.class, () -> service.chatCompletion(command()));

        assertEquals("QA_SESSION_ALREADY_REMOVED", exception.getCode());
        verify(knowledgeBaseClient, never()).chat(org.mockito.ArgumentMatchers.any(KnowledgeChatRequest.class));
    }

    private static ChatCompletionCommand command() {
        return new ChatCompletionCommand(
                5001L,
                null,
                List.of(new ChatCompletionCommand.ChatMessage("user", "什么是三才？")),
                false,
                null,
                null,
                null,
                null);
    }

    private static QaSession openSession() {
        return new QaSession(
                1L,
                5001L,
                "USER",
                "1001",
                "kuzhambu-qa",
                "黄帝问答",
                "GLOBAL",
                "GENERAL",
                "SANCAI_ENTRY",
                10001L,
                "OPEN",
                new Date(),
                new Date(),
                null);
    }
}
