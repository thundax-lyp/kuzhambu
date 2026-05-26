package com.thundax.kuzhambu.biz.core.service.command;

import com.thundax.kuzhambu.biz.core.entity.valueobject.DepartmentId;
import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
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
