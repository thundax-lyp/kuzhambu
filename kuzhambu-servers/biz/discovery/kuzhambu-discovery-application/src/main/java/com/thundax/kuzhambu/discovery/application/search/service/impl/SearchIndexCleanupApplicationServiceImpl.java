package com.thundax.kuzhambu.discovery.application.search.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.discovery.application.search.command.SearchIndexCleanupCommand;
import com.thundax.kuzhambu.discovery.application.search.service.SearchIndexCleanupApplicationService;
import com.thundax.kuzhambu.discovery.application.search.support.SearchIndexGateway;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class SearchIndexCleanupApplicationServiceImpl implements SearchIndexCleanupApplicationService {

    private final SearchIndexGateway searchIndexGateway;
    private Clock clock = Clock.systemUTC();

    public SearchIndexCleanupApplicationServiceImpl(SearchIndexGateway searchIndexGateway) {
        this.searchIndexGateway = searchIndexGateway;
    }

    @Override
    public Integer cleanupDeletedDocuments(SearchIndexCleanupCommand command) {
        int retentionDays = command == null ? 0 : command.retentionDays();
        Instant threshold = Instant.now(clock).minus(Duration.ofDays(Math.max(retentionDays, 0)));
        return searchIndexGateway.cleanupDeletedDocumentsOlderThan(threshold);
    }

    SearchIndexCleanupApplicationServiceImpl useClock(Clock clock) {
        if (clock != null) {
            this.clock = clock;
        }
        return this;
    }
}
