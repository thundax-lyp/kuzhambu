package com.thundax.kuzhambu.biz.core.service.query;

import com.thundax.kuzhambu.biz.core.entity.enums.MenuVisibility;
import com.thundax.kuzhambu.biz.core.entity.valueobject.AccessRank;
import com.thundax.kuzhambu.biz.core.entity.valueobject.MenuId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuQuery {
    private List<MenuId> ids;
    private MenuId childId;
    private MenuId ancestorId;
    private MenuId parentId;
    private MenuVisibility visibility;
    private AccessRank maxRank;
}
