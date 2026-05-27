package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import com.thundax.kuzhambu.system.domain.core.valueobject.DepartmentId;
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
