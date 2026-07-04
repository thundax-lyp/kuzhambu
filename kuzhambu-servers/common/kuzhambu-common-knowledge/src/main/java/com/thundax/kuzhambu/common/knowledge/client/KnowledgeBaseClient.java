package com.thundax.kuzhambu.common.knowledge.client;

import com.thundax.kuzhambu.common.knowledge.model.KnowledgeChatRequest;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeChatResult;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeCollectionCreateRequest;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeCollectionListRequest;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeCollectionPageResult;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeCollectionResult;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeDatasetCreateRequest;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeDatasetListRequest;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeDatasetPageResult;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeDatasetResult;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeHealthResult;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeSyncRequest;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeSyncResult;

public interface KnowledgeBaseClient {

    KnowledgeHealthResult health();

    KnowledgeDatasetPageResult listDatasets(KnowledgeDatasetListRequest request);

    KnowledgeDatasetResult createDataset(KnowledgeDatasetCreateRequest request);

    KnowledgeCollectionPageResult listCollections(KnowledgeCollectionListRequest request);

    KnowledgeCollectionResult createCollection(KnowledgeCollectionCreateRequest request);

    KnowledgeSyncResult syncCollection(KnowledgeSyncRequest request);

    KnowledgeChatResult chat(KnowledgeChatRequest request);
}
