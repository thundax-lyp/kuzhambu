package com.thundax.kuzhambu.discovery.application.search.service;

import com.thundax.kuzhambu.discovery.application.search.command.SearchIndexCleanupCommand;

public interface SearchIndexCleanupApplicationService {

    Integer cleanupDeletedDocuments(SearchIndexCleanupCommand command);
}
