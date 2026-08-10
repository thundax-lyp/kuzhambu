package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;

public record MoveDepartmentCommand(DepartmentId fromId, DepartmentId toId, TreeNodeMoveType moveType) {}
