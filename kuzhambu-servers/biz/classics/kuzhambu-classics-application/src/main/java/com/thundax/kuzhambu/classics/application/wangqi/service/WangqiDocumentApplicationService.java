package com.thundax.kuzhambu.classics.application.wangqi.service;

import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentCommand;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentSourceFileCommand;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentQuery;
import com.thundax.kuzhambu.classics.application.wangqi.result.WangqiDocumentSourceFile;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface WangqiDocumentApplicationService {

    WangqiDocument get(WangqiDocumentId id);

    PageResult<WangqiDocument> page(WangqiDocumentQuery query, PageQuery page);

    List<WangqiDocument> listTimeline(WangqiDocumentQuery query);

    WangqiDocumentId add(WangqiDocumentCommand command);

    WangqiDocumentId update(WangqiDocumentCommand command);

    WangqiDocumentSourceFile changeSourceFile(WangqiDocumentSourceFileCommand command);

    WangqiDocumentSourceFile getSourceFile(WangqiDocumentId id);

    ClassicsStoredContentResult getSourceFileContent(WangqiDocumentId id);

    void changeStorageObject(WangqiDocumentId id, StorageObjectId storageObjectId);

    void delete(WangqiDocumentId id);
}
