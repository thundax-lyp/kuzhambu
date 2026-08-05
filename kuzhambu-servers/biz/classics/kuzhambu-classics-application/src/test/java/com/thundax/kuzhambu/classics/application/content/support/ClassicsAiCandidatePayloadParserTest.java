package com.thundax.kuzhambu.classics.application.content.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsAiCandidatePayloadParserTest {

    private final ClassicsAiCandidatePayloadParser parser = new ClassicsAiCandidatePayloadParser(new ObjectMapper());

    @Test
    void parseTagsShouldAcceptFencedJson() {
        List<String> tags = parser.parseTags("```json\n{\"tags\":[\"经部\",\"史部\"]}\n```");

        assertEquals(List.of("经部", "史部"), tags);
    }

    @Test
    void parseQaPairsShouldAcceptFencedJson() {
        List<AiCandidateQaPairPayload> pairs =
                parser.parseQaPairs("```\n{\"qaPairs\":[{\"question\":\"问\",\"answer\":\"答\"}]}\n```");

        assertEquals(1, pairs.size());
        assertEquals("问", pairs.get(0).getQuestion());
        assertEquals("答", pairs.get(0).getAnswer());
    }

    @Test
    void parseQaPairsShouldAcceptSnakeCaseField() {
        List<AiCandidateQaPairPayload> pairs =
                parser.parseQaPairs("```json\n{\"qa_pairs\":[{\"question\":\"问\",\"answer\":\"答\"}]}\n```");

        assertEquals(1, pairs.size());
        assertEquals("问", pairs.get(0).getQuestion());
        assertEquals("答", pairs.get(0).getAnswer());
    }

    @Test
    void parseTagsShouldRejectExplainedFencedJson() {
        assertThrows(BizException.class, () -> parser.parseTags("结果如下：\n```json\n{\"tags\":[\"经部\"]}\n```"));
    }
}
