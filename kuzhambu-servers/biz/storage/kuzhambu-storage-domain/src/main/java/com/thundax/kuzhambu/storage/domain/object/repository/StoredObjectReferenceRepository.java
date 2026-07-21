package com.thundax.kuzhambu.storage.domain.object.repository;

import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import java.util.List;

public interface StoredObjectReferenceRepository {

    List<String> listReferenceOwnerTypes();

    List<StoredObjectReference> listReferences(StoredObject entity);

    List<StoredObjectId> listObjectIdsByOwner(StorageOwnerRef ownerRef);

    void insertReferences(List<StoredObjectReference> list);

    boolean exists(StoredObjectReference reference);

    long countByObjectId(StoredObjectId objectId);

    void deleteByObjectId(String id);

    int deleteByOwner(StorageOwnerRef ownerRef);
}
