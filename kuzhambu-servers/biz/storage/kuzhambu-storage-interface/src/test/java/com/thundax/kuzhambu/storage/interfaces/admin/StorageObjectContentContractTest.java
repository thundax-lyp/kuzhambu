package com.thundax.kuzhambu.storage.interfaces.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.application.query.OpenReadableStorageContentQuery;
import com.thundax.kuzhambu.storage.application.result.StoredObjectContentResult;
import com.thundax.kuzhambu.storage.application.service.StorageContentApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageMultipartUploadApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageObjectApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageUploadApplicationService;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;

class StorageObjectContentContractTest {

    @Test
    void contentRouteShouldKeepReadableContentPath() throws Exception {
        GetMapping methodMapping = StorageObjectController.class
                .getDeclaredMethod(
                        "content", String.class, Boolean.class, jakarta.servlet.http.HttpServletResponse.class)
                .getAnnotation(GetMapping.class);

        assertEquals("{id}/content", methodMapping.value()[0]);
    }

    @Test
    void contentShouldWriteObjectBytesAndHeaders() throws Exception {
        StorageObjectController controller = controller();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.content("10", false, response);

        assertEquals("image/png", response.getContentType());
        assertEquals(11, response.getContentLength());
        assertTrue(response.getHeader("Content-Disposition").startsWith("inline;"));
        assertTrue(response.getHeader("Content-Disposition").contains("sancai.png"));
        assertEquals("image-bytes", response.getContentAsString());
    }

    @Test
    void contentShouldSupportDownloadDisposition() throws Exception {
        StorageObjectController controller = controller();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.content("10", true, response);

        assertTrue(response.getHeader("Content-Disposition").startsWith("attachment;"));
    }

    @Test
    void contentShouldWriteRfc5987Filename() throws Exception {
        StorageObjectController controller = controller();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.content("11", false, response);

        String disposition = response.getHeader("Content-Disposition");
        assertTrue(disposition.contains("filename=\"三才图.png\""));
        assertTrue(disposition.contains("filename*=UTF-8''%E4%B8%89%E6%89%8D%E5%9B%BE.png"));
    }

    @Test
    void contentShouldRejectDeletedOrMissingObject() {
        StorageObjectController controller = controller();

        assertThrows(RuntimeException.class, () -> controller.content("404", false, new MockHttpServletResponse()));
    }

    private static StorageObjectController controller() {
        return new StorageObjectController(
                unused(StorageObjectApplicationService.class),
                storageService(),
                unused(StorageUploadApplicationService.class),
                unused(StorageMultipartUploadApplicationService.class));
    }

    private static StorageContentApplicationService storageService() {
        return (StorageContentApplicationService) Proxy.newProxyInstance(
                StorageContentApplicationService.class.getClassLoader(),
                new Class<?>[] {StorageContentApplicationService.class},
                (proxy, method, args) -> {
                    if ("openReadableContent".equals(method.getName())) {
                        OpenReadableStorageContentQuery query = (OpenReadableStorageContentQuery) args[0];
                        if (query == null || query.id() == null || query.id().value() == 404L) {
                            throw new BizException("Storage object not found");
                        }
                        StoredObject storage = new StoredObject();
                        storage.setId(query.id());
                        storage.setOriginalFilename(query.id().value() == 11L ? "三才图.png" : "sancai.png");
                        storage.setContentType("image/png");
                        storage.setSize(11L);
                        return new StoredObjectContentResult(
                                storage, new ByteArrayInputStream("image-bytes".getBytes()));
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
