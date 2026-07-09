package com.thundax.kuzhambu.common.knowledge.support;

import com.thundax.kuzhambu.common.knowledge.client.KnowledgeBaseClient;
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
import java.util.Collections;

public class DisabledKnowledgeBaseClient implements KnowledgeBaseClient {

    private static final String MESSAGE =
            "Knowledge base integration is disabled. Configure kuzhambu.knowledge.enabled=true.";

    @Override
    public KnowledgeHealthResult health() {
        return new KnowledgeHealthResult(false, "disabled", MESSAGE, Collections.emptyMap());
    }

    @Override
    public KnowledgeBasePageResult listKnowledgeBases(KnowledgeBaseListRequest request) {
        throw disabled();
    }

    @Override
    public KnowledgeBaseResult ensureKnowledgeBase(KnowledgeBaseEnsureRequest request) {
        throw disabled();
    }

    @Override
    public KnowledgeItemPageResult listKnowledgeItems(KnowledgeItemListRequest request) {
        throw disabled();
    }

    @Override
    public KnowledgeItemResult upsertKnowledgeItem(KnowledgeItemUpsertRequest request) {
        throw disabled();
    }

    @Override
    public KnowledgeSyncResult syncKnowledgeItem(KnowledgeSyncRequest request) {
        throw disabled();
    }

    @Override
    public KnowledgeSyncResult deleteKnowledgeItem(KnowledgeItemDeleteRequest request) {
        throw disabled();
    }

    @Override
    public KnowledgeChatResult chat(KnowledgeChatRequest request) {
        throw disabled();
    }

    private IllegalStateException disabled() {
        return new IllegalStateException(MESSAGE);
    }
}
