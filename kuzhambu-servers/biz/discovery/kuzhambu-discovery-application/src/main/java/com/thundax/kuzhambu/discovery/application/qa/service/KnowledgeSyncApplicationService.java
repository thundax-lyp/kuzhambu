package com.thundax.kuzhambu.discovery.application.qa.service;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.application.qa.command.SyncKnowledgeContentCommand;
import com.thundax.kuzhambu.discovery.application.qa.query.KnowledgeSyncItemPageQuery;
import com.thundax.kuzhambu.discovery.application.qa.result.KnowledgeHealthResult;
import com.thundax.kuzhambu.discovery.application.qa.result.KnowledgeSyncItemResult;

public interface KnowledgeSyncApplicationService {
    KnowledgeHealthResult health();

    Long rebuild();

    KnowledgeSyncItemResult syncContent(SyncKnowledgeContentCommand command);

    KnowledgeSyncItemResult deleteContent(SyncKnowledgeContentCommand command);

    PageResult<KnowledgeSyncItemResult> pageSyncItems(KnowledgeSyncItemPageQuery query);
}
