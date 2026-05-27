package com.thundax.kuzhambu.system.interfaces.admin.core.assembler;

import com.thundax.kuzhambu.system.application.core.service.query.LogQuery;
import com.thundax.kuzhambu.system.domain.core.codec.DepartmentIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.LogIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Department;
import com.thundax.kuzhambu.system.domain.core.model.entity.Log;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.LogPageRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.LogDepartmentResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.LogResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.LogUserResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class LogInterfaceAssembler {
    private LogInterfaceAssembler() {}

    @NonNull
    public static LogResponse toResponse(
            Log entity,
            User user,
            String loginName,
            Department department,
            Function<DepartmentId, Department> departmentLoader) {
        if (entity == null) {
            return LogResponse.builder().build();
        }
        return LogResponse.builder()
                .id(LogIdCodec.toStringValue(entity.getId()))
                .remarks(entity.getRemarks())
                .createDate(entity.getLogDate())
                .type(entity.getType() == null ? null : entity.getType().value())
                .title(entity.getTitle())
                .remoteAddr(entity.getRemoteAddr())
                .userAgent(entity.getUserAgent())
                .method(entity.getMethod())
                .requestUri(entity.getRequestUri())
                .requestParams(entity.getRequestParams())
                .createUser(toUserResponse(user, loginName, department, departmentLoader))
                .build();
    }

    @NonNull
    public static LogQuery toQuery(@NonNull LogPageRequest request) {
        LogQuery query = new LogQuery();
        query.setTitle(request.getTitle());
        query.setRemoteAddr(request.getRemoteAddr());
        query.setRequestUri(request.getRequestUri());
        query.setUserLoginName(request.getUserLoginName());
        query.setUserName(request.getUserName());
        query.setBeginDate(request.getBeginDate());
        query.setEndDate(request.getEndDate());
        return query;
    }

    @NonNull
    private static LogUserResponse toUserResponse(
            User entity, String loginName, Department department, Function<DepartmentId, Department> departmentLoader) {
        if (entity == null) {
            return LogUserResponse.builder().build();
        }
        return LogUserResponse.builder()
                .id(UserIdCodec.toStringValue(entity.getId()))
                .loginName(loginName)
                .name(entity.getName())
                .department(toDepartmentResponse(department, departmentLoader))
                .build();
    }

    @NonNull
    private static LogDepartmentResponse toDepartmentResponse(
            Department entity, Function<DepartmentId, Department> departmentLoader) {
        if (entity == null) {
            return LogDepartmentResponse.builder().build();
        }
        return LogDepartmentResponse.builder()
                .id(DepartmentIdCodec.toStringValue(entity.getId()))
                .name(entity.getName())
                .namePath(namePath(entity, departmentLoader))
                .build();
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
