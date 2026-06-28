package com.thundax.kuzhambu.system.application.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.application.store.StoredObjectStore;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.facade.StorageReadableContentFacade;
import com.thundax.kuzhambu.storage.facade.request.GetReadableContentFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.GetReadableContentFacadeResponse;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalCredentialApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityApplicationService;
import com.thundax.kuzhambu.system.application.core.service.MenuApplicationService;
import com.thundax.kuzhambu.system.application.core.service.RoleApplicationService;
import com.thundax.kuzhambu.system.application.core.service.UserApplicationService;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CurrentUserApplicationServiceImplTest {

    @Test
    void getAvatarInputStreamShouldUseStorageReadableContentFacade() throws Exception {
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        StorageReadableContentFacade storageReadableContentFacade = mock(StorageReadableContentFacade.class);
        StoredObjectStore storedObjectStore = mock(StoredObjectStore.class);
        CurrentUserApplicationServiceImpl service =
                service(storageApplicationService, storageReadableContentFacade, storedObjectStore);
        StoredObject avatar = avatar(100L, 7001L);
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1});

        when(storageApplicationService.list(any(StorageQuery.class))).thenReturn(List.of(avatar));
        when(storageReadableContentFacade.existsReadableContent(any(GetReadableContentFacadeRequest.class)))
                .thenReturn(true);
        when(storageReadableContentFacade.getReadableContent(any(GetReadableContentFacadeRequest.class)))
                .thenReturn(GetReadableContentFacadeResponse.builder()
                        .inputStream(inputStream)
                        .build());

        InputStream result = service.getAvatarInputStream(UserId.of(100L));

        assertSame(inputStream, result);
        ArgumentCaptor<GetReadableContentFacadeRequest> requestCaptor =
                ArgumentCaptor.forClass(GetReadableContentFacadeRequest.class);
        verify(storageReadableContentFacade).existsReadableContent(requestCaptor.capture());
        assertEquals(7001L, requestCaptor.getValue().getStorageObjectId());
        assertEquals("100", requestCaptor.getValue().getOwnerId());
        assertEquals(StorageOwnerType.USER.value(), requestCaptor.getValue().getOwnerType());
        verify(storageReadableContentFacade).getReadableContent(any(GetReadableContentFacadeRequest.class));
        verify(storedObjectStore, never()).exists(any(StoredObject.class));
        verify(storedObjectStore, never()).open(any(StoredObject.class));
    }

    @Test
    void getAvatarInputStreamShouldReturnNullWhenFacadeReportsUnreadable() {
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        StorageReadableContentFacade storageReadableContentFacade = mock(StorageReadableContentFacade.class);
        StoredObjectStore storedObjectStore = mock(StoredObjectStore.class);
        CurrentUserApplicationServiceImpl service =
                service(storageApplicationService, storageReadableContentFacade, storedObjectStore);

        when(storageApplicationService.list(any(StorageQuery.class))).thenReturn(List.of(avatar(100L, 7001L)));
        when(storageReadableContentFacade.existsReadableContent(any(GetReadableContentFacadeRequest.class)))
                .thenReturn(false);

        assertNull(service.getAvatarInputStream(UserId.of(100L)));
        verify(storageReadableContentFacade, never()).getReadableContent(any(GetReadableContentFacadeRequest.class));
        verifyNoInteractions(storedObjectStore);
    }

    private static CurrentUserApplicationServiceImpl service(
            StorageApplicationService storageApplicationService,
            StorageReadableContentFacade storageReadableContentFacade,
            StoredObjectStore storedObjectStore) {
        return new CurrentUserApplicationServiceImpl(
                mock(UserApplicationService.class),
                mock(RoleApplicationService.class),
                mock(MenuApplicationService.class),
                mock(PrincipalIdentityApplicationService.class),
                mock(PrincipalCredentialApplicationService.class),
                storageApplicationService,
                storageReadableContentFacade,
                storedObjectStore);
    }

    private static StoredObject avatar(long userId, long storageObjectId) {
        StoredObject avatar = new StoredObject();
        avatar.setId(StoredObjectId.of(storageObjectId));
        avatar.setOwnerId(String.valueOf(userId));
        avatar.setOwnerType(StorageOwnerType.USER);
        avatar.setObjectStatus(StoredObjectStatus.ACTIVE);
        avatar.setRemarks("avatar");
        return avatar;
    }
}
