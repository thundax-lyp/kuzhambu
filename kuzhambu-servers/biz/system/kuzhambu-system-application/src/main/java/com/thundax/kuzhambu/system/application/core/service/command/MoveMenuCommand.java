package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.system.application.core.entity.valueobject.MenuId;
import com.thundax.kuzhambu.common.core.tree.TreeNodeMoveType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoveMenuCommand {
    private MenuId fromId;
    private MenuId toId;
    private TreeNodeMoveType moveType;
}
