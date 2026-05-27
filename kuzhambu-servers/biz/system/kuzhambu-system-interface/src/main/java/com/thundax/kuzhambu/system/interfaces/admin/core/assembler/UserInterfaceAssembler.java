package com.thundax.kuzhambu.system.interfaces.admin.core.assembler;

import com.thundax.kuzhambu.system.application.core.entity.Department;
import com.thundax.kuzhambu.system.application.core.entity.Role;
import com.thundax.kuzhambu.system.application.core.entity.User;
import com.thundax.kuzhambu.system.application.core.entity.enums.UserPrivilege;
import com.thundax.kuzhambu.system.application.core.entity.enums.UserStatus;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeUserInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.CreateUserCommand;
import com.thundax.kuzhambu.system.application.core.service.query.UserQuery;
import com.thundax.kuzhambu.system.domain.core.codec.AccessRankCodec;
import com.thundax.kuzhambu.system.domain.core.codec.DepartmentIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.RoleIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.model.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.domain.model.valueobject.RoleId;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.UserDepartmentResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.UserResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.UserRoleResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class UserInterfaceAssembler {
    private UserInterfaceAssembler() {}

    @NonNull
    public static UserResponse toResponse(
            User entity,
            String loginName,
            Department department,
            List<Role> roleList,
            String avatarUrl,
            Function<DepartmentId, Department> departmentLoader) {
        if (entity == null) {
            return UserResponse.builder().build();
        }

        return UserResponse.builder()
                .id(UserIdCodec.toStringValue(entity.getId()))
                .remarks(entity.getRemarks())
                .loginName(loginName)
                .ranks(AccessRankCodec.toValue(entity.getRank()))
                .name(entity.getName())
                .email(entity.getEmail())
                .mobile(entity.getMobile())
                .avatar(avatarUrl)
                .superAdmin(entity.isSuper())
                .admin(entity.isAdmin())
                .enable(entity.isEnable())
                .department(toDepartmentResponse(department, departmentLoader))
                .roleList(
                        roleList == null
                                ? new ArrayList<>()
                                : roleList.stream()
                                        .map(UserInterfaceAssembler::toRoleResponse)
                                        .collect(Collectors.toList()))
                .build();
    }

    @NonNull
    public static UserDepartmentResponse toDepartmentResponse(
            Department entity, Function<DepartmentId, Department> departmentLoader) {
        if (entity == null) {
            return UserDepartmentResponse.builder().build();
        }

        return UserDepartmentResponse.builder()
                .id(DepartmentIdCodec.toStringValue(entity.getId()))
                .parentId(DepartmentIdCodec.toStringValue(entity.getParentId()))
                .name(entity.getName())
                .namePath(namePath(entity, departmentLoader))
                .build();
    }

    @NonNull
    public static UserRoleResponse toRoleResponse(Role entity) {
        if (entity == null) {
            return UserRoleResponse.builder().build();
        }

        return UserRoleResponse.builder()
                .id(RoleIdCodec.toStringValue(entity.getId()))
                .name(entity.getName())
                .build();
    }

    @NonNull
    public static UserQuery toQuery(@NonNull UserQueryRequest request) {
        UserQuery query = new UserQuery();
        query.setDepartmentId(DepartmentIdCodec.toDomain(request.getDepartmentId()));
        query.setLoginName(emptyToNull(request.getLoginName()));
        query.setName(emptyToNull(request.getName()));
        if (request.getEnable() != null) {
            query.setStatus(request.getEnable() ? UserStatus.ENABLED : UserStatus.DISABLED);
        }
        query.setOrderBy(emptyToNull(request.getOrderBy()));
        return query;
    }

    @NonNull
    public static CreateUserCommand toCreateCommand(@NonNull UserSaveRequest request, String encryptedPassword) {
        User entity = toEntity(new User(), request);
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
        User entity = toEntity(new User(), request);
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
    public static User toEntity(@NonNull User entity, @NonNull UserSaveRequest request) {
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
                node = departmentLoader.apply(node.getParentId());
            }
        }
        return StringUtils.join(names, "/");
    }

    private static String emptyToNull(String value) {
        return StringUtils.isEmpty(value) ? null : value;
    }
}
