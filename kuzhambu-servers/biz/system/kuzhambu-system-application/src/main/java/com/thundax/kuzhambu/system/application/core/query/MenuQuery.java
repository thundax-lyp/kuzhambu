package com.thundax.kuzhambu.system.application.core.query;

import com.thundax.kuzhambu.system.domain.core.model.enums.MenuVisibility;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.AccessRank;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import java.util.List;

public record MenuQuery(
        List<MenuId> ids,
        MenuId childId,
        MenuId ancestorId,
        MenuId parentId,
        MenuVisibility visibility,
        AccessRank maxRank) {}
