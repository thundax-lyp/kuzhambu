package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.enums.MenuVisibility;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;

public record ChangeMenuVisibilityCommand(MenuId id, MenuVisibility visibility) {}
