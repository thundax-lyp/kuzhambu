package com.thundax.kuzhambu.discovery.application.search.service;

import java.util.Date;

public interface SearchIndexSyncApplicationService {

    Boolean syncUpsert(String contentType, String contentId, Integer currentVersionNo);

    Boolean syncDelete(String contentType, String contentId, Integer currentVersionNo, Date occurredAt);
}
