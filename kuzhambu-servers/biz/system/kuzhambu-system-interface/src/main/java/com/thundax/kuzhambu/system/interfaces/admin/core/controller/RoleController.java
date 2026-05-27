package com.thundax.kuzhambu.system.interfaces.admin.core.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.OptionInterfaceAssembler;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalType;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityService;
import com.thundax.kuzhambu.system.application.auth.service.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.core.entity.Dict;
import com.thundax.kuzhambu.system.application.core.entity.Menu;
import com.thundax.kuzhambu.system.application.core.entity.Role;
import com.thundax.kuzhambu.system.application.core.entity.User;
import com.thundax.kuzhambu.system.application.core.entity.enums.RoleStatus;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.DepartmentIdCodec;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.MenuIdCodec;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.RoleId;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.RoleIdCodec;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.UserId;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.UserIdCodec;
import com.thundax.kuzhambu.system.application.core.service.DepartmentService;
import com.thundax.kuzhambu.system.application.core.service.DictService;
import com.thundax.kuzhambu.system.application.core.service.MenuService;
import com.thundax.kuzhambu.system.application.core.service.RoleService;
import com.thundax.kuzhambu.system.application.core.service.UserService;
import com.thundax.kuzhambu.system.application.core.service.command.AssignRoleUsersCommand;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeRoleStatusCommand;
import com.thundax.kuzhambu.system.application.core.service.command.RoleSortCommand;
import com.thundax.kuzhambu.system.application.core.service.query.DepartmentQuery;
import com.thundax.kuzhambu.system.application.core.service.query.DictQuery;
import com.thundax.kuzhambu.system.application.core.service.query.MenuQuery;
import com.thundax.kuzhambu.system.application.core.service.query.RoleQuery;
import com.thundax.kuzhambu.system.application.core.service.query.UserQuery;
import com.thundax.kuzhambu.system.interfaces.admin.core.aop.annotation.SysLogger;
import com.thundax.kuzhambu.system.interfaces.admin.core.assembler.RoleInterfaceAssembler;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleAssignUserRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleIdRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleMenuRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleSortRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleStatusRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.RoleUserRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.RoleMenuResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.RoleOptionsResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.RoleResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.RoleUserResponse;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.RoleUserTreeNodeResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "系统/权限")
@SysLogger(module = {"系统", "权限"})
@RequestMapping(value = "/api/sys/role")
@WrappedApiController
public class RoleController {

    private static final String DEPARTMENT_ID_PREFIX = "DEPARTMENT_";
    private static final String ROLE_PRIVILEGE_DICT_TYPE = "role_privilege";
    private static final String ROLE_STATUS_DICT_TYPE = "role_status";

    private final RoleService roleService;
    private final MenuService menuService;
    private final DepartmentService departmentService;
    private final DictService dictService;
    private final UserService userService;
    private final PrincipalIdentityService principalIdentityService;

    @Autowired
    public RoleController(
            RoleService roleService,
            MenuService menuService,
            DepartmentService departmentService,
            DictService dictService,
            UserService userService,
            PrincipalIdentityService principalIdentityService) {

        this.roleService = roleService;
        this.menuService = menuService;
        this.departmentService = departmentService;
        this.dictService = dictService;
        this.userService = userService;
        this.principalIdentityService = principalIdentityService;
    }

    @Operation(summary = "获取对象", description = "sys:role:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:role:view")
    @SysLogger(value = "读取")
    @PostMapping(value = "get")
    public RoleResponse get(@Valid @RequestBody RoleIdRequest request) {
        Role bean = roleService.get(RoleIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }
        return toResponse(bean);
    }

    @Operation(summary = "获取列表", description = "sys:role:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:role:view")
    @SysLogger(value = "列表")
    @PostMapping(value = "list")
    public List<RoleResponse> list(@Valid @RequestBody RoleQueryRequest request) {
        RoleQuery query = RoleInterfaceAssembler.toQuery(request);

        return roleService.list(query).stream().map(role -> toResponse(role)).collect(Collectors.toList());
    }

