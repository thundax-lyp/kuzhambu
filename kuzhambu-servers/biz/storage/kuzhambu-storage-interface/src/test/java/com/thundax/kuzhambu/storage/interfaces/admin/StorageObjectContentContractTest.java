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
                .getDeclaredMethod("content", String.class, jakarta.servlet.http.HttpServletResponse.class)
                .getAnnotation(GetMapping.class);

        assertEquals("{id}/content", methodMapping.value()[0]);
    }

    @Test
    void contentShouldWriteObjectBytesAndHeaders() throws Exception {
        StorageObjectController controller = new StorageObjectController(storageService());
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.content("10", response);

        assertEquals("image/png", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition").contains("sancai.png"));
        assertEquals("image-bytes", response.getContentAsString());
    }

    @Test
    void contentShouldRejectDeletedOrMissingObject() {
        StorageObjectController controller = new StorageObjectController(storageService());

        assertThrows(RuntimeException.class, () -> controller.content("404", new MockHttpServletResponse()));
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
                        storage.setOriginalFilename("sancai.png");
                        storage.setContentType("image/png");
                        return new StoredObjectContent(storage, new ByteArrayInputStream("image-bytes".getBytes()));
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
