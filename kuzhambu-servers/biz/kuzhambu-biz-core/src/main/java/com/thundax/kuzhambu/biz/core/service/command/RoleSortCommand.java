package com.thundax.kuzhambu.biz.core.service.command;

import com.thundax.kuzhambu.biz.core.entity.valueobject.RoleId;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
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
