package com.thundax.kuzhambu.system.application.core.service.query;

import com.thundax.kuzhambu.system.application.core.entity.enums.MenuVisibility;
import com.thundax.kuzhambu.system.domain.model.valueobject.AccessRank;
import com.thundax.kuzhambu.system.domain.model.valueobject.MenuId;
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
