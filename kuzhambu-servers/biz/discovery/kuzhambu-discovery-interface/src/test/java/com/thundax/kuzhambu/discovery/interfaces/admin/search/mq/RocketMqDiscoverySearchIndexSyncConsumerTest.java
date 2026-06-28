package com.thundax.kuzhambu.discovery.interfaces.admin.search.mq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.thundax.kuzhambu.classics.facade.dto.ClassicsSearchIndexSyncEventFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsSearchIndexSyncMessageFacadeDto;
import com.thundax.kuzhambu.discovery.application.search.service.SearchIndexSyncApplicationService;
import java.util.Date;
import org.junit.jupiter.api.Test;

class RocketMqDiscoverySearchIndexSyncConsumerTest {

    @Test
    void onMessageShouldDelegateUpsertEvent() {
        SearchIndexSyncApplicationService service = mock(SearchIndexSyncApplicationService.class);
        RocketMqDiscoverySearchIndexSyncConsumer consumer = new RocketMqDiscoverySearchIndexSyncConsumer(service);
        ClassicsSearchIndexSyncMessageFacadeDto message = ClassicsSearchIndexSyncMessageFacadeDto.builder()
                .eventId("event-1")
                .eventType(ClassicsSearchIndexSyncEventFacadeDto.UPSERT)
                .contentType("SANCAI_ENTRY")
                .contentId("1001")
                .currentVersionNo(3)
                .occurredAt(new Date())
                .build();

        consumer.onMessage(message);

        verify(service).syncUpsert("SANCAI_ENTRY", "1001", 3);
    }

    @Test
    void onMessageShouldDelegateDeleteEvent() {
        SearchIndexSyncApplicationService service = mock(SearchIndexSyncApplicationService.class);
        RocketMqDiscoverySearchIndexSyncConsumer consumer = new RocketMqDiscoverySearchIndexSyncConsumer(service);
        Date occurredAt = new Date();
        ClassicsSearchIndexSyncMessageFacadeDto message = ClassicsSearchIndexSyncMessageFacadeDto.builder()
                .eventId("event-2")
                .eventType(ClassicsSearchIndexSyncEventFacadeDto.DELETE)
                .contentType("WANGQI_DOCUMENT")
                .contentId("2002")
                .currentVersionNo(5)
                .occurredAt(occurredAt)
                .build();

        consumer.onMessage(message);

        verify(service).syncDelete("WANGQI_DOCUMENT", "2002", 5, occurredAt);
    }
}
