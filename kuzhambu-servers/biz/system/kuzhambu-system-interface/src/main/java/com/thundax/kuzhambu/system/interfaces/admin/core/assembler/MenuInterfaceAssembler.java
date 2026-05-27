package com.thundax.kuzhambu.system.interfaces.admin.core.assembler;

import com.thundax.kuzhambu.system.application.core.service.command.ChangeMenuInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.CreateMenuCommand;
import com.thundax.kuzhambu.system.application.core.service.query.MenuQuery;
import com.thundax.kuzhambu.system.domain.core.codec.AccessRankCodec;
import com.thundax.kuzhambu.system.domain.core.codec.MenuIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.enums.MenuVisibility;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.MenuQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.MenuSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.MenuResponse;
import org.springframework.lang.NonNull;

public final class MenuInterfaceAssembler {
    private MenuInterfaceAssembler() {}

    @NonNull
    public static MenuResponse toResponse(Menu entity) {
        if (entity == null) {
            return MenuResponse.builder().build();
        }
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
    public static MenuResponse toTreeResponse(Menu entity) {
        if (entity == null) {
            return MenuResponse.builder().build();
        }
        return MenuResponse.builder()
                .id(MenuIdCodec.toStringValue(entity.getId()))
                .parentId(MenuIdCodec.toStringValue(entity.getParentId()))
                .name(entity.getName())
                .build();
    }

    @NonNull
    public static MenuQuery toQuery(@NonNull MenuQueryRequest request) {
        MenuQuery query = new MenuQuery();
        query.setParentId(MenuIdCodec.toDomain(request.getParentId()));
        if (request.getDisplay() != null) {
            query.setVisibility(request.getDisplay() ? MenuVisibility.VISIBLE : MenuVisibility.HIDDEN);
        }
        return query;
    }

    @NonNull
    public static Menu toDomain(@NonNull Menu entity, @NonNull MenuSaveRequest request) {
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
}
