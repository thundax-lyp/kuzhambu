package com.thundax.kuzhambu.storage.application.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageObjectStatusCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.service.command.CreateStorageCommand;
import com.thundax.kuzhambu.storage.application.service.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.StorageSortCommand;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.application.store.StoredObjectStore;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class StorageUploadStreamHelperTest {

    private static final String ORIGINAL_FILENAME = "render.zip";
    private static final String CONTENT_TYPE = "application/zip";
    private static final byte[] PAYLOAD = "render-html-result".getBytes();

    @Test
    void uploadServerArtifactShouldUploadPayloadAndGenerateStorageObject() {
        FakeStore store = new FakeStore();
        FakeStorageService storageService = new FakeStorageService();
        StorageUploadStreamHelper helper = new StorageUploadStreamHelper(storageService, store);

        StorageUploadResult result = helper.uploadServerArtifact(
                new ByteArrayInputStream(PAYLOAD), ORIGINAL_FILENAME, CONTENT_TYPE, PAYLOAD.length);

        assertNotNull(result);
        assertFalse(result.hasError());
        assertNotNull(result.getStorage());
        assertEquals(CONTENT_TYPE, result.getStorage().getContentType());
        assertEquals(ORIGINAL_FILENAME, result.getStorage().getOriginalFilename());
        assertEquals("zip", result.getStorage().getExtendName());
        assertEquals(PAYLOAD.length, result.getStorage().getSize());
        assertEquals("/api/storage/object/100/content", result.getStorage().getAccessEndpoint());
        assertEquals(1, store.savedStorages.size());
        assertEquals(1, storageService.createCommands.size());
        CreateStorageCommand command = storageService.createCommands.get(0);
        assertEquals(ORIGINAL_FILENAME, command.getOriginalFilename());
        assertEquals(CONTENT_TYPE, command.getContentType());
        assertEquals(PAYLOAD.length, command.getSize());
    }

    @Test
    void uploadShouldRejectInvalidSuffix() {
        StorageUploadStreamHelper helper = new StorageUploadStreamHelper(new FakeStorageService(), new FakeStore());

        StorageUploadResult result = helper.upload(
                new ByteArrayInputStream("x".getBytes()),
                "script.exe",
                "application/octet-stream",
                1L,
                List.of("jpg"),
                StorageOwnerType.USER,
                "u-1");

        assertTrue(result.hasError());
        assertEquals("无效的后缀名", result.getError());
    }

    @Test
    void uploadShouldReturnErrorWhenStoreFails() {
        FakeStore store = new FakeStore();
        store.saveFailure = new IOException("write failed");
        StorageUploadStreamHelper helper = new StorageUploadStreamHelper(new FakeStorageService(), store);

        StorageUploadResult result = helper.upload(
                new ByteArrayInputStream(PAYLOAD),
                ORIGINAL_FILENAME,
                CONTENT_TYPE,
                PAYLOAD.length,
                null,
                StorageOwnerType.USER,
                "u-1");

        assertTrue(result.hasError());
        assertEquals("write failed", result.getError());
    }

    private static final class FakeStorageService implements StorageApplicationService {
        private final List<CreateStorageCommand> createCommands = new ArrayList<>();

        @Override
        public StoredObject get(StoredObjectId id) {
            return null;
        }

        @Override
        public List<StoredObject> list(StorageQuery query) {
            return List.of();
        }

        @Override
        public PageResult<StoredObject> page(StorageQuery query, PageQuery page) {
            return null;
        }

        @Override
        public StoredObjectId create(CreateStorageCommand command) {
            createCommands.add(command);
            return StoredObjectId.of(100L);
        }

        @Override
        public void change(ChangeStorageCommand command) {
            // no-op for test
        }

        @Override
        public int remove(StoredObjectId id) {
            return 0;
        }

        @Override
        public List<String> listMimeTypes(StorageQuery query) {
            return List.of();
        }

        @Override
        public List<String> listReferenceOwnerTypes(StorageQuery query) {
            return List.of();
        }

        @Override
        public int changeObjectStatus(ChangeStorageObjectStatusCommand command) {
            return 0;
        }

        @Override
        public int changeReferenceStatus(ChangeStorageReferenceStatusCommand command) {
            return 0;
        }

        @Override
        public int removeReferences(RemoveStorageReferencesCommand command) {
            return 0;
        }

        @Override
        public void addReferences(AddStorageReferencesCommand command) {
            // no-op for test
        }

        @Override
        public List<StoredObjectReference> listReferences(StorageQuery query) {
            return List.of();
        }

        @Override
        public boolean existsReadableContent(StorageQuery query) {
            return false;
        }

        @Override
        public StoredObjectContent openReadableContent(StoredObjectId id) {
            return null;
        }

        @Override
        public void sort(StorageSortCommand command) {
            // no-op for test
        }
    }

    private static final class FakeStore implements StoredObjectStore {
        private final List<StoredObject> savedStorages = new ArrayList<>();
        private IOException saveFailure;

        @Override
        public StoredObject save(StoredObject storage, InputStream inputStream) throws IOException {
            if (saveFailure != null) {
                throw saveFailure;
            }
            byte[] bytes = inputStream.readAllBytes();
            storage.setBucketName("local");
            storage.setObjectKey("artifact/" + storage.getOriginalFilename());
            storage.setSize((long) bytes.length);
            storage.setAccessEndpoint("/api/storage/object/100/content");
            storage.setObjectStatus(StoredObjectStatus.ACTIVE);
            storage.setReferenceStatus(StoredObjectReferenceStatus.REFERENCED);
            storage.setId(StoredObjectId.of(100L));
            savedStorages.add(storage);
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
        public void delete(StoredObject storage) {
            // no-op for test
        }
    }
}
