package com.thundax.kuzhambu.storage.interfaces.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadStreamHelper;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.command.CreateStorageCommand;
import com.thundax.kuzhambu.storage.application.store.StoredObjectStore;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.StorageObjectResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
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
        StorageApplicationService service = storageApplicationService();
        return new StorageObjectController(
                service, new StorageUploadStreamHelper(service, new MemoryStoredObjectStore()));
    }

    private static StorageApplicationService storageApplicationService() {
        return (StorageApplicationService) Proxy.newProxyInstance(
                StorageApplicationService.class.getClassLoader(),
                new Class<?>[] {StorageApplicationService.class},
                (proxy, method, args) -> {
                    if ("create".equals(method.getName())) {
                        CreateStorageCommand command = (CreateStorageCommand) args[0];
                        assertEquals("sancai.png", command.getOriginalFilename());
                        assertEquals("image/png", command.getContentType());
                        assertEquals(5L, command.getSize());
                        return StoredObjectId.of(10L);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static final class MemoryStoredObjectStore implements StoredObjectStore {

        @Override
        public StoredObject save(StoredObject storage, InputStream inputStream) throws IOException {
            StoredObject storedObject = new StoredObject();
            storedObject.setBucketName("local");
            storedObject.setObjectKey("202606/sancai.png");
            storedObject.setSize((long) inputStream.readAllBytes().length);
            return storedObject;
        }

        @Override
        public boolean exists(StoredObject storage) {
            return false;
        }

        @Override
        public InputStream open(StoredObject storage) {
            return InputStream.nullInputStream();
        }
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
}
