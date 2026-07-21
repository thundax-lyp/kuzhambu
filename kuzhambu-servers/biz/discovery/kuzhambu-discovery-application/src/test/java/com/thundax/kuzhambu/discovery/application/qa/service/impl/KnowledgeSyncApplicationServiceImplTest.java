package com.thundax.kuzhambu.discovery.application.qa.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsQaKnowledgeFacadeDto;
import com.thundax.kuzhambu.classics.facade.request.ClassicsQaKnowledgeFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.common.knowledge.client.KnowledgeBaseClient;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseEnsureRequest;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseResult;
import com.thundax.kuzhambu.common.knowledge.model.health.KnowledgeHealthResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemDeleteRequest;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemUpsertRequest;
import com.thundax.kuzhambu.common.knowledge.model.sync.KnowledgeSyncRequest;
import com.thundax.kuzhambu.common.knowledge.model.sync.KnowledgeSyncResult;
import com.thundax.kuzhambu.discovery.application.qa.command.SyncKnowledgeContentCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.KnowledgeSyncItemResult;
import com.thundax.kuzhambu.discovery.application.qa.support.KnowledgeDocumentAssembler;
import com.thundax.kuzhambu.discovery.application.qa.support.KnowledgeItemTextRenderer;
import com.thundax.kuzhambu.discovery.application.qa.support.KnowledgeRevisionCalculator;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaKnowledgeSyncItem;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaKnowledgeSyncBatchRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaKnowledgeSyncItemRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeSyncApplicationServiceImplTest {

    @Test
    void syncContentShouldUpsertAndSyncAndPersistSucceededItem() {
        KnowledgeBaseClient knowledgeBaseClient = mock(KnowledgeBaseClient.class);
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        QaKnowledgeSyncItemRepository itemRepository = mock(QaKnowledgeSyncItemRepository.class);
        QaKnowledgeSyncBatchRepository batchRepository = mock(QaKnowledgeSyncBatchRepository.class);

        when(knowledgeBaseClient.health())
                .thenReturn(new KnowledgeHealthResult(true, "fastgpt", "available", Map.of("provider", "fastgpt")));
        when(knowledgeBaseClient.ensureKnowledgeBase(any(KnowledgeBaseEnsureRequest.class)))
                .thenReturn(new KnowledgeBaseResult("kb-1", "kuzhambu-qa", Map.of()));
        when(classicsFacade.getQaKnowledge(any(ClassicsQaKnowledgeFacadeRequest.class)))
                .thenReturn(ClassicsQaKnowledgeFacadeResponse.builder()
                        .knowledge(buildKnowledge())
                        .build());
        when(knowledgeBaseClient.upsertKnowledgeItem(any(KnowledgeItemUpsertRequest.class)))
                .thenReturn(new KnowledgeItemResult("item-1", "kb-1", "SANCAI_ENTRY:1001", "黄帝问答", Map.of()));
        when(knowledgeBaseClient.syncKnowledgeItem(any(KnowledgeSyncRequest.class)))
                .thenReturn(new KnowledgeSyncResult("sync-1", "SUCCEEDED", Map.of()));
        when(itemRepository.getBySourceId("SANCAI_ENTRY:1001")).thenReturn(null);
        when(itemRepository.save(any(QaKnowledgeSyncItem.class))).thenReturn(9001L);

        KnowledgeSyncApplicationServiceImpl service = new KnowledgeSyncApplicationServiceImpl(
                knowledgeBaseClient,
                classicsFacade,
                itemRepository,
                batchRepository,
                new KnowledgeDocumentAssembler(),
                new KnowledgeItemTextRenderer(),
                new KnowledgeRevisionCalculator());

        KnowledgeSyncItemResult result =
                service.syncContent(new SyncKnowledgeContentCommand("SANCAI_ENTRY", 1001L, 2, "req-1", "trace-1"));

        assertEquals("SUCCEEDED", result.getSyncStatus());
        assertEquals("fastgpt", result.getProvider());
        assertEquals("item-1", result.getExternalKnowledgeItemId());
        assertEquals("黄帝", result.getTitle());
        assertNotNull(result.getSyncedAt());
        verify(itemRepository).save(any(QaKnowledgeSyncItem.class));
    }

    @Test
    void syncContentShouldPersistFailureWhenUpsertReturnsEmpty() {
        KnowledgeBaseClient knowledgeBaseClient = mock(KnowledgeBaseClient.class);
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        QaKnowledgeSyncItemRepository itemRepository = mock(QaKnowledgeSyncItemRepository.class);
        QaKnowledgeSyncBatchRepository batchRepository = mock(QaKnowledgeSyncBatchRepository.class);

        when(knowledgeBaseClient.health())
                .thenReturn(new KnowledgeHealthResult(true, "fastgpt", "available", Map.of("provider", "fastgpt")));
        when(knowledgeBaseClient.ensureKnowledgeBase(any(KnowledgeBaseEnsureRequest.class)))
                .thenReturn(new KnowledgeBaseResult("kb-1", "kuzhambu-qa", Map.of()));
        when(classicsFacade.getQaKnowledge(any(ClassicsQaKnowledgeFacadeRequest.class)))
                .thenReturn(ClassicsQaKnowledgeFacadeResponse.builder()
                        .knowledge(buildKnowledge())
                        .build());
        when(knowledgeBaseClient.upsertKnowledgeItem(any(KnowledgeItemUpsertRequest.class)))
                .thenReturn(null);
        when(itemRepository.getBySourceId("SANCAI_ENTRY:1001")).thenReturn(null);
        when(itemRepository.save(any(QaKnowledgeSyncItem.class))).thenReturn(9001L);

        KnowledgeSyncApplicationServiceImpl service = new KnowledgeSyncApplicationServiceImpl(
                knowledgeBaseClient,
                classicsFacade,
                itemRepository,
                batchRepository,
                new KnowledgeDocumentAssembler(),
                new KnowledgeItemTextRenderer(),
                new KnowledgeRevisionCalculator());

        KnowledgeSyncItemResult result =
                service.syncContent(new SyncKnowledgeContentCommand("SANCAI_ENTRY", 1001L, 2, "req-1", "trace-1"));

        assertEquals("FAILED", result.getSyncStatus());
        assertEquals("Knowledge item upsert failed", result.getFailureReason());
        ArgumentCaptor<QaKnowledgeSyncItem> syncItemCaptor = ArgumentCaptor.forClass(QaKnowledgeSyncItem.class);
        verify(itemRepository).save(syncItemCaptor.capture());
        assertEquals("kb-1", syncItemCaptor.getValue().getExternalKnowledgeBaseId());
    }

    @Test
    void deleteContentShouldDeleteAndMarkDeletedStatus() {
        KnowledgeBaseClient knowledgeBaseClient = mock(KnowledgeBaseClient.class);
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        QaKnowledgeSyncItemRepository itemRepository = mock(QaKnowledgeSyncItemRepository.class);
        QaKnowledgeSyncBatchRepository batchRepository = mock(QaKnowledgeSyncBatchRepository.class);

        when(knowledgeBaseClient.deleteKnowledgeItem(any(KnowledgeItemDeleteRequest.class)))
                .thenReturn(new KnowledgeSyncResult("del-1", "DELETED", Map.of("result", "ok")));
        when(itemRepository.getBySourceId("SANCAI_ENTRY:1001"))
                .thenReturn(new QaKnowledgeSyncItem(
                        1001L,
                        "SANCAI_ENTRY:1001",
                        "SANCAI_ENTRY",
                        1001L,
                        "kuzhambu-qa",
                        2,
                        "rev-1",
                        "fastgpt",
                        "kb-1",
                        "item-1",
                        "SUCCEEDED",
                        null,
                        null,
                        null,
                        null));

        KnowledgeSyncApplicationServiceImpl service = new KnowledgeSyncApplicationServiceImpl(
                knowledgeBaseClient,
                classicsFacade,
                itemRepository,
                batchRepository,
                new KnowledgeDocumentAssembler(),
                new KnowledgeItemTextRenderer(),
                new KnowledgeRevisionCalculator());

        KnowledgeSyncItemResult result =
                service.deleteContent(new SyncKnowledgeContentCommand("SANCAI_ENTRY", 1001L, 2, "req-1", "trace-1"));

        assertEquals("DELETED", result.getSyncStatus());
        assertEquals("item-1", result.getExternalKnowledgeItemId());
        verify(itemRepository).update(any(QaKnowledgeSyncItem.class));
        verify(knowledgeBaseClient)
                .deleteKnowledgeItem(eq(new KnowledgeItemDeleteRequest(
                        "kuzhambu-qa", "item-1", "SANCAI_ENTRY:1001", Map.of("operation", "deleteContent"))));
    }

    private ClassicsQaKnowledgeFacadeDto buildKnowledge() {
        return ClassicsQaKnowledgeFacadeDto.builder()
                .sourceId("SANCAI_ENTRY:1001")
                .contentType("SANCAI_ENTRY")
                .contentId("1001")
                .knowledgeBase("kuzhambu-qa")
                .currentVersionNo(2)
                .title("黄帝")
                .categoryPath("卷一")
                .summary("上古皇帝")
                .body("黄帝是上古帝王")
                .build();
    }
}
