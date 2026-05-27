package com.thundax.kuzhambu.system.interfaces.admin.core.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityService;
import com.thundax.kuzhambu.system.application.auth.service.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.core.entity.Department;
import com.thundax.kuzhambu.system.application.core.entity.Log;
import com.thundax.kuzhambu.system.application.core.entity.User;
import com.thundax.kuzhambu.system.application.core.service.DepartmentService;
import com.thundax.kuzhambu.system.application.core.service.LogService;
import com.thundax.kuzhambu.system.application.core.service.UserService;
import com.thundax.kuzhambu.system.application.core.service.query.LogQuery;
import com.thundax.kuzhambu.system.domain.auth.enums.PrincipalType;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.interfaces.admin.core.assembler.LogInterfaceAssembler;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.LogPageRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.LogResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "系统/日志")
@RequestMapping(value = "/api/sys/log")
@WrappedApiController
public class LogController {

    private final LogService logService;
    private final UserService userService;
    private final PrincipalIdentityService principalIdentityService;
    private final DepartmentService departmentService;

    @Autowired
    public LogController(
            LogService logService,
            UserService userService,
            PrincipalIdentityService principalIdentityService,
            DepartmentService departmentService) {
        this.logService = logService;
        this.userService = userService;
        this.principalIdentityService = principalIdentityService;
        this.departmentService = departmentService;
    }

    @Operation(summary = "获取列表", description = "super")
    @HasPermission(value = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "page")
    public PageResponse<LogResponse> page(@Valid @RequestBody LogPageRequest request) {
        LogQuery query = LogInterfaceAssembler.toQuery(request);

        return PageResponseHelper.fromPageResult(
                logService.page(query, PageInterfaceAssembler.toPageQuery(request)), this::toResponse);
    }

    private LogResponse toResponse(Log log) {
        User user = getLogUser(log);
        Department department = user == null ? null : departmentService.get(user.getDepartmentId());
        return LogInterfaceAssembler.toResponse(
                log, user, getAccountLoginName(user), department, departmentService::get);
    }

    private User getLogUser(Log log) {
        if (log == null || StringUtils.isBlank(log.getUserId())) {
            return null;
        }
        try {
            return userService.get(UserIdCodec.toDomain(Long.valueOf(log.getUserId())));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String getAccountLoginName(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        PrincipalIdentity identity = principalIdentityService.get(identityQuery(
                PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(user.getId())),
                PrincipalIdentityType.USER_ACCOUNT));
        return identity == null ? null : identity.getIdentityValue();
    }

    private PrincipalIdentityQuery identityQuery(PrincipalKey principalKey, PrincipalIdentityType identityType) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setPrincipalKey(principalKey);
        query.setIdentityType(identityType);
        return query;
    }
}
