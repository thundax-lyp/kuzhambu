package com.thundax.kuzhambu.system.infra.core.persistence.assembler;

import com.thundax.kuzhambu.system.domain.core.codec.AccessRankCodec;
import com.thundax.kuzhambu.system.domain.core.codec.MenuIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.enums.MenuVisibility;
import com.thundax.kuzhambu.system.infra.core.persistence.dataobject.MenuDO;
import java.util.ArrayList;
import java.util.List;

public final class MenuPersistenceAssembler {

    private MenuPersistenceAssembler() {}

    public static MenuDO toObject(Menu entity) {
        if (entity == null) {
            return null;
        }
        MenuDO dataObject = new MenuDO();
        dataObject.setId(MenuIdCodec.toValue(entity.getId()));
        dataObject.setParentId(MenuIdCodec.toValue(entity.getParentId()));
        dataObject.setName(entity.getName());
        dataObject.setPerms(entity.getPerms());
        dataObject.setRanks(AccessRankCodec.toValue(entity.getRank()));
        dataObject.setVisibility(visibilityValue(entity.getVisibility()));
        dataObject.setDisplayParams(entity.getDisplayParams());
        dataObject.setUrl(entity.getUrl());
        dataObject.setTarget(entity.getTarget());
        dataObject.setRemarks(entity.getRemarks());
        return dataObject;
    }

    public static Menu toDomain(MenuDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Menu entity = new Menu();
        entity.setId(MenuIdCodec.toDomain(dataObject.getId()));
        entity.setParentId(MenuIdCodec.toDomain(dataObject.getParentId()));
        entity.setName(dataObject.getName());
        entity.setPerms(dataObject.getPerms());
        entity.setRank(AccessRankCodec.toDomain(dataObject.getRanks()));
        entity.setVisibility(visibilityFrom(dataObject.getVisibility()));
        entity.setDisplayParams(dataObject.getDisplayParams());
        entity.setUrl(dataObject.getUrl());
        entity.setTarget(dataObject.getTarget());
        entity.setRemarks(dataObject.getRemarks());
        return entity;
    }

    public static List<Menu> toDomainList(List<MenuDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Menu> entities = new ArrayList<>();
        for (MenuDO dataObject : dataObjects) {
            entities.add(toDomain(dataObject));
        }
        return entities;
    }

    public static MenuDO toParentUpdateObject(Long id, Long parentId) {
        MenuDO dataObject = new MenuDO();
        dataObject.setId(id);
        dataObject.setParentId(parentId);
        return dataObject;
    }

    private static String visibilityValue(MenuVisibility visibility) {
        return visibility == null ? null : visibility.value();
    }

    private static MenuVisibility visibilityFrom(String visibility) {
        return visibility == null ? null : MenuVisibility.from(visibility);
    }
}
