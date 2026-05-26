package com.thundax.kuzhambu.biz.core.service.command;

import com.thundax.kuzhambu.biz.core.entity.enums.MenuVisibility;
import com.thundax.kuzhambu.biz.core.entity.valueobject.MenuId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeMenuVisibilityCommand {
    private MenuId id;
    private MenuVisibility visibility;
}
