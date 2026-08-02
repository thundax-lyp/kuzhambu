package com.thundax.kuzhambu.discovery.infra.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationDocument;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

class ElasticsearchSearchPublicationGatewayTest {

    private final DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
    private final ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
    private final ElasticsearchSearchIndexGateway gateway =
            new ElasticsearchSearchIndexGateway(properties, operations, new DiscoverySearchDocumentAssembler());

    @Test
    void prepareShouldFullyOverwritePublicationDocument() {
        SearchPublicationDocument source = new SearchPublicationDocument(
                "SANCAI_ENTRY:101",
                "SANCAI_ENTRY",
                "101",
                "9001",
                7,
                "天文",
                "摘要",
                "11",
                "天文",
                "21",
                "第一卷",
                List.of("标题", "正文"),
                List.of("星象"),
                Instant.ofEpochMilli(1234));
        ArgumentCaptor<DiscoverySearchDocument> captor = ArgumentCaptor.forClass(DiscoverySearchDocument.class);

        gateway.preparePublication(source);

        verify(operations).save(captor.capture(), any(IndexCoordinates.class));
        DiscoverySearchDocument saved = captor.getValue();
        assertEquals("SANCAI_ENTRY:101", saved.getDocumentId());
        assertEquals("9001", saved.getContentVersionId());
        assertEquals(7, saved.getContentVersionNo());
        assertEquals("PREPARING", saved.getPublicationStatus());
        assertEquals(Boolean.FALSE, saved.getDeleted());
        assertEquals(List.of("标题", "正文"), saved.getTextSegments());
        assertEquals("标题\n正文", saved.getBodyText());
    }

    @Test
    void readyShouldRequireMatchingVersionAndRemainIdempotent() {
        DiscoverySearchDocument existing = publicationDocument("9001", 7, "PREPARING");
        when(operations.get(eq("SANCAI_ENTRY:101"), eq(DiscoverySearchDocument.class), any(IndexCoordinates.class)))
                .thenReturn(existing);

        assertTrue(gateway.markPublicationReady("SANCAI_ENTRY:101", "9001", 7));
        assertEquals("READY", existing.getPublicationStatus());
        assertTrue(gateway.markPublicationReady("SANCAI_ENTRY:101", "9001", 7));
        verify(operations, org.mockito.Mockito.times(2)).save(eq(existing), any(IndexCoordinates.class));
    }

    @Test
    void readyShouldRejectMissingOrMismatchedVersion() {
        when(operations.get(eq("SANCAI_ENTRY:101"), eq(DiscoverySearchDocument.class), any(IndexCoordinates.class)))
                .thenReturn(null, publicationDocument("other", 7, "PREPARING"));

        assertFalse(gateway.markPublicationReady("SANCAI_ENTRY:101", "9001", 7));
        assertFalse(gateway.markPublicationReady("SANCAI_ENTRY:101", "9001", 7));
        verify(operations, never()).save(any(DiscoverySearchDocument.class), any(IndexCoordinates.class));
    }

    @Test
    void offlineAndDeleteShouldTreatMissingDocumentAsSuccess() {
        when(operations.get(eq("SANCAI_ENTRY:101"), eq(DiscoverySearchDocument.class), any(IndexCoordinates.class)))
                .thenReturn(null);

        assertTrue(gateway.markPublicationOffline("SANCAI_ENTRY:101", Instant.ofEpochMilli(1234)));
        gateway.deletePublication("SANCAI_ENTRY:101");

        verify(operations).delete(eq("SANCAI_ENTRY:101"), any(IndexCoordinates.class));
        verify(operations, never()).save(any(DiscoverySearchDocument.class), any(IndexCoordinates.class));
    }

    @Test
    void probeShouldExposePublicationVersionAndMissingState() {
        DiscoverySearchDocument existing = publicationDocument("9001", 7, "READY");
        existing.setDeleted(Boolean.FALSE);
        when(operations.get(eq("SANCAI_ENTRY:101"), eq(DiscoverySearchDocument.class), any(IndexCoordinates.class)))
                .thenReturn(existing, (DiscoverySearchDocument) null);

        var ready = gateway.probePublication("SANCAI_ENTRY:101");
        var missing = gateway.probePublication("SANCAI_ENTRY:101");

        assertTrue(ready.isPresent());
        assertEquals("READY", ready.getPublicationStatus());
        assertEquals("9001", ready.getContentVersionId());
        assertFalse(missing.isPresent());
    }

    private DiscoverySearchDocument publicationDocument(
            String contentVersionId, Integer contentVersionNo, String publicationStatus) {
        DiscoverySearchDocument document = new DiscoverySearchDocument();
        document.setDocumentId("SANCAI_ENTRY:101");
        document.setContentVersionId(contentVersionId);
        document.setContentVersionNo(contentVersionNo);
        document.setPublicationStatus(publicationStatus);
        return document;
    }
}
