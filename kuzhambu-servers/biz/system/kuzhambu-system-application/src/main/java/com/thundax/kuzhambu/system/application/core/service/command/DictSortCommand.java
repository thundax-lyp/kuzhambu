package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.system.application.core.entity.valueobject.DictId;
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
public class DictSortCommand {

    private List<DictId> orderedIds;
    private SortDirection sortDirection;
}
