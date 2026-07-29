package com.thundax.kuzhambu.storage.interfaces.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.storage.application.command.AbortMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.command.CompleteMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.command.InitMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.command.UploadMultipartPartCommand;
import com.thundax.kuzhambu.storage.application.service.StorageContentApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageMultipartUploadApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageObjectApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageUploadApplicationService;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadPart;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.AbortMultipartUploadRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.CompleteMultipartUploadRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.InitMultipartUploadRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.UploadMultipartPartRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.AbortMultipartUploadResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.CompleteMultipartUploadResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.InitMultipartUploadResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.UploadMultipartPartResponse;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

class StorageObjectMultipartUploadContractTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void initiateRouteShouldKeepMultipartContract() throws Exception {
        PostMapping methodMapping = StorageObjectController.class
                .getDeclaredMethod("initiate", InitMultipartUploadRequest.class)
                .getAnnotation(PostMapping.class);
        assertEquals("multipart/initiate", methodMapping.value()[0]);
    }

    @Test
    void initiateShouldMapRequestAndKeepResponseContract() throws Exception {
        AtomicReference<InitMultipartUploadCommand> commandRef = new AtomicReference<>();
        StorageObjectController controller = controller(multipartUploadApplicationServiceForInitiate(commandRef));

        InitMultipartUploadRequest request = new InitMultipartUploadRequest();
        request.setUploadId("upload-1");
        request.setBusinessType("bizA");
        request.setOwnerType(StorageOwnerType.USER.value());
        request.setOwnerId("owner-1");
        request.setOriginalFilename("sancai.png");
        request.setMimeType("image/png");
        request.setBucketName("bucket-a");
        request.setObjectKey("path/sancai");
        request.setProviderUploadId("provider-1");
        request.setTotalSize(100L);
        request.setPartSize(5L);

        InitMultipartUploadResponse response = controller.initiate(request);
        JsonNode json = OBJECT_MAPPER.valueToTree(response);

        InitMultipartUploadCommand command = commandRef.get();
        assertEquals("upload-1", command.getUploadId().value());
        assertEquals("bizA", command.getBusinessType());
        assertEquals(StorageOwnerType.USER, command.getOwnerRef().ownerType());
        assertEquals("owner-1", command.getOwnerRef().ownerId());
        assertEquals("sancai.png", command.getOriginalFilename());
        assertEquals("image/png", command.getMimeType().value());
        assertNull(command.getBucketName());
        assertNull(command.getObjectKey());
        assertNull(command.getProviderUploadId());
        assertEquals(100L, command.getTotalSize().value());
        assertEquals(5L, command.getPartSize().value());

        assertEquals("upload-1", json.get("uploadId").asText());
        assertEquals(false, json.has("providerUploadId"));
        assertEquals(false, json.has("ownerType"));
        assertEquals(false, json.has("ownerId"));
        assertEquals("bizA", json.get("businessType").asText());
        assertEquals("sancai.png", json.get("originalFilename").asText());
        assertEquals("image/png", json.get("mimeType").asText());
        assertEquals(false, json.has("bucketName"));
        assertEquals(false, json.has("objectKey"));
        assertEquals(100L, json.get("totalSize").asLong());
        assertEquals(5L, json.get("partSize").asLong());
        assertEquals(0, json.get("uploadedPartCount").asInt());
        assertEquals(
                MultipartUploadStatus.INITIATED.value(),
                json.get("uploadStatus").asText());
    }

    @Test
    void uploadPartRouteShouldKeepMultipartContract() throws Exception {
        PostMapping methodMapping = StorageObjectController.class
                .getDeclaredMethod("uploadPart", UploadMultipartPartRequest.class, MultipartFile.class)
                .getAnnotation(PostMapping.class);
        assertEquals("multipart/uploadPart", methodMapping.value()[0]);
    }

    @Test
    void uploadPartShouldMapRequestAndKeepResponseContract() throws Exception {
        AtomicReference<UploadMultipartPartCommand> commandRef = new AtomicReference<>();
        StorageObjectController controller = controller(multipartUploadApplicationServiceForUploadPart(commandRef));

        UploadMultipartPartRequest request = new UploadMultipartPartRequest();
        request.setUploadId("upload-1");
        request.setPartNumber(2);
        request.setEtag("etag-2");
        request.setSize(16L);
        MultipartFile file =
                new MockMultipartFile("file", "part-2.bin", "application/octet-stream", new byte[] {0x31, 0x32});

        UploadMultipartPartResponse response = controller.uploadPart(request, file);
        JsonNode json = OBJECT_MAPPER.valueToTree(response);

        UploadMultipartPartCommand command = commandRef.get();
        assertEquals("upload-1", command.getUploadId().value());
        assertEquals(2, command.getPartNumber().value());
        assertEquals("etag-2", command.getEtag());
        assertEquals(16L, command.getSize().value());
        assertNotNull(command.getInputStream());
        assertTrue(command.getInputStream().available() > 0);

        assertEquals("upload-1", json.get("uploadId").asText());
        assertEquals(2, json.get("partNumber").asInt());
        assertEquals("etag-2", json.get("etag").asText());
        assertEquals(16L, json.get("size").asLong());
        assertNull(json.get("uploadStatus"));
    }

    @Test
    void completeRouteShouldKeepMultipartContract() throws Exception {
        PostMapping methodMapping = StorageObjectController.class
                .getDeclaredMethod("complete", CompleteMultipartUploadRequest.class)
                .getAnnotation(PostMapping.class);
        assertEquals("multipart/complete", methodMapping.value()[0]);
    }

    @Test
    void completeShouldMapRequestAndKeepResponseContract() throws Exception {
        AtomicReference<CompleteMultipartUploadCommand> commandRef = new AtomicReference<>();
        StorageObjectController controller = controller(multipartUploadApplicationServiceForComplete(commandRef));

        CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest();
        request.setUploadId("upload-2");
        request.setBucketName("bucket-b");
        request.setObjectKey("folder/sancai.bin");
        request.setSize(120L);
        request.setAccessEndpoint("/api/storage/object/22/content");

        CompleteMultipartUploadResponse response = controller.complete(request);
        JsonNode json = OBJECT_MAPPER.valueToTree(response);

        CompleteMultipartUploadCommand command = commandRef.get();
        assertEquals("upload-2", command.getUploadId().value());
        assertNull(command.getBucketName());
        assertNull(command.getObjectKey());
        assertNull(command.getSize());
        assertNull(command.getAccessEndpoint());

        assertEquals("22", json.get("id").asText());
        assertEquals("upload-2", json.get("uploadId").asText());
        assertEquals(false, json.has("ownerType"));
        assertEquals(false, json.has("ownerId"));
        assertEquals("sancai.bin", json.get("originalFilename").asText());
        assertEquals("application/octet-stream", json.get("mimeType").asText());
        assertEquals("bucket-b", json.get("bucketName").asText());
        assertEquals("folder/sancai.bin", json.get("objectKey").asText());
        assertEquals(120L, json.get("size").asLong());
        assertEquals(
                "/api/storage/object/22/content", json.get("accessEndpoint").asText());
        assertEquals(StoredObjectStatus.ACTIVE.value(), json.get("objectStatus").asText());
        assertEquals(
                StoredObjectReferenceStatus.UNREFERENCED.value(),
                json.get("referenceStatus").asText());
    }

    @Test
    void abortRouteShouldKeepMultipartContract() throws Exception {
        PostMapping methodMapping = StorageObjectController.class
                .getDeclaredMethod("abort", AbortMultipartUploadRequest.class)
                .getAnnotation(PostMapping.class);
        assertEquals("multipart/abort", methodMapping.value()[0]);
    }

    @Test
    void abortShouldMapRequestAndKeepResponseContract() throws Exception {
        AtomicReference<AbortMultipartUploadCommand> commandRef = new AtomicReference<>();
        StorageObjectController controller = controller(multipartUploadApplicationServiceForAbort(commandRef));

        AbortMultipartUploadRequest request = new AbortMultipartUploadRequest();
        request.setUploadId("upload-3");

        AbortMultipartUploadResponse response = controller.abort(request);
        JsonNode json = OBJECT_MAPPER.valueToTree(response);

        AbortMultipartUploadCommand command = commandRef.get();
        assertEquals("upload-3", command.getUploadId().value());

        assertEquals("upload-3", json.get("uploadId").asText());
        assertEquals(
                MultipartUploadStatus.ABORTED.value(), json.get("uploadStatus").asText());
    }

    private static StorageObjectController controller(
            StorageMultipartUploadApplicationService multipartUploadApplicationService) {
        return new StorageObjectController(
                unused(StorageObjectApplicationService.class),
                unused(StorageContentApplicationService.class),
                unused(StorageUploadApplicationService.class),
                multipartUploadApplicationService);
    }

    private static StorageMultipartUploadApplicationService multipartUploadApplicationServiceForInitiate(
            AtomicReference<InitMultipartUploadCommand> commandRef) {
        return (StorageMultipartUploadApplicationService) Proxy.newProxyInstance(
                StorageMultipartUploadApplicationService.class.getClassLoader(),
                new Class<?>[] {StorageMultipartUploadApplicationService.class},
                (proxy, method, args) -> {
                    if ("init".equals(method.getName())) {
                        InitMultipartUploadCommand command = (InitMultipartUploadCommand) args[0];
                        commandRef.set(command);
                        MultipartUploadSession session = new MultipartUploadSession();
                        session.setUploadId(command.getUploadId().value());
                        session.setProviderUploadId(
                                command.getProviderUploadId() == null
                                        ? null
                                        : command.getProviderUploadId().value());
                        session.setBusinessType(command.getBusinessType());
                        session.setOriginalFilename(command.getOriginalFilename());
                        session.setMimeType(command.getMimeType().value());
                        session.setBucketName(
                                command.getBucketName() == null
                                        ? null
                                        : command.getBucketName().value());
                        session.setObjectKey(
                                command.getObjectKey() == null
                                        ? null
                                        : command.getObjectKey().value());
                        session.setTotalSize(command.getTotalSize().value());
                        session.setPartSize(command.getPartSize().value());
                        return session;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static StorageMultipartUploadApplicationService multipartUploadApplicationServiceForUploadPart(
            AtomicReference<UploadMultipartPartCommand> commandRef) {
        return (StorageMultipartUploadApplicationService) Proxy.newProxyInstance(
                StorageMultipartUploadApplicationService.class.getClassLoader(),
                new Class<?>[] {StorageMultipartUploadApplicationService.class},
                (proxy, method, args) -> {
                    if ("uploadPart".equals(method.getName())) {
                        UploadMultipartPartCommand command = (UploadMultipartPartCommand) args[0];
                        commandRef.set(command);
                        MultipartUploadPart part = new MultipartUploadPart();
                        part.setUploadId(command.getUploadId().value());
                        part.setPartNumber(command.getPartNumber().value());
                        part.setEtag(command.getEtag());
                        part.setSize(command.getSize().value());
                        return part;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static StorageMultipartUploadApplicationService multipartUploadApplicationServiceForComplete(
            AtomicReference<CompleteMultipartUploadCommand> commandRef) {
        return (StorageMultipartUploadApplicationService) Proxy.newProxyInstance(
                StorageMultipartUploadApplicationService.class.getClassLoader(),
                new Class<?>[] {StorageMultipartUploadApplicationService.class},
                (proxy, method, args) -> {
                    if ("complete".equals(method.getName())) {
                        CompleteMultipartUploadCommand command = (CompleteMultipartUploadCommand) args[0];
                        commandRef.set(command);
                        StoredObject storage = new StoredObject();
                        storage.setId(StoredObjectIdCodec.toDomain(22L));
                        storage.setOriginalFilename("sancai.bin");
                        storage.setMimeType("application/octet-stream");
                        storage.setBucketName("bucket-b");
                        storage.setObjectKey("folder/sancai.bin");
                        storage.setSize(120L);
                        storage.setAccessEndpoint("/api/storage/object/22/content");
                        storage.setObjectStatus(StoredObjectStatus.ACTIVE);
                        storage.setReferenceStatus(StoredObjectReferenceStatus.UNREFERENCED);
                        return storage;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static StorageMultipartUploadApplicationService multipartUploadApplicationServiceForAbort(
            AtomicReference<AbortMultipartUploadCommand> commandRef) {
        return (StorageMultipartUploadApplicationService) Proxy.newProxyInstance(
                StorageMultipartUploadApplicationService.class.getClassLoader(),
                new Class<?>[] {StorageMultipartUploadApplicationService.class},
                (proxy, method, args) -> {
                    if ("abort".equals(method.getName())) {
                        AbortMultipartUploadCommand command = (AbortMultipartUploadCommand) args[0];
                        commandRef.set(command);
                        return 1;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    @SuppressWarnings("unchecked")
    private static <T> T unused(Class<T> serviceType) {
        return (T) Proxy.newProxyInstance(
                serviceType.getClassLoader(), new Class<?>[] {serviceType}, (proxy, method, args) -> {
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
