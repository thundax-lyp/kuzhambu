package com.thundax.kuzhambu.system.interfaces.admin.core.controller;

import com.thundax.kuzhambu.common.core.crypto.Sm2Crypto;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiResponse;
import com.thundax.kuzhambu.common.web.assembler.OptionInterfaceAssembler;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.system.application.auth.command.PrincipalCredentialCommand;
import com.thundax.kuzhambu.system.application.auth.command.PrincipalIdentityCommand;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueQuery;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalCredentialQuery;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.auth.service.PreAuthSessionApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalCredentialApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityApplicationService;
import com.thundax.kuzhambu.system.application.auth.utils.PasswordHelper;
import com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeUserStatusCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.query.DepartmentQuery;
import com.thundax.kuzhambu.system.application.core.query.DictQuery;
import com.thundax.kuzhambu.system.application.core.query.RoleQuery;
import com.thundax.kuzhambu.system.application.core.query.UserQuery;
import com.thundax.kuzhambu.system.application.core.service.CurrentUserApplicationService;
import com.thundax.kuzhambu.system.application.core.service.DepartmentApplicationService;
import com.thundax.kuzhambu.system.application.core.service.DictApplicationService;
import com.thundax.kuzhambu.system.application.core.service.RoleApplicationService;
import com.thundax.kuzhambu.system.application.core.service.UserApplicationService;
import com.thundax.kuzhambu.system.application.core.utils.SysApiUtils;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalCredential;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionToken;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.core.codec.DepartmentIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.RoleIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Department;
import com.thundax.kuzhambu.system.domain.core.model.entity.Dict;
import com.thundax.kuzhambu.system.domain.core.model.entity.Role;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.enums.RoleStatus;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.AccessRank;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import com.thundax.kuzhambu.system.interfaces.admin.auth.security.CurrentUserResolver;
import com.thundax.kuzhambu.system.interfaces.admin.core.assembler.UserInterfaceAssembler;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserAvatarRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserCheckRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserDepartmentRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserIdRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserRoleRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.UserStatusRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.UserDepartmentResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.UserOptionsResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.UserResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.UserRoleResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.support.AdminAvatarUrlBuilder;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "系统模块", description = "系统管理")
@SysLogger(module = {"系统", "用户"})
@RequestMapping(value = "/api/sys/user")
@WrappedApiController
public class UserController {

    private static final int DEFAULT_PASSWORD_FAILED_LIMIT = 0;
    private static final String PRIVATE_KEY_ITEM = "privateKey";
    private static final String USER_RANK_DICT_TYPE = "user_rank";
    private static final String USER_STATUS_DICT_TYPE = "user_status";

    private final UserApplicationService userService;
    private final DepartmentApplicationService departmentService;
    private final DictApplicationService dictService;
    private final RoleApplicationService roleService;
    private final PrincipalIdentityApplicationService principalIdentityService;
    private final PrincipalCredentialApplicationService principalCredentialService;
    private final PreAuthSessionApplicationService preAuthSessionService;
    private final CurrentUserResolver currentUserResolver;
    private final CurrentUserApplicationService currentUserService;
    private final AdminAvatarUrlBuilder avatarUrlBuilder;

    @Autowired
    public UserController(
            UserApplicationService userService,
            DepartmentApplicationService departmentService,
            DictApplicationService dictService,
            RoleApplicationService roleService,
            PrincipalIdentityApplicationService principalIdentityService,
            PrincipalCredentialApplicationService principalCredentialService,
            PreAuthSessionApplicationService preAuthSessionService,
            CurrentUserResolver currentUserResolver,
            CurrentUserApplicationService currentUserService,
            AdminAvatarUrlBuilder avatarUrlBuilder) {

        this.userService = userService;
        this.departmentService = departmentService;
        this.dictService = dictService;
        this.roleService = roleService;
        this.principalIdentityService = principalIdentityService;
        this.principalCredentialService = principalCredentialService;
        this.preAuthSessionService = preAuthSessionService;
        this.currentUserResolver = currentUserResolver;
        this.currentUserService = currentUserService;
        this.avatarUrlBuilder = avatarUrlBuilder;
    }

