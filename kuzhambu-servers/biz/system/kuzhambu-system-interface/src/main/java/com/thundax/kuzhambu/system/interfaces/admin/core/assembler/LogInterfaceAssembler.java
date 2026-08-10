package com.thundax.kuzhambu.system.interfaces.admin.core.assembler;

import com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.core.command.CreateLogCommand;
import com.thundax.kuzhambu.system.application.core.command.DeleteLogCommand;
import com.thundax.kuzhambu.system.application.core.query.GetUserQuery;
import com.thundax.kuzhambu.system.application.core.query.LogQuery;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
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
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class LogInterfaceAssembler {
    private LogInterfaceAssembler() {}

    @NonNull
    public static LogResponse toResponse(
            @NonNull Log entity,
            @NonNull Optional<User> user,
            @NonNull Optional<String> loginName,
            @NonNull Optional<Department> department,
            @NonNull Function<DepartmentId, Department> departmentLoader) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(loginName, "loginName must not be null");
        Objects.requireNonNull(department, "department must not be null");
        Objects.requireNonNull(departmentLoader, "departmentLoader must not be null");
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
        Objects.requireNonNull(request, "request must not be null");
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
    public static DeleteLogCommand toDeleteCommand(@NonNull Instant beginDate, @NonNull Instant endDate) {
        Objects.requireNonNull(beginDate, "beginDate must not be null");
        Objects.requireNonNull(endDate, "endDate must not be null");
        return new DeleteLogCommand(new LogQuery(null, null, null, null, null, null, beginDate, endDate));
    }

    @NonNull
    public static GetUserQuery toUserQuery(@NonNull UserId userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return new GetUserQuery(userId);
    }

    @NonNull
    public static PrincipalIdentityQuery toPrincipalIdentityQuery(
            @NonNull PrincipalKey principalKey, @NonNull PrincipalIdentityType identityType) {
        Objects.requireNonNull(principalKey, "principalKey must not be null");
        Objects.requireNonNull(identityType, "identityType must not be null");
        return new PrincipalIdentityQuery(null, identityType, null, principalKey, null);
    }

    @NonNull
    private static LogUserResponse toUserResponse(
            Optional<User> entity,
            Optional<String> loginName,
            Optional<Department> department,
            Function<DepartmentId, Department> departmentLoader) {
        if (entity.isEmpty()) {
            return LogUserResponse.builder().build();
        }
        User user = entity.get();
        return LogUserResponse.builder()
                .id(UserIdCodec.toStringValue(user.getId()))
                .loginName(loginName.orElse(null))
                .name(user.getName())
                .department(toDepartmentResponse(department, departmentLoader))
                .build();
    }

    @NonNull
    private static LogDepartmentResponse toDepartmentResponse(
            Optional<Department> entity, Function<DepartmentId, Department> departmentLoader) {
        if (entity.isEmpty()) {
            return LogDepartmentResponse.builder().build();
        }
        Department department = entity.get();
        return LogDepartmentResponse.builder()
                .id(DepartmentIdCodec.toStringValue(department.getId()))
                .name(department.getName())
                .namePath(namePath(department, departmentLoader))
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
