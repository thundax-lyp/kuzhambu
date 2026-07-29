package com.thundax.kuzhambu.system.application.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto;
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
import com.thundax.kuzhambu.system.application.core.query.CurrentUserAvatarQuery;
import com.thundax.kuzhambu.system.application.core.query.CurrentUserQuery;
import com.thundax.kuzhambu.system.application.core.query.GetMenuQuery;
import com.thundax.kuzhambu.system.application.core.query.MenuQuery;
import com.thundax.kuzhambu.system.application.core.query.RoleQuery;
import com.thundax.kuzhambu.system.application.core.query.UserQuery;
import com.thundax.kuzhambu.system.application.core.result.UserAvatarResult;
import com.thundax.kuzhambu.system.application.core.service.MenuApplicationService;
import com.thundax.kuzhambu.system.application.core.service.RoleApplicationService;
import com.thundax.kuzhambu.system.application.core.service.UserApplicationService;
import com.thundax.kuzhambu.system.domain.core.codec.MenuIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.RoleIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.entity.Role;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserPrivilege;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.AccessRank;
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

    private static final int TOO_LARGE_AVATAR_BYTES = 5 * 1024 * 1024 + 1;

    @Test
    void getAvatarInputStreamShouldUseStorageFacade() throws Exception {
        StorageFacade storageFacade = mock(StorageFacade.class);
        CurrentUserApplicationServiceImpl service = service(storageFacade);
        StorageObjectFacadeDto avatar = avatar(100L, 7001L);
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1});

        when(storageFacade.list(any(ListStorageFacadeRequest.class)))
                .thenReturn(ListStorageFacadeResponse.builder()
                        .storedObjects(List.of(avatar))
                        .build());
        when(storageFacade.exists(any(OpenStorageFacadeRequest.class))).thenReturn(true);
        when(storageFacade.open(any(OpenStorageFacadeRequest.class)))
                .thenReturn(OpenStorageFacadeResponse.builder()
                        .inputStream(inputStream)
                        .build());

        InputStream result = service.getAvatarInputStream(new CurrentUserAvatarQuery(UserIdCodec.toDomain(100L)));

        assertSame(inputStream, result);
        ArgumentCaptor<OpenStorageFacadeRequest> requestCaptor =
                ArgumentCaptor.forClass(OpenStorageFacadeRequest.class);
        verify(storageFacade).exists(requestCaptor.capture());
        assertEquals(7001L, requestCaptor.getValue().getStorageObjectId());
        assertEquals("100", requestCaptor.getValue().getOwnerId());
        assertEquals("USER", requestCaptor.getValue().getOwnerType());
        verify(storageFacade).open(any(OpenStorageFacadeRequest.class));
    }

    @Test
    void getAvatarInputStreamShouldReturnNullWhenFacadeReportsUnreadable() {
        StorageFacade storageFacade = mock(StorageFacade.class);
        CurrentUserApplicationServiceImpl service = service(storageFacade);

        when(storageFacade.list(any(ListStorageFacadeRequest.class)))
                .thenReturn(ListStorageFacadeResponse.builder()
                        .storedObjects(List.of(avatar(100L, 7001L)))
                        .build());
        when(storageFacade.exists(any(OpenStorageFacadeRequest.class))).thenReturn(false);

        assertNull(service.getAvatarInputStream(new CurrentUserAvatarQuery(UserIdCodec.toDomain(100L))));
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
                        .objectStatus("ACTIVE")
                        .remarks("avatar")
                        .build());

        UserAvatarResult avatar = service.changeAvatar(
                new ChangeCurrentUserAvatarCommand(UserIdCodec.toDomain(100L), validAvatarInputStream(), "avatar.png"));

        assertEquals(9001L, avatar.getStorageObjectId());
        ArgumentCaptor<UploadStorageFacadeRequest> uploadCaptor =
                ArgumentCaptor.forClass(UploadStorageFacadeRequest.class);
        verify(storageFacade).upload(uploadCaptor.capture());
        assertEquals("USER", uploadCaptor.getValue().getOwnerType());
        assertEquals("100", uploadCaptor.getValue().getOwnerId());
        assertEquals("ACTIVE", uploadCaptor.getValue().getObjectStatus());
        assertEquals("avatar", uploadCaptor.getValue().getRemarks());
    }

    @Test
    void changeAvatarShouldRejectOversizedUploadBeforeDecoding() {
        StorageFacade storageFacade = mock(StorageFacade.class);
        CurrentUserApplicationServiceImpl service = service(storageFacade);

        when(storageFacade.list(any(ListStorageFacadeRequest.class)))
                .thenReturn(ListStorageFacadeResponse.builder()
                        .storedObjects(List.of())
                        .build());

        byte[] tooLargeBytes = new byte[TOO_LARGE_AVATAR_BYTES];
        assertThrows(
                BizException.class,
                () -> service.changeAvatar(new ChangeCurrentUserAvatarCommand(
                        UserIdCodec.toDomain(100L), new ByteArrayInputStream(tooLargeBytes), "avatar.png")));

        verify(storageFacade, never()).upload(any(UploadStorageFacadeRequest.class));
    }

    @Test
    void removeAvatarShouldUseStorageFacade() {
        StorageFacade storageFacade = mock(StorageFacade.class);
        CurrentUserApplicationServiceImpl service = service(storageFacade);

        when(storageFacade.list(any(ListStorageFacadeRequest.class)))
                .thenReturn(ListStorageFacadeResponse.builder()
                        .storedObjects(List.of(avatar(100L, 7001L)))
                        .build());
        doNothing().when(storageFacade).remove(any(RemoveStorageFacadeRequest.class));

        service.removeAvatar(new com.thundax.kuzhambu.system.application.core.command.RemoveCurrentUserAvatarCommand(
                UserIdCodec.toDomain(100L)));

        ArgumentCaptor<RemoveStorageFacadeRequest> removeCaptor =
                ArgumentCaptor.forClass(RemoveStorageFacadeRequest.class);
        verify(storageFacade).remove(removeCaptor.capture());
        assertEquals(7001L, removeCaptor.getValue().getStorageObjectId());
    }

    @Test
    void listAccessibleMenusShouldBatchLoadMenusForNormalUser() {
        UserApplicationService userService = mock(UserApplicationService.class);
        RoleApplicationService roleService = mock(RoleApplicationService.class);
        MenuApplicationService menuService = mock(MenuApplicationService.class);
        CurrentUserApplicationServiceImpl service =
                service(userService, roleService, menuService, mock(StorageFacade.class));

        Role role = new Role();
        role.setId(RoleIdCodec.toDomain(1L));
        Menu grantedMenu = menu(10L, 3);
        Menu deniedMenu = menu(11L, 8);
        when(userService.listUserRoles(any(UserQuery.class))).thenReturn(List.of(role));
        when(roleService.listRoleMenus(any(RoleQuery.class))).thenReturn(List.of(grantedMenu, deniedMenu));
        when(menuService.list(any(MenuQuery.class))).thenReturn(List.of(grantedMenu, deniedMenu));

        CurrentUserQuery query = new CurrentUserQuery();
        query.setUserId(UserIdCodec.toDomain(100L));
        query.setPrivilege(UserPrivilege.NORMAL);
        query.setRank(AccessRank.of(5));

        List<Menu> menus = service.listAccessibleMenus(query);

        assertEquals(List.of(grantedMenu), menus);
        verify(menuService).list(any(MenuQuery.class));
        verify(menuService, never()).get(any(GetMenuQuery.class));
    }

    private static CurrentUserApplicationServiceImpl service(StorageFacade storageFacade) {
        return service(
                mock(UserApplicationService.class),
                mock(RoleApplicationService.class),
                mock(MenuApplicationService.class),
                storageFacade);
    }

    private static CurrentUserApplicationServiceImpl service(
            UserApplicationService userService,
            RoleApplicationService roleService,
            MenuApplicationService menuService,
            StorageFacade storageFacade) {
        return new CurrentUserApplicationServiceImpl(
                userService,
                roleService,
                menuService,
                mock(PrincipalIdentityApplicationService.class),
                mock(PrincipalCredentialApplicationService.class),
                storageFacade);
    }

    private static Menu menu(long id, int rank) {
        Menu menu = new Menu();
        menu.setId(MenuIdCodec.toDomain(id));
        menu.setRank(AccessRank.of(rank));
        return menu;
    }

    private static StorageObjectFacadeDto avatar(long userId, long storageObjectId) {
        return StorageObjectFacadeDto.builder()
                .id(storageObjectId)
                .ownerId(String.valueOf(userId))
                .ownerType("USER")
                .objectStatus("ACTIVE")
                .remarks("avatar")
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
