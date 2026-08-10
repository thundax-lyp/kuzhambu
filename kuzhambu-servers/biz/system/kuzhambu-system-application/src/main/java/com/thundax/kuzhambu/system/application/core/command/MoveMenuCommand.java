package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;

public record MoveMenuCommand(MenuId fromId, MenuId toId, TreeNodeMoveType moveType) {}
