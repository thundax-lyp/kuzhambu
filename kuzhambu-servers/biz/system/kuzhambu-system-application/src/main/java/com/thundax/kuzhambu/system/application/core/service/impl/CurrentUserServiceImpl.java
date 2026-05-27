package com.thundax.kuzhambu.system.application.core.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.storage.application.entity.StoredObject;
import com.thundax.kuzhambu.storage.application.entity.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.application.entity.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.application.entity.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.application.service.StorageService;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageCommand;
import com.thundax.kuzhambu.storage.application.service.command.CreateStorageCommand;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.application.store.StoredObjectStore;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalCredential;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.application.auth.exception.InvalidPasswordException;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalCredentialService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityService;
import com.thundax.kuzhambu.system.application.auth.service.command.PrincipalCredentialCommand;
import com.thundax.kuzhambu.system.application.auth.service.query.PrincipalCredentialQuery;
import com.thundax.kuzhambu.system.application.auth.service.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.auth.utils.PasswordHelper;
import com.thundax.kuzhambu.system.application.core.entity.Menu;
import com.thundax.kuzhambu.system.application.core.entity.Role;
import com.thundax.kuzhambu.system.application.core.entity.User;
import com.thundax.kuzhambu.system.application.core.entity.enums.UserPrivilege;
import com.thundax.kuzhambu.system.application.core.service.CurrentUserService;
import com.thundax.kuzhambu.system.application.core.service.MenuService;
import com.thundax.kuzhambu.system.application.core.service.RoleService;
import com.thundax.kuzhambu.system.application.core.service.UserService;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeCurrentUserInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeCurrentUserPasswordCommand;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeUserInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.RemoveCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.service.query.CurrentUserQuery;
import com.thundax.kuzhambu.system.application.core.service.query.MenuQuery;
import com.thundax.kuzhambu.system.application.core.service.query.RoleQuery;
import com.thundax.kuzhambu.system.application.core.service.query.UserQuery;
import com.thundax.kuzhambu.system.application.core.utils.SysApiUtils;
import com.thundax.kuzhambu.system.domain.auth.enums.PrincipalType;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.core.valueobject.MenuId;
import com.thundax.kuzhambu.system.domain.core.valueobject.UserId;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
public class CurrentUserServiceImpl implements CurrentUserService {

    private static final int DEFAULT_PASSWORD_FAILED_LIMIT = 0;
    private static final String AVATAR_REMARKS = "avatar";
    private static final String AVATAR_NAME = "avatar";
    private static final String AVATAR_FILENAME = "avatar.jpg";
    private static final String JPG = "jpg";
    private static final String IMAGE_JPEG = "image/jpeg";
    private static final int MAX_AVATAR_WIDTH = 400;
    private static final int MAX_AVATAR_HEIGHT = 400;
    private static final float IMAGE_QUALITY = 0.8f;

    private final UserService userService;
    private final RoleService roleService;
    private final MenuService menuService;
    private final PrincipalIdentityService principalIdentityService;
    private final PrincipalCredentialService principalCredentialService;
    private final StorageService storageService;
    private final StoredObjectStore storedObjectStore;

    public CurrentUserServiceImpl(
            UserService userService,
            RoleService roleService,
            MenuService menuService,
            PrincipalIdentityService principalIdentityService,
            PrincipalCredentialService principalCredentialService,
            StorageService storageService,
            StoredObjectStore storedObjectStore) {
        this.userService = userService;
        this.roleService = roleService;
        this.menuService = menuService;
        this.principalIdentityService = principalIdentityService;
        this.principalCredentialService = principalCredentialService;
        this.storageService = storageService;
        this.storedObjectStore = storedObjectStore;
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
        StoredObject storage = toAvatarStorage(command.getUserId(), command.getOriginalFilename());
        try {
            applyStoredObject(storage, storedObjectStore.save(storage, new ByteArrayInputStream(avatarBytes)));
        } catch (IOException e) {
            throw storageFailure(e.getMessage());
        }
        storage.setId(storageService.create(toCreateStorageCommand(storage)));
        return storage;
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
        if (avatar == null || !storedObjectStore.exists(avatar)) {
            return null;
        }
        try {
            return storedObjectStore.open(avatar);
        } catch (IOException e) {
            throw storageFailure(e.getMessage());
        }
    }

