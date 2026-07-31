package com.thundax.kuzhambu.classics.application.publication.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationFragment;
import com.thundax.kuzhambu.common.knowledge.client.KnowledgeBaseClient;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionResult;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataPageResult;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataPushRequest;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataPushResult;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ClassicsPublicationFastGptGatewayTest {

    private final KnowledgeBaseClient client = mock(KnowledgeBaseClient.class);
    private final ClassicsPublicationFastGptGateway gateway = new ClassicsPublicationFastGptGateway(client);

    @Test
    void fullReplaceShouldDisableDeleteFromOffsetZeroAndPushInTwoHundredItemBatches() {
        when(client.getCollection(any())).thenReturn(new KnowledgeCollectionResult("collection-1", true, Map.of()));
        when(client.listCollectionData(any()))
                .thenReturn(
                        new KnowledgeCollectionDataPageResult(
                                2,
                                List.of(
                                        new KnowledgeCollectionDataResult("data-1"),
                                        new KnowledgeCollectionDataResult("data-2")),
                                Map.of()),
                        new KnowledgeCollectionDataPageResult(0, List.of(), Map.of()));
        when(client.pushCollectionData(any()))
                .thenReturn(
                        new KnowledgeCollectionDataPushResult(200, Map.of()),
                        new KnowledgeCollectionDataPushResult(1, Map.of()));
        List<ClassicsPublicationFragment> fragments = fragments(201);

        gateway.fullReplace("collection-1", fragments);
        var ordered = inOrder(client);
        ordered.verify(client).updateCollection(any());
        ordered.verify(client).listCollectionData(any());
        ordered.verify(client, times(2)).deleteCollectionData(any());
        ordered.verify(client).listCollectionData(any());
        ArgumentCaptor<KnowledgeCollectionDataPushRequest> captor =
                ArgumentCaptor.forClass(KnowledgeCollectionDataPushRequest.class);
        verify(client, times(2)).pushCollectionData(captor.capture());
        assertEquals(200, captor.getAllValues().get(0).data().size());
        assertEquals(1, captor.getAllValues().get(1).data().size());
        assertEquals(200, captor.getAllValues().get(1).data().get(0).chunkIndex());
    }

    @Test
    void fullReplaceShouldCreateVirtualCollectionAndImmediatelyDisableIt() {
        when(client.createCollection(any()))
                .thenReturn(new KnowledgeCollectionResult("collection-new", true, Map.of()));
        when(client.listCollectionData(any()))
                .thenReturn(new KnowledgeCollectionDataPageResult(0, List.of(), Map.of()));

        String collectionId = gateway.createCollection("SANCAI_ENTRY:101:天文");
        gateway.fullReplace(collectionId, List.of());

        assertEquals("collection-new", collectionId);
        var ordered = inOrder(client);
        ordered.verify(client).createCollection(any());
        ordered.verify(client).updateCollection(any());
        ordered.verify(client).listCollectionData(any());
    }

    @Test
    void fullReplaceShouldFailWhenFastGptDoesNotAcceptEveryFragment() {
        when(client.listCollectionData(any()))
                .thenReturn(new KnowledgeCollectionDataPageResult(0, List.of(), Map.of()));
        when(client.pushCollectionData(any())).thenReturn(new KnowledgeCollectionDataPushResult(199, Map.of()));

        assertThrows(IllegalStateException.class, () -> gateway.fullReplace("collection-1", fragments(200)));
    }

    private List<ClassicsPublicationFragment> fragments(int count) {
        List<ClassicsPublicationFragment> fragments = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            fragments.add(new ClassicsPublicationFragment("q" + index, "a" + index, index));
        }
        return fragments;
    }
}
