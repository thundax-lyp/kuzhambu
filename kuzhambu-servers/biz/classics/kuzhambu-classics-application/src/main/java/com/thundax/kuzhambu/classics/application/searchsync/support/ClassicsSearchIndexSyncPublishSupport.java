package com.thundax.kuzhambu.classics.application.searchsync.support;

import com.thundax.kuzhambu.classics.application.searchsync.service.ClassicsSearchIndexSyncPublisher;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsSearchIndexSyncEventFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsSearchIndexSyncMessageFacadeDto;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class ClassicsSearchIndexSyncPublishSupport {

    private final ClassicsSearchIndexSyncPublisher publisher;

    public ClassicsSearchIndexSyncPublishSupport(ClassicsSearchIndexSyncPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishUpsertAfterCommit(ClassicsContentType contentType, String contentId, Integer currentVersionNo) {
        publishAfterCommit(
                buildMessage(ClassicsSearchIndexSyncEventFacadeDto.UPSERT, contentType, contentId, currentVersionNo));
    }

    public void publishDeleteAfterCommit(ClassicsContentType contentType, String contentId, Integer currentVersionNo) {
        publishAfterCommit(
                buildMessage(ClassicsSearchIndexSyncEventFacadeDto.DELETE, contentType, contentId, currentVersionNo));
    }

    private void publishAfterCommit(ClassicsSearchIndexSyncMessageFacadeDto message) {
        ensureTransactionSynchronizationActive();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publisher.publish(message);
            }
        });
    }

    private ClassicsSearchIndexSyncMessageFacadeDto buildMessage(
            ClassicsSearchIndexSyncEventFacadeDto eventType,
            ClassicsContentType contentType,
            String contentId,
            Integer currentVersionNo) {
        return ClassicsSearchIndexSyncMessageFacadeDto.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .contentType(contentType.value())
                .contentId(contentId)
                .currentVersionNo(currentVersionNo)
                .occurredAt(new Date())
                .build();
    }

    private void ensureTransactionSynchronizationActive() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Classics search index sync publish requires an active transaction");
        }
    }
}
