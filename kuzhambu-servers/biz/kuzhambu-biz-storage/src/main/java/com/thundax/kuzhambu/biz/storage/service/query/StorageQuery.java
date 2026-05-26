package com.thundax.kuzhambu.biz.storage.service.query;

import com.thundax.kuzhambu.biz.storage.entity.enums.StorageOwnerType;
import com.thundax.kuzhambu.biz.storage.entity.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.biz.storage.entity.enums.StoredObjectStatus;
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
public class StorageQuery {
    private StoredObjectId id;
    private List<StoredObjectId> ids;
    private String contentType;
    private String referenceOwnerId;
    private String referenceOwnerType;
    private String ownerId;
    private StorageOwnerType ownerType;
    private StoredObjectStatus objectStatus;
    private StoredObjectReferenceStatus referenceStatus;
    private String originalFilename;
    private String remarks;
    private SortDirection sortDirection = SortDirection.ASC;
}
