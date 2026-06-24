package com.thundax.kuzhambu.discovery.infra.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiscoverySearchDocumentAssemblerTest {

    private final DiscoverySearchDocumentAssembler assembler = new DiscoverySearchDocumentAssembler();

    @Test
    void toDocumentShouldMapSancaiEntryFields() {
        SearchSourceContent sourceContent = new SearchSourceContent(
                "CLASSICS",
                "SANCAI_ENTRY",
                "1001",
                "SANCAI_ENTRY",
                "11",
                "天文",
                "黄帝",
                "摘要",
                List.of("原文", "<p>译文</p>", " "),
                List.of(),
                "PUBLISHED",
                "PUBLIC",
                3,
                new Date(1_718_000_000_000L),
                new Date(1_718_100_000_000L));

        DiscoverySearchDocument document = assembler.toDocument(sourceContent);

        assertEquals("SANCAI_ENTRY:1001", document.getDocumentId());
        assertEquals("天文", document.getCategoryName());
        assertEquals("/classics/sancai/1001", document.getSourcePath());
        assertEquals("原文\n译文", document.getBodyText());
        assertEquals(3, document.getSourceVersionNo());
        assertEquals(Boolean.FALSE, document.getDeleted());
    }

    @Test
    void toDocumentShouldMapWangqiAndMingSourcePaths() {
        DiscoverySearchDocument wangqiDocument = assembler.toDocument(new SearchSourceContent(
                "CLASSICS",
                "WANGQI_DOCUMENT",
                "2001",
                "WANGQI_DOCUMENT",
                null,
                null,
                "天工",
                "摘要",
                List.of("正文"),
                List.of(),
                "PUBLISHED",
                "PUBLIC",
                2,
                null,
                null));
        DiscoverySearchDocument mingDocument = assembler.toDocument(new SearchSourceContent(
                "CLASSICS",
                "MING_CUSTOMS",
                "3001",
                "MING_CUSTOMS",
                "节令",
                "节令",
                "元旦",
                "摘要",
                List.of("正文"),
                List.of(),
                "PUBLISHED",
                "PUBLIC",
                4,
                null,
                null));

        assertEquals("/classics/wangqi/2001", wangqiDocument.getSourcePath());
        assertEquals("/classics/ming-customs/3001", mingDocument.getSourcePath());
        assertTrue(mingDocument.getTagNames().isEmpty());
    }
}
