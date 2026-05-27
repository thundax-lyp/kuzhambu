package com.thundax.kuzhambu.system.infra.core.persistence.assembler;

import com.thundax.kuzhambu.system.domain.core.codec.AccessRankCodec;
import com.thundax.kuzhambu.system.domain.core.codec.DepartmentIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserPrivilege;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserStatus;
import com.thundax.kuzhambu.system.infra.core.persistence.dataobject.UserDO;
import com.thundax.kuzhambu.system.infra.core.persistence.dataobject.UserRoleDO;
import java.util.ArrayList;
import java.util.List;

public final class UserPersistenceAssembler {

    private UserPersistenceAssembler() {}

    public static UserDO toObject(User entity) {
        if (entity == null) {
            return null;
        }
        UserDO dataObject = new UserDO();
        dataObject.setId(UserIdCodec.toValue(entity.getId()));
        dataObject.setDepartmentId(DepartmentIdCodec.toValue(entity.getDepartmentId()));
        dataObject.setEmail(entity.getEmail());
        dataObject.setMobile(entity.getMobile());
        dataObject.setTel(entity.getTel());
        dataObject.setName(entity.getName());
        dataObject.setRanks(AccessRankCodec.toValue(entity.getRank()));
        dataObject.setPrivilege(privilegeValue(entity.getPrivilege()));
        dataObject.setStatus(statusValue(entity.getStatus()));
        dataObject.setRemarks(entity.getRemarks());
        return dataObject;
    }

    public static User toDomain(UserDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        User entity = new User();
        entity.setId(UserIdCodec.toDomain(dataObject.getId()));
        entity.setDepartmentId(DepartmentIdCodec.toDomain(dataObject.getDepartmentId()));
        entity.setEmail(dataObject.getEmail());
        entity.setMobile(dataObject.getMobile());
        entity.setTel(dataObject.getTel());
        entity.setName(dataObject.getName());
        entity.setRank(AccessRankCodec.toDomain(dataObject.getRanks()));
        entity.setPrivilege(privilegeFrom(dataObject.getPrivilege()));
        entity.setStatus(statusFrom(dataObject.getStatus()));
        entity.setRemarks(dataObject.getRemarks());
        return entity;
    }

    public static List<User> toDomainList(List<UserDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<User> entities = new ArrayList<>();
        for (UserDO dataObject : dataObjects) {
            entities.add(toDomain(dataObject));
        }
        return entities;
    }

    public static UserRoleDO toUserRoleObject(Long userId, Long roleId) {
        return new UserRoleDO(userId, roleId);
    }

    private static String privilegeValue(UserPrivilege privilege) {
        return privilege == null ? null : privilege.value();
    }

    private static UserPrivilege privilegeFrom(String privilege) {
        return privilege == null ? null : UserPrivilege.from(privilege);
    }

    private static String statusValue(UserStatus status) {
        return status == null ? null : status.value();
    }

    private static UserStatus statusFrom(String status) {
        return status == null ? null : UserStatus.from(status);
    }
}
