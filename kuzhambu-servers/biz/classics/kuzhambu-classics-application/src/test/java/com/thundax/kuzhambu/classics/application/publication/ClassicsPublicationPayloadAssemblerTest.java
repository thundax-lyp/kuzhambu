package com.thundax.kuzhambu.classics.application.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationPayload;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationPayloadAssembler;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsPublicationPayloadAssemblerTest {
    private final ClassicsPublicationPayloadAssembler assembler = new ClassicsPublicationPayloadAssembler();

    @Test
    void shouldBuildDeterministicWangqiPayloadAndDeduplicateActiveTags() {
        ClassicsPublicationPayload payload = assembler.assemble(
                job(ClassicsContentType.WANGQI_DOCUMENT, 12L, 91L, 7),
                version(
                        ClassicsContentType.WANGQI_DOCUMENT,
                        12L,
                        91L,
                        7,
                        """
                        {
                          "contentType": "WANGQI_DOCUMENT",
                          "contentId": 12,
                          "contentUpdatedAt": "2026-07-31T08:00:00Z",
                          "title": "  王圻文稿  ",
                          "summary": " 摘要 ",
                          "content": "正文\\n第二行",
                          "tags": [
                            {"tagNameSnapshot": "天文", "status": "ACTIVE"},
                            {"tagNameSnapshot": " 天文 ", "status": "ACTIVE"},
                            {"tagNameSnapshot": "废弃", "status": "INACTIVE"},
                            {"tagNameSnapshot": "舆地", "status": "ACTIVE"}
                          ],
                          "qaPairs": [
                            {"question": " 问题一 ", "answer": " 答案一 "},
                            {"question": " ", "answer": "ignored"}
                          ]
                        }
                        """));

        assertEquals("WANGQI_DOCUMENT:12", payload.searchDocument().getSourceId());
        assertEquals("91", payload.searchDocument().getContentVersionId());
        assertEquals("王圻文稿", payload.searchDocument().getTitle());
        assertEquals(List.of("天文", "舆地"), payload.searchDocument().getTagNames());
        assertEquals(
                List.of("王圻文稿", "摘要", "正文\n第二行", "问题一", "答案一"),
                payload.searchDocument().getTextSegments());
        assertEquals("WANGQI_DOCUMENT:12:王圻文稿", payload.fastGptCollectionName());
        assertEquals(2, payload.fastGptFragments().size());
        assertEquals(
                "摘要：摘要\n正文：正文\n第二行\n标签：天文、舆地", payload.fastGptFragments().get(0).answer());
        assertEquals(0, payload.fastGptFragments().get(0).chunkIndex());
        assertEquals("问题一", payload.fastGptFragments().get(1).question());
        assertEquals(1, payload.fastGptFragments().get(1).chunkIndex());
    }

    @Test
    void shouldRejectBlankTitleBeforeExternalCalls() {
        ClassicsContentVersion version = version(
                ClassicsContentType.MING_CUSTOMS,
                8L,
                30L,
                2,
                """
                {
                  "contentType": "MING_CUSTOMS",
                  "contentId": 8,
                  "title": " ",
                  "content": "content",
                  "tags": [],
                  "qaPairs": []
                }
                """);

        assertThrows(
                IllegalArgumentException.class,
                () -> assembler.assemble(job(ClassicsContentType.MING_CUSTOMS, 8L, 30L, 2), version));
    }

    @Test
    void shouldRejectSnapshotIdentityMismatch() {
        ClassicsContentVersion version = version(
                ClassicsContentType.SANCAI_ENTRY,
                5L,
                40L,
                3,
                """
                {
                  "contentType": "SANCAI_ENTRY",
                  "contentId": 6,
                  "title": "条目",
                  "originalText": "正文",
                  "tags": [],
                  "qaPairs": []
                }
                """);

        assertThrows(
                IllegalArgumentException.class,
                () -> assembler.assemble(job(ClassicsContentType.SANCAI_ENTRY, 5L, 40L, 3), version));
    }

    private static ClassicsPublicationJob job(
            ClassicsContentType contentType, long contentId, long versionId, int versionNo) {
        ClassicsPublicationJob job = new ClassicsPublicationJob();
        job.setContentType(contentType);
        job.setContentId(contentId);
        job.setContentVersionId(versionId);
        job.setContentVersionNo(versionNo);
        return job;
    }

    private static ClassicsContentVersion version(
            ClassicsContentType contentType, long contentId, long versionId, int versionNo, String snapshotJson) {
        ClassicsContentVersion version = new ClassicsContentVersion();
        version.setId(new ClassicsContentVersionId(versionId));
        version.setContentType(contentType);
        version.setContentId(new ClassicsContentId(contentId));
        version.setVersionNo(versionNo);
        version.setSnapshotJson(snapshotJson);
        return version;
    }
}
