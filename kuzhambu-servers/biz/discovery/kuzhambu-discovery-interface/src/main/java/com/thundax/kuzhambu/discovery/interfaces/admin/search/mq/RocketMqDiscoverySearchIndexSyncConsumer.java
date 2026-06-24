package com.thundax.kuzhambu.discovery.interfaces.admin.search.mq;

import com.thundax.kuzhambu.classics.application.searchsync.model.ClassicsSearchIndexSyncEventType;
import com.thundax.kuzhambu.classics.application.searchsync.model.ClassicsSearchIndexSyncMessage;
import com.thundax.kuzhambu.discovery.application.search.service.SearchIndexSyncApplicationService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "kuzhambu.discovery.search.index-sync.consumer-enabled", havingValue = "true")
@RocketMQMessageListener(
        topic = "${kuzhambu.discovery.search.index-sync.topic:kuzhambu.discovery.search.index-sync}",
        consumerGroup =
                "${kuzhambu.discovery.search.index-sync.consumer-group:kuzhambu-discovery-search-index-sync-consumer}",
        selectorExpression = "*")
public class RocketMqDiscoverySearchIndexSyncConsumer implements RocketMQListener<ClassicsSearchIndexSyncMessage> {

    private final SearchIndexSyncApplicationService searchIndexSyncApplicationService;

    public RocketMqDiscoverySearchIndexSyncConsumer(
            SearchIndexSyncApplicationService searchIndexSyncApplicationService) {
        this.searchIndexSyncApplicationService = searchIndexSyncApplicationService;
    }

    @Override
    public void onMessage(ClassicsSearchIndexSyncMessage message) {
        if (message == null || message.getEventType() == null) {
            return;
        }
        if (message.getEventType() == ClassicsSearchIndexSyncEventType.DELETE) {
            searchIndexSyncApplicationService.syncDelete(
                    message.getContentType(),
                    message.getContentId(),
                    message.getCurrentVersionNo(),
                    message.getOccurredAt());
            return;
        }
        searchIndexSyncApplicationService.syncUpsert(
                message.getContentType(), message.getContentId(), message.getCurrentVersionNo());
    }
}
