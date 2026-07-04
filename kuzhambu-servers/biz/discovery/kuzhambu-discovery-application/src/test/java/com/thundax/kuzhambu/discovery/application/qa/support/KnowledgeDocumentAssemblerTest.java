package com.thundax.kuzhambu.discovery.application.qa.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.thundax.kuzhambu.classics.facade.dto.ClassicsQaKnowledgeFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsQaKnowledgeFacadeDto.QaPair;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeDocumentAssemblerTest {

    private final KnowledgeDocumentAssembler assembler = new KnowledgeDocumentAssembler();
    private final KnowledgeItemTextRenderer textRenderer = new KnowledgeItemTextRenderer();

    @Test
    void shouldAssembleMetadataAndKnowledgeFromClassicsResponse() {
        Date now = new Date();
        KnowledgeDocument document = assembler.toKnowledgeDocument(createQaKnowledgeResponse(now));

        assertEquals(
                new KnowledgeDocument.Metadata(
                        "SANCAI_ENTRY:1001",
                        "SANCAI_ENTRY",
                        "1001",
                        "kuzhambu-qa",
                        5,
                        "rev-2026",
                        "PUBLIC",
                        "PUBLISHED",
                        "/classics/sancai/1001",
                        now),
                document.metadata());
        assertEquals("三才", document.knowledge().title());
    }

    @Test
    void shouldNotRenderMetadataFieldsIntoKnowledgeText() {
        KnowledgeDocument document = assembler.toKnowledgeDocument(createQaKnowledgeResponse(new Date()));
        String rendered = textRenderer.render(document.knowledge());

        assertFalse(rendered.contains("SANCAI_ENTRY:1001"));
        assertFalse(rendered.contains("kuzhambu-qa"));
        assertFalse(rendered.contains("/classics/sancai/1001"));
        assertFalse(rendered.contains("PUBLISHED"));
        assertFalse(rendered.contains("PUBLIC"));
    }

    @Test
    void shouldRenderConfirmedQaPairsAndSkipEmptyKnowledgeFields() {
        KnowledgeDocument document = assembler.toKnowledgeDocument(ClassicsQaKnowledgeFacadeResponse.builder()
                .knowledge(ClassicsQaKnowledgeFacadeDto.builder()
                        .sourceId("MING_CUSTOMS:2001")
                        .contentType("MING_CUSTOMS")
                        .contentId("2001")
                        .knowledgeBase("kuzhambu-qa")
                        .currentVersionNo(2)
                        .knowledgeRevision("rev")
                        .visibility("PUBLIC")
                        .status("PUBLISHED")
                        .sourcePath("/classics/ming-customs/2001")
                        .updatedAt(new Date(2000L))
                        .title("节令")
                        .categoryPath("年节")
                        .summary("")
                        .body("正文")
                        .originalText(null)
                        .translationText("   ")
                        .originalExcerpts("摘录")
                        .tags(List.of("礼制", "", "历法"))
                        .qaPairs(List.of(new QaPair("为什么", ""), new QaPair("什么是节令", "天文纪时")))
                        .build())
                .build());
        String rendered = textRenderer.render(document.knowledge());

        assertEquals("标题：节令\n位置：年节\n正文：正文\n原文摘录：摘录\n标签：礼制、历法\n\n已确认问答：\nQ: 什么是节令\nA: 天文纪时", rendered);
        assertFalse(rendered.contains("Q: 为什么"));
        assertFalse(rendered.contains("不应该渲染"));
    }

    private ClassicsQaKnowledgeFacadeResponse createQaKnowledgeResponse(Date updatedAt) {
        return ClassicsQaKnowledgeFacadeResponse.builder()
                .knowledge(ClassicsQaKnowledgeFacadeDto.builder()
                        .sourceId("SANCAI_ENTRY:1001")
                        .contentType("SANCAI_ENTRY")
                        .contentId("1001")
                        .knowledgeBase("kuzhambu-qa")
                        .currentVersionNo(5)
                        .knowledgeRevision("rev-2026")
                        .visibility("PUBLIC")
                        .status("PUBLISHED")
                        .sourcePath("/classics/sancai/1001")
                        .updatedAt(updatedAt)
                        .title("三才")
                        .categoryPath("天文")
                        .summary("源信息")
                        .body("内容")
                        .originalText("原文")
                        .translationText("译文")
                        .originalExcerpts("摘录")
                        .tags(List.of("礼制", "天文"))
                        .qaPairs(List.of(new QaPair("谁问", "黄帝")))
                        .build())
                .build();
    }
}