    @Operation(summary = "获取角色选项", description = "sys:role:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:role:view")
    @PostMapping(value = "options")
    public RoleOptionsResponse options() {
        DictQuery statusQuery = new DictQuery();
        statusQuery.setType(ROLE_STATUS_DICT_TYPE);
        DictQuery privilegeQuery = new DictQuery();
        privilegeQuery.setType(ROLE_PRIVILEGE_DICT_TYPE);
        return RoleOptionsResponse.builder()
                .statusOptions(OptionInterfaceAssembler.toOptionResponseList(
                        dictService.list(statusQuery), Dict::getValue, Dict::getLabel))
                .privilegeOptions(OptionInterfaceAssembler.toOptionResponseList(
                        dictService.list(privilegeQuery), Dict::getValue, Dict::getLabel))
                .build();
    }

    @Operation(summary = "添加", description = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:role:edit")
    @SysLogger(value = "添加")
    @PostMapping(value = "create")
    public RoleResponse add(@Valid @RequestBody RoleSaveRequest request) {
        validateMenus(request.getMenuList());

        if (request.getId() != null) {
            Role bean = roleService.get(RoleIdCodec.toDomain(request.getId()));
            if (bean != null) {
                throw AdminResponseExceptions.objectExists();
            }
        }

        Role entity = RoleInterfaceAssembler.toEntity(new Role(), request);
        entity.setId(roleService.create(RoleInterfaceAssembler.toCreateCommand(request)));

        return toResponse(entity);
    }

    @Operation(summary = "更新", description = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:role:edit")
    @SysLogger(value = "更新")
    @PostMapping(value = "update")
    public RoleResponse update(@Valid @RequestBody RoleSaveRequest request) {
        validateMenus(request.getMenuList());

        Role bean = roleService.get(RoleIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }

        Role entity = RoleInterfaceAssembler.toEntity(bean, request);

        roleService.changeInfo(RoleInterfaceAssembler.toChangeInfoCommand(request));

        return toResponse(entity);
    }

    @Operation(summary = "启用/禁用", description = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:role:edit")
    @SysLogger(value = "启用")
    @PostMapping(value = "enable")
    public Boolean updateStatus(@Valid @RequestBody List<RoleStatusRequest> list) {
        List<ChangeRoleStatusCommand> commandList = new ArrayList<>();
        for (RoleStatusRequest request : RequestListHelper.present(list)) {
            Role bean = roleService.get(RoleIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
            commandList.add(new ChangeRoleStatusCommand(
                    bean.getId(), Boolean.TRUE.equals(request.getEnable()) ? RoleStatus.ENABLED : RoleStatus.DISABLED));
        }
        if (commandList.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("list");
        }

        commandList.forEach(roleService::changeStatus);

        return true;
    }

    @Operation(summary = "排序", description = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:role:edit")
    @SysLogger(value = "排序")
    @PostMapping(value = "sort")
    public Boolean updatePriority(@Valid @RequestBody RoleSortRequest request) {
        roleService.sort(new RoleSortCommand(
                RequestListHelper.map(
                        readOrderedIds(request == null ? null : request.getOrderedIds()), RoleIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
    }

    private List<Long> readOrderedIds(List<String> sourceList) {
        List<String> orderedIdValues = RequestListHelper.present(sourceList);
        if (sourceList == null || orderedIdValues.size() != sourceList.size() || orderedIdValues.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("orderedIds");
        }
        List<Long> orderedIds = orderedIdValues.stream()
                .map(value -> Long.valueOf(value.trim()))
                .collect(Collectors.toList());
        Set<Long> uniqueIds = new HashSet<>(orderedIds);
        if (uniqueIds.size() != orderedIds.size()) {
            throw AdminResponseExceptions.invalidParameter("orderedIds");
        }
        return orderedIds;
    }

    @Operation(summary = "删除", description = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:role:edit")
    @SysLogger(value = "删除")
    @PostMapping(value = "delete")
    public Boolean delete(@Valid @RequestBody List<RoleIdRequest> list) {
        List<RoleId> idList = new ArrayList<>();
        for (RoleIdRequest request : RequestListHelper.present(list)) {
            Role bean = roleService.get(RoleIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
            idList.add(bean.getId());
        }
        if (idList.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("list");
        }

        idList.forEach(roleService::remove);

        return true;
    }

    @Operation(summary = "获取菜单树", description = "sys:role:view, sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission({"sys:role:view", "sys:role:edit"})
    @PostMapping(value = "menu/tree")
    public List<RoleMenuResponse> menuTree() {
        return menuService.list(new MenuQuery()).stream()
                .map(menu -> RoleInterfaceAssembler.toMenuResponse(menu))
                .collect(Collectors.toList());
    }

    @Operation(summary = "获取用户树", description = "sys:role:view, sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission({"sys:role:view", "sys:role:edit"})
    @PostMapping(value = "user/tree")
    public List<RoleUserTreeNodeResponse> userTree() {
        List<RoleUserTreeNodeResponse> list = new ArrayList<>();

        list.addAll(departmentService.list(new DepartmentQuery()).stream()
                .map(department -> RoleInterfaceAssembler.toDepartmentTreeNode(
                        DEPARTMENT_ID_PREFIX + DepartmentIdCodec.toValue(department.getId()), department))
                .collect(Collectors.toList()));

        list.addAll(userService.list(new UserQuery()).stream()
                .map(user -> RoleInterfaceAssembler.toUserTreeNode(
                        DEPARTMENT_ID_PREFIX,
                        user,
                        getAccountLoginName(user),
                        departmentService.get(user.getDepartmentId()),
                        departmentService::get))
                .collect(Collectors.toList()));

        return list;
    }

    @Operation(summary = "获取权限用户列表", description = "sys:role:view, sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission({"sys:role:view", "sys:role:edit"})
    @PostMapping(value = "user/list")
    public List<RoleUserResponse> userList(@Valid @RequestBody RoleIdRequest request) {
        Role bean = roleService.get(RoleIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }

        return roleService.listRoleUsers(roleQuery(request.getId())).stream()
                .map(user -> toUserResponse(userService.get(user.getId())))
                .collect(Collectors.toList());
    }

    @Operation(summary = "更新权限用户列表", description = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "sys:role:edit")
    @SysLogger(value = "授权")
    @PostMapping(value = "user/assign")
    public Boolean assignUser(@Valid @RequestBody RoleAssignUserRequest request) {
        validateAssignUser(request);

        roleService.assignUsers(new AssignRoleUsersCommand(
                RoleIdCodec.toDomain(request.getRoleId()),
                request.getUsers().stream()
                        .map(vo -> UserIdCodec.toDomain(vo.getId()))
                        .collect(Collectors.toList())));

        return true;
    }

    private RoleResponse toResponse(Role role) {
        return RoleInterfaceAssembler.toResponse(role, roleService.listRoleMenus(roleQuery(role)));
    }

    private RoleUserResponse toUserResponse(User user) {
        return RoleInterfaceAssembler.toUserResponse(
                user, getAccountLoginName(user), departmentService.get(user.getDepartmentId()), departmentService::get);
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

    private void validateAssignUser(RoleAssignUserRequest request) {
        Role roleBean = roleService.get(RoleIdCodec.toDomain(request.getRoleId()));
        if (roleBean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }

        if (request.getUsers() == null || request.getUsers().isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("users");
        }

        for (RoleUserRequest userRequest : request.getUsers()) {
            User userBean = userService.get(UserIdCodec.toDomain(userRequest.getId()));
            if (userBean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
        }
    }

    private UserQuery userQuery(UserId userId) {
        UserQuery query = new UserQuery();
        query.setId(userId);
        return query;
    }

    private UserQuery userQuery(Long userId) {
        return userQuery(UserIdCodec.toDomain(userId));
    }

    private RoleQuery roleQuery(Role role) {
        return roleQuery(RoleIdCodec.toValue(role.getId()));
    }

    private RoleQuery roleQuery(String roleId) {
        RoleQuery query = new RoleQuery();
        query.setId(RoleIdCodec.toDomain(roleId));
        return query;
    }

    private RoleQuery roleQuery(Long roleId) {
        RoleQuery query = new RoleQuery();
        query.setId(RoleIdCodec.toDomain(roleId));
        return query;
    }

    private void validateMenus(List<RoleMenuRequest> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            return;
        }
        for (RoleMenuRequest request : requestList) {
            if (request == null || request.getId() == null) {
                throw AdminResponseExceptions.invalidParameter("menus.id");

            } else {
                Menu bean = menuService.get(MenuIdCodec.toDomain(request.getId()));
                if (bean == null) {
                    throw AdminResponseExceptions.objectNotFound();
                }
            }
        }
    }
}
