package com.thundax.kuzhambu.system.domain.core.model.entity;

import com.thundax.kuzhambu.common.core.sort.Sortable;
import com.thundax.kuzhambu.system.domain.core.codec.MenuIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.enums.RolePrivilege;
import com.thundax.kuzhambu.system.domain.core.model.enums.RoleStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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

    private List<Long> menuIdList;

    public boolean isAdmin() {
        return RolePrivilege.ADMIN == getPrivilege();
    }

    public boolean isEnable() {
        return RoleStatus.ENABLED == getStatus();
    }

    public List<Long> getMenuIdList() {
        if (this.menuIdList == null) {
            this.menuIdList = new ArrayList<>();
        }
        return this.menuIdList;
    }

    public void setMenuIdList(List<Long> menuIdList) {
        this.menuIdList = menuIdList;
    }

    public void setMenuList(List<Menu> menuList) {
        this.menuIdList = menuList == null
                ? new ArrayList<>()
                : menuList.stream()
                        .map(menu -> MenuIdCodec.toValue(menu.getId()))
                        .collect(Collectors.toList());
    }
}
