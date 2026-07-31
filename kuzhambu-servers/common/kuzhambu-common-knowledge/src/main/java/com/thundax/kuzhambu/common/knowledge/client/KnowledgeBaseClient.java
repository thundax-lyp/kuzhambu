package com.thundax.kuzhambu.common.knowledge.client;

import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseEnsureRequest;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseListRequest;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBasePageResult;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseResult;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatRequest;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatResult;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatStreamHandler;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionCreateRequest;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionReferenceRequest;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionResult;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionUpdateRequest;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataListRequest;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataPageResult;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataPushRequest;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataPushResult;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataReferenceRequest;
import com.thundax.kuzhambu.common.knowledge.model.health.KnowledgeHealthResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemDeleteRequest;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemListRequest;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemPageResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemUpsertRequest;
import com.thundax.kuzhambu.common.knowledge.model.sync.KnowledgeSyncRequest;
import com.thundax.kuzhambu.common.knowledge.model.sync.KnowledgeSyncResult;

public interface KnowledgeBaseClient {

    KnowledgeHealthResult health();

    KnowledgeBasePageResult listKnowledgeBases(KnowledgeBaseListRequest request);

    KnowledgeBaseResult ensureKnowledgeBase(KnowledgeBaseEnsureRequest request);

    KnowledgeItemPageResult listKnowledgeItems(KnowledgeItemListRequest request);

    KnowledgeItemResult upsertKnowledgeItem(KnowledgeItemUpsertRequest request);

    KnowledgeSyncResult syncKnowledgeItem(KnowledgeSyncRequest request);

    KnowledgeSyncResult deleteKnowledgeItem(KnowledgeItemDeleteRequest request);

    KnowledgeCollectionResult createCollection(KnowledgeCollectionCreateRequest request);

    KnowledgeCollectionResult getCollection(KnowledgeCollectionReferenceRequest request);

    void updateCollection(KnowledgeCollectionUpdateRequest request);

    void deleteCollection(KnowledgeCollectionReferenceRequest request);

    KnowledgeCollectionDataPageResult listCollectionData(KnowledgeCollectionDataListRequest request);

    void deleteCollectionData(KnowledgeCollectionDataReferenceRequest request);

    KnowledgeCollectionDataPushResult pushCollectionData(KnowledgeCollectionDataPushRequest request);

    KnowledgeChatResult chat(KnowledgeChatRequest request);

    KnowledgeChatResult chatStream(KnowledgeChatRequest request, KnowledgeChatStreamHandler streamHandler);
}
