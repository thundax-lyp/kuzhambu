package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoveDepartmentCommand {
    private DepartmentId fromId;
    private DepartmentId toId;
    private TreeNodeMoveType moveType;
}
