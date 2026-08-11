package com.thundax.kuzhambu.storage.interfaces.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundaryAspect;
import com.thundax.kuzhambu.common.web.exception.KuzhambuException;
import com.thundax.kuzhambu.common.web.exception.WebErrorCode;
import com.thundax.kuzhambu.storage.application.command.UploadStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.service.StorageContentApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageMultipartUploadApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageObjectApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageUploadApplicationService;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.StorageObjectResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
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
        assertEquals(false, json.has("ownerId"));
        assertEquals(false, json.has("ownerType"));
    }

    @Test
    void uploadShouldRejectEmptyFileAndUnsupportedSuffix() {
        StorageObjectController controller = controller();

        KuzhambuException emptyFileException = assertThrows(
                KuzhambuException.class,
                () -> controller.upload(new InMemoryMultipartFile("empty.png", "image/png", new byte[0]), null, null));
        assertEquals(WebErrorCode.BAD_REQUEST, emptyFileException.getErrorCode());
        assertEquals("无效的参数: 文件不能为空", emptyFileException.getDefaultMessage());

        KuzhambuException unsupportedSuffixException = assertThrows(
                KuzhambuException.class,
                () -> controller.upload(
                        new InMemoryMultipartFile("virus.exe", "application/octet-stream", "x".getBytes()),
                        null,
                        null));
        assertEquals(WebErrorCode.BAD_REQUEST, unsupportedSuffixException.getErrorCode());
        assertEquals("无效的参数: 无效的后缀名", unsupportedSuffixException.getDefaultMessage());
    }

    @Test
    void uploadShouldKeepTechnicalBizExceptionAsSystemError() {
        StorageObjectController controller = controller();

        KuzhambuException exception = assertThrows(
                KuzhambuException.class,
                () -> controller.upload(
                        new InMemoryMultipartFile("technical.png", "image/png", "x".getBytes()), null, null));
        assertEquals(WebErrorCode.SYSTEM_ERROR, exception.getErrorCode());
        assertEquals(BizExceptionBoundaryAspect.TECHNICAL_FAILURE_MESSAGE, exception.getDefaultMessage());
    }

    private static StorageObjectController controller() {
        return new StorageObjectController(
                unused(StorageObjectApplicationService.class),
                unused(StorageContentApplicationService.class),
                new FakeStorageUploadApplicationService(),
                unused(StorageMultipartUploadApplicationService.class));
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

    private static final class FakeStorageUploadApplicationService implements StorageUploadApplicationService {

        @Override
        public StoredObject upload(UploadStorageObjectCommand command) {
            if (command == null
                    || command.inputStream() == null
                    || command.size() == null
                    || command.size().value() <= 0L) {
                throw new BizException("文件不能为空");
            }
            if (!"png".equalsIgnoreCase(extension(command.originalFilename()))) {
                throw new BizException("无效的后缀名");
            }
            if ("technical.png".equals(command.originalFilename())) {
                throw new BizException(
                        BizExceptionBoundaryAspect.TECHNICAL_FAILURE_CODE,
                        BizExceptionBoundaryAspect.TECHNICAL_FAILURE_MESSAGE_KEY,
                        BizExceptionBoundaryAspect.TECHNICAL_FAILURE_MESSAGE);
            }
            assertEquals("sancai.png", command.originalFilename());
            assertEquals("image/png", command.contentType());
            assertEquals(5L, command.size().value());
            assertEquals(StorageOwnerType.USER, command.ownerRef().ownerType());
            assertEquals("user-1", command.ownerRef().ownerId());

            StoredObject storage = new StoredObject();
            storage.setId(StoredObjectIdCodec.toDomain(10L));
            storage.setOriginalFilename(command.originalFilename());
            storage.setContentType(command.contentType());
            storage.setSize(command.size().value());
            storage.setAccessEndpoint("/api/storage/object/10/content");
            return storage;
        }

        private String extension(String filename) {
            if (filename == null || !filename.contains(".")) {
                return "";
            }
            return filename.substring(filename.lastIndexOf('.') + 1);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T unused(Class<T> serviceType) {
        return (T) Proxy.newProxyInstance(
                serviceType.getClassLoader(), new Class<?>[] {serviceType}, (proxy, method, args) -> {
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
