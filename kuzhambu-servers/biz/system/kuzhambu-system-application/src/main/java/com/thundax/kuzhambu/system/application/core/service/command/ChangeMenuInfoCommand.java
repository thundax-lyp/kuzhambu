package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.system.application.core.entity.enums.MenuVisibility;
import com.thundax.kuzhambu.system.domain.core.valueobject.AccessRank;
import com.thundax.kuzhambu.system.domain.core.valueobject.MenuId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeMenuInfoCommand {
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
