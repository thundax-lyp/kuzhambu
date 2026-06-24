package com.thundax.kuzhambu.discovery.application.search.service;

public interface SearchIndexCleanupApplicationService {

    Integer cleanupDeletedDocuments(int retentionDays);
}
