package com.thundax.kuzhambu.storage.interfaces.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.StorageDeleteRequest;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class StorageObjectDeleteContractTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void deleteRouteShouldKeepBackendRequestContract() throws Exception {
        RequestMapping classMapping = StorageObjectController.class.getAnnotation(RequestMapping.class);
        assertEquals("/api/storage/object", classMapping.value()[0]);

        PostMapping methodMapping = StorageObjectController.class
                .getDeclaredMethod("delete", StorageDeleteRequest.class)
                .getAnnotation(PostMapping.class);
        assertEquals("delete", methodMapping.value()[0]);

        StorageDeleteRequest request = OBJECT_MAPPER.readValue("{\"ids\":[1,2]}", StorageDeleteRequest.class);
        assertEquals(List.of(1L, 2L), request.getIds());

        JsonNode json = OBJECT_MAPPER.valueToTree(request);
        assertEquals(1, json.size(), json::toString);
        assertTrue(json.has("ids"), json::toString);
    }

    @Test
    void deleteShouldRemoveExistingObjects() {
        List<StoredObjectId> removedIds = new ArrayList<>();
        StorageObjectController controller = new StorageObjectController(storageService(removedIds));
        StorageDeleteRequest request = new StorageDeleteRequest();
        request.setIds(List.of(1L, 2L));

        assertTrue(controller.delete(request));

        assertEquals(List.of(StoredObjectId.of(1L), StoredObjectId.of(2L)), removedIds);
    }

    @Test
    void deleteShouldRejectMissingOrEmptyIds() {
        StorageObjectController controller = new StorageObjectController(storageService(new ArrayList<>()));

        StorageDeleteRequest missingRequest = new StorageDeleteRequest();
        missingRequest.setIds(List.of(404L));
        assertThrows(RuntimeException.class, () -> controller.delete(missingRequest));

        StorageDeleteRequest emptyRequest = new StorageDeleteRequest();
        emptyRequest.setIds(List.of());
        assertThrows(RuntimeException.class, () -> controller.delete(emptyRequest));
    }

    private static StorageApplicationService storageService(List<StoredObjectId> removedIds) {
        return (StorageApplicationService) Proxy.newProxyInstance(
                StorageApplicationService.class.getClassLoader(),
                new Class<?>[] {StorageApplicationService.class},
                (proxy, method, args) -> {
                    if ("get".equals(method.getName())) {
                        StoredObjectId id = (StoredObjectId) args[0];
                        if (id == null || id.value() == 404L) {
                            return null;
                        }
                        StoredObject object = new StoredObject();
                        object.setId(id);
                        return object;
                    }
                    if ("remove".equals(method.getName())) {
                        removedIds.add((StoredObjectId) args[0]);
                        return 1;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
