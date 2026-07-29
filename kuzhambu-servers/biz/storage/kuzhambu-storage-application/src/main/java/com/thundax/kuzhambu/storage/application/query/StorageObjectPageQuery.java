package com.thundax.kuzhambu.storage.application.query;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StorageObjectPageQuery {
    private StorageMimeType mimeType;
    private StoredObjectStatus objectStatus;
    private StoredObjectReferenceStatus referenceStatus;
    private StorageOwnerRef referenceOwnerRef;
    private String originalFilename;
    private String remarks;
    private SortDirection sortDirection = SortDirection.ASC;
    private int pageNo;
    private int pageSize;
}
