package com.thundax.kuzhambu.storage.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageReferenceOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectContentRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectRepository;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StorageOrphanObjectCleanupSchedulerTest {

    private static final Instant NOW = Instant.parse("2026-06-20T10:00:00Z");

    @Test
    void cleanupShouldDeleteMarkedDeletedUnreferencedObjectAndValidateObjectInfo() {
        FakeRepository repository = new FakeRepository();
        RecordingStore store = new RecordingStore();
        StoredObject expired = storage(1001L, StoredObjectStatus.DELETED, 13);
        repository.objects.add(expired);

        int count = scheduler(repository, store).cleanupExpiredOrphans();

        assertEquals(1, count);
        assertEquals(List.of(expired), store.deletedObjects);
        assertEquals(List.of(StoredObjectIdCodec.toDomain(1001L)), repository.physicalDeletedIds);
    }

    @Test
    void cleanupShouldKeepExpiredActiveUnreferencedObject() {
        FakeRepository repository = new FakeRepository();
        RecordingStore store = new RecordingStore();
        repository.objects.add(storage(1002L, StoredObjectStatus.ACTIVE, 13));

        int count = scheduler(repository, store).cleanupExpiredOrphans();

        assertEquals(0, count);
        assertEquals(List.of(StoredObjectIdCodec.toDomain(1002L)), repository.objectStatusUpdatedIds);
        assertEquals(List.of(), repository.physicalDeletedIds);
        assertEquals(List.of(), store.deletedObjects);
    }

    @Test
    void cleanupShouldKeepUnexpiredObject() {
        FakeRepository repository = new FakeRepository();
        RecordingStore store = new RecordingStore();
        repository.objects.add(storage(1002L, StoredObjectStatus.ACTIVE, 11));

        int count = scheduler(repository, store).cleanupExpiredOrphans();

        assertEquals(0, count);
        assertEquals(List.of(), store.deletedObjects);
        assertEquals(List.of(), repository.physicalDeletedIds);
    }

    @Test
    void cleanupShouldKeepReferencedObject() {
        FakeRepository repository = new FakeRepository();
        RecordingStore store = new RecordingStore();
        repository.objects.add(storage(1003L, StoredObjectStatus.ACTIVE, 13));
        repository.referencedIds.add(1003L);

        int count = scheduler(repository, store).cleanupExpiredOrphans();

        assertEquals(0, count);
        assertEquals(List.of(), store.deletedObjects);
        assertEquals(List.of(), repository.physicalDeletedIds);
        assertEquals(List.of(), repository.objectStatusUpdatedIds);
    }

    @Test
    void cleanupShouldDeleteMarkedDeletedUnreferencedObject() {
        FakeRepository repository = new FakeRepository();
        RecordingStore store = new RecordingStore();
        repository.objects.add(storage(1004L, StoredObjectStatus.DELETED, 13));

        int count = scheduler(repository, store).cleanupExpiredOrphans();

        assertEquals(1, count);
        assertEquals(1, store.deletedObjects.size());
        assertEquals(StoredObjectStatus.DELETED, store.deletedObjects.get(0).getObjectStatus());
        assertEquals(
                StoredObjectReferenceStatus.UNREFERENCED,
                store.deletedObjects.get(0).getReferenceStatus());
        assertEquals(
                StoredObjectIdCodec.toDomain(1004L), store.deletedObjects.get(0).getId());
        assertEquals(List.of(StoredObjectIdCodec.toDomain(1004L)), repository.physicalDeletedIds);
    }

    @Test
    void cleanupShouldKeepReferencedDeletedObject() {
        FakeRepository repository = new FakeRepository();
        RecordingStore store = new RecordingStore();
        repository.objects.add(storage(1005L, StoredObjectStatus.DELETED, 13));
        repository.referencedIds.add(1005L);

        int count = scheduler(repository, store).cleanupExpiredOrphans();

        assertEquals(0, count);
        assertEquals(List.of(), store.deletedObjects);
        assertEquals(List.of(), repository.physicalDeletedIds);
        assertEquals(List.of(), repository.objectStatusUpdatedIds);
    }

    @Test
    void cleanupShouldExposeStoreDeleteFailure() {
        FakeRepository repository = new FakeRepository();
        RecordingStore store = new RecordingStore();
        store.deleteFailure = new IOException("delete failed");
        repository.objects.add(storage(1006L, StoredObjectStatus.DELETED, 13));

        assertThrows(RuntimeException.class, () -> scheduler(repository, store).cleanupExpiredOrphans());
        assertEquals(List.of(), repository.physicalDeletedIds);
    }

    @Test
    void cleanupShouldNotPhysicallyDeleteWhenObjectStatusUpdateDoesNotTakeEffect() {
        FakeRepository repository = new FakeRepository();
        RecordingStore store = new RecordingStore();
        repository.skipObjectStatusMutation = true;
        repository.objects.add(storage(1010L, StoredObjectStatus.ACTIVE, 13));

        int count = scheduler(repository, store).cleanupExpiredOrphans();

        assertEquals(0, count);
        assertEquals(List.of(StoredObjectIdCodec.toDomain(1010L)), repository.objectStatusUpdatedIds);
        assertEquals(List.of(), repository.physicalDeletedIds);
        assertEquals(List.of(), store.deletedObjects);
    }

    private static StorageOrphanObjectCleanupScheduler scheduler(
            StoredObjectRepository repository, StoredObjectContentRepository store) {
        return new StorageOrphanObjectCleanupScheduler(repository, store).useClock(Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void cleanupShouldMarkActiveOrphanAndDeletePreviouslyMarkedOrphan() {
        FakeRepository repository = new FakeRepository();
        RecordingStore store = new RecordingStore();
        repository.objects.add(storage(1007L, StoredObjectStatus.ACTIVE, 13));
        repository.objects.add(storage(1009L, StoredObjectStatus.DELETED, 13));
        repository.objects.add(storage(1008L, StoredObjectStatus.DELETED, 13));
        repository.referencedIds.add(1008L);

        int count = scheduler(repository, store).cleanupExpiredOrphans();

        assertEquals(1, count);
        assertEquals(List.of(StoredObjectIdCodec.toDomain(1007L)), repository.objectStatusUpdatedIds);
        assertEquals(1, store.deletedObjects.size());
        assertEquals(
                StoredObjectIdCodec.toDomain(1009L), store.deletedObjects.get(0).getId());
        assertEquals(StoredObjectStatus.DELETED, store.deletedObjects.get(0).getObjectStatus());
        assertEquals(
                StoredObjectReferenceStatus.UNREFERENCED,
                store.deletedObjects.get(0).getReferenceStatus());
        assertEquals(List.of(StoredObjectIdCodec.toDomain(1009L)), repository.physicalDeletedIds);
    }

    private static StoredObject storage(long id, StoredObjectStatus objectStatus, long storedHoursAgo) {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectIdCodec.toDomain(id));
        storage.setObjectStatus(objectStatus);
        storage.setReferenceStatus(StoredObjectReferenceStatus.UNREFERENCED);
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
        private final List<StoredObjectId> objectStatusUpdatedIds = new ArrayList<>();
        private final Set<Long> referencedIds = new HashSet<>();
        private boolean skipObjectStatusMutation;

        @Override
        public StoredObject getById(StoredObjectId id) {
            return null;
        }

        @Override
        public List<StoredObject> listByIds(List<StoredObjectId> idList) {
            return List.of();
        }

        @Override
        public List<StoredObject> list(
                StorageMimeType mimeType,
                StoredObjectStatus objectStatus,
                StoredObjectReferenceStatus referenceStatus,
                String referenceOwnerId,
                StorageReferenceOwnerType referenceOwnerType,
                String name,
                String remarks,
                SortDirection sortDirection) {
            return List.of();
        }

        @Override
        public PageResult<StoredObject> page(
                StorageMimeType mimeType,
                StoredObjectStatus objectStatus,
                StoredObjectReferenceStatus referenceStatus,
                String referenceOwnerId,
                StorageReferenceOwnerType referenceOwnerType,
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
        public List<StoredObject> listExpiredActiveUnreferenced(Instant storedBefore) {
            return objects.stream()
                    .filter(storage -> StoredObjectStatus.ACTIVE == storage.getObjectStatus())
                    .filter(storage -> storage != null
                            && storage.getId() != null
                            && !referencedIds.contains(storage.getId().value()))
                    .filter(storage -> !storage.getStoredAt().isAfter(storedBefore))
                    .toList();
        }

        @Override
        public List<StoredObject> listExpiredDeletedUnreferenced(Instant storedBefore) {
            return objects.stream()
                    .filter(storage -> StoredObjectStatus.DELETED == storage.getObjectStatus())
                    .filter(storage -> storage != null
                            && storage.getId() != null
                            && !referencedIds.contains(storage.getId().value()))
                    .filter(storage -> !objectStatusUpdatedIds.contains(storage.getId()) || !skipObjectStatusMutation)
                    .filter(storage -> !storage.getStoredAt().isAfter(storedBefore))
                    .toList();
        }

        @Override
        public List<StorageMimeType> listMimeTypes() {
            return List.of();
        }

        @Override
        public int updateObjectStatus(StoredObject storage) {
            objectStatusUpdatedIds.add(storage.getId());
            if (skipObjectStatusMutation) {
                return 0;
            }
            objects.stream()
                    .filter(item -> item.getId() != null && item.getId().equals(storage.getId()))
                    .findFirst()
                    .ifPresent(item -> item.setObjectStatus(storage.getObjectStatus()));
            return 0;
        }

        @Override
        public int updateReferenceStatus(StoredObject storage) {
            return 0;
        }
    }
}
