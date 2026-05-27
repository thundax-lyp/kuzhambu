package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.system.domain.core.valueobject.DictId;
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
