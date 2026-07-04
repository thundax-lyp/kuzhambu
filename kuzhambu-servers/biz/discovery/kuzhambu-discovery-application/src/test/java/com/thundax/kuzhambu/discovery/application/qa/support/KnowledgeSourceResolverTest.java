package com.thundax.kuzhambu.discovery.application.qa.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsQaKnowledgeFacadeDto;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class KnowledgeSourceResolverTest {

    private final ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
    private final KnowledgeSourceResolver sourceResolver = new KnowledgeSourceResolver(classicsFacade);

    @Test
    void shouldMarkSourceAvailableWhenKnowledgeIsPublicPublished() {
        QaSource source = sampleSource();
        when(classicsFacade.getQaKnowledge(argThat(request ->
                        "SANCAI_ENTRY".equals(request.getContentType()) && "1001".equals(request.getContentId()))))
                .thenReturn(ClassicsQaKnowledgeFacadeResponse.builder()
                        .knowledge(ClassicsQaKnowledgeFacadeDto.builder()
                                .visibility("PUBLIC")
                                .status("PUBLISHED")
                                .build())
                        .build());

        QaSource resolved = sourceResolver.resolve(source);

        assertSame(source, resolved);
        assertEquals("AVAILABLE", resolved.getSourceStatus());
        verify(classicsFacade)
                .getQaKnowledge(argThat(request ->
                        "SANCAI_ENTRY".equals(request.getContentType()) && "1001".equals(request.getContentId())));
    }

    @Test
    void shouldMarkSourceUnavailableWhenCurrentKnowledgeIsPrivate() {
        QaSource source = sampleSource();
        when(classicsFacade.getQaKnowledge(argThat(request ->
                        "SANCAI_ENTRY".equals(request.getContentType()) && "1001".equals(request.getContentId()))))
                .thenReturn(ClassicsQaKnowledgeFacadeResponse.builder()
                        .knowledge(ClassicsQaKnowledgeFacadeDto.builder()
                                .visibility("PRIVATE")
                                .status("PUBLISHED")
                                .build())
                        .build());

        QaSource resolved = sourceResolver.resolve(source);

        assertEquals("UNAVAILABLE", resolved.getSourceStatus());
    }

    @Test
    void shouldMarkSourceUnavailableWhenKnowledgeFacadeReturnsNull() {
        QaSource source = sampleSource();
        when(classicsFacade.getQaKnowledge(argThat(request ->
                        "SANCAI_ENTRY".equals(request.getContentType()) && "1001".equals(request.getContentId()))))
                .thenReturn(null);

        QaSource resolved = sourceResolver.resolve(source);

        assertEquals("UNAVAILABLE", resolved.getSourceStatus());
    }

    private QaSource sampleSource() {
        return new QaSource(
                1L,
                1001L,
                10L,
                "SANCAI_ENTRY",
                1001L,
                "SANCAI",
                "黄帝",
                "卷一",
                "正文",
                "source-path",
                1,
                BigDecimal.ONE,
                "CITED",
                null);
    }
}
