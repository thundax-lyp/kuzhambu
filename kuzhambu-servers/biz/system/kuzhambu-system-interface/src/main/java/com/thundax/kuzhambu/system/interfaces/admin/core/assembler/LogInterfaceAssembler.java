package com.thundax.kuzhambu.system.interfaces.admin.core.assembler;

import com.thundax.kuzhambu.system.application.core.command.CreateLogCommand;
import com.thundax.kuzhambu.system.application.core.command.DeleteLogCommand;
import com.thundax.kuzhambu.system.application.core.query.GetUserQuery;
import com.thundax.kuzhambu.system.application.core.query.LogQuery;
import com.thundax.kuzhambu.system.domain.core.codec.DepartmentIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.LogIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Department;
import com.thundax.kuzhambu.system.domain.core.model.entity.Log;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.LogPageRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.LogDepartmentResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.LogResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.LogUserResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        return new LogQuery(
                null,
                request.getRemoteAddr(),
                request.getTitle(),
                request.getRequestUri(),
                request.getUserLoginName(),
                request.getUserName(),
                request.getBeginDate(),
                request.getEndDate());
    }

    @NonNull
    public static CreateLogCommand toCreateCommand(@NonNull Log entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return new CreateLogCommand(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getLogDate(),
                entity.getTitle(),
                entity.getRemoteAddr(),
                entity.getUserAgent(),
                entity.getMethod(),
                entity.getRequestUri(),
                entity.getRequestParams(),
                entity.getRemarks());
    }

    @NonNull
    public static DeleteLogCommand toDeleteCommand(Instant beginDate, Instant endDate) {
        return new DeleteLogCommand(new LogQuery(null, null, null, null, null, null, beginDate, endDate));
    }

    @NonNull
    public static GetUserQuery toUserQuery(@NonNull UserId userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return new GetUserQuery(userId);
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
