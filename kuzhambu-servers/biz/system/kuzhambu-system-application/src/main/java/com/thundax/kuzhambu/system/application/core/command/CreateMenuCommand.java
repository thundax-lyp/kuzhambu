package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.enums.MenuVisibility;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.AccessRank;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;

public record CreateMenuCommand(
        MenuId id,
        MenuId parentId,
        String name,
        String perms,
        AccessRank rank,
        MenuVisibility visibility,
        String displayParams,
        String url,
        String target,
        String remarks) {}
