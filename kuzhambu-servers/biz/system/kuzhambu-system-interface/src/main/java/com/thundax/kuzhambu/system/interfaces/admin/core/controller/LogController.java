package com.thundax.kuzhambu.system.interfaces.admin.core.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityApplicationService;
import com.thundax.kuzhambu.system.application.core.query.GetDepartmentQuery;
import com.thundax.kuzhambu.system.application.core.query.GetUserQuery;
import com.thundax.kuzhambu.system.application.core.query.LogQuery;
import com.thundax.kuzhambu.system.application.core.service.DepartmentManagementApplicationService;
import com.thundax.kuzhambu.system.application.core.service.SystemLogApplicationService;
import com.thundax.kuzhambu.system.application.core.service.UserManagementApplicationService;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Department;
import com.thundax.kuzhambu.system.domain.core.model.entity.Log;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.interfaces.admin.core.assembler.LogInterfaceAssembler;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.LogPageRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.LogResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "系统模块-系统日志", description = "系统日志")
@SysLogger(module = {"系统", "日志"})
@RequestMapping(value = "/api/sys/log")
@WrappedApiController
public class LogController {

    private final SystemLogApplicationService logService;
    private final UserManagementApplicationService userService;
    private final PrincipalIdentityApplicationService principalIdentityService;
    private final DepartmentManagementApplicationService departmentService;

    @Autowired
    public LogController(
            SystemLogApplicationService logService,
            UserManagementApplicationService userService,
            PrincipalIdentityApplicationService principalIdentityService,
            DepartmentManagementApplicationService departmentService) {
        this.logService = logService;
        this.userService = userService;
        this.principalIdentityService = principalIdentityService;
        this.departmentService = departmentService;
    }

    @Operation(summary = "获取列表", description = "system:log:view")
    @HasPermission(value = "system:log:view")
    @IgnoreSysLogger
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
        Department department = user == null ? null : getDepartment(user.getDepartmentId());
        return LogInterfaceAssembler.toResponse(log, user, getAccountLoginName(user), department, this::getDepartment);
    }

    private User getLogUser(Log log) {
        if (log == null || log.getUserId() == null) {
            return null;
        }
        return userService.get(new GetUserQuery(log.getUserId()));
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
        return new PrincipalIdentityQuery(null, identityType, null, principalKey, null);
    }

    private Department getDepartment(DepartmentId departmentId) {
        return departmentService.get(new GetDepartmentQuery(departmentId));
    }
}
