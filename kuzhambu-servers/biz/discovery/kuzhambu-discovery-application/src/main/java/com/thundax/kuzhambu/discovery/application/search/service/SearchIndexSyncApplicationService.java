package com.thundax.kuzhambu.discovery.application.search.service;

import com.thundax.kuzhambu.discovery.application.search.command.SearchIndexSyncDeleteCommand;
import com.thundax.kuzhambu.discovery.application.search.command.SearchIndexSyncUpsertCommand;

public interface SearchIndexSyncApplicationService {

    Boolean syncUpsert(SearchIndexSyncUpsertCommand command);

    Boolean syncDelete(SearchIndexSyncDeleteCommand command);
}
