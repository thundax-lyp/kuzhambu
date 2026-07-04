package com.thundax.kuzhambu.discovery.domain.qa.repository;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaKnowledgeSyncItem;
import java.util.List;

public interface QaKnowledgeSyncItemRepository {

    QaKnowledgeSyncItem getBySourceId(String sourceId);

    List<QaKnowledgeSyncItem> listBySyncStatus(String syncStatus, Integer limit);

    Long save(QaKnowledgeSyncItem entity);

    int update(QaKnowledgeSyncItem entity);
}