    @Operation(summary = "获取对象", description = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:user:view")
    @SysLogger(value = "读取")
    @PostMapping(value = "get")
    @WrappedApiResponse
    public UserResponse get(@Valid @RequestBody UserIdRequest request) {
        User bean = userService.get(UserIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }
        return toResponse(bean);
    }

    @Operation(summary = "获取列表", description = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:user:view")
    @SysLogger(value = "列表")
    @PostMapping(value = "list")
    @WrappedApiResponse
    public List<UserResponse> list(@Valid @RequestBody UserQueryRequest request) {
        UserQuery query = readQuery(request);

        return userService.list(query).stream().map(user -> toResponse(user)).collect(Collectors.toList());
    }

    @Operation(summary = "获取分页列表", description = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:user:view")
    @SysLogger(value = "分页")
    @PostMapping(value = "page")
    @WrappedApiResponse
    public PageResponse<UserResponse> page(@Valid @RequestBody UserQueryRequest request) {
        UserQuery query = readQuery(request);

        return PageResponseHelper.fromPageResult(
                userService.page(query, PageInterfaceAssembler.toPageQuery(request)), this::toResponse);
    }

    @Operation(summary = "获取用户选项", description = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:user:view")
    @IgnoreSysLogger
    @PostMapping(value = "options")
    @WrappedApiResponse
    public UserOptionsResponse options() {
        DictQuery statusQuery = new DictQuery();
        statusQuery.setType(USER_STATUS_DICT_TYPE);
        DictQuery rankQuery = new DictQuery();
        rankQuery.setType(USER_RANK_DICT_TYPE);
        return UserOptionsResponse.builder()
                .statusOptions(OptionInterfaceAssembler.toOptionResponseList(
                        dictService.list(statusQuery), Dict::getValue, Dict::getLabel))
                .rankOptions(OptionInterfaceAssembler.toOptionResponseList(
                        dictService.list(rankQuery), Dict::getValue, Dict::getLabel))
                .build();
    }

    @Operation(summary = "添加", description = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:user:edit")
    @SysLogger(value = "添加")
    @PostMapping(value = "create")
    @WrappedApiResponse
    public UserResponse add(@Valid @RequestBody UserSaveRequest request) {
        // 解密密码（数据需要加密传输）
        String password = Sm2Crypto.decrypt(request.getLoginPass(), getPrivateKey(request.getToken()));
        request.setLoginPass(password);
        validateDepartment(request.getDepartment());
        validateRoles(request.getRoleList());
        validateCreatableRank(currentUserResolver.currentUser(), request);

        if (!isLoginNameAvailable(request.getLoginName(), request.getId())) {
            throw AdminResponseExceptions.invalidParameter("loginName");
        }
        validateUniqueContact(request);

        if (StringUtils.isBlank(request.getLoginPass())) {
            throw AdminResponseExceptions.invalidParameter("password");
        }
        validatePassword(request.getLoginPass());

        User entity = UserInterfaceAssembler.toDomain(new User(), request);
        String encryptedPassword = PasswordHelper.encrypt(request.getLoginPass());

        if (entity.getId() != null) {
            User bean = userService.get(entity.getId());
            if (bean != null) {
                throw AdminResponseExceptions.objectExists();
            }
        }

        entity.setId(userService.create(UserInterfaceAssembler.toCreateCommand(request, encryptedPassword)));
        PrincipalIdentity accountIdentity = upsertAccountIdentity(entity, request.getLoginName());
        upsertPassword(entity, encryptedPassword, accountIdentity);

        return toResponse(entity);
    }

    @Operation(summary = "更新", description = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:user:edit")
    @SysLogger(value = "更新")
    @PostMapping(value = "update")
    @WrappedApiResponse
    public UserResponse update(@Valid @RequestBody UserSaveRequest request) {
        // 解密密码（数据需要加密传输）
        if (StringUtils.isNotBlank(request.getLoginPass())) {
            String password = Sm2Crypto.decrypt(request.getLoginPass(), getPrivateKey(request.getToken()));
            // 先解密，否则密码规则无法校验
            request.setLoginPass(password);
            validatePassword(request.getLoginPass());
        }
        validateDepartment(request.getDepartment());
        validateRoles(request.getRoleList());

        if (!isLoginNameAvailable(request.getLoginName(), request.getId())) {
            throw AdminResponseExceptions.invalidParameter("loginName");
        }
        validateUniqueContact(request);

        User bean = userService.get(UserIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }
        User currentUser = currentUserResolver.currentUser();
        validateEditableUser(currentUser, bean);
        // 非超管用户无权限开启/关闭管理员
        if (!currentUser.isSuper() && Boolean.TRUE.equals(request.getAdmin()) != bean.isAdmin()) {
            throw AdminResponseExceptions.permissionDenied();
        }
        validateEditableRank(currentUser, request);

        User entity = UserInterfaceAssembler.toDomain(bean, request);

        userService.changeInfo(UserInterfaceAssembler.toChangeInfoCommand(request));

        PrincipalIdentity accountIdentity = upsertAccountIdentity(entity, request.getLoginName());
        if (StringUtils.isNotBlank(request.getLoginPass())) {
            upsertPassword(entity, PasswordHelper.encrypt(request.getLoginPass()), accountIdentity);
        }

        return toResponse(entity);
    }

    @Operation(summary = "上传头像", description = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
        @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:user:edit")
    @SysLogger(value = "上传头像")
    @PostMapping(value = "avatar/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @WrappedApiResponse
    public Boolean uploadAvatar(@RequestParam("id") String id, MultipartFile avatar) {
        User bean = userService.get(UserIdCodec.toDomain(Long.valueOf(id)));
        if (bean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }
        validateEditableUser(currentUserResolver.currentUser(), bean);
        try {
            currentUserService.changeAvatar(new ChangeCurrentUserAvatarCommand(
                    UserIdCodec.toDomain(Long.valueOf(id)), avatar.getInputStream(), avatar.getOriginalFilename()));
        } catch (IOException e) {
            throw AdminResponseExceptions.system(e.getMessage());
        }
        return true;
    }

    @Operation(summary = "删除头像", description = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:user:edit")
    @SysLogger(value = "删除头像")
    @PostMapping(value = "avatar/delete")
    @WrappedApiResponse
    public Boolean deleteAvatar(@Valid @RequestBody UserAvatarRequest request) {
        User bean = userService.get(UserIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }
        validateEditableUser(currentUserResolver.currentUser(), bean);
        currentUserService.removeAvatar(new RemoveCurrentUserAvatarCommand(UserIdCodec.toDomain(request.getId())));
        return true;
    }

    @Operation(summary = "获取头像相对路径", description = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:user:view")
    @IgnoreSysLogger
    @PostMapping(value = "avatar")
    public String avatar(@Valid @RequestBody UserAvatarRequest request) {
        return readAvatarUrl(UserIdCodec.toDomain(request.getId()));
    }

    @Operation(summary = "启用/禁用", description = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:user:edit")
    @SysLogger(value = "启用")
    @PostMapping(value = "enable")
    @WrappedApiResponse
    public Boolean updateStatus(@Valid @RequestBody List<UserStatusRequest> list) {
        User currentUser = currentUserResolver.currentUser();

        List<ChangeUserStatusCommand> commandList = new ArrayList<>();
        for (UserStatusRequest request : RequestListHelper.present(list)) {
            User bean = userService.get(UserIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
            validateEditableStatusUser(currentUser, bean);
            commandList.add(new ChangeUserStatusCommand(
                    bean.getId(), Boolean.TRUE.equals(request.getEnable()) ? UserStatus.ENABLED : UserStatus.DISABLED));
        }
        if (commandList.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("list");
        }

        commandList.forEach(userService::changeStatus);

        return true;
    }

    @Operation(summary = "删除", description = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:user:edit")
    @SysLogger(value = "删除")
    @PostMapping(value = "delete")
    @WrappedApiResponse
    public Boolean delete(@Valid @RequestBody List<UserIdRequest> list) {
        User currentUser = currentUserResolver.currentUser();

        List<UserId> idList = new ArrayList<>();
        for (UserIdRequest request : RequestListHelper.present(list)) {
            User bean = userService.get(UserIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
            validateEditableUser(currentUser, bean);
            idList.add(bean.getId());
        }
        if (idList.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("list");
        }

        idList.forEach(userService::remove);

        return true;
    }

    @Operation(summary = "检查 [loginName]是否存在", description = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:user:view")
    @IgnoreSysLogger
    @PostMapping(value = "check")
    @WrappedApiResponse
    public Boolean check(@Valid @RequestBody UserCheckRequest request) {
        return isLoginNameAvailable(request.getLoginName(), request.getId());
    }

    @Operation(summary = "获取部门树", description = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:user:view")
    @IgnoreSysLogger
    @PostMapping(value = "department/tree")
    @WrappedApiResponse
    public List<UserDepartmentResponse> departmentTree() {
        return departmentService.list(new DepartmentQuery()).stream()
                .map(department -> UserInterfaceAssembler.toDepartmentResponse(department, departmentService::get))
                .collect(Collectors.toList());
    }

    @Operation(summary = "获取权限列表", description = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:user:view")
    @IgnoreSysLogger
    @PostMapping(value = "role/list")
    @WrappedApiResponse
    public List<UserRoleResponse> roleList() {
        RoleQuery query = new RoleQuery();
        query.setStatus(RoleStatus.ENABLED);
        return roleService.list(query).stream()
                .map(role -> UserInterfaceAssembler.toRoleResponse(role))
                .collect(Collectors.toList());
    }

    @Operation(summary = "用户头像", description = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "user")
    @IgnoreSysLogger
    @GetMapping(value = "avatar")
    public void avatarImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userId = request.getParameter("id");
        if (StringUtils.isBlank(userId)) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }

        StoredObject avatar = currentUserService.getAvatar(UserIdCodec.toDomain(Long.valueOf(userId)));
        InputStream inputStream = currentUserService.getAvatarInputStream(UserIdCodec.toDomain(Long.valueOf(userId)));
        if (avatar == null || inputStream == null) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType(avatar.getMimeType());

        try (InputStream avatarInputStream = inputStream;
                OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int readBytes;
            while ((readBytes = avatarInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, readBytes);
            }
        }
    }

    private UserQuery readQuery(UserQueryRequest request) {
        UserQuery query = UserInterfaceAssembler.toQuery(request);

        if (request.getDepartmentId() != null) {
            Department department = departmentService.get(DepartmentIdCodec.toDomain(request.getDepartmentId()));
            if (department == null) {
                throw AdminResponseExceptions.objectNotFound();
            }

            query.setDepartmentId(department.getId());
        }

        return query;
    }

    private void validateDepartment(UserDepartmentRequest request) {
        if (request == null || request.getId() == null) {
            throw AdminResponseExceptions.invalidParameter("department.id");

        } else {
            Department bean = departmentService.get(DepartmentIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
        }
    }

    private void validateRoles(List<UserRoleRequest> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            return;
        }
        for (UserRoleRequest request : requestList) {
            if (request == null || request.getId() == null) {
                throw AdminResponseExceptions.invalidParameter("roles.id");

            } else {
                Role bean = roleService.get(RoleIdCodec.toDomain(request.getId()));
                if (bean == null) {
                    throw AdminResponseExceptions.objectNotFound();
                }
            }
        }
    }

    private boolean isLoginNameAvailable(String loginName, String id) {
        if (StringUtils.isBlank(loginName)) {
            return true;
        }
        PrincipalIdentity identity =
                principalIdentityService.get(identityQuery(PrincipalIdentityType.USER_ACCOUNT, loginName));
        if (identity == null) {
            return true;
        }

        return identity.getPrincipalKey() != null
                && Objects.equals(identity.getPrincipalKey().getPrincipalId(), readUserIdValue(id));
    }

    private Long readUserIdValue(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return UserIdCodec.toValue(UserIdCodec.toDomain(id));
    }

    private void validateUniqueContact(UserSaveRequest request) {
        UserId excludedId = UserIdCodec.toDomain(request.getId());
        if (StringUtils.isNotBlank(request.getEmail())) {
            UserQuery query = new UserQuery();
            query.setEmail(request.getEmail());
            query.setExcludedId(excludedId);
            if (userService.existsEmail(query)) {
                throw AdminResponseExceptions.invalidParameter("email");
            }
        }
        if (StringUtils.isNotBlank(request.getMobile())) {
            UserQuery query = new UserQuery();
            query.setMobile(request.getMobile());
            query.setExcludedId(excludedId);
            if (userService.existsMobile(query)) {
                throw AdminResponseExceptions.invalidParameter("mobile");
            }
        }
    }

    private void validateEditableUser(User currentUser, User targetUser) {
        if (currentUser != null && currentUser.isSuper()) {
            return;
        }
        if (currentUser == null
                || currentUser.getRank() == null
                || targetUser == null
                || targetUser.getRank() == null
                || targetUser.getRank().value() >= currentUser.getRank().value()) {
            throw AdminResponseExceptions.permissionDenied();
        }
    }

    private void validateEditableStatusUser(User currentUser, User targetUser) {
        if (currentUser == null
                || currentUser.getId() == null
                || currentUser.getRank() == null
                || targetUser == null
                || targetUser.getId() == null
                || targetUser.getRank() == null
                || Objects.equals(currentUser.getId(), targetUser.getId())
                || targetUser.getRank().value() >= currentUser.getRank().value()) {
            throw AdminResponseExceptions.permissionDenied();
        }
    }

    private void validateEditableRank(User currentUser, UserSaveRequest request) {
        if (currentUser != null && currentUser.isSuper()) {
            return;
        }
        int currentRank = currentUser == null || currentUser.getRank() == null
                ? AccessRank.MIN_VALUE
                : currentUser.getRank().value();
        if (request.getRanks() != null && request.getRanks() >= currentRank) {
            throw AdminResponseExceptions.permissionDenied();
        }
    }

    private void validateCreatableRank(User currentUser, UserSaveRequest request) {
        int currentRank = currentUser == null || currentUser.getRank() == null
                ? AccessRank.MIN_VALUE
                : currentUser.getRank().value();
        int maxRank = Math.max(currentRank - 1, AccessRank.MIN_VALUE);
        if (request.getRanks() == null) {
            request.setRanks(maxRank);
            return;
        }
        if (request.getRanks() > maxRank) {
            throw AdminResponseExceptions.permissionDenied();
        }
    }

    private UserResponse toResponse(User user) {
        Department department = departmentService.get(user.getDepartmentId());
        return UserInterfaceAssembler.toResponse(
                user,
                getAccountLoginName(user.getId()),
                department,
                loadUserRoles(user),
                readAvatarUrl(user.getId()),
                departmentService::get);
    }

    private List<Role> loadUserRoles(User user) {
        List<Role> userRoles = userService.listUserRoles(userQuery(user.getId()));
        if (userRoles == null) {
            return new ArrayList<>();
        }
        return userRoles.stream()
                .map(role -> role == null ? null : roleService.get(role.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private UserQuery userQuery(UserId userId) {
        UserQuery query = new UserQuery();
        query.setId(userId);
        return query;
    }

    private String getAccountLoginName(UserId userId) {
        PrincipalIdentity identity = getAccountIdentity(userId);
        return identity == null ? null : identity.getIdentityValue();
    }

    private PrincipalIdentity getAccountIdentity(UserId userId) {
        if (userId == null) {
            return null;
        }
        return principalIdentityService.get(identityQuery(
                PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(userId)), PrincipalIdentityType.USER_ACCOUNT));
    }

    private void upsertPassword(User user, String encryptedPassword, PrincipalIdentity accountIdentity) {
        if (accountIdentity == null || StringUtils.isBlank(encryptedPassword)) {
            return;
        }
        PrincipalCredential credential = principalCredentialService.get(
                credentialQuery(accountIdentity.getId(), PrincipalCredentialType.USER_PASSWORD));
        if (credential == null) {
            credential = new PrincipalCredential();
            credential.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(user.getId())));
            credential.setIdentityId(accountIdentity.getId());
            credential.setCredentialType(PrincipalCredentialType.USER_PASSWORD);
            credential.setCredentialValue(encryptedPassword);
            credential.setStatus(PrincipalCredentialStatus.ACTIVE);
            credential.setNeedChangePassword(false);
            credential.setFailedCount(0);
            credential.setFailedLimit(DEFAULT_PASSWORD_FAILED_LIMIT);
            principalCredentialService.create(new PrincipalCredentialCommand(credential));
            return;
        }
        credential.setCredentialValue(encryptedPassword);
        credential.setStatus(PrincipalCredentialStatus.ACTIVE);
        credential.setNeedChangePassword(false);
        credential.setFailedCount(0);
        credential.setLockedUntil(null);
        credential.setLastVerifiedAt(null);
        principalCredentialService.change(new PrincipalCredentialCommand(credential));
    }

    private PrincipalIdentity upsertAccountIdentity(User user, String loginName) {
        if (user == null || user.getId() == null || StringUtils.isBlank(loginName)) {
            return null;
        }
        PrincipalKey principalKey = PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(user.getId()));
        PrincipalIdentity accountIdentity = getAccountIdentity(user.getId());
        if (accountIdentity == null) {
            accountIdentity = new PrincipalIdentity();
            accountIdentity.setPrincipalKey(principalKey);
            accountIdentity.setType(PrincipalIdentityType.USER_ACCOUNT);
            accountIdentity.setIdentityValue(loginName);
            accountIdentity.setStatus(PrincipalIdentityStatus.ENABLED);
            accountIdentity.setId(principalIdentityService.create(new PrincipalIdentityCommand(accountIdentity)));
            return accountIdentity;
        }

        accountIdentity.setPrincipalKey(principalKey);
        accountIdentity.setType(PrincipalIdentityType.USER_ACCOUNT);
        accountIdentity.setIdentityValue(loginName);
        accountIdentity.setStatus(PrincipalIdentityStatus.ENABLED);
        principalIdentityService.change(new PrincipalIdentityCommand(accountIdentity));
        return accountIdentity;
    }

    private void validatePassword(String password) {
        if (!password.matches(SysApiUtils.PASSWORD_VALIDATE_PATTERN)) {
            throw AdminResponseExceptions.invalidParameter("password");
        }
    }

    private PrincipalIdentityQuery identityQuery(PrincipalIdentityType identityType, String identityValue) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setIdentityType(identityType);
        query.setIdentityValue(identityValue);
        return query;
    }

    private PrincipalIdentityQuery identityQuery(PrincipalKey principalKey, PrincipalIdentityType identityType) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setPrincipalKey(principalKey);
        query.setIdentityType(identityType);
        return query;
    }

    private PrincipalCredentialQuery credentialQuery(
            PrincipalIdentityId identityId, PrincipalCredentialType credentialType) {
        PrincipalCredentialQuery query = new PrincipalCredentialQuery();
        query.setIdentityId(identityId);
        query.setCredentialType(credentialType);
        return query;
    }

    private String getPrivateKey(String token) {
        PreAuthSessionId sessionId = preAuthSessionService.getIdByToken(PreAuthSessionToken.of(token));
        if (sessionId == null) {
            throw AdminResponseExceptions.invalidToken();
        }
        String privateKey = preAuthSessionService.getValue(new PreAuthSessionValueQuery(sessionId, PRIVATE_KEY_ITEM));
        if (StringUtils.isBlank(privateKey)) {
            throw AdminResponseExceptions.invalidToken();
        }
        return privateKey;
    }

    private String readAvatarUrl(UserId userId) {
        if (!currentUserService.existsAvatar(userId)) {
            return null;
        }
        return avatarUrlBuilder.build(UserIdCodec.toStringValue(userId));
    }
}
