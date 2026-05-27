package com.thundax.kuzhambu.system.infra.core.assembler;

import com.thundax.kuzhambu.system.domain.core.codec.RoleIdCodec;
import com.thundax.kuzhambu.system.domain.model.entity.Role;
import com.thundax.kuzhambu.system.domain.model.enums.RolePrivilege;
import com.thundax.kuzhambu.system.domain.model.enums.RoleStatus;
import com.thundax.kuzhambu.system.infra.core.dataobject.MenuRoleDO;
import com.thundax.kuzhambu.system.infra.core.dataobject.RoleDO;
import com.thundax.kuzhambu.system.infra.core.dataobject.UserRoleDO;
import java.util.ArrayList;
import java.util.List;

public final class RolePersistenceAssembler {

    private RolePersistenceAssembler() {}

    public static RoleDO toDataObject(Role entity) {
        if (entity == null) {
            return null;
        }
        RoleDO dataObject = new RoleDO();
        dataObject.setId(RoleIdCodec.toValue(entity.getId()));
        dataObject.setName(entity.getName());
        dataObject.setPrivilege(privilegeValue(entity.getPrivilege()));
        dataObject.setStatus(statusValue(entity.getStatus()));
        dataObject.setPriority(priorityOrDefault(entity.getPriority()));
        dataObject.setRemarks(entity.getRemarks());
        return dataObject;
    }

    public static Role toEntity(RoleDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        Role entity = new Role();
        entity.setId(RoleIdCodec.toDomain(dataObject.getId()));
        entity.setName(dataObject.getName());
        entity.setPrivilege(privilegeFrom(dataObject.getPrivilege()));
        entity.setStatus(statusFrom(dataObject.getStatus()));
        entity.setPriority(priorityOrDefault(dataObject.getPriority()));
        entity.setRemarks(dataObject.getRemarks());
        return entity;
    }

    public static List<Role> toEntityList(List<RoleDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<Role> entities = new ArrayList<>();
        for (RoleDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    public static MenuRoleDO toMenuRoleDataObject(Long roleId, Long menuId) {
        return new MenuRoleDO(roleId, menuId);
    }

    public static UserRoleDO toUserRoleDataObject(Long userId, Long roleId) {
        return new UserRoleDO(userId, roleId);
    }

    private static int priorityOrDefault(Integer priority) {
        return priority == null || priority < 0 ? 0 : priority;
    }

    private static String privilegeValue(RolePrivilege privilege) {
        return privilege == null ? null : privilege.value();
    }

    private static RolePrivilege privilegeFrom(String privilege) {
        return privilege == null ? null : RolePrivilege.from(privilege);
    }

    private static String statusValue(RoleStatus status) {
        return status == null ? null : status.value();
    }

    private static RoleStatus statusFrom(String status) {
        return status == null ? null : RoleStatus.from(status);
    }
}
