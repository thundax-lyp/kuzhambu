package com.thundax.kuzhambu.starter.admin.config;

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminKnowledgeBaseFallbackConfiguration {

    @Bean
    @ConditionalOnMissingBean(KnowledgeBaseClient.class)
    public KnowledgeBaseClient unavailableKnowledgeBaseClient() {
        return new UnavailableKnowledgeBaseClient();
    }

    private static final class UnavailableKnowledgeBaseClient implements KnowledgeBaseClient {

        private static final String PROVIDER = "none";
        private static final String MESSAGE = "Knowledge base client is not configured";

        @Override
        public KnowledgeHealthResult health() {
            return new KnowledgeHealthResult(false, PROVIDER, MESSAGE, Collections.emptyMap());
        }

        @Override
        public KnowledgeBasePageResult listKnowledgeBases(KnowledgeBaseListRequest request) {
            throw unavailable();
        }

        @Override
        public KnowledgeBaseResult ensureKnowledgeBase(KnowledgeBaseEnsureRequest request) {
            throw unavailable();
        }

        @Override
        public KnowledgeItemPageResult listKnowledgeItems(KnowledgeItemListRequest request) {
            throw unavailable();
        }

        @Override
        public KnowledgeItemResult upsertKnowledgeItem(KnowledgeItemUpsertRequest request) {
            throw unavailable();
        }

        @Override
        public KnowledgeSyncResult syncKnowledgeItem(KnowledgeSyncRequest request) {
            throw unavailable();
        }

        @Override
        public KnowledgeSyncResult deleteKnowledgeItem(KnowledgeItemDeleteRequest request) {
            throw unavailable();
        }

        @Override
        public KnowledgeChatResult chat(KnowledgeChatRequest request) {
            throw unavailable();
        }

        private IllegalStateException unavailable() {
            return new IllegalStateException(MESSAGE);
        }
    }
}
