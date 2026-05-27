package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleSortCommand {

    private List<RoleId> orderedIds;
    private SortDirection sortDirection;
}
