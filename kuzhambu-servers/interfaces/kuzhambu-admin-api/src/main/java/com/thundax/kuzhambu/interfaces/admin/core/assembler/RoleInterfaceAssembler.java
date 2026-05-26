package com.thundax.kuzhambu.interfaces.admin.core.assembler;

import com.thundax.kuzhambu.biz.core.entity.Department;
import com.thundax.kuzhambu.biz.core.entity.Menu;
import com.thundax.kuzhambu.biz.core.entity.Role;
import com.thundax.kuzhambu.biz.core.entity.User;
import com.thundax.kuzhambu.biz.core.entity.enums.RolePrivilege;
import com.thundax.kuzhambu.biz.core.entity.enums.RoleStatus;
import com.thundax.kuzhambu.biz.core.entity.valueobject.DepartmentId;
import com.thundax.kuzhambu.biz.core.entity.valueobject.DepartmentIdCodec;
import com.thundax.kuzhambu.biz.core.entity.valueobject.MenuId;
import com.thundax.kuzhambu.biz.core.entity.valueobject.MenuIdCodec;
import com.thundax.kuzhambu.biz.core.entity.valueobject.RoleIdCodec;
import com.thundax.kuzhambu.biz.core.entity.valueobject.UserIdCodec;
import com.thundax.kuzhambu.biz.core.service.command.ChangeRoleInfoCommand;
import com.thundax.kuzhambu.biz.core.service.command.CreateRoleCommand;
import com.thundax.kuzhambu.biz.core.service.query.RoleQuery;
import com.thundax.kuzhambu.interfaces.admin.core.controller.request.RoleQueryRequest;
import com.thundax.kuzhambu.interfaces.admin.core.controller.request.RoleSaveRequest;
import com.thundax.kuzhambu.interfaces.admin.core.controller.response.RoleDepartmentResponse;
import com.thundax.kuzhambu.interfaces.admin.core.controller.response.RoleMenuResponse;
import com.thundax.kuzhambu.interfaces.admin.core.controller.response.RoleResponse;
import com.thundax.kuzhambu.interfaces.admin.core.controller.response.RoleUserResponse;
import com.thundax.kuzhambu.interfaces.admin.core.controller.response.RoleUserTreeNodeResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class RoleInterfaceAssembler {
    private RoleInterfaceAssembler() {}

    @NonNull
    public static RoleResponse toResponse(Role entity, List<Menu> menuList) {
        if (entity == null) {
            return RoleResponse.builder().build();
        }

        return RoleResponse.builder()
                .id(RoleIdCodec.toStringValue(entity.getId()))
                .remarks(entity.getRemarks())
                .name(entity.getName())
                .admin(entity.isAdmin())
                .enable(entity.isEnable())
                .menuList(
                        menuList == null
                                ? new ArrayList<>()
                                : menuList.stream()
                                        .map(RoleInterfaceAssembler::toMenuResponse)
                                        .collect(Collectors.toList()))
                .build();
    }

    @NonNull
    public static RoleMenuResponse toMenuResponse(Menu entity) {
        if (entity == null) {
            return RoleMenuResponse.builder().build();
        }

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
            User entity, String loginName, Department department, Function<DepartmentId, Department> departmentLoader) {
        if (entity == null) {
            return RoleUserResponse.builder().build();
        }

        return RoleUserResponse.builder()
                .id(UserIdCodec.toStringValue(entity.getId()))
                .name(entity.getName())
                .loginName(loginName)
                .department(toDepartmentResponse(department, departmentLoader))
                .build();
    }

    @NonNull
    public static RoleUserTreeNodeResponse toDepartmentTreeNode(String id, Department entity) {
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
            String departmentIdPrefix,
            User entity,
            String loginName,
            Department department,
            Function<DepartmentId, Department> departmentLoader) {
        return RoleUserTreeNodeResponse.builder()
                .id(UserIdCodec.toStringValue(entity.getId()))
                .parentId(departmentIdPrefix + DepartmentIdCodec.toStringValue(entity.getDepartmentId()))
                .name(entity.getName())
                .user(toUserResponse(entity, loginName, department, departmentLoader))
                .build();
    }

    @NonNull
    public static RoleQuery toQuery(@NonNull RoleQueryRequest request) {
        RoleQuery query = new RoleQuery();
        if (request.getEnable() != null) {
            query.setStatus(request.getEnable() ? RoleStatus.ENABLED : RoleStatus.DISABLED);
        }
        return query;
    }

    @NonNull
    public static CreateRoleCommand toCreateCommand(@NonNull RoleSaveRequest request) {
        CreateRoleCommand command = new CreateRoleCommand();
        command.setId(RoleIdCodec.toDomain(request.getId()));
        command.setRemarks(request.getRemarks());
        command.setName(request.getName());
        command.setPrivilege(Boolean.TRUE.equals(request.getAdmin()) ? RolePrivilege.ADMIN : RolePrivilege.NORMAL);
        command.setStatus(Boolean.TRUE.equals(request.getEnable()) ? RoleStatus.ENABLED : RoleStatus.DISABLED);
        command.setMenuIdList(toMenuIds(request));
        return command;
    }

    @NonNull
    public static ChangeRoleInfoCommand toChangeInfoCommand(@NonNull RoleSaveRequest request) {
        ChangeRoleInfoCommand command = new ChangeRoleInfoCommand();
        command.setId(RoleIdCodec.toDomain(request.getId()));
        command.setRemarks(request.getRemarks());
        command.setName(request.getName());
        command.setPrivilege(Boolean.TRUE.equals(request.getAdmin()) ? RolePrivilege.ADMIN : RolePrivilege.NORMAL);
        command.setStatus(Boolean.TRUE.equals(request.getEnable()) ? RoleStatus.ENABLED : RoleStatus.DISABLED);
        command.setMenuIdList(toMenuIds(request));
        return command;
    }

    @NonNull
    public static Role toEntity(@NonNull Role entity, @NonNull RoleSaveRequest request) {
        entity.setId(RoleIdCodec.toDomain(request.getId()));
        entity.setRemarks(request.getRemarks());
        entity.setName(request.getName());
        entity.setPrivilege(Boolean.TRUE.equals(request.getAdmin()) ? RolePrivilege.ADMIN : RolePrivilege.NORMAL);
        entity.setStatus(Boolean.TRUE.equals(request.getEnable()) ? RoleStatus.ENABLED : RoleStatus.DISABLED);
        entity.setMenuIdList(
                request.getMenuList() == null
                        ? new ArrayList<>()
                        : request.getMenuList().stream()
                                .map(menu -> MenuIdCodec.toValue(MenuIdCodec.toDomain(menu.getId())))
                                .collect(Collectors.toList()));
        return entity;
    }

    private static List<MenuId> toMenuIds(RoleSaveRequest request) {
        return request.getMenuList() == null
                ? new ArrayList<>()
                : request.getMenuList().stream()
                        .map(menu -> MenuIdCodec.toDomain(menu.getId()))
                        .collect(Collectors.toList());
    }

    @NonNull
    private static RoleDepartmentResponse toDepartmentResponse(
            Department entity, Function<DepartmentId, Department> departmentLoader) {
        if (entity == null) {
            return RoleDepartmentResponse.builder().build();
        }

        return RoleDepartmentResponse.builder()
                .id(DepartmentIdCodec.toStringValue(entity.getId()))
                .name(entity.getName())
                .namePath(namePath(entity, departmentLoader))
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
                node = departmentLoader.apply(node.getParentId());
            }
        }
        return StringUtils.join(names, "/");
    }
}
