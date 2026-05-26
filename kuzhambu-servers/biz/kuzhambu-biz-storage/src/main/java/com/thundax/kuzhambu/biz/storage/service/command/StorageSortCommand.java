package com.thundax.kuzhambu.biz.storage.service.command;

import com.thundax.kuzhambu.biz.storage.entity.valueobject.StoredObjectId;
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
public class StorageSortCommand {

    private List<StoredObjectId> orderedIds;
    private SortDirection sortDirection;
}
