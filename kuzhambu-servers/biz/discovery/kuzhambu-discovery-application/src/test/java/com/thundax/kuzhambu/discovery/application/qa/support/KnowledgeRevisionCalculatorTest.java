package com.thundax.kuzhambu.discovery.application.qa.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.thundax.kuzhambu.discovery.application.qa.support.KnowledgeDocument.QaPair;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeRevisionCalculatorTest {

    private final KnowledgeRevisionCalculator calculator = new KnowledgeRevisionCalculator();

    @Test
    void shouldGenerateStableRevisionForSameKnowledgeContent() {
        KnowledgeDocument.Knowledge knowledge = new KnowledgeDocument.Knowledge(
                "三才",
                "天文 / 卷一",
                "源信息",
                "内容",
                "原文",
                "译文",
                "摘录",
                List.of("天文", "礼制"),
                List.of(new QaPair("谁是黄帝", "上古帝王")));
        KnowledgeDocument document = new KnowledgeDocument(
                new KnowledgeDocument.Metadata(
                        "SANCAI_ENTRY:1001",
                        "SANCAI_ENTRY",
                        "1001",
                        "SANCAI",
                        1,
                        "rev",
                        "PUBLIC",
                        "PUBLISHED",
                        "/classics/sancai/1001",
                        null),
                knowledge);

        String revision1 = calculator.calculate(document);
        String revision2 = calculator.calculate(document);

        assertNotNull(revision1);
        assertNotNull(revision2);
        assertEquals(revision1, revision2);
    }

    @Test
    void shouldRegenerateRevisionWhenTagsChange() {
        KnowledgeDocument.Knowledge origin = new KnowledgeDocument.Knowledge(
                "三才", "天文 / 卷一", "源信息", "内容", "原文", "译文", "摘录", List.of("天文", "礼制"), List.of());
        KnowledgeDocument.Knowledge changed = new KnowledgeDocument.Knowledge(
                "三才", "天文 / 卷一", "源信息", "内容", "原文", "译文", "摘录", List.of("天文", "礼制", "历法"), List.of());

        String revision1 = calculator.calculate(origin);
        String revision2 = calculator.calculate(changed);

        assertNotEquals(revision1, revision2);
    }

    @Test
    void shouldRegenerateRevisionWhenConfirmedQaPairsChange() {
        KnowledgeDocument.Knowledge origin = new KnowledgeDocument.Knowledge(
                "三才", "天文 / 卷一", "源信息", "内容", "原文", "译文", "摘录", List.of("天文"), List.of(new QaPair("黄帝是谁", "帝王")));
        KnowledgeDocument.Knowledge changed = new KnowledgeDocument.Knowledge(
                "三才",
                "天文 / 卷一",
                "源信息",
                "内容",
                "原文",
                "译文",
                "摘录",
                List.of("天文"),
                List.of(new QaPair("黄帝是谁", "帝王"), new QaPair("黄帝生平", "帝王始祖")));

        String revision1 = calculator.calculate(origin);
        String revision2 = calculator.calculate(changed);

        assertNotEquals(revision1, revision2);
    }
}
