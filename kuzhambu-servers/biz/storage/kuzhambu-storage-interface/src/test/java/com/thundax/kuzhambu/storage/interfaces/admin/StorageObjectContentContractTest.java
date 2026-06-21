package com.thundax.kuzhambu.storage.interfaces.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
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
        StorageObjectController controller = new StorageObjectController(storageService());
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
        StorageObjectController controller = new StorageObjectController(storageService());
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.content("10", true, response);

        assertTrue(response.getHeader("Content-Disposition").startsWith("attachment;"));
    }

    @Test
    void contentShouldWriteRfc5987Filename() throws Exception {
        StorageObjectController controller = new StorageObjectController(storageService());
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.content("11", false, response);

        String disposition = response.getHeader("Content-Disposition");
        assertTrue(disposition.contains("filename=\"三才图.png\""));
        assertTrue(disposition.contains("filename*=UTF-8''%E4%B8%89%E6%89%8D%E5%9B%BE.png"));
    }

    @Test
    void contentShouldRejectDeletedOrMissingObject() {
        StorageObjectController controller = new StorageObjectController(storageService());

        assertThrows(RuntimeException.class, () -> controller.content("404", false, new MockHttpServletResponse()));
    }

    private static StorageApplicationService storageService() {
        return (StorageApplicationService) Proxy.newProxyInstance(
                StorageApplicationService.class.getClassLoader(),
                new Class<?>[] {StorageApplicationService.class},
                (proxy, method, args) -> {
                    if ("openReadableContent".equals(method.getName())) {
                        StoredObjectId id = (StoredObjectId) args[0];
                        if (id == null || id.value() == 404L) {
                            throw new BizException("Storage object not found");
                        }
                        StoredObject storage = new StoredObject();
                        storage.setId(id);
                        storage.setOriginalFilename(id.value() == 11L ? "三才图.png" : "sancai.png");
                        storage.setContentType("image/png");
                        storage.setSize(11L);
                        return new StoredObjectContent(storage, new ByteArrayInputStream("image-bytes".getBytes()));
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
