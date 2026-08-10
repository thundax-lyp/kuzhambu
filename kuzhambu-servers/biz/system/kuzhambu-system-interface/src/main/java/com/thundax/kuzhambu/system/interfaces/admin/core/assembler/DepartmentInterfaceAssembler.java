package com.thundax.kuzhambu.system.interfaces.admin.core.assembler;

import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import com.thundax.kuzhambu.system.application.core.command.ChangeDepartmentInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateDepartmentCommand;
import com.thundax.kuzhambu.system.application.core.command.MoveDepartmentCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveDepartmentCommand;
import com.thundax.kuzhambu.system.application.core.query.DepartmentQuery;
import com.thundax.kuzhambu.system.application.core.query.GetDepartmentQuery;
import com.thundax.kuzhambu.system.domain.core.codec.DepartmentIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Department;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DepartmentQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DepartmentSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.DepartmentResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class DepartmentInterfaceAssembler {
    private DepartmentInterfaceAssembler() {}

    @NonNull
    public static DepartmentResponse toResponse(
            @NonNull Department entity, @NonNull Function<DepartmentId, Department> departmentLoader) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(departmentLoader, "departmentLoader must not be null");
        return DepartmentResponse.builder()
                .id(DepartmentIdCodec.toStringValue(entity.getId()))
                .remarks(entity.getRemarks())
                .parentId(DepartmentIdCodec.toStringValue(entity.getParentId()))
                .name(entity.getName())
                .shortName(entity.getShortName())
                .namePath(namePath(entity, departmentLoader))
                .build();
    }

    @NonNull
    public static DepartmentResponse toTreeResponse(@NonNull Department entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return DepartmentResponse.builder()
                .id(DepartmentIdCodec.toStringValue(entity.getId()))
                .parentId(DepartmentIdCodec.toStringValue(entity.getParentId()))
                .name(entity.getName())
                .shortName(entity.getShortName())
                .build();
    }

    @NonNull
    public static DepartmentQuery toQuery(@NonNull DepartmentQueryRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new DepartmentQuery(
                null, null, DepartmentIdCodec.toDomain(request.getParentId()), request.getName(), request.getRemarks());
    }

    @NonNull
    public static DepartmentQuery toChildRelationQuery(@NonNull Department child, @NonNull Department ancestor) {
        Objects.requireNonNull(child, "child must not be null");
        Objects.requireNonNull(ancestor, "ancestor must not be null");
        return new DepartmentQuery(child.getId(), ancestor.getId(), null, null, null);
    }

    @NonNull
    public static DepartmentQuery toListAllQuery() {
        return new DepartmentQuery(null, null, null, null, null);
    }

    @NonNull
    public static GetDepartmentQuery toGetQuery(@NonNull DepartmentId id) {
        Objects.requireNonNull(id, "id must not be null");
        return new GetDepartmentQuery(id);
    }

    @NonNull
    public static Department toDomain(@NonNull Department entity, @NonNull DepartmentSaveRequest request) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(request, "request must not be null");
        entity.setId(DepartmentIdCodec.toDomain(request.getId()));
        entity.setRemarks(request.getRemarks());
        if (request.getParentId() != null) {
            entity.setParentId(DepartmentIdCodec.toDomain(request.getParentId()));
        }
        entity.setName(request.getName());
        entity.setShortName(request.getShortName());
        return entity;
    }

    @NonNull
    public static CreateDepartmentCommand toCreateCommand(@NonNull DepartmentSaveRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Department entity = toDomain(new Department(), request);
        return new CreateDepartmentCommand(
                entity.getId(), entity.getParentId(), entity.getName(), entity.getShortName(), entity.getRemarks());
    }

    @NonNull
    public static ChangeDepartmentInfoCommand toChangeInfoCommand(@NonNull DepartmentSaveRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Department entity = toDomain(new Department(), request);
        return new ChangeDepartmentInfoCommand(
                entity.getId(), entity.getParentId(), entity.getName(), entity.getShortName(), entity.getRemarks());
    }

    @NonNull
    public static RemoveDepartmentCommand toRemoveCommand(@NonNull Department entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return new RemoveDepartmentCommand(entity.getId());
    }

    @NonNull
    public static RemoveDepartmentCommand toRemoveCommand(@NonNull DepartmentId id) {
        Objects.requireNonNull(id, "id must not be null");
        return new RemoveDepartmentCommand(id);
    }

    @NonNull
    public static MoveDepartmentCommand toMoveCommand(
            @NonNull Department from, @NonNull Department to, @NonNull TreeNodeMoveType moveType) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        Objects.requireNonNull(moveType, "moveType must not be null");
        return new MoveDepartmentCommand(from.getId(), to.getId(), moveType);
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
