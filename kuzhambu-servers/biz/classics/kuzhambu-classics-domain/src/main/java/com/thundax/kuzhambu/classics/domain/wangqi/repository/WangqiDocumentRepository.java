package com.thundax.kuzhambu.classics.domain.wangqi.repository;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocumentEvent;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;

public interface WangqiDocumentRepository {

    WangqiDocument getById(WangqiDocumentId id);

    PageResult<WangqiDocument> page(
            String keyword, String visibility, SortDirection sortDirection, int pageNo, int pageSize);

    List<WangqiDocument> listTimeline(String keyword, String visibility, SortDirection sortDirection);

    List<WangqiDocumentEvent> listEvents(List<WangqiDocumentId> documentIds);

    WangqiDocumentId insert(WangqiDocument document);

    int update(WangqiDocument document);

    int updateRestoredVersion(WangqiDocument document);

    int updateStorageObjectId(WangqiDocumentId id, StorageObjectId storageObjectId);

    int updateVisibility(WangqiDocumentId id, String visibility);

    int deleteById(WangqiDocumentId id);
}
