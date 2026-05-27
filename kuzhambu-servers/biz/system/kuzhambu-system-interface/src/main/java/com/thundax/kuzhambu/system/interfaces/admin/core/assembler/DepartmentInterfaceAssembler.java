package com.thundax.kuzhambu.system.interfaces.admin.core.assembler;

import com.thundax.kuzhambu.system.application.core.entity.Department;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeDepartmentInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.CreateDepartmentCommand;
import com.thundax.kuzhambu.system.application.core.service.query.DepartmentQuery;
import com.thundax.kuzhambu.system.domain.core.codec.DepartmentIdCodec;
import com.thundax.kuzhambu.system.domain.core.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DepartmentQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DepartmentSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.DepartmentResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class DepartmentInterfaceAssembler {
    private DepartmentInterfaceAssembler() {}

    @NonNull
    public static DepartmentResponse toResponse(
            Department entity, Function<DepartmentId, Department> departmentLoader) {
        if (entity == null) {
            return DepartmentResponse.builder().build();
        }
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
    public static DepartmentResponse toTreeResponse(Department entity) {
        if (entity == null) {
            return DepartmentResponse.builder().build();
        }
        return DepartmentResponse.builder()
                .id(DepartmentIdCodec.toStringValue(entity.getId()))
                .parentId(DepartmentIdCodec.toStringValue(entity.getParentId()))
                .name(entity.getName())
                .shortName(entity.getShortName())
                .build();
    }

    @NonNull
    public static DepartmentQuery toQuery(@NonNull DepartmentQueryRequest request) {
        DepartmentQuery query = new DepartmentQuery();
        query.setParentId(DepartmentIdCodec.toDomain(request.getParentId()));
        query.setName(request.getName());
        query.setRemarks(request.getRemarks());
        return query;
    }

    @NonNull
    public static Department toEntity(@NonNull Department entity, @NonNull DepartmentSaveRequest request) {
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
        Department entity = toEntity(new Department(), request);
        return new CreateDepartmentCommand(
                entity.getId(), entity.getParentId(), entity.getName(), entity.getShortName(), entity.getRemarks());
    }

    @NonNull
    public static ChangeDepartmentInfoCommand toChangeInfoCommand(@NonNull DepartmentSaveRequest request) {
        Department entity = toEntity(new Department(), request);
        return new ChangeDepartmentInfoCommand(
                entity.getId(), entity.getParentId(), entity.getName(), entity.getShortName(), entity.getRemarks());
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
