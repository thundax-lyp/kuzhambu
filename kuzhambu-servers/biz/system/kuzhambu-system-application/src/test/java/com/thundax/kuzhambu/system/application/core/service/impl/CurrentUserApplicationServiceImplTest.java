package com.thundax.kuzhambu.system.application.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.request.ListStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.RemoveStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.ListStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalCredentialApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityApplicationService;
import com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.service.MenuApplicationService;
import com.thundax.kuzhambu.system.application.core.service.RoleApplicationService;
import com.thundax.kuzhambu.system.application.core.service.UserApplicationService;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CurrentUserApplicationServiceImplTest {

    @Test
    void getAvatarInputStreamShouldUseStorageFacade() throws Exception {
        StorageFacade storageFacade = mock(StorageFacade.class);
        CurrentUserApplicationServiceImpl service = service(storageFacade);
        StoredObject avatar = avatar(100L, 7001L);
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1});

        when(storageFacade.list(any(ListStorageFacadeRequest.class)))
                .thenReturn(ListStorageFacadeResponse.builder()
                        .storedObjects(List.of(readableAvatar(avatar)))
                        .build());
        when(storageFacade.exists(any(OpenStorageFacadeRequest.class))).thenReturn(true);
        when(storageFacade.open(any(OpenStorageFacadeRequest.class)))
                .thenReturn(OpenStorageFacadeResponse.builder()
                        .inputStream(inputStream)
                        .build());

        InputStream result = service.getAvatarInputStream(UserId.of(100L));

        assertSame(inputStream, result);
        ArgumentCaptor<OpenStorageFacadeRequest> requestCaptor =
                ArgumentCaptor.forClass(OpenStorageFacadeRequest.class);
        verify(storageFacade).exists(requestCaptor.capture());
        assertEquals(7001L, requestCaptor.getValue().getStorageObjectId());
        assertEquals("100", requestCaptor.getValue().getOwnerId());
        assertEquals(StorageOwnerType.USER.value(), requestCaptor.getValue().getOwnerType());
        verify(storageFacade).open(any(OpenStorageFacadeRequest.class));
    }

    @Test
    void getAvatarInputStreamShouldReturnNullWhenFacadeReportsUnreadable() {
        StorageFacade storageFacade = mock(StorageFacade.class);
        CurrentUserApplicationServiceImpl service = service(storageFacade);

        when(storageFacade.list(any(ListStorageFacadeRequest.class)))
                .thenReturn(ListStorageFacadeResponse.builder()
                        .storedObjects(List.of(readableAvatar(avatar(100L, 7001L))))
                        .build());
        when(storageFacade.exists(any(OpenStorageFacadeRequest.class))).thenReturn(false);

        assertNull(service.getAvatarInputStream(UserId.of(100L)));
        verify(storageFacade, never()).open(any(OpenStorageFacadeRequest.class));
    }

    @Test
    void changeAvatarShouldUseStorageFacade() {
        StorageFacade storageFacade = mock(StorageFacade.class);
        CurrentUserApplicationServiceImpl service = service(storageFacade);

        when(storageFacade.list(any(ListStorageFacadeRequest.class)))
                .thenReturn(ListStorageFacadeResponse.builder()
                        .storedObjects(List.of())
                        .build());
        when(storageFacade.upload(any(UploadStorageFacadeRequest.class)))
                .thenReturn(UploadStorageFacadeResponse.builder()
                        .storageObjectId(9001L)
                        .originalFilename("avatar.jpg")
                        .contentType("image/jpeg")
                        .objectStatus(StoredObjectStatus.ACTIVE.value())
                        .remarks("avatar")
                        .build());

        StoredObject avatar = service.changeAvatar(
                new ChangeCurrentUserAvatarCommand(UserId.of(100L), validAvatarInputStream(), "avatar.png"));

        assertEquals(9001L, avatar.getId().value());
        ArgumentCaptor<UploadStorageFacadeRequest> uploadCaptor =
                ArgumentCaptor.forClass(UploadStorageFacadeRequest.class);
        verify(storageFacade).upload(uploadCaptor.capture());
        assertEquals(StorageOwnerType.USER.value(), uploadCaptor.getValue().getOwnerType());
        assertEquals("100", uploadCaptor.getValue().getOwnerId());
        assertEquals(StoredObjectStatus.ACTIVE.value(), uploadCaptor.getValue().getObjectStatus());
        assertEquals("avatar", uploadCaptor.getValue().getRemarks());
    }

    @Test
    void removeAvatarShouldUseStorageFacade() {
        StorageFacade storageFacade = mock(StorageFacade.class);
        CurrentUserApplicationServiceImpl service = service(storageFacade);

        when(storageFacade.list(any(ListStorageFacadeRequest.class)))
                .thenReturn(ListStorageFacadeResponse.builder()
                        .storedObjects(List.of(readableAvatar(avatar(100L, 7001L))))
                        .build());
        doNothing().when(storageFacade).remove(any(RemoveStorageFacadeRequest.class));

        service.removeAvatar(new com.thundax.kuzhambu.system.application.core.command.RemoveCurrentUserAvatarCommand(
                UserId.of(100L)));

        ArgumentCaptor<RemoveStorageFacadeRequest> removeCaptor =
                ArgumentCaptor.forClass(RemoveStorageFacadeRequest.class);
        verify(storageFacade).remove(removeCaptor.capture());
        assertEquals(7001L, removeCaptor.getValue().getStorageObjectId());
    }

    private static CurrentUserApplicationServiceImpl service(StorageFacade storageFacade) {
        return new CurrentUserApplicationServiceImpl(
                mock(UserApplicationService.class),
                mock(RoleApplicationService.class),
                mock(MenuApplicationService.class),
                mock(PrincipalIdentityApplicationService.class),
                mock(PrincipalCredentialApplicationService.class),
                storageFacade);
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

    private static com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto readableAvatar(StoredObject avatar) {
        return com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto.builder()
                .id(avatar.getId() == null ? null : avatar.getId().value())
                .originalFilename(avatar.getOriginalFilename())
                .contentType(avatar.getContentType())
                .ownerId(avatar.getOwnerId())
                .ownerType(
                        avatar.getOwnerType() == null
                                ? null
                                : avatar.getOwnerType().value())
                .size(avatar.getSize())
                .objectStatus(
                        avatar.getObjectStatus() == null
                                ? null
                                : avatar.getObjectStatus().value())
                .referenceStatus(
                        avatar.getReferenceStatus() == null
                                ? null
                                : avatar.getReferenceStatus().value())
                .remarks(avatar.getRemarks())
                .build();
    }

    private static InputStream validAvatarInputStream() {
        try {
            BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
