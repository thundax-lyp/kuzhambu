package com.thundax.kuzhambu.discovery.interfaces.admin.search.mq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.thundax.kuzhambu.classics.application.searchsync.model.ClassicsSearchIndexSyncEventType;
import com.thundax.kuzhambu.classics.application.searchsync.model.ClassicsSearchIndexSyncMessage;
import com.thundax.kuzhambu.discovery.application.search.service.SearchIndexSyncApplicationService;
import java.util.Date;
import org.junit.jupiter.api.Test;

class RocketMqDiscoverySearchIndexSyncConsumerTest {

    @Test
    void onMessageShouldDelegateUpsertEvent() {
        SearchIndexSyncApplicationService service = mock(SearchIndexSyncApplicationService.class);
        RocketMqDiscoverySearchIndexSyncConsumer consumer = new RocketMqDiscoverySearchIndexSyncConsumer(service);
        ClassicsSearchIndexSyncMessage message = new ClassicsSearchIndexSyncMessage(
                "event-1", ClassicsSearchIndexSyncEventType.UPSERT, "SANCAI_ENTRY", "1001", 3, new Date());

        consumer.onMessage(message);

        verify(service).syncUpsert("SANCAI_ENTRY", "1001", 3);
    }

    @Test
    void onMessageShouldDelegateDeleteEvent() {
        SearchIndexSyncApplicationService service = mock(SearchIndexSyncApplicationService.class);
        RocketMqDiscoverySearchIndexSyncConsumer consumer = new RocketMqDiscoverySearchIndexSyncConsumer(service);
        Date occurredAt = new Date();
        ClassicsSearchIndexSyncMessage message = new ClassicsSearchIndexSyncMessage(
                "event-2", ClassicsSearchIndexSyncEventType.DELETE, "WANGQI_DOCUMENT", "2002", 5, occurredAt);

        consumer.onMessage(message);

        verify(service).syncDelete("WANGQI_DOCUMENT", "2002", 5, occurredAt);
    }
}
