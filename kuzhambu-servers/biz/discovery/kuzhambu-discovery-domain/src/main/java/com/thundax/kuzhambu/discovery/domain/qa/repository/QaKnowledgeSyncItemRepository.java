package com.thundax.kuzhambu.discovery.domain.qa.repository;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaKnowledgeSyncItem;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.KnowledgeSourceId;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaKnowledgeSyncStatus;
import java.util.List;

public interface QaKnowledgeSyncItemRepository {

    QaKnowledgeSyncItem getBySourceId(KnowledgeSourceId sourceId);

    List<QaKnowledgeSyncItem> listBySyncStatus(QaKnowledgeSyncStatus syncStatus, Integer limit);

    KnowledgeSourceId save(QaKnowledgeSyncItem entity);

    int update(QaKnowledgeSyncItem entity);
}
