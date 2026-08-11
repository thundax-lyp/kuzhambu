package com.thundax.kuzhambu.storage.interfaces.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.storage.application.command.RemoveStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.query.GetStorageObjectQuery;
import com.thundax.kuzhambu.storage.application.service.StorageContentApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageMultipartUploadApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageObjectApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageUploadApplicationService;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.StorageObjectController;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.StorageDeleteRequest;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
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
        StorageObjectController controller = controller(storageService(removedIds));
        StorageDeleteRequest request = new StorageDeleteRequest();
        request.setIds(List.of(1L, 2L));

        assertTrue(controller.delete(request));

        assertEquals(List.of(StoredObjectIdCodec.toDomain(1L), StoredObjectIdCodec.toDomain(2L)), removedIds);
    }

    @Test
    void deleteShouldAllowUnreferencedObject() {
        List<StoredObjectId> removedIds = new ArrayList<>();
        StorageObjectController controller = controller(storageService(removedIds));
        StorageDeleteRequest request = new StorageDeleteRequest();
        request.setIds(List.of(101L));

        assertTrue(controller.delete(request));
        assertEquals(List.of(StoredObjectIdCodec.toDomain(101L)), removedIds);
    }

    @Test
    void deleteShouldRejectMissingOrEmptyIds() {
        StorageObjectController controller = controller(storageService(new ArrayList<>()));

        StorageDeleteRequest missingRequest = new StorageDeleteRequest();
        missingRequest.setIds(List.of(404L));
        assertThrows(RuntimeException.class, () -> controller.delete(missingRequest));

        StorageDeleteRequest emptyRequest = new StorageDeleteRequest();
        emptyRequest.setIds(List.of());
        assertThrows(RuntimeException.class, () -> controller.delete(emptyRequest));
    }

    @Test
    void deleteShouldRejectReferencedObject() {
        List<StoredObjectId> removedIds = new ArrayList<>();
        StorageObjectController controller = controller(storageService(removedIds, Arrays.asList(100L)));
        StorageDeleteRequest request = new StorageDeleteRequest();
        request.setIds(List.of(100L));

        assertThrows(RuntimeException.class, () -> controller.delete(request));
        assertTrue(removedIds.isEmpty());
    }

    private static StorageObjectController controller(StorageObjectApplicationService storageObjectApplicationService) {
        return new StorageObjectController(
                storageObjectApplicationService,
                unused(StorageContentApplicationService.class),
                unused(StorageUploadApplicationService.class),
                unused(StorageMultipartUploadApplicationService.class));
    }

    private static StorageObjectApplicationService storageService(List<StoredObjectId> removedIds) {
        return storageService(removedIds, List.of());
    }

    private static StorageObjectApplicationService storageService(
            List<StoredObjectId> removedIds, List<Long> referencedIds) {
        return (StorageObjectApplicationService) Proxy.newProxyInstance(
                StorageObjectApplicationService.class.getClassLoader(),
                new Class<?>[] {StorageObjectApplicationService.class},
                (proxy, method, args) -> {
                    if ("get".equals(method.getName())) {
                        GetStorageObjectQuery query = (GetStorageObjectQuery) args[0];
                        StoredObjectId id = query == null ? null : query.id();
                        if (id == null || id.value() == 404L) {
                            return null;
                        }
                        StoredObject object = new StoredObject();
                        object.setId(id);
                        if (referencedIds.contains(id.value())) {
                            object.setReferenceStatus(StoredObjectReferenceStatus.REFERENCED);
                        } else {
                            object.setReferenceStatus(StoredObjectReferenceStatus.UNREFERENCED);
                        }
                        return object;
                    }
                    if ("remove".equals(method.getName())) {
                        RemoveStorageObjectCommand command = (RemoveStorageObjectCommand) args[0];
                        StoredObjectId id = command == null ? null : command.id();
                        if (referencedIds.contains(id.value())) {
                            throw new RuntimeException("Storage 对象已被其他业务引用，无法删除");
                        }
                        removedIds.add(id);
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
