package com.thundax.kuzhambu.biz.core.service.command;

import com.thundax.kuzhambu.biz.core.entity.enums.MenuVisibility;
import com.thundax.kuzhambu.biz.core.entity.valueobject.AccessRank;
import com.thundax.kuzhambu.biz.core.entity.valueobject.MenuId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuCommand {
    private MenuId id;
    private MenuId parentId;
    private String name;
    private String perms;
    private AccessRank rank;
    private MenuVisibility visibility;
    private String displayParams;
    private String url;
    private String target;
    private String remarks;
}
