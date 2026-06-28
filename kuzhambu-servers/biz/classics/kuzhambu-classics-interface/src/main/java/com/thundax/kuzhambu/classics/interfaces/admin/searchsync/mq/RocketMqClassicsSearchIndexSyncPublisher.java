package com.thundax.kuzhambu.classics.interfaces.admin.searchsync.mq;

import com.thundax.kuzhambu.classics.application.searchsync.service.ClassicsSearchIndexSyncPublisher;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsSearchIndexSyncEventFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsSearchIndexSyncMessageFacadeDto;
import com.thundax.kuzhambu.common.rocketmq.KuzhambuMqMessage;
import com.thundax.kuzhambu.common.rocketmq.KuzhambuMqSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RocketMqClassicsSearchIndexSyncPublisher implements ClassicsSearchIndexSyncPublisher {

    private final KuzhambuMqSender mqSender;

    @Value("${kuzhambu.discovery.search.index-sync.topic}")
    private String topic;

    @Value("${kuzhambu.discovery.search.index-sync.producer-tag-upsert}")
    private String upsertTag;

    @Value("${kuzhambu.discovery.search.index-sync.producer-tag-delete}")
    private String deleteTag;

    @Override
    public void publish(ClassicsSearchIndexSyncMessageFacadeDto message) {
        mqSender.send(
                KuzhambuMqMessage.forTopicWithTag(topic, resolveTag(message.getEventType()), buildKey(message), message)
                        .withHeader("kuzhambu-message-type", "classics-search-index-sync"));
    }

    private String resolveTag(ClassicsSearchIndexSyncEventFacadeDto eventType) {
        return switch (eventType) {
            case UPSERT -> upsertTag;
            case DELETE -> deleteTag;
        };
    }

    private String buildKey(ClassicsSearchIndexSyncMessageFacadeDto message) {
        return message.getContentType() + ":" + message.getContentId() + ":" + message.getCurrentVersionNo();
    }
}
