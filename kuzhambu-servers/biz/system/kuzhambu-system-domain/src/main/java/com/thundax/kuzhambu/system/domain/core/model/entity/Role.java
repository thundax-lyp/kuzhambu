package com.thundax.kuzhambu.system.domain.core.model.entity;

import com.thundax.kuzhambu.common.core.sort.Sortable;
import com.thundax.kuzhambu.system.domain.core.model.enums.RolePrivilege;
import com.thundax.kuzhambu.system.domain.core.model.enums.RoleStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Sortable {
    private RoleId id;
    private String name;
    private RolePrivilege privilege = RolePrivilege.NORMAL;
    private RoleStatus status;
    private int priority;
    private String remarks;

    public boolean isAdmin() {
        return RolePrivilege.ADMIN == getPrivilege();
    }

    public boolean isEnable() {
        return RoleStatus.ENABLED == getStatus();
    }
}
