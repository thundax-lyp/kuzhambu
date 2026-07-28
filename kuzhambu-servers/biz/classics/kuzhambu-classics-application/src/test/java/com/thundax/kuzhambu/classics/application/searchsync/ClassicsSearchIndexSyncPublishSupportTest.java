package com.thundax.kuzhambu.classics.application.searchsync;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.thundax.kuzhambu.classics.application.searchsync.service.ClassicsSearchIndexSyncPublisher;
import com.thundax.kuzhambu.classics.application.searchsync.support.ClassicsSearchIndexSyncPublishSupport;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsSearchIndexSyncEventFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsSearchIndexSyncMessageFacadeDto;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class ClassicsSearchIndexSyncPublishSupportTest {

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void publishUpsertAfterCommitShouldRegisterTransactionCallback() {
        ClassicsSearchIndexSyncPublisher publisher = mock(ClassicsSearchIndexSyncPublisher.class);
        ClassicsSearchIndexSyncPublishSupport support = new ClassicsSearchIndexSyncPublishSupport(publisher);
        TransactionSynchronizationManager.initSynchronization();

        support.publishUpsertAfterCommit(ClassicsContentType.SANCAI_ENTRY, "1001", 3);

        verifyNoInteractions(publisher);
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        assertEquals(1, synchronizations.size());

        synchronizations.forEach(TransactionSynchronization::afterCommit);

        ArgumentCaptor<ClassicsSearchIndexSyncMessageFacadeDto> captor =
                ArgumentCaptor.forClass(ClassicsSearchIndexSyncMessageFacadeDto.class);
        verify(publisher).publish(captor.capture());
        ClassicsSearchIndexSyncMessageFacadeDto message = captor.getValue();
        assertNotNull(message.getEventId());
        assertEquals(ClassicsSearchIndexSyncEventFacadeDto.UPSERT, message.getEventType());
        assertEquals("SANCAI_ENTRY", message.getContentType());
        assertEquals("1001", message.getContentId());
        assertEquals(3, message.getCurrentVersionNo());
        assertNotNull(message.getOccurredAt());
    }

    @Test
    void publishDeleteAfterCommitShouldRegisterTransactionCallback() {
        ClassicsSearchIndexSyncPublisher publisher = mock(ClassicsSearchIndexSyncPublisher.class);
        ClassicsSearchIndexSyncPublishSupport support = new ClassicsSearchIndexSyncPublishSupport(publisher);
        TransactionSynchronizationManager.initSynchronization();

        support.publishDeleteAfterCommit(ClassicsContentType.WANGQI_DOCUMENT, "2002", 5);

        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        assertEquals(1, synchronizations.size());

        synchronizations.forEach(TransactionSynchronization::afterCommit);

        ArgumentCaptor<ClassicsSearchIndexSyncMessageFacadeDto> captor =
                ArgumentCaptor.forClass(ClassicsSearchIndexSyncMessageFacadeDto.class);
        verify(publisher).publish(captor.capture());
        assertEquals(
                ClassicsSearchIndexSyncEventFacadeDto.DELETE, captor.getValue().getEventType());
        assertEquals("WANGQI_DOCUMENT", captor.getValue().getContentType());
        assertEquals("2002", captor.getValue().getContentId());
        assertEquals(5, captor.getValue().getCurrentVersionNo());
    }

    @Test
    void publishAfterCommitShouldFailWhenTransactionSynchronizationIsMissing() {
        ClassicsSearchIndexSyncPublisher publisher = mock(ClassicsSearchIndexSyncPublisher.class);
        ClassicsSearchIndexSyncPublishSupport support = new ClassicsSearchIndexSyncPublishSupport(publisher);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> support.publishUpsertAfterCommit(ClassicsContentType.MING_CUSTOMS, "3003", 7));

        assertTrue(exception.getMessage().contains("active transaction"));
        verifyNoInteractions(publisher);
    }

    @Test
    void publishAfterCommitShouldNotPropagatePublisherFailure() {
        ClassicsSearchIndexSyncPublisher publisher = mock(ClassicsSearchIndexSyncPublisher.class);
        doThrow(new IllegalStateException("rocketmq topic invalid"))
                .when(publisher)
                .publish(any());
        ClassicsSearchIndexSyncPublishSupport support = new ClassicsSearchIndexSyncPublishSupport(publisher);
        TransactionSynchronizationManager.initSynchronization();

        support.publishUpsertAfterCommit(ClassicsContentType.SANCAI_ENTRY, "1001", 3);

        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        assertEquals(1, synchronizations.size());
        assertDoesNotThrow(() -> synchronizations.forEach(TransactionSynchronization::afterCommit));
        verify(publisher).publish(any());
    }
}
