package com.thundax.kuzhambu.storage.application.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectContentRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectRepository;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Lazy(false)
public class StorageOrphanObjectCleanupScheduler {

    private static final Duration ORPHAN_ALIVE_TIME = Duration.ofHours(12);

    private final StoredObjectRepository repository;
    private final StoredObjectContentRepository storedObjectContentRepository;
    private Clock clock = Clock.systemUTC();

    public StorageOrphanObjectCleanupScheduler(
            StoredObjectRepository repository, StoredObjectContentRepository storedObjectContentRepository) {
        this.repository = repository;
        this.storedObjectContentRepository = storedObjectContentRepository;
    }

    StorageOrphanObjectCleanupScheduler useClock(Clock clock) {
        if (clock != null) {
            this.clock = clock;
        }
        return this;
    }

    @Scheduled(cron = "0 0 0/4 * * ?")
    public int cleanupExpiredOrphans() {
        Instant threshold = Instant.now(clock).minus(ORPHAN_ALIVE_TIME);
        List<StoredObject> activeOrphans = repository.listExpiredActiveUnreferenced(threshold);
        Set<StoredObjectId> markedThisRun = new HashSet<>();
        for (StoredObject orphan : activeOrphans) {
            orphan.setObjectStatus(com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus.DELETED);
            repository.updateObjectStatus(orphan);
            markedThisRun.add(orphan.getId());
        }
        List<StoredObject> candidates = repository.listExpiredDeletedUnreferenced(threshold);
        int count = 0;
        for (StoredObject candidate : candidates) {
            if (markedThisRun.contains(candidate.getId())) {
                continue;
            }
            delete(candidate);
            count += repository.physicalDeleteById(candidate.getId());
        }
        return count;
    }

    private void delete(StoredObject storage) {
        try {
            storedObjectContentRepository.delete(storage);
        } catch (IOException exception) {
            StoredObjectId id = storage == null ? null : storage.getId();
            String message = "Storage orphan object delete failed: " + id;
            throw new BizException("STORAGE-90006", "storage.orphan.delete-failed", message, exception);
        }
    }
}
