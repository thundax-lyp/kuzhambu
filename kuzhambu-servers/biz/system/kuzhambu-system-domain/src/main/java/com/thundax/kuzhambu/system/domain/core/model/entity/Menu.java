package com.thundax.kuzhambu.system.domain.core.model.entity;

import com.thundax.kuzhambu.system.domain.core.model.enums.MenuVisibility;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.AccessRank;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
    private MenuId id;

    private MenuId parentId;

    private String name;
    private String perms;
    private AccessRank rank = AccessRank.of(0);
    private MenuVisibility visibility = MenuVisibility.VISIBLE;
    private String displayParams;
    private String url;
    private String target;
    private String remarks;

    public boolean isDisplay() {
        return MenuVisibility.VISIBLE == getVisibility();
    }

    public AccessRank getRank() {
        return rank == null ? AccessRank.of(null) : rank;
    }

    public void setRank(AccessRank rank) {
        this.rank = rank == null ? AccessRank.of(null) : rank;
    }
}
