package com.thundax.kuzhambu.system.interfaces.admin.core.assembler;

import com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.core.command.AssignRoleUsersCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeRoleInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeRoleStatusCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateRoleCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveRoleCommand;
import com.thundax.kuzhambu.system.application.core.command.RoleSortCommand;
import com.thundax.kuzhambu.system.application.core.query.GetRoleQuery;
import com.thundax.kuzhambu.system.application.core.query.RoleQuery;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.core.codec.DepartmentIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.MenuIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.RoleIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Department;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.entity.Role;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.enums.RolePrivilege;
import com.thundax.kuzhambu.system.domain.core.model.enums.RoleStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleAssignUserRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleStatusRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.RoleDepartmentResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.RoleMenuResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.RoleResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.RoleUserResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.RoleUserTreeNodeResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class RoleInterfaceAssembler {
    private RoleInterfaceAssembler() {}

    @NonNull
    public static RoleResponse toResponse(@NonNull Role entity, @NonNull List<Menu> menuList) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(menuList, "menuList must not be null");
        return RoleResponse.builder()
                .id(RoleIdCodec.toStringValue(entity.getId()))
                .remarks(entity.getRemarks())
                .name(entity.getName())
                .admin(entity.isAdmin())
                .enable(entity.isEnable())
                .menuList(menuList.stream()
                        .map(RoleInterfaceAssembler::toMenuResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    @NonNull
    public static RoleMenuResponse toMenuResponse(@NonNull Menu entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        String parentId = MenuIdCodec.toStringValue(entity.getParentId());
        return RoleMenuResponse.builder()
                .id(MenuIdCodec.toStringValue(entity.getId()))
                .parentId(parentId)
                .name(entity.getName())
                .perms(entity.getPerms())
                .build();
    }

    @NonNull
    public static RoleUserResponse toUserResponse(
            @NonNull User entity,
            @NonNull Optional<String> loginName,
            @NonNull Optional<Department> department,
            @NonNull Function<DepartmentId, Department> departmentLoader) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(loginName, "loginName must not be null");
        Objects.requireNonNull(department, "department must not be null");
        Objects.requireNonNull(departmentLoader, "departmentLoader must not be null");
        return RoleUserResponse.builder()
                .id(UserIdCodec.toStringValue(entity.getId()))
                .name(entity.getName())
                .loginName(loginName.orElse(null))
                .department(toDepartmentResponse(department, departmentLoader))
                .build();
    }

    @NonNull
    public static RoleUserTreeNodeResponse toDepartmentTreeNode(@NonNull String id, @NonNull Department entity) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(entity, "entity must not be null");
        return RoleUserTreeNodeResponse.builder()
                .id(id)
                .parentId(
                        entity.getParentId() == null
                                ? null
                                : idPrefix(DepartmentIdCodec.toStringValue(entity.getParentId())))
                .name(entity.getName())
                .build();
    }

    @NonNull
    public static RoleUserTreeNodeResponse toUserTreeNode(
            @NonNull String departmentIdPrefix,
            @NonNull User entity,
            @NonNull Optional<String> loginName,
            @NonNull Optional<Department> department,
            @NonNull Function<DepartmentId, Department> departmentLoader) {
        Objects.requireNonNull(departmentIdPrefix, "departmentIdPrefix must not be null");
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(loginName, "loginName must not be null");
        Objects.requireNonNull(department, "department must not be null");
        Objects.requireNonNull(departmentLoader, "departmentLoader must not be null");
        return RoleUserTreeNodeResponse.builder()
                .id(UserIdCodec.toStringValue(entity.getId()))
                .parentId(departmentIdPrefix + DepartmentIdCodec.toStringValue(entity.getDepartmentId()))
                .name(entity.getName())
                .user(toUserResponse(entity, loginName, department, departmentLoader))
                .build();
    }

    @NonNull
    public static RoleQuery toQuery(@NonNull RoleQueryRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new RoleQuery(
                null,
                request.getEnable() == null ? null : request.getEnable() ? RoleStatus.ENABLED : RoleStatus.DISABLED);
    }

    @NonNull
    public static RoleQuery toIdQuery(@NonNull RoleId id) {
        Objects.requireNonNull(id, "id must not be null");
        return new RoleQuery(id, null);
    }

    @NonNull
    public static RoleQuery toEnabledQuery() {
        return new RoleQuery(null, RoleStatus.ENABLED);
    }

    @NonNull
    public static GetRoleQuery toGetQuery(@NonNull RoleId id) {
        Objects.requireNonNull(id, "id must not be null");
        return new GetRoleQuery(id);
    }

    @NonNull
    public static PrincipalIdentityQuery toPrincipalIdentityQuery(
            @NonNull PrincipalKey principalKey, @NonNull PrincipalIdentityType identityType) {
        Objects.requireNonNull(principalKey, "principalKey must not be null");
        Objects.requireNonNull(identityType, "identityType must not be null");
        return new PrincipalIdentityQuery(null, identityType, null, principalKey, null);
    }

    @NonNull
    public static CreateRoleCommand toCreateCommand(@NonNull RoleSaveRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new CreateRoleCommand(
                RoleIdCodec.toDomain(request.getId()),
                request.getName(),
                Boolean.TRUE.equals(request.getAdmin()) ? RolePrivilege.ADMIN : RolePrivilege.NORMAL,
                Boolean.TRUE.equals(request.getEnable()) ? RoleStatus.ENABLED : RoleStatus.DISABLED,
                request.getRemarks(),
                toMenuIds(request));
    }

    @NonNull
    public static ChangeRoleInfoCommand toChangeInfoCommand(@NonNull RoleSaveRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ChangeRoleInfoCommand(
                RoleIdCodec.toDomain(request.getId()),
                request.getName(),
                Boolean.TRUE.equals(request.getAdmin()) ? RolePrivilege.ADMIN : RolePrivilege.NORMAL,
                Boolean.TRUE.equals(request.getEnable()) ? RoleStatus.ENABLED : RoleStatus.DISABLED,
                request.getRemarks(),
                toMenuIds(request));
    }

    @NonNull
    public static ChangeRoleStatusCommand toChangeStatusCommand(
            @NonNull Role entity, @NonNull RoleStatusRequest request) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(request, "request must not be null");
        return new ChangeRoleStatusCommand(
                entity.getId(), Boolean.TRUE.equals(request.getEnable()) ? RoleStatus.ENABLED : RoleStatus.DISABLED);
    }

    @NonNull
    public static RoleSortCommand toSortCommand(@NonNull List<Long> orderedIds) {
        Objects.requireNonNull(orderedIds, "orderedIds must not be null");
        return new RoleSortCommand(
                orderedIds.stream().map(RoleIdCodec::toDomain).collect(Collectors.toList()));
    }

    @NonNull
    public static RemoveRoleCommand toRemoveCommand(@NonNull RoleId id) {
        Objects.requireNonNull(id, "id must not be null");
        return new RemoveRoleCommand(id);
    }

    @NonNull
    public static AssignRoleUsersCommand toAssignUsersCommand(@NonNull RoleAssignUserRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new AssignRoleUsersCommand(RoleIdCodec.toDomain(request.getRoleId()), toUserIds(request));
    }

    @NonNull
    public static Role toDomain(@NonNull Role entity, @NonNull RoleSaveRequest request) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(request, "request must not be null");
        entity.setId(RoleIdCodec.toDomain(request.getId()));
        entity.setRemarks(request.getRemarks());
        entity.setName(request.getName());
        entity.setPrivilege(Boolean.TRUE.equals(request.getAdmin()) ? RolePrivilege.ADMIN : RolePrivilege.NORMAL);
        entity.setStatus(Boolean.TRUE.equals(request.getEnable()) ? RoleStatus.ENABLED : RoleStatus.DISABLED);
        return entity;
    }

    private static List<MenuId> toMenuIds(RoleSaveRequest request) {
        return request.getMenuList() == null
                ? new ArrayList<>()
                : request.getMenuList().stream()
                        .map(menu -> MenuIdCodec.toDomain(menu.getId()))
                        .collect(Collectors.toList());
    }

    private static List<UserId> toUserIds(RoleAssignUserRequest request) {
        return request.getUsers() == null
                ? new ArrayList<>()
                : request.getUsers().stream()
                        .map(user -> UserIdCodec.toDomain(user.getId()))
                        .collect(Collectors.toList());
    }

    @NonNull
    private static RoleDepartmentResponse toDepartmentResponse(
            Optional<Department> entity, Function<DepartmentId, Department> departmentLoader) {
        if (entity.isEmpty()) {
            return RoleDepartmentResponse.builder().build();
        }
        Department department = entity.get();

        return RoleDepartmentResponse.builder()
                .id(DepartmentIdCodec.toStringValue(department.getId()))
                .name(department.getName())
                .namePath(namePath(department, departmentLoader))
                .build();
    }

    private static String idPrefix(String id) {
        return "DEPARTMENT_" + id;
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
}
