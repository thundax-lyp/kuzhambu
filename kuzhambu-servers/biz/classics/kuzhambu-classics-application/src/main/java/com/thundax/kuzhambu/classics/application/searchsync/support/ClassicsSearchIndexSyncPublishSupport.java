package com.thundax.kuzhambu.classics.application.searchsync.support;

import com.thundax.kuzhambu.classics.application.searchsync.model.ClassicsSearchIndexSyncEventType;
import com.thundax.kuzhambu.classics.application.searchsync.model.ClassicsSearchIndexSyncMessage;
import com.thundax.kuzhambu.classics.application.searchsync.service.ClassicsSearchIndexSyncPublisher;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import java.util.Date;
import java.util.UUID;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ClassicsSearchIndexSyncPublishSupport {

    private final ClassicsSearchIndexSyncPublisher publisher;

    public ClassicsSearchIndexSyncPublishSupport(ClassicsSearchIndexSyncPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishUpsertAfterCommit(ClassicsContentType contentType, String contentId, Integer currentVersionNo) {
        publishAfterCommit(
                buildMessage(ClassicsSearchIndexSyncEventType.UPSERT, contentType, contentId, currentVersionNo));
    }

    public void publishDeleteAfterCommit(ClassicsContentType contentType, String contentId, Integer currentVersionNo) {
        publishAfterCommit(
                buildMessage(ClassicsSearchIndexSyncEventType.DELETE, contentType, contentId, currentVersionNo));
    }

    private void publishAfterCommit(ClassicsSearchIndexSyncMessage message) {
        ensureTransactionSynchronizationActive();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publisher.publish(message);
            }
        });
    }

    private ClassicsSearchIndexSyncMessage buildMessage(
            ClassicsSearchIndexSyncEventType eventType,
            ClassicsContentType contentType,
            String contentId,
            Integer currentVersionNo) {
        return new ClassicsSearchIndexSyncMessage(
                UUID.randomUUID().toString(), eventType, contentType.value(), contentId, currentVersionNo, new Date());
    }

    private void ensureTransactionSynchronizationActive() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Classics search index sync publish requires an active transaction");
        }
    }
}
