package com.thundax.kuzhambu.discovery.infra.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ElasticsearchSearchIndexGatewayTest {

    @Test
    void searchShouldThrowUnsupportedOperationExceptionWithIndexName() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        properties.setIndexName("discovery-search-test");
        ElasticsearchSearchIndexGateway gateway = new ElasticsearchSearchIndexGateway(properties);

        UnsupportedOperationException exception =
                assertThrows(UnsupportedOperationException.class, () -> gateway.search(null, null, 1, 20));

        assertTrue(exception.getMessage().contains("discovery-search-test"));
    }

    @Test
    void rebuildAndUpsertShouldThrowUnsupportedOperationExceptionWithIndexName() {
        DiscoverySearchIndexProperties properties = new DiscoverySearchIndexProperties();
        properties.setIndexName("discovery-search-test");
        ElasticsearchSearchIndexGateway gateway = new ElasticsearchSearchIndexGateway(properties);

        UnsupportedOperationException rebuildException =
                assertThrows(UnsupportedOperationException.class, () -> gateway.rebuildIndex(Collections.emptyList()));
        UnsupportedOperationException upsertException = assertThrows(
                UnsupportedOperationException.class,
                () -> gateway.upsertDocuments(Collections.<SearchSourceContent>emptyList()));

        assertTrue(rebuildException.getMessage().contains("discovery-search-test"));
        assertTrue(upsertException.getMessage().contains("discovery-search-test"));
    }
}
