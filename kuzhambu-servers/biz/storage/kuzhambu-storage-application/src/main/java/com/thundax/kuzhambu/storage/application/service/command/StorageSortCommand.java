package com.thundax.kuzhambu.storage.application.service.command;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.domain.model.valueobject.StoredObjectId;
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
