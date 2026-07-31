package com.thundax.kuzhambu.classics.application.publication.support;

import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationFastGptProbeResult;
import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationFragment;
import com.thundax.kuzhambu.common.knowledge.client.KnowledgeBaseClient;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionCreateRequest;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionReferenceRequest;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionResult;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionUpdateRequest;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataListRequest;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataPushItem;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataPushRequest;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataReferenceRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ClassicsPublicationFastGptGateway {

    private static final int DATA_PAGE_SIZE = 30;
    private static final int PUSH_BATCH_SIZE = 200;

    private final KnowledgeBaseClient knowledgeBaseClient;

    public ClassicsPublicationFastGptGateway(KnowledgeBaseClient knowledgeBaseClient) {
        this.knowledgeBaseClient = knowledgeBaseClient;
    }

    public String fullReplace(String collectionId, String collectionName, List<ClassicsPublicationFragment> fragments) {
        String effectiveCollectionId = ensureDisabledCollection(collectionId, collectionName);
        deleteAllData(effectiveCollectionId);
        pushAllData(effectiveCollectionId, fragments == null ? Collections.emptyList() : fragments);
        return effectiveCollectionId;
    }

    public void enable(String collectionId) {
        knowledgeBaseClient.updateCollection(new KnowledgeCollectionUpdateRequest(collectionId, false));
    }

    public void disable(String collectionId) {
        if (collectionId != null) {
            knowledgeBaseClient.updateCollection(new KnowledgeCollectionUpdateRequest(collectionId, true));
        }
    }

    public void delete(String collectionId) {
        if (collectionId != null) {
            knowledgeBaseClient.deleteCollection(new KnowledgeCollectionReferenceRequest(collectionId));
        }
    }

    public ClassicsPublicationFastGptProbeResult probe(String collectionId) {
        if (collectionId == null) {
            return ClassicsPublicationFastGptProbeResult.missing();
        }
        KnowledgeCollectionResult result =
                knowledgeBaseClient.getCollection(new KnowledgeCollectionReferenceRequest(collectionId));
        return result == null
                ? ClassicsPublicationFastGptProbeResult.missing()
                : new ClassicsPublicationFastGptProbeResult(true, result.forbid());
    }

    private String ensureDisabledCollection(String collectionId, String collectionName) {
        String effectiveCollectionId = collectionId;
        if (effectiveCollectionId == null) {
            KnowledgeCollectionResult created = knowledgeBaseClient.createCollection(
                    new KnowledgeCollectionCreateRequest(null, collectionName, "virtual"));
            effectiveCollectionId = created.collectionId();
        }
        knowledgeBaseClient.updateCollection(new KnowledgeCollectionUpdateRequest(effectiveCollectionId, true));
        return effectiveCollectionId;
    }

    private void deleteAllData(String collectionId) {
        while (true) {
            var page = knowledgeBaseClient.listCollectionData(
                    new KnowledgeCollectionDataListRequest(collectionId, 0, DATA_PAGE_SIZE));
            if (page.items() == null || page.items().isEmpty()) {
                return;
            }
            page.items()
                    .forEach(item -> knowledgeBaseClient.deleteCollectionData(
                            new KnowledgeCollectionDataReferenceRequest(item.dataId())));
        }
    }

    private void pushAllData(String collectionId, List<ClassicsPublicationFragment> fragments) {
        int inserted = 0;
        for (int start = 0; start < fragments.size(); start += PUSH_BATCH_SIZE) {
            int end = Math.min(start + PUSH_BATCH_SIZE, fragments.size());
            List<KnowledgeCollectionDataPushItem> batch = new ArrayList<>(end - start);
            for (ClassicsPublicationFragment fragment : fragments.subList(start, end)) {
                batch.add(new KnowledgeCollectionDataPushItem(
                        fragment.question(), fragment.answer(), fragment.chunkIndex()));
            }
            inserted += knowledgeBaseClient
                    .pushCollectionData(new KnowledgeCollectionDataPushRequest(collectionId, batch))
                    .insertLen();
        }
        if (inserted != fragments.size()) {
            throw new IllegalStateException(
                    "FastGPT accepted " + inserted + " of " + fragments.size() + " publication fragments");
        }
    }
}
