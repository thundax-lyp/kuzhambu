package com.thundax.kuzhambu.system.interfaces.admin.core.assembler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionQuery;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueQuery;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserPasswordCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.query.CurrentUserAvatarQuery;
import com.thundax.kuzhambu.system.application.core.query.CurrentUserQuery;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionToken;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.core.codec.AccessRankCodec;
import com.thundax.kuzhambu.system.domain.core.codec.MenuIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.PersonalInfoUpdateRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalAvatarResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalInfoResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalMenuResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.PersonalPermsResponse;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.lang.NonNull;

public final class PersonalInterfaceAssembler {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PersonalInterfaceAssembler() {}

    @NonNull
    public static PrincipalIdentityQuery toPrincipalIdentityQuery(
            @NonNull PrincipalKey principalKey, @NonNull PrincipalIdentityType identityType) {
        Objects.requireNonNull(principalKey, "principalKey must not be null");
        Objects.requireNonNull(identityType, "identityType must not be null");
        return new PrincipalIdentityQuery(null, identityType, null, principalKey, null);
    }

    @NonNull
    public static PreAuthSessionQuery toPreAuthSessionQuery(@NonNull String token) {
        Objects.requireNonNull(token, "token must not be null");
        return new PreAuthSessionQuery(null, PreAuthSessionToken.of(token), null);
    }

    @NonNull
    public static PreAuthSessionValueQuery toPreAuthSessionValueQuery(
            @NonNull PreAuthSessionId sessionId, @NonNull String item) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(item, "item must not be null");
        return new PreAuthSessionValueQuery(sessionId, item);
    }

    @NonNull
    public static ChangeCurrentUserInfoCommand toChangeCurrentUserInfoCommand(
            @NonNull User currentUser, @NonNull PersonalInfoUpdateRequest request) {
        Objects.requireNonNull(currentUser, "currentUser must not be null");
        Objects.requireNonNull(request, "request must not be null");
        return new ChangeCurrentUserInfoCommand(
                currentUser.getId(),
                currentUser.getDepartmentId(),
                request.getEmail(),
                request.getMobile(),
                currentUser.getTel(),
                request.getName(),
                currentUser.getRank(),
                currentUser.getPrivilege(),
                currentUser.getStatus(),
                currentUser.getRemarks());
    }

    @NonNull
    public static ChangeCurrentUserPasswordCommand toChangeCurrentUserPasswordCommand(
            @NonNull User currentUser, @NonNull String oldPassword, @NonNull String password) {
        Objects.requireNonNull(currentUser, "currentUser must not be null");
        Objects.requireNonNull(oldPassword, "oldPassword must not be null");
        Objects.requireNonNull(password, "password must not be null");
        return new ChangeCurrentUserPasswordCommand(currentUser.getId(), oldPassword, password);
    }

    @NonNull
    public static ChangeCurrentUserAvatarCommand toChangeCurrentUserAvatarCommand(
            @NonNull User currentUser, @NonNull InputStream inputStream, @NonNull String originalFilename) {
        Objects.requireNonNull(currentUser, "currentUser must not be null");
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        Objects.requireNonNull(originalFilename, "originalFilename must not be null");
        return new ChangeCurrentUserAvatarCommand(currentUser.getId(), inputStream, originalFilename);
    }

    @NonNull
    public static RemoveCurrentUserAvatarCommand toRemoveCurrentUserAvatarCommand(@NonNull User currentUser) {
        Objects.requireNonNull(currentUser, "currentUser must not be null");
        return new RemoveCurrentUserAvatarCommand(currentUser.getId());
    }

    @NonNull
    public static CurrentUserAvatarQuery toCurrentUserAvatarQuery(@NonNull User currentUser) {
        Objects.requireNonNull(currentUser, "currentUser must not be null");
        return new CurrentUserAvatarQuery(currentUser.getId());
    }

    @NonNull
    public static CurrentUserQuery toCurrentUserQuery(@NonNull User currentUser) {
        Objects.requireNonNull(currentUser, "currentUser must not be null");
        return new CurrentUserQuery(
                currentUser.getId(), currentUser.getPrivilege(), currentUser.getStatus(), currentUser.getRank());
    }

    @NonNull
    public static PersonalInfoResponse toInfoResponse(
            @NonNull User entity, @NonNull Optional<String> loginName, @NonNull Optional<String> avatarUrl) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(loginName, "loginName must not be null");
        Objects.requireNonNull(avatarUrl, "avatarUrl must not be null");
        return PersonalInfoResponse.builder()
                .id(UserIdCodec.toStringValue(entity.getId()))
                .loginName(loginName.orElse(null))
                .ranks(AccessRankCodec.toValue(entity.getRank()))
                .name(entity.getName())
                .mobile(entity.getMobile())
                .email(entity.getEmail())
                .avatar(avatarUrl.orElse(null))
                .admin(entity.isAdmin())
                .superAdmin(entity.isSuper())
                .build();
    }

    @NonNull
    public static PersonalAvatarResponse toAvatarResponse(@NonNull Optional<String> avatarUrl) {
        Objects.requireNonNull(avatarUrl, "avatarUrl must not be null");
        return PersonalAvatarResponse.builder().avatar(avatarUrl.orElse(null)).build();
    }

    @NonNull
    public static PersonalMenuResponse toMenuResponse(@NonNull Menu entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return PersonalMenuResponse.builder()
                .id(MenuIdCodec.toStringValue(entity.getId()))
                .parentId(MenuIdCodec.toStringValue(entity.getParentId()))
                .name(entity.getName())
                .url(entity.getUrl())
                .icon(readIcon(entity.getDisplayParams()))
                .displayParams(entity.getDisplayParams())
                .build();
    }

    @NonNull
    public static PersonalPermsResponse toPermsResponse(@NonNull Set<String> perms) {
        Objects.requireNonNull(perms, "perms must not be null");
        return PersonalPermsResponse.builder().perms(perms).build();
    }

    @NonNull
    public static User toDomain(@NonNull User entity, @NonNull PersonalInfoUpdateRequest request) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(request, "request must not be null");
        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        entity.setMobile(request.getMobile());
        return entity;
    }

    private static String readIcon(String displayParams) {
        if (displayParams == null || displayParams.trim().isEmpty()) {
            return null;
        }
        try {
            JsonNode icon = OBJECT_MAPPER.readTree(displayParams).get("icon");
            if (icon == null || !icon.isTextual() || icon.asText().trim().isEmpty()) {
                return null;
            }
            return icon.asText().trim();
        } catch (Exception ignored) {
            return null;
        }
    }
}
