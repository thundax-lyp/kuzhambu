package com.thundax.kuzhambu.classics.application.wangqi.service;

import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentSaveCommand;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentVisibilityCommand;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentPageQuery;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface WangqiDocumentApplicationService {

    WangqiDocument get(Long id);

    PageResult<WangqiDocument> page(WangqiDocumentPageQuery query, PageQuery page);

    List<WangqiDocument> listTimeline(WangqiDocumentPageQuery query);

    Long save(WangqiDocumentSaveCommand command);

    void changeStorageObject(Long id, Long storageObjectId);

    void changeVisibility(WangqiDocumentVisibilityCommand command);

    void delete(Long id);
}
