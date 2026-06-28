package com.thundax.kuzhambu.system.application.core.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto;
import com.thundax.kuzhambu.storage.facade.request.ListStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.RemoveStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.ListStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import com.thundax.kuzhambu.system.application.auth.command.PrincipalCredentialCommand;
import com.thundax.kuzhambu.system.application.auth.exception.InvalidPasswordException;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalCredentialQuery;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalCredentialApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityApplicationService;
import com.thundax.kuzhambu.system.application.auth.utils.PasswordHelper;
import com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserPasswordCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeUserInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.query.CurrentUserQuery;
import com.thundax.kuzhambu.system.application.core.query.MenuQuery;
import com.thundax.kuzhambu.system.application.core.query.RoleQuery;
import com.thundax.kuzhambu.system.application.core.query.UserQuery;
import com.thundax.kuzhambu.system.application.core.service.CurrentUserApplicationService;
import com.thundax.kuzhambu.system.application.core.service.MenuApplicationService;
import com.thundax.kuzhambu.system.application.core.service.RoleApplicationService;
import com.thundax.kuzhambu.system.application.core.service.UserApplicationService;
import com.thundax.kuzhambu.system.application.core.utils.SysApiUtils;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalCredential;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.entity.Role;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserPrivilege;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class CurrentUserApplicationServiceImpl implements CurrentUserApplicationService {

    private static final int DEFAULT_PASSWORD_FAILED_LIMIT = 0;
    private static final String AVATAR_REMARKS = "avatar";
    private static final String AVATAR_FILENAME = "avatar.jpg";
    private static final String JPG = "jpg";
    private static final String IMAGE_JPEG = "image/jpeg";
    private static final int MAX_AVATAR_WIDTH = 400;
    private static final int MAX_AVATAR_HEIGHT = 400;
    private static final float IMAGE_QUALITY = 0.8f;

    private final UserApplicationService userService;
    private final RoleApplicationService roleService;
    private final MenuApplicationService menuService;
    private final PrincipalIdentityApplicationService principalIdentityService;
    private final PrincipalCredentialApplicationService principalCredentialService;
    private final StorageFacade storageFacade;

    public CurrentUserApplicationServiceImpl(
            UserApplicationService userService,
            RoleApplicationService roleService,
            MenuApplicationService menuService,
            PrincipalIdentityApplicationService principalIdentityService,
            PrincipalCredentialApplicationService principalCredentialService,
            StorageFacade storageFacade) {
        this.userService = userService;
        this.roleService = roleService;
        this.menuService = menuService;
        this.principalIdentityService = principalIdentityService;
        this.principalCredentialService = principalCredentialService;
        this.storageFacade = storageFacade;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User changeInfo(ChangeCurrentUserInfoCommand command) {
        userService.changeInfo(new ChangeUserInfoCommand(
                command.getUserId(),
                command.getDepartmentId(),
                command.getEmail(),
                command.getMobile(),
                command.getTel(),
                command.getName(),
                command.getRank(),
                command.getPrivilege(),
                command.getStatus(),
                command.getRemarks(),
                getAccountLoginName(command.getUserId()),
                null));
        return toUser(command);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangeCurrentUserPasswordCommand command) {
        String oldPassword = command.getOldPassword();
        String password = command.getPassword();
        if (StringUtils.isBlank(password)) {
            throw new BizException("SYS-00001", "sys.exception.invalid-parameter", "password");
        } else if (!password.matches(SysApiUtils.PASSWORD_VALIDATE_PATTERN)) {
            throw new BizException(SysApiUtils.PASSWORD_VALIDATE_MESSAGE);
        }

        PrincipalIdentity accountIdentity = getAccountIdentity(command.getUserId());
        PrincipalCredential credential = accountIdentity == null
                ? null
                : principalCredentialService.get(
                        credentialQuery(accountIdentity.getId(), PrincipalCredentialType.USER_PASSWORD));
        if (credential == null || !PasswordHelper.validate(oldPassword, credential.getCredentialValue())) {
            throw new InvalidPasswordException();
        }

        upsertPassword(command.getUserId(), accountIdentity, PasswordHelper.encrypt(password));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoredObject changeAvatar(ChangeCurrentUserAvatarCommand command) {
        if (command == null || command.getUserId() == null || command.getInputStream() == null) {
            throw invalidParameter("avatar");
        }

        removeAvatar(command.getUserId());

        byte[] avatarBytes = readAvatarBytes(command.getInputStream());
        UploadStorageFacadeResponse uploaded = storageFacade.upload(UploadStorageFacadeRequest.builder()
                .inputStream(new ByteArrayInputStream(avatarBytes))
                .originalFilename(originalFilename(command.getOriginalFilename()))
                .contentType(IMAGE_JPEG)
                .sizeBytes((long) avatarBytes.length)
                .ownerType(StorageOwnerType.USER.value())
                .ownerId(String.valueOf(command.getUserId().value()))
                .objectStatus(StoredObjectStatus.ACTIVE.value())
                .referenceStatus(StoredObjectReferenceStatus.UNREFERENCED.value())
                .remarks(AVATAR_REMARKS)
                .build());
        return toStoredObject(uploaded);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAvatar(RemoveCurrentUserAvatarCommand command) {
        if (command == null || command.getUserId() == null) {
            throw invalidParameter("userId");
        }
        removeAvatar(command.getUserId());
    }

    @Override
    public StoredObject getAvatar(UserId userId) {
        List<StoredObject> avatars = listAvatars(userId);
        return avatars.isEmpty() ? null : avatars.get(avatars.size() - 1);
    }

    @Override
    public InputStream getAvatarInputStream(UserId userId) {
        StoredObject avatar = getAvatar(userId);
        if (avatar == null || storageFacade == null) {
            return null;
        }
        OpenStorageFacadeRequest request = toReadableContentRequest(avatar);
        if (!storageFacade.exists(request)) {
            return null;
        }
        OpenStorageFacadeResponse content = storageFacade.open(request);
        return content == null ? null : content.getInputStream();
    }

    @Override
    public boolean existsAvatar(UserId userId) {
        StoredObject avatar = getAvatar(userId);
        return avatar != null && storageFacade.exists(toReadableContentRequest(avatar));
    }

    @Override
    public List<Menu> listAccessibleMenus(CurrentUserQuery query) {
        if (query != null && UserPrivilege.SUPER == query.getPrivilege()) {
            return sortedMenus(menuService.list(new MenuQuery()));
        }

        List<Role> roleList = userService.listUserRoles(userQuery(query.getUserId()));
        boolean isAdmin = query != null && UserPrivilege.ADMIN == query.getPrivilege()
                || roleList.stream().anyMatch(Role::isAdmin);
        if (isAdmin) {
            MenuQuery menuQuery = new MenuQuery();
            menuQuery.setMaxRank(query.getRank());
            return sortedMenus(menuService.list(menuQuery));
        }

        List<MenuId> menuIds = roleList.stream()
                .flatMap(role -> roleService.listRoleMenus(roleQuery(role)).stream())
                .map(Menu::getId)
                .distinct()
                .filter(menuId -> {
                    Menu menu = menuService.get(menuId);
                    return menu != null && query.getRank().canAccess(menu.getRank());
                })
                .collect(Collectors.toList());
        MenuQuery menuQuery = new MenuQuery();
        menuQuery.setIds(menuIds);
        return sortedMenus(menuService.list(menuQuery));
    }

    private RoleQuery roleQuery(Role role) {
        RoleQuery query = new RoleQuery();
        query.setId(role.getId());
        return query;
    }

    private List<Menu> sortedMenus(List<Menu> menus) {
        return menus == null ? new ArrayList<>() : new ArrayList<>(menus);
    }

    private UserQuery userQuery(UserId userId) {
        UserQuery query = new UserQuery();
        query.setId(userId);
        return query;
    }

    private List<StoredObject> listAvatars(UserId userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        ListStorageFacadeResponse response = storageFacade.list(ListStorageFacadeRequest.builder()
                .ownerType(StorageOwnerType.USER.value())
                .ownerId(String.valueOf(userId.value()))
                .objectStatus(StoredObjectStatus.ACTIVE.value())
                .remarks(AVATAR_REMARKS)
                .build());
        if (response == null || response.getStoredObjects() == null) {
            return Collections.emptyList();
        }
        return response.getStoredObjects().stream().map(this::toStoredObject).collect(Collectors.toList());
    }

    private void removeAvatar(UserId userId) {
        for (StoredObject storage : listAvatars(userId)) {
            storageFacade.remove(RemoveStorageFacadeRequest.builder()
                    .storageObjectId(
                            storage.getId() == null ? null : storage.getId().value())
                    .build());
        }
    }

    @Override
    public List<Menu> listVisibleMenus(CurrentUserQuery query) {
        List<Menu> visibleMenus =
                listAccessibleMenus(query).stream().filter(Menu::isDisplay).collect(Collectors.toList());
        List<Menu> menuList =
                visibleMenus.stream().filter(menu -> menu.getParentId() == null).collect(Collectors.toList());

        for (int idx = 0; idx < menuList.size(); idx++) {
            Menu parent = menuList.get(idx);
            List<Menu> childList = visibleMenus.stream()
                    .filter(menu -> Objects.equals(menu.getParentId(), parent.getId()))
                    .collect(Collectors.toList());
            menuList.addAll(childList);
        }
        return menuList;
    }

    private PrincipalIdentity getAccountIdentity(UserId userId) {
        if (userId == null) {
            return null;
        }
        return principalIdentityService.get(
                identityQuery(PrincipalKey.of(PrincipalType.USER, userId.value()), PrincipalIdentityType.USER_ACCOUNT));
    }

    private String getAccountLoginName(UserId userId) {
        PrincipalIdentity identity = getAccountIdentity(userId);
        return identity == null ? null : identity.getIdentityValue();
    }

    private void upsertPassword(UserId userId, PrincipalIdentity accountIdentity, String encryptedPassword) {
        if (userId == null || accountIdentity == null || StringUtils.isBlank(encryptedPassword)) {
            return;
        }
        PrincipalCredential credential = principalCredentialService.get(
                credentialQuery(accountIdentity.getId(), PrincipalCredentialType.USER_PASSWORD));
        if (credential == null) {
            credential = new PrincipalCredential();
            credential.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, userId.value()));
            credential.setIdentityId(accountIdentity.getId());
            credential.setCredentialType(PrincipalCredentialType.USER_PASSWORD);
            credential.setCredentialValue(encryptedPassword);
            credential.setStatus(PrincipalCredentialStatus.ACTIVE);
            credential.setNeedChangePassword(false);
            credential.setFailedCount(0);
            credential.setFailedLimit(DEFAULT_PASSWORD_FAILED_LIMIT);
            principalCredentialService.create(new PrincipalCredentialCommand(credential));
            return;
        }

        credential.setCredentialValue(encryptedPassword);
        credential.setStatus(PrincipalCredentialStatus.ACTIVE);
        credential.setNeedChangePassword(false);
        credential.setFailedCount(0);
        credential.setLockedUntil(null);
        credential.setLastVerifiedAt(null);
        principalCredentialService.change(new PrincipalCredentialCommand(credential));
    }

    private byte[] readAvatarBytes(InputStream inputStream) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.Builder<?> builder = Thumbnails.of(inputStream);
            BufferedImage image = builder.scale(1.0f).asBufferedImage();

            int originWidth = image.getWidth();
            int originHeight = image.getHeight();
            if (originWidth <= 0 || originHeight <= 0) {
                throw invalidParameter("avatar");
            }

            if (originWidth > MAX_AVATAR_WIDTH || originHeight > MAX_AVATAR_HEIGHT) {
                double scale = Math.min(
                        (double) MAX_AVATAR_WIDTH / (double) originWidth,
                        (double) MAX_AVATAR_HEIGHT / (double) originHeight);
                builder = Thumbnails.of(image).scale(scale);
            } else {
                builder = Thumbnails.of(image).size(originWidth, originHeight);
            }

            builder.outputFormat(JPG);
            builder.outputQuality(IMAGE_QUALITY);
            builder.toOutputStream(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw storageFailure(e.getMessage());
        } catch (RuntimeException e) {
            throw invalidParameter("avatar");
        }
    }

    private String originalFilename(String originalFilename) {
        if (StringUtils.isBlank(FilenameUtils.getExtension(originalFilename))) {
            return AVATAR_FILENAME;
        }
        return originalFilename;
    }

    private OpenStorageFacadeRequest toReadableContentRequest(StoredObject avatar) {
        if (avatar == null) {
            return null;
        }
        return OpenStorageFacadeRequest.builder()
                .storageObjectId(avatar.getId() == null ? null : avatar.getId().value())
                .ownerId(avatar.getOwnerId())
                .ownerType(
                        avatar.getOwnerType() == null
                                ? null
                                : avatar.getOwnerType().value())
                .build();
    }

    private StoredObject toStoredObject(UploadStorageFacadeResponse response) {
        if (response == null) {
            return null;
        }
        StoredObject storage = new StoredObject();
        storage.setId(response.getStorageObjectId() == null ? null : StoredObjectId.of(response.getStorageObjectId()));
        storage.setOriginalFilename(response.getOriginalFilename());
        storage.setContentType(response.getContentType());
        storage.setName(response.getName());
        storage.setExtendName(response.getExtendName());
        storage.setMimeType(response.getMimeType());
        storage.setBucketName(response.getBucketName());
        storage.setObjectKey(response.getObjectKey());
        storage.setSize(response.getSizeBytes());
        storage.setAccessEndpoint(response.getAccessEndpoint());
        storage.setObjectStatus(
                StringUtils.isBlank(response.getObjectStatus())
                        ? null
                        : StoredObjectStatus.from(response.getObjectStatus()));
        storage.setReferenceStatus(
                StringUtils.isBlank(response.getReferenceStatus())
                        ? null
                        : StoredObjectReferenceStatus.from(response.getReferenceStatus()));
        storage.setRemarks(response.getRemarks());
        return storage;
    }

    private StoredObject toStoredObject(StorageObjectFacadeDto dto) {
        if (dto == null) {
            return null;
        }
        StoredObject storage = new StoredObject();
        storage.setId(dto.getId() == null ? null : StoredObjectId.of(dto.getId()));
        storage.setOriginalFilename(dto.getOriginalFilename());
        storage.setContentType(dto.getContentType());
        storage.setOwnerId(dto.getOwnerId());
        storage.setOwnerType(
                StringUtils.isBlank(dto.getOwnerType()) ? null : StorageOwnerType.from(dto.getOwnerType()));
        storage.setSize(dto.getSize());
        storage.setObjectStatus(
                StringUtils.isBlank(dto.getObjectStatus()) ? null : StoredObjectStatus.from(dto.getObjectStatus()));
        storage.setReferenceStatus(
                StringUtils.isBlank(dto.getReferenceStatus())
                        ? null
                        : StoredObjectReferenceStatus.from(dto.getReferenceStatus()));
        storage.setRemarks(dto.getRemarks());
        return storage;
    }

    private BizException invalidParameter(String name) {
        return new BizException("SYS-00001", "sys.exception.invalid-parameter", "无效的参数: " + name);
    }

    private BizException storageFailure(String message) {
        return new BizException(
                "SYS-00005", "sys.exception.storage-failure", StringUtils.defaultIfBlank(message, "存储处理失败"));
    }

    private PrincipalIdentityQuery identityQuery(PrincipalKey principalKey, PrincipalIdentityType identityType) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setPrincipalKey(principalKey);
        query.setIdentityType(identityType);
        return query;
    }

    private PrincipalCredentialQuery credentialQuery(
            PrincipalIdentityId identityId, PrincipalCredentialType credentialType) {
        PrincipalCredentialQuery query = new PrincipalCredentialQuery();
        query.setIdentityId(identityId);
        query.setCredentialType(credentialType);
        return query;
    }

    private User toUser(ChangeCurrentUserInfoCommand command) {
        User user = new User();
        user.setId(command.getUserId());
        user.setDepartmentId(command.getDepartmentId());
        user.setEmail(command.getEmail());
        user.setMobile(command.getMobile());
        user.setTel(command.getTel());
        user.setName(command.getName());
        user.setRank(command.getRank());
        user.setPrivilege(command.getPrivilege());
        user.setStatus(command.getStatus());
        user.setRemarks(command.getRemarks());
        return user;
    }
}
