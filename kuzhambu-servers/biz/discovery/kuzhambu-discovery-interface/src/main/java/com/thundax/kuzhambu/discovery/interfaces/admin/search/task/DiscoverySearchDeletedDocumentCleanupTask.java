package com.thundax.kuzhambu.discovery.interfaces.admin.search.task;

import com.thundax.kuzhambu.discovery.application.search.service.SearchIndexCleanupApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.assembler.DiscoverySearchStatisticsInterfaceAssembler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Lazy(false)
public class DiscoverySearchDeletedDocumentCleanupTask {

    private final SearchIndexCleanupApplicationService searchIndexCleanupApplicationService;
    private final int retentionDays;

    public DiscoverySearchDeletedDocumentCleanupTask(
            SearchIndexCleanupApplicationService searchIndexCleanupApplicationService,
            @Value("${kuzhambu.discovery.search.deleted-cleanup.retention-days:7}") int retentionDays) {
        this.searchIndexCleanupApplicationService = searchIndexCleanupApplicationService;
        this.retentionDays = retentionDays;
    }

    @Scheduled(cron = "${kuzhambu.discovery.search.deleted-cleanup.cron:0 0 3 * * ?}")
    public Integer cleanupDeletedDocuments() {
        return searchIndexCleanupApplicationService.cleanupDeletedDocuments(
                DiscoverySearchStatisticsInterfaceAssembler.toCleanupCommand(retentionDays));
    }
}
