package com.thundax.kuzhambu.storage.application.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.application.store.StoredObjectStore;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectRepository;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Lazy(false)
public class StorageOrphanObjectCleanupScheduler {

    private static final Duration ORPHAN_ALIVE_TIME = Duration.ofHours(12);

    private final StoredObjectRepository repository;
    private final StoredObjectStore storedObjectStore;
    private final Clock clock;

    @Autowired
    public StorageOrphanObjectCleanupScheduler(StoredObjectRepository repository, StoredObjectStore storedObjectStore) {
        this(repository, storedObjectStore, Clock.systemUTC());
    }

    StorageOrphanObjectCleanupScheduler(
            StoredObjectRepository repository, StoredObjectStore storedObjectStore, Clock clock) {
        this.repository = repository;
        this.storedObjectStore = storedObjectStore;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 0/4 * * ?")
    public int cleanupExpiredOrphans() {
        Instant threshold = Instant.now(clock).minus(ORPHAN_ALIVE_TIME);
        List<StoredObject> candidates = repository.listExpiredUnreferencedActive(threshold);
        int count = 0;
        for (StoredObject candidate : candidates) {
            delete(candidate);
            count += repository.physicalDeleteById(candidate.getId());
        }
        return count;
    }

    private void delete(StoredObject storage) {
        try {
            storedObjectStore.delete(storage);
        } catch (IOException exception) {
            StoredObjectId id = storage == null ? null : storage.getId();
            String message = "Storage orphan object delete failed: " + id;
            throw new BizException("STORAGE-90006", "storage.orphan.delete-failed", message, exception);
        }
    }
}
