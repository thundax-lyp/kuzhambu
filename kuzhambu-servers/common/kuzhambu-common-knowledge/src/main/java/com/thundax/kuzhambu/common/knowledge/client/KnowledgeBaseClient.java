package com.thundax.kuzhambu.common.knowledge.client;

import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseEnsureRequest;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseListRequest;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBasePageResult;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseResult;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatRequest;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatResult;
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

    KnowledgeChatResult chat(KnowledgeChatRequest request);
}
