package com.thundax.kuzhambu.storage.interfaces.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageObjectStatusCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.service.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.StorageSortCommand;
import com.thundax.kuzhambu.storage.application.service.command.UploadStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.application.service.result.StorageUploadResult;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.StorageObjectResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

class StorageObjectUploadContractTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void uploadRouteShouldKeepMultipartContract() throws Exception {
        PostMapping methodMapping = StorageObjectController.class
                .getDeclaredMethod("upload", MultipartFile.class, String.class, String.class)
                .getAnnotation(PostMapping.class);
        assertEquals("upload", methodMapping.value()[0]);
        assertEquals(MediaType.MULTIPART_FORM_DATA_VALUE, methodMapping.consumes()[0]);
    }

    @Test
    void uploadShouldReturnStorageObjectResponseCoreFields() throws Exception {
        StorageObjectController controller = controller();

        StorageObjectResponse response = controller.upload(
                new InMemoryMultipartFile("sancai.png", "image/png", "image".getBytes()), "USER", "user-1");

        JsonNode json = OBJECT_MAPPER.valueToTree(response);
        assertEquals("10", json.get("id").asText());
        assertEquals("sancai.png", json.get("originalFilename").asText());
        assertEquals("image/png", json.get("contentType").asText());
        assertEquals(5L, json.get("size").asLong());
        assertEquals(
                "/api/storage/object/10/content", json.get("accessEndpoint").asText());
        assertEquals(false, json.has("ownerId"));
        assertEquals(false, json.has("ownerType"));
    }

    @Test
    void uploadShouldRejectEmptyFileAndUnsupportedSuffix() {
        StorageObjectController controller = controller();

        assertThrows(
                RuntimeException.class,
                () -> controller.upload(new InMemoryMultipartFile("empty.png", "image/png", new byte[0]), null, null));
        assertThrows(
                RuntimeException.class,
                () -> controller.upload(
                        new InMemoryMultipartFile("virus.exe", "application/octet-stream", "x".getBytes()),
                        null,
                        null));
    }

    private static StorageObjectController controller() {
        return new StorageObjectController(new FakeStorageApplicationService());
    }

    private static final class InMemoryMultipartFile implements MultipartFile {

        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        private InMemoryMultipartFile(String originalFilename, String contentType, byte[] content) {
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) {
            throw new UnsupportedOperationException("transferTo");
        }
    }

    private static final class FakeStorageApplicationService implements StorageApplicationService {

        @Override
        public StorageUploadResult upload(UploadStorageObjectCommand command) {
            if (command == null || command.getInputStream() == null || command.getSize() <= 0L) {
                return StorageUploadResult.builder().error("文件不能为空").build();
            }
            if (!"png".equalsIgnoreCase(extension(command.getOriginalFilename()))) {
                return StorageUploadResult.builder().error("无效的后缀名").build();
            }
            assertEquals("sancai.png", command.getOriginalFilename());
            assertEquals("image/png", command.getContentType());
            assertEquals(5L, command.getSize());
            assertEquals(StorageOwnerType.USER, command.getOwnerType());
            assertEquals("user-1", command.getOwnerId());

            StoredObject storage = new StoredObject();
            storage.setId(StoredObjectId.of(10L));
            storage.setOriginalFilename(command.getOriginalFilename());
            storage.setContentType(command.getContentType());
            storage.setSize(command.getSize());
            storage.setAccessEndpoint("/api/storage/object/10/content");
            return StorageUploadResult.builder().storage(storage).build();
        }

        @Override
        public StoredObject get(StoredObjectId id) {
            throw new UnsupportedOperationException("get");
        }

        @Override
        public List<StoredObject> list(StorageQuery query) {
            throw new UnsupportedOperationException("list");
        }

        @Override
        public com.thundax.kuzhambu.common.core.page.PageResult<StoredObject> page(
                StorageQuery query, com.thundax.kuzhambu.common.core.page.PageQuery page) {
            throw new UnsupportedOperationException("page");
        }

        @Override
        public StoredObjectId create(
                com.thundax.kuzhambu.storage.application.service.command.CreateStorageCommand command) {
            throw new UnsupportedOperationException("create");
        }

        @Override
        public void change(ChangeStorageCommand command) {
            throw new UnsupportedOperationException("change");
        }

        @Override
        public int remove(StoredObjectId id) {
            throw new UnsupportedOperationException("remove");
        }

        @Override
        public List<String> listMimeTypes(StorageQuery query) {
            throw new UnsupportedOperationException("listMimeTypes");
        }

        @Override
        public List<String> listReferenceOwnerTypes(StorageQuery query) {
            throw new UnsupportedOperationException("listReferenceOwnerTypes");
        }

        @Override
        public int changeObjectStatus(ChangeStorageObjectStatusCommand command) {
            throw new UnsupportedOperationException("changeObjectStatus");
        }

        @Override
        public int changeReferenceStatus(ChangeStorageReferenceStatusCommand command) {
            throw new UnsupportedOperationException("changeReferenceStatus");
        }

        @Override
        public int removeReferences(RemoveStorageReferencesCommand command) {
            throw new UnsupportedOperationException("removeReferences");
        }

        @Override
        public void addReferences(AddStorageReferencesCommand command) {
            throw new UnsupportedOperationException("addReferences");
        }

        @Override
        public List<StoredObjectReference> listReferences(StorageQuery query) {
            throw new UnsupportedOperationException("listReferences");
        }

        @Override
        public boolean existsReadableContent(StorageQuery query) {
            throw new UnsupportedOperationException("existsReadableContent");
        }

        @Override
        public StoredObjectContent openReadableContent(StoredObjectId id) {
            throw new UnsupportedOperationException("openReadableContent");
        }

        @Override
        public void sort(StorageSortCommand command) {
            throw new UnsupportedOperationException("sort");
        }

        private String extension(String filename) {
            if (filename == null || !filename.contains(".")) {
                return "";
            }
            return filename.substring(filename.lastIndexOf('.') + 1);
        }
    }
}
