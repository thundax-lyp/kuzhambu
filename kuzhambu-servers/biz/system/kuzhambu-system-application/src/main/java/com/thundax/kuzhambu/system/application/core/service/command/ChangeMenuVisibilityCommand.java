package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.system.application.core.entity.enums.MenuVisibility;
import com.thundax.kuzhambu.system.domain.core.valueobject.MenuId;
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
