package com.thundax.kuzhambu.storage.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectContentRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectRepository;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class StorageOrphanObjectCleanupSchedulerTest {

    private static final Instant NOW = Instant.parse("2026-06-20T10:00:00Z");

    @Test
    void cleanupShouldRemoveExpiredUnreferencedActiveObject() {
        FakeRepository repository = new FakeRepository();
        RecordingStore store = new RecordingStore();
        StoredObject expired = storage(1001L, StoredObjectStatus.ACTIVE, StoredObjectReferenceStatus.UNREFERENCED, 13);
        repository.objects.add(expired);

        int count = scheduler(repository, store).cleanupExpiredOrphans();

        assertEquals(1, count);
        assertEquals(List.of(expired), store.deletedObjects);
        assertEquals(List.of(StoredObjectId.of(1001L)), repository.physicalDeletedIds);
    }

    @Test
    void cleanupShouldKeepUnexpiredObject() {
        FakeRepository repository = new FakeRepository();
        RecordingStore store = new RecordingStore();
        repository.objects.add(storage(1002L, StoredObjectStatus.ACTIVE, StoredObjectReferenceStatus.UNREFERENCED, 11));

        int count = scheduler(repository, store).cleanupExpiredOrphans();

        assertEquals(0, count);
        assertEquals(List.of(), store.deletedObjects);
        assertEquals(List.of(), repository.physicalDeletedIds);
    }

    @Test
    void cleanupShouldKeepReferencedObject() {
        FakeRepository repository = new FakeRepository();
        RecordingStore store = new RecordingStore();
        repository.objects.add(storage(1003L, StoredObjectStatus.ACTIVE, StoredObjectReferenceStatus.REFERENCED, 13));

        int count = scheduler(repository, store).cleanupExpiredOrphans();

        assertEquals(0, count);
        assertEquals(List.of(), store.deletedObjects);
        assertEquals(List.of(), repository.physicalDeletedIds);
    }

    @Test
    void cleanupShouldKeepNonActiveObject() {
        FakeRepository repository = new FakeRepository();
        RecordingStore store = new RecordingStore();
        repository.objects.add(
                storage(1004L, StoredObjectStatus.DELETED, StoredObjectReferenceStatus.UNREFERENCED, 13));

        int count = scheduler(repository, store).cleanupExpiredOrphans();

        assertEquals(0, count);
        assertEquals(List.of(), store.deletedObjects);
        assertEquals(List.of(), repository.physicalDeletedIds);
    }

    @Test
    void cleanupShouldExposeStoreDeleteFailure() {
        FakeRepository repository = new FakeRepository();
        RecordingStore store = new RecordingStore();
        store.deleteFailure = new IOException("delete failed");
        repository.objects.add(storage(1005L, StoredObjectStatus.ACTIVE, StoredObjectReferenceStatus.UNREFERENCED, 13));

        assertThrows(RuntimeException.class, () -> scheduler(repository, store).cleanupExpiredOrphans());
        assertEquals(List.of(), repository.physicalDeletedIds);
    }

    private static StorageOrphanObjectCleanupScheduler scheduler(
            StoredObjectRepository repository, StoredObjectContentRepository store) {
        return new StorageOrphanObjectCleanupScheduler(repository, store).useClock(Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static StoredObject storage(
            long id,
            StoredObjectStatus objectStatus,
            StoredObjectReferenceStatus referenceStatus,
            long storedHoursAgo) {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectId.of(id));
        storage.setObjectStatus(objectStatus);
        storage.setReferenceStatus(referenceStatus);
        storage.setStoredAt(NOW.minusSeconds(storedHoursAgo * 60 * 60));
        return storage;
    }

    private static final class RecordingStore implements StoredObjectContentRepository {
        private final List<StoredObject> deletedObjects = new ArrayList<>();
        private IOException deleteFailure;

        @Override
        public StoredObject save(StoredObject storage, InputStream inputStream) {
            return storage;
        }

        @Override
        public boolean exists(StoredObject storage) {
            return true;
        }

        @Override
        public InputStream open(StoredObject storage) {
            return InputStream.nullInputStream();
        }

        @Override
        public void delete(StoredObject storage) throws IOException {
            if (deleteFailure != null) {
                throw deleteFailure;
            }
            deletedObjects.add(storage);
        }
    }

    private static final class FakeRepository implements StoredObjectRepository {
        private final List<StoredObject> objects = new ArrayList<>();
        private final List<StoredObjectId> physicalDeletedIds = new ArrayList<>();

        @Override
        public StoredObject getById(StoredObjectId id) {
            return null;
        }

        @Override
        public List<StoredObject> listByIds(List<Long> idList) {
            return List.of();
        }

        @Override
        public List<StoredObject> list(
                String mimeType,
                String ownerId,
                String ownerType,
                String objectStatus,
                String referenceStatus,
                String referenceOwnerId,
                String referenceOwnerType,
                String name,
                String remarks,
                SortDirection sortDirection) {
            return List.of();
        }

        @Override
        public PageResult<StoredObject> page(
                String mimeType,
                String ownerId,
                String ownerType,
                String objectStatus,
                String referenceStatus,
                String referenceOwnerId,
                String referenceOwnerType,
                String name,
                String remarks,
                SortDirection sortDirection,
                int pageNo,
                int pageSize) {
            return new PageResult<>();
        }

        @Override
        public StoredObjectId insert(StoredObject entity) {
            return entity == null ? null : entity.getId();
        }

        @Override
        public int update(StoredObject entity) {
            return 0;
        }

        @Override
        public int maxPriority() {
            return 0;
        }

        @Override
        public int updatePriority(StoredObjectId id, int priority) {
            return 0;
        }

        @Override
        public int deleteById(StoredObjectId id) {
            return 0;
        }

        @Override
        public int physicalDeleteById(StoredObjectId id) {
            physicalDeletedIds.add(id);
            return 1;
        }

        @Override
        public List<StoredObject> listExpiredUnreferencedActive(Instant storedBefore) {
            return objects.stream()
                    .filter(storage -> StoredObjectStatus.ACTIVE == storage.getObjectStatus())
                    .filter(storage -> StoredObjectReferenceStatus.UNREFERENCED == storage.getReferenceStatus())
                    .filter(storage -> !storage.getStoredAt().isAfter(storedBefore))
                    .toList();
        }

        @Override
        public List<String> listMimeTypes() {
            return List.of();
        }

        @Override
        public int updateObjectStatus(StoredObject storage) {
            return 0;
        }

        @Override
        public int updateReferenceStatus(StoredObject storage) {
            return 0;
        }
    }
}
