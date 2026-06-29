package com.thundax.kuzhambu.storage.interfaces.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.storage.application.service.MultipartUploadApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.command.InitMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.service.command.UploadMultipartPartCommand;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadPart;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.domain.object.model.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.InitMultipartUploadRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.UploadMultipartPartRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.InitMultipartUploadResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.UploadMultipartPartResponse;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

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
        StorageObjectController controller =
                new StorageObjectController(storageService(), multipartUploadApplicationServiceForInitiate(commandRef));

        InitMultipartUploadRequest request = new InitMultipartUploadRequest();
        request.setUploadId("upload-1");
        request.setOwnerId("owner-1");
        request.setOwnerType("USER");
        request.setBusinessType("bizA");
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
        assertEquals("upload-1", command.getUploadId());
        assertEquals("owner-1", command.getOwnerId());
        assertEquals(StorageOwnerType.USER, command.getOwnerType());
        assertEquals("bizA", command.getBusinessType());
        assertEquals("sancai.png", command.getOriginalFilename());
        assertEquals("image/png", command.getMimeType());
        assertEquals("bucket-a", command.getBucketName());
        assertEquals("path/sancai", command.getObjectKey());
        assertEquals("provider-1", command.getProviderUploadId());
        assertEquals(100L, command.getTotalSize());
        assertEquals(5L, command.getPartSize());

        assertEquals("upload-1", json.get("uploadId").asText());
        assertEquals("provider-1", json.get("providerUploadId").asText());
        assertEquals("USER", json.get("ownerType").asText());
        assertEquals("owner-1", json.get("ownerId").asText());
        assertEquals("bizA", json.get("businessType").asText());
        assertEquals("sancai.png", json.get("originalFilename").asText());
        assertEquals("image/png", json.get("mimeType").asText());
        assertEquals("bucket-a", json.get("bucketName").asText());
        assertEquals("path/sancai", json.get("objectKey").asText());
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
                .getDeclaredMethod("uploadPart", UploadMultipartPartRequest.class)
                .getAnnotation(PostMapping.class);
        assertEquals("multipart/uploadPart", methodMapping.value()[0]);
    }

    @Test
    void uploadPartShouldMapRequestAndKeepResponseContract() throws Exception {
        AtomicReference<UploadMultipartPartCommand> commandRef = new AtomicReference<>();
        StorageObjectController controller = new StorageObjectController(
                storageService(), multipartUploadApplicationServiceForUploadPart(commandRef));

        UploadMultipartPartRequest request = new UploadMultipartPartRequest();
        request.setUploadId("upload-1");
        request.setPartNumber(2);
        request.setEtag("etag-2");
        request.setSize(16L);

        UploadMultipartPartResponse response = controller.uploadPart(request);
        JsonNode json = OBJECT_MAPPER.valueToTree(response);

        UploadMultipartPartCommand command = commandRef.get();
        assertEquals("upload-1", command.getUploadId());
        assertEquals(2, command.getPartNumber());
        assertEquals("etag-2", command.getEtag());
        assertEquals(16L, command.getSize());

        assertEquals("upload-1", json.get("uploadId").asText());
        assertEquals(2, json.get("partNumber").asInt());
        assertEquals("etag-2", json.get("etag").asText());
        assertEquals(16L, json.get("size").asLong());
        assertNull(json.get("uploadStatus"));
    }

    private static StorageApplicationService storageService() {
        return (StorageApplicationService) Proxy.newProxyInstance(
                StorageApplicationService.class.getClassLoader(),
                new Class<?>[] {StorageApplicationService.class},
                (proxy, method, args) -> {
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static MultipartUploadApplicationService multipartUploadApplicationServiceForInitiate(
            AtomicReference<InitMultipartUploadCommand> commandRef) {
        return (MultipartUploadApplicationService) Proxy.newProxyInstance(
                MultipartUploadApplicationService.class.getClassLoader(),
                new Class<?>[] {MultipartUploadApplicationService.class},
                (proxy, method, args) -> {
                    if ("init".equals(method.getName())) {
                        InitMultipartUploadCommand command = (InitMultipartUploadCommand) args[0];
                        commandRef.set(command);
                        MultipartUploadSession session = new MultipartUploadSession();
                        session.setUploadId(command.getUploadId());
                        session.setProviderUploadId(command.getProviderUploadId());
                        session.setOwnerType(command.getOwnerType());
                        session.setOwnerId(command.getOwnerId());
                        session.setBusinessType(command.getBusinessType());
                        session.setOriginalFilename(command.getOriginalFilename());
                        session.setMimeType(command.getMimeType());
                        session.setBucketName(command.getBucketName());
                        session.setObjectKey(command.getObjectKey());
                        session.setTotalSize(command.getTotalSize());
                        session.setPartSize(command.getPartSize());
                        return session;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static MultipartUploadApplicationService multipartUploadApplicationServiceForUploadPart(
            AtomicReference<UploadMultipartPartCommand> commandRef) {
        return (MultipartUploadApplicationService) Proxy.newProxyInstance(
                MultipartUploadApplicationService.class.getClassLoader(),
                new Class<?>[] {MultipartUploadApplicationService.class},
                (proxy, method, args) -> {
                    if ("uploadPart".equals(method.getName())) {
                        UploadMultipartPartCommand command = (UploadMultipartPartCommand) args[0];
                        commandRef.set(command);
                        MultipartUploadPart part = new MultipartUploadPart();
                        part.setUploadId(command.getUploadId());
                        part.setPartNumber(command.getPartNumber());
                        part.setEtag(command.getEtag());
                        part.setSize(command.getSize());
                        return part;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