    @Override
    public boolean existsAvatar(UserId userId) {
        StoredObject avatar = getAvatar(userId);
        return avatar != null && storedObjectStore.exists(avatar);
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
        StorageQuery query = new StorageQuery();
        query.setOwnerType(StorageOwnerType.USER);
        query.setOwnerId(userId == null ? null : String.valueOf(userId.value()));
        query.setObjectStatus(StoredObjectStatus.ACTIVE);
        query.setRemarks(AVATAR_REMARKS);
        return storageService.list(query);
    }

    private void removeAvatar(UserId userId) {
        for (StoredObject storage : listAvatars(userId)) {
            storageService.remove(storage.getId());
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

    private StoredObject toAvatarStorage(UserId userId, String originalFilename) {
        StoredObject storage = new StoredObject();
        storage.setOriginalFilename(originalFilename(originalFilename));
        storage.setContentType(IMAGE_JPEG);
        storage.setName(AVATAR_NAME);
        storage.setExtendName(JPG);
        storage.setMimeType(IMAGE_JPEG);
        storage.setOwnerId(String.valueOf(userId.value()));
        storage.setOwnerType(StorageOwnerType.USER);
        storage.setObjectStatus(StoredObjectStatus.ACTIVE);
        storage.setReferenceStatus(StoredObjectReferenceStatus.UNREFERENCED);
        storage.setRemarks(AVATAR_REMARKS);
        return storage;
    }

    private String originalFilename(String originalFilename) {
        if (StringUtils.isBlank(FilenameUtils.getExtension(originalFilename))) {
            return AVATAR_FILENAME;
        }
        return originalFilename;
    }

    private void applyStoredObject(StoredObject storage, StoredObject storedObject) {
        storage.setBucketName(storedObject.getBucketName());
        storage.setObjectKey(storedObject.getObjectKey());
        storage.setSize(storedObject.getSize());
        storage.setAccessEndpoint(storedObject.getAccessEndpoint());
    }

    private CreateStorageCommand toCreateStorageCommand(StoredObject storage) {
        CreateStorageCommand command = new CreateStorageCommand();
        command.setOriginalFilename(storage.getOriginalFilename());
        command.setContentType(storage.getContentType());
        command.setName(storage.getName());
        command.setExtendName(storage.getExtendName());
        command.setMimeType(storage.getMimeType());
        command.setOwnerId(storage.getOwnerId());
        command.setOwnerType(storage.getOwnerType());
        command.setBucketName(storage.getBucketName());
        command.setObjectKey(storage.getObjectKey());
        command.setSize(storage.getSize());
        command.setAccessEndpoint(storage.getAccessEndpoint());
        command.setObjectStatus(storage.getObjectStatus());
        command.setReferenceStatus(storage.getReferenceStatus());
        command.setRemarks(storage.getRemarks());
        return command;
    }

    private ChangeStorageCommand toChangeStorageCommand(StoredObject storage) {
        ChangeStorageCommand command = new ChangeStorageCommand();
        command.setId(storage.getId());
        command.setOriginalFilename(storage.getOriginalFilename());
        command.setContentType(storage.getContentType());
        command.setName(storage.getName());
        command.setExtendName(storage.getExtendName());
        command.setMimeType(storage.getMimeType());
        command.setOwnerId(storage.getOwnerId());
        command.setOwnerType(storage.getOwnerType());
        command.setBucketName(storage.getBucketName());
        command.setObjectKey(storage.getObjectKey());
        command.setSize(storage.getSize());
        command.setAccessEndpoint(storage.getAccessEndpoint());
        command.setObjectStatus(storage.getObjectStatus());
        command.setReferenceStatus(storage.getReferenceStatus());
        command.setRemarks(storage.getRemarks());
        return command;
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
