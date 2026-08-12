package com.thundax.kuzhambu.system.interfaces.admin.core.assembler;

import com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalCredentialCommand;
import com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalIdentityCommand;
import com.thundax.kuzhambu.system.application.auth.command.CreatePrincipalCredentialCommand;
import com.thundax.kuzhambu.system.application.auth.command.CreatePrincipalIdentityCommand;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionQuery;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueQuery;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalCredentialQuery;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeUserAccountCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeUserInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeUserStatusCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateUserAccountCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateUserCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveUserCommand;
import com.thundax.kuzhambu.system.application.core.query.CurrentUserAvatarQuery;
import com.thundax.kuzhambu.system.application.core.query.GetUserQuery;
import com.thundax.kuzhambu.system.application.core.query.UserQuery;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalCredential;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionToken;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.core.codec.AccessRankCodec;
import com.thundax.kuzhambu.system.domain.core.codec.DepartmentIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.RoleIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Department;
import com.thundax.kuzhambu.system.domain.core.model.entity.Role;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserPrivilege;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserStatusRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.UserDepartmentResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.UserResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.UserRoleResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class UserInterfaceAssembler {
    private UserInterfaceAssembler() {}

    @NonNull
    public static CreatePrincipalCredentialCommand toCreatePrincipalCredentialCommand(
            @NonNull PrincipalCredential credential) {
        Objects.requireNonNull(credential, "credential must not be null");
        return new CreatePrincipalCredentialCommand(
                credential.getPrincipalKey(),
                credential.getIdentityId(),
                credential.getCredentialType(),
                credential.getCredentialValue(),
                credential.getStatus(),
                credential.isNeedChangePassword(),
                credential.getFailedCount(),
                credential.getFailedLimit(),
                credential.getLockedUntil(),
                credential.getExpiresAt(),
                credential.getLastVerifiedAt());
    }

    @NonNull
    public static ChangePrincipalCredentialCommand toChangePrincipalCredentialCommand(
            @NonNull PrincipalCredential credential) {
        Objects.requireNonNull(credential, "credential must not be null");
        return new ChangePrincipalCredentialCommand(
                credential.getId(),
                credential.getPrincipalKey(),
                credential.getIdentityId(),
                credential.getCredentialType(),
                credential.getCredentialValue(),
                credential.getStatus(),
                credential.isNeedChangePassword(),
                credential.getFailedCount(),
                credential.getFailedLimit(),
                credential.getLockedUntil(),
                credential.getExpiresAt(),
                credential.getLastVerifiedAt());
    }

    @NonNull
    public static PrincipalCredentialQuery toPrincipalCredentialQuery(
            @NonNull PrincipalIdentityId identityId, @NonNull PrincipalCredentialType credentialType) {
        Objects.requireNonNull(identityId, "identityId must not be null");
        Objects.requireNonNull(credentialType, "credentialType must not be null");
        return new PrincipalCredentialQuery(null, identityId, credentialType, null, null);
    }

    @NonNull
    public static CreatePrincipalIdentityCommand toCreatePrincipalIdentityCommand(@NonNull PrincipalIdentity identity) {
        Objects.requireNonNull(identity, "identity must not be null");
        return new CreatePrincipalIdentityCommand(
                identity.getPrincipalKey(), identity.getType(), identity.getIdentityValue(), identity.getStatus());
    }

    @NonNull
    public static ChangePrincipalIdentityCommand toChangePrincipalIdentityCommand(@NonNull PrincipalIdentity identity) {
        Objects.requireNonNull(identity, "identity must not be null");
        return new ChangePrincipalIdentityCommand(
                identity.getId(),
                identity.getPrincipalKey(),
                identity.getType(),
                identity.getIdentityValue(),
                identity.getStatus());
    }

    @NonNull
    public static PrincipalIdentityQuery toPrincipalIdentityQuery(
            @NonNull PrincipalIdentityType identityType, @NonNull String identityValue) {
        Objects.requireNonNull(identityType, "identityType must not be null");
        Objects.requireNonNull(identityValue, "identityValue must not be null");
        return new PrincipalIdentityQuery(null, identityType, identityValue, null, null);
    }

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
    public static UserResponse toResponse(
            @NonNull User entity,
            @NonNull Optional<String> loginName,
            @NonNull Optional<Department> department,
            @NonNull List<Role> roleList,
            @NonNull Optional<String> avatarUrl,
            @NonNull Function<DepartmentId, Department> departmentLoader) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(loginName, "loginName must not be null");
        Objects.requireNonNull(department, "department must not be null");
        Objects.requireNonNull(roleList, "roleList must not be null");
        Objects.requireNonNull(avatarUrl, "avatarUrl must not be null");
        Objects.requireNonNull(departmentLoader, "departmentLoader must not be null");
        return UserResponse.builder()
                .id(UserIdCodec.toStringValue(entity.getId()))
                .remarks(entity.getRemarks())
                .loginName(loginName.orElse(null))
                .ranks(AccessRankCodec.toValue(entity.getRank()))
                .name(entity.getName())
                .email(entity.getEmail())
                .mobile(entity.getMobile())
                .avatar(avatarUrl.orElse(null))
                .superAdmin(entity.isSuper())
                .admin(entity.isAdmin())
                .enable(entity.isEnable())
                .department(toOptionalDepartmentResponse(department, departmentLoader))
                .roleList(roleList.stream()
                        .map(UserInterfaceAssembler::toRoleResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    @NonNull
    public static UserDepartmentResponse toDepartmentResponse(
            @NonNull Department entity, @NonNull Function<DepartmentId, Department> departmentLoader) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(departmentLoader, "departmentLoader must not be null");
        return UserDepartmentResponse.builder()
                .id(DepartmentIdCodec.toStringValue(entity.getId()))
                .parentId(DepartmentIdCodec.toStringValue(entity.getParentId()))
                .name(entity.getName())
                .namePath(namePath(entity, departmentLoader))
                .build();
    }

    @NonNull
    private static UserDepartmentResponse toOptionalDepartmentResponse(
            Optional<Department> entity, Function<DepartmentId, Department> departmentLoader) {
        return entity.map(department -> toDepartmentResponse(department, departmentLoader))
                .orElseGet(() -> UserDepartmentResponse.builder().build());
    }

    @NonNull
    public static UserRoleResponse toRoleResponse(@NonNull Role entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return UserRoleResponse.builder()
                .id(RoleIdCodec.toStringValue(entity.getId()))
                .name(entity.getName())
                .build();
    }

    @NonNull
    public static UserQuery toQuery(@NonNull UserQueryRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return toQuery(request, Optional.ofNullable(DepartmentIdCodec.toDomain(request.getDepartmentId())));
    }

    @NonNull
    public static UserQuery toQuery(@NonNull UserQueryRequest request, @NonNull Optional<DepartmentId> departmentId) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(departmentId, "departmentId must not be null");
        return new UserQuery(
                null,
                departmentId.orElse(null),
                emptyToNull(request.getLoginName()),
                null,
                null,
                emptyToNull(request.getName()),
                request.getEnable() == null ? null : request.getEnable() ? UserStatus.ENABLED : UserStatus.DISABLED,
                null,
                emptyToNull(request.getOrderBy()),
                null);
    }

    @NonNull
    public static UserQuery toListAllQuery() {
        return new UserQuery(null, null, null, null, null, null, null, null, null, null);
    }

    @NonNull
    public static UserQuery toUserRolesQuery(@NonNull UserId userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return new UserQuery(userId, null, null, null, null, null, null, null, null, null);
    }

    @NonNull
    public static UserQuery toEmailQuery(@NonNull String email, @NonNull Optional<UserId> excludedId) {
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(excludedId, "excludedId must not be null");
        return new UserQuery(null, null, null, email, null, null, null, null, null, excludedId.orElse(null));
    }

    @NonNull
    public static UserQuery toMobileQuery(@NonNull String mobile, @NonNull Optional<UserId> excludedId) {
        Objects.requireNonNull(mobile, "mobile must not be null");
        Objects.requireNonNull(excludedId, "excludedId must not be null");
        return new UserQuery(null, null, null, null, mobile, null, null, null, null, excludedId.orElse(null));
    }

    @NonNull
    public static GetUserQuery toGetQuery(@NonNull UserId userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return new GetUserQuery(userId);
    }

    @NonNull
    public static ChangeUserStatusCommand toChangeStatusCommand(
            @NonNull User user, @NonNull UserStatusRequest request) {
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(request, "request must not be null");
        UserStatus status = Boolean.TRUE.equals(request.getEnable()) ? UserStatus.ENABLED : UserStatus.DISABLED;
        return new ChangeUserStatusCommand(user.getId(), status, user, auditAfterUser(user.getId(), status, user));
    }

    private static User auditAfterUser(UserId id, UserStatus status, User beforeUser) {
        User user = new User();
        if (beforeUser != null) {
            user.setId(beforeUser.getId());
            user.setName(beforeUser.getName());
            user.setPrivilege(beforeUser.getPrivilege());
        } else {
            user.setId(id);
        }
        user.setStatus(status);
        return user;
    }

    @NonNull
    public static RemoveUserCommand toRemoveCommand(@NonNull UserId userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return new RemoveUserCommand(userId);
    }

    @NonNull
    public static ChangeCurrentUserAvatarCommand toChangeCurrentUserAvatarCommand(
            @NonNull UserId userId, @NonNull InputStream inputStream, @NonNull Optional<String> originalFilename) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        Objects.requireNonNull(originalFilename, "originalFilename must not be null");
        return new ChangeCurrentUserAvatarCommand(userId, inputStream, originalFilename.orElse(null));
    }

    @NonNull
    public static RemoveCurrentUserAvatarCommand toRemoveCurrentUserAvatarCommand(@NonNull UserId userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return new RemoveCurrentUserAvatarCommand(userId);
    }

    @NonNull
    public static CurrentUserAvatarQuery toCurrentUserAvatarQuery(@NonNull UserId userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return new CurrentUserAvatarQuery(userId);
    }

    @NonNull
    public static CreateUserCommand toCreateCommand(
            @NonNull UserSaveRequest request, @NonNull String encryptedPassword) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(encryptedPassword, "encryptedPassword must not be null");
        User entity = toDomain(new User(), request);
        return new CreateUserCommand(
                entity.getId(),
                entity.getDepartmentId(),
                entity.getEmail(),
                entity.getMobile(),
                entity.getTel(),
                entity.getName(),
                entity.getRank(),
                entity.getPrivilege(),
                entity.getStatus(),
                entity.getRemarks(),
                request.getLoginName(),
                encryptedPassword,
                toRoleIdList(request));
    }

    @NonNull
    public static ChangeUserInfoCommand toChangeInfoCommand(@NonNull UserSaveRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        User entity = toDomain(new User(), request);
        return new ChangeUserInfoCommand(
                entity.getId(),
                entity.getDepartmentId(),
                entity.getEmail(),
                entity.getMobile(),
                entity.getTel(),
                entity.getName(),
                entity.getRank(),
                entity.getPrivilege(),
                entity.getStatus(),
                entity.getRemarks(),
                request.getLoginName(),
                toRoleIdList(request));
    }

    @NonNull
    public static User toDomain(@NonNull User entity, @NonNull UserSaveRequest request) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(request, "request must not be null");
        entity.setId(UserIdCodec.toDomain(request.getId()));
        entity.setRemarks(request.getRemarks());
        if (request.getDepartment() != null) {
            entity.setDepartmentId(
                    DepartmentIdCodec.toDomain(request.getDepartment().getId()));
        }
        entity.setRank(AccessRankCodec.toDomain(request.getRanks()));
        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        entity.setMobile(request.getMobile());
        entity.setPrivilege(Boolean.TRUE.equals(request.getAdmin()) ? UserPrivilege.ADMIN : UserPrivilege.NORMAL);
        entity.setStatus(Boolean.TRUE.equals(request.getEnable()) ? UserStatus.ENABLED : UserStatus.DISABLED);
        return entity;
    }

    @NonNull
    public static List<RoleId> toRoleIdList(@NonNull UserSaveRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return request.getRoleList() == null
                ? new ArrayList<>()
                : request.getRoleList().stream()
                        .map(role -> RoleIdCodec.toDomain(role.getId()))
                        .collect(Collectors.toList());
    }

    private static String namePath(Department department, Function<DepartmentId, Department> departmentLoader) {
        List<String> names = new ArrayList<>();
        Department node = department;
        while (node != null && DepartmentIdCodec.toStringValue(node.getId()) != null) {
            node = departmentLoader.apply(node.getId());
            if (node != null) {
                names.add(0, node.getName());
                node = node.getParentId() == null ? null : departmentLoader.apply(node.getParentId());
            }
        }
        return StringUtils.join(names, "/");
    }

    private static String emptyToNull(String value) {
        return StringUtils.isEmpty(value) ? null : value;
    }

    @NonNull
    public static CreateUserAccountCommand toCreateUserAccountCommand(
            @NonNull UserSaveRequest request, @NonNull String encryptedPassword) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(encryptedPassword, "encryptedPassword must not be null");
        return new CreateUserAccountCommand(
                toCreateCommand(request, encryptedPassword), request.getLoginName(), encryptedPassword);
    }

    @NonNull
    public static ChangeUserAccountCommand toChangeUserAccountCommand(
            @NonNull UserSaveRequest request, @NonNull Optional<String> encryptedPassword, @NonNull UserId userId) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(encryptedPassword, "encryptedPassword must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        return new ChangeUserAccountCommand(
                toChangeInfoCommand(request), userId, request.getLoginName(), encryptedPassword);
    }
}
