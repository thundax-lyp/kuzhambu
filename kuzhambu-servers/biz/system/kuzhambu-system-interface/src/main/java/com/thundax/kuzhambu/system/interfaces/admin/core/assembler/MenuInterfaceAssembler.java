package com.thundax.kuzhambu.system.interfaces.admin.core.assembler;

import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import com.thundax.kuzhambu.system.application.core.command.ChangeMenuInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeMenuVisibilityCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateMenuCommand;
import com.thundax.kuzhambu.system.application.core.command.MoveMenuCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveMenuCommand;
import com.thundax.kuzhambu.system.application.core.query.GetMenuQuery;
import com.thundax.kuzhambu.system.application.core.query.MenuQuery;
import com.thundax.kuzhambu.system.domain.core.codec.AccessRankCodec;
import com.thundax.kuzhambu.system.domain.core.codec.MenuIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.enums.MenuVisibility;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.MenuQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.MenuSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.MenuResponse;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class MenuInterfaceAssembler {
    private MenuInterfaceAssembler() {}

    @NonNull
    public static MenuResponse toResponse(@NonNull Menu entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        String parentId = MenuIdCodec.toStringValue(entity.getParentId());
        return MenuResponse.builder()
                .id(MenuIdCodec.toStringValue(entity.getId()))
                .remarks(entity.getRemarks())
                .parentId(parentId)
                .name(entity.getName())
                .perms(entity.getPerms())
                .ranks(AccessRankCodec.toValue(entity.getRank()))
                .display(entity.isDisplay())
                .displayParams(entity.getDisplayParams())
                .url(entity.getUrl())
                .build();
    }

    @NonNull
    public static MenuResponse toTreeResponse(@NonNull Menu entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return MenuResponse.builder()
                .id(MenuIdCodec.toStringValue(entity.getId()))
                .parentId(MenuIdCodec.toStringValue(entity.getParentId()))
                .name(entity.getName())
                .build();
    }

    @NonNull
    public static MenuQuery toQuery(@NonNull MenuQueryRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new MenuQuery(
                null,
                null,
                null,
                MenuIdCodec.toDomain(request.getParentId()),
                request.getDisplay() == null
                        ? null
                        : request.getDisplay() ? MenuVisibility.VISIBLE : MenuVisibility.HIDDEN,
                null);
    }

    @NonNull
    public static MenuQuery toListAllQuery() {
        return new MenuQuery(null, null, null, null, null, null);
    }

    @NonNull
    public static MenuQuery toChildRelationQuery(@NonNull Menu child, @NonNull Menu ancestor) {
        Objects.requireNonNull(child, "child must not be null");
        Objects.requireNonNull(ancestor, "ancestor must not be null");
        return new MenuQuery(null, child.getId(), ancestor.getId(), null, null, null);
    }

    @NonNull
    public static GetMenuQuery toGetQuery(@NonNull MenuId id) {
        Objects.requireNonNull(id, "id must not be null");
        return new GetMenuQuery(id);
    }

    @NonNull
    public static Menu toDomain(@NonNull Menu entity, @NonNull MenuSaveRequest request) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(request, "request must not be null");
        entity.setId(MenuIdCodec.toDomain(request.getId()));
        entity.setRemarks(request.getRemarks());
        if (request.getParentId() != null) {
            entity.setParentId(MenuIdCodec.toDomain(request.getParentId()));
        }
        entity.setName(request.getName());
        entity.setPerms(request.getPerms());
        entity.setRank(AccessRankCodec.toDomain(request.getRanks()));
        entity.setVisibility(
                Boolean.TRUE.equals(request.getDisplay()) ? MenuVisibility.VISIBLE : MenuVisibility.HIDDEN);
        entity.setDisplayParams(request.getDisplayParams());
        entity.setUrl(request.getUrl());
        return entity;
    }

    @NonNull
    public static CreateMenuCommand toCreateCommand(@NonNull MenuSaveRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Menu entity = toDomain(new Menu(), request);
        return new CreateMenuCommand(
                entity.getId(),
                entity.getParentId(),
                entity.getName(),
                entity.getPerms(),
                entity.getRank(),
                entity.getVisibility(),
                entity.getDisplayParams(),
                entity.getUrl(),
                entity.getTarget(),
                entity.getRemarks());
    }

    @NonNull
    public static ChangeMenuInfoCommand toChangeInfoCommand(@NonNull MenuSaveRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Menu entity = toDomain(new Menu(), request);
        return new ChangeMenuInfoCommand(
                entity.getId(),
                entity.getParentId(),
                entity.getName(),
                entity.getPerms(),
                entity.getRank(),
                entity.getVisibility(),
                entity.getDisplayParams(),
                entity.getUrl(),
                entity.getTarget(),
                entity.getRemarks());
    }

    @NonNull
    public static ChangeMenuVisibilityCommand toChangeVisibilityCommand(@NonNull Menu entity, boolean display) {
        Objects.requireNonNull(entity, "entity must not be null");
        return new ChangeMenuVisibilityCommand(
                entity.getId(), display ? MenuVisibility.VISIBLE : MenuVisibility.HIDDEN);
    }

    @NonNull
    public static RemoveMenuCommand toRemoveCommand(@NonNull MenuId id) {
        Objects.requireNonNull(id, "id must not be null");
        return new RemoveMenuCommand(id);
    }

    @NonNull
    public static MoveMenuCommand toMoveCommand(
            @NonNull Menu from, @NonNull Menu to, @NonNull TreeNodeMoveType moveType) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        Objects.requireNonNull(moveType, "moveType must not be null");
        return new MoveMenuCommand(from.getId(), to.getId(), moveType);
    }
}
