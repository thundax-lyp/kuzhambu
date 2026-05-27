package com.thundax.kuzhambu.system.interfaces.admin.core.controller;

import com.thundax.kuzhambu.common.core.tree.TreeNodeListHelper;
import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import com.thundax.kuzhambu.system.application.core.service.MenuService;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeMenuVisibilityCommand;
import com.thundax.kuzhambu.system.application.core.service.command.MoveMenuCommand;
import com.thundax.kuzhambu.system.application.core.service.query.MenuQuery;
import com.thundax.kuzhambu.system.domain.core.codec.MenuIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.enums.MenuVisibility;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import com.thundax.kuzhambu.system.interfaces.admin.core.aop.annotation.SysLogger;
import com.thundax.kuzhambu.system.interfaces.admin.core.assembler.MenuInterfaceAssembler;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.MenuDisplayRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.MenuIdRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.MenuMoveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.MenuQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.MenuSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.MenuResponse;
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

@Tag(name = "系统/菜单")
@SysLogger(module = {"系统", "菜单"})
@RequestMapping(value = "/api/sys/menu")
@WrappedApiController
public class MenuController {

    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @Operation(summary = "获取对象", description = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "super")
    @SysLogger(value = "读取")
    @PostMapping(value = "get")
    public MenuResponse get(@Valid @RequestBody MenuIdRequest request) {
        Menu bean = menuService.get(MenuIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }
        return MenuInterfaceAssembler.toResponse(bean);
    }

    @Operation(summary = "获取列表", description = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "super")
    @SysLogger(value = "读取")
    @PostMapping(value = "list")
    public List<MenuResponse> list(@Valid @RequestBody MenuQueryRequest request) {
        MenuQuery query = MenuInterfaceAssembler.toQuery(request);

        return menuService.list(query).stream()
                .map(menu -> MenuInterfaceAssembler.toResponse(menu))
                .collect(Collectors.toList());
    }

    @Operation(summary = "添加", description = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "super")
    @SysLogger(value = "添加")
    @PostMapping(value = "create")
    public MenuResponse add(@Valid @RequestBody MenuSaveRequest request) {
        Menu entity = MenuInterfaceAssembler.toDomain(new Menu(), request);
        if (entity.getId() != null) {
            Menu bean = menuService.get(entity.getId());
            if (bean != null) {
                throw AdminResponseExceptions.objectExists();
            }
        }

        if (entity.getParentId() != null) {
            Menu parent = menuService.get(entity.getParentId());
            if (parent == null) {
                throw AdminResponseExceptions.invalidParameter("parentId");
            }
        }

        entity.setId(menuService.create(MenuInterfaceAssembler.toCreateCommand(request)));

        return MenuInterfaceAssembler.toResponse(entity);
    }

    @Operation(summary = "更新", description = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "super")
    @SysLogger(value = "修改")
    @PostMapping(value = "update")
    public MenuResponse update(@Valid @RequestBody MenuSaveRequest request) {
        Menu bean = menuService.get(MenuIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw AdminResponseExceptions.invalidParameter("id");
        }

        if (request.getParentId() != null) {
            Menu parent = menuService.get(MenuIdCodec.toDomain(request.getParentId()));
            if (parent == null) {
                throw AdminResponseExceptions.invalidParameter("parentId");
            }
        }

        Menu entity = MenuInterfaceAssembler.toDomain(bean, request);

        menuService.changeInfo(MenuInterfaceAssembler.toChangeInfoCommand(request));

        return MenuInterfaceAssembler.toResponse(entity);
    }

    @Operation(summary = "显示/隐藏", description = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "super")
    @SysLogger(value = "显示")
    @PostMapping(value = "display")
    public Boolean updateVisibility(@Valid @RequestBody List<MenuDisplayRequest> list) {
        List<ChangeMenuVisibilityCommand> commandList = new ArrayList<>();
        for (MenuDisplayRequest request : RequestListHelper.present(list)) {
            Menu bean = menuService.get(MenuIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
            commandList.add(new ChangeMenuVisibilityCommand(
                    bean.getId(),
                    Boolean.TRUE.equals(request.getDisplay()) ? MenuVisibility.VISIBLE : MenuVisibility.HIDDEN));
        }
        if (commandList.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("list");
        }

        commandList.forEach(menuService::changeVisibility);

        return true;
    }

    @Operation(summary = "删除", description = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "super")
    @SysLogger(value = "删除")
    @PostMapping(value = "delete")
    public Boolean delete(@Valid @RequestBody List<MenuIdRequest> list) {
        List<MenuId> idList = new ArrayList<>();
        for (MenuIdRequest request : RequestListHelper.present(list)) {
            Menu bean = menuService.get(MenuIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
            idList.add(bean.getId());
        }
        if (idList.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("list");
        }

        idList.forEach(menuService::remove);

        return true;
    }

    @Operation(summary = "获取列表", description = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "super")
    @SysLogger(value = "读取")
    @PostMapping(value = "tree")
    public List<MenuResponse> tree(@Valid @RequestBody List<MenuIdRequest> excludeList) {
        List<Menu> beanList = menuService.list(new MenuQuery());

        Set<MenuId> excludeIds =
                new HashSet<>(RequestListHelper.map(excludeList, request -> MenuIdCodec.toDomain(request.getId())));
        beanList.removeIf(bean -> excludeIds.contains(bean.getId()));

        TreeNodeListHelper.remove(
                beanList,
                new TreeNodeListHelper.TreeNodeSupport<Menu, MenuId>() {

                    @Override
                    public MenuId getId(Menu menu) {
                        return menu.getId();
                    }

                    @Override
                    public MenuId getParentId(Menu menu) {
                        return menu.getParentId();
                    }

                    @Override
                    public boolean isRoot(Menu menu) {
                        return menu.getParentId() == null;
                    }
                },
                excludeIds);

        return beanList.stream()
                .map(menu -> MenuInterfaceAssembler.toTreeResponse(menu))
                .collect(Collectors.toList());
    }

    @Operation(summary = "排序", description = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "super")
    @SysLogger(value = "排序")
    @PostMapping(value = "move")
    public Boolean move(@Valid @RequestBody MenuMoveRequest request) {
        Menu fromBean = menuService.get(MenuIdCodec.toDomain(request.getFromNodeId()));
        if (fromBean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }

        Menu toBean = menuService.get(MenuIdCodec.toDomain(request.getToNodeId()));
        if (toBean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }

        if (toBean.equals(fromBean) || menuService.existsChildRelation(childRelationQuery(toBean, fromBean))) {
            throw AdminResponseExceptions.moveTreeNode();
        }

        menuService.move(new MoveMenuCommand(fromBean.getId(), toBean.getId(), readMoveTreeNodeType(request)));

        return true;
    }

    private TreeNodeMoveType readMoveTreeNodeType(MenuMoveRequest request) {
        switch (request.getType()) {
            case MenuMoveRequest.TYPE_BEFORE:
                return TreeNodeMoveType.BEFORE;
            case MenuMoveRequest.TYPE_INSIDE:
                return TreeNodeMoveType.INSIDE;
            case MenuMoveRequest.TYPE_INSIDE_LAST:
                return TreeNodeMoveType.INSIDE_LAST;
            default:
                return TreeNodeMoveType.AFTER;
        }
    }

    private MenuQuery childRelationQuery(Menu child, Menu ancestor) {
        MenuQuery query = new MenuQuery();
        query.setChildId(child.getId());
        query.setAncestorId(ancestor.getId());
        return query;
    }
}
