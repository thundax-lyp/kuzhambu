package com.thundax.kuzhambu.storage.domain.object.repository;

import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import java.util.List;

public interface StoredObjectReferenceRepository {

    List<String> listReferenceOwnerTypes();

    List<StoredObjectReference> listReferences(StoredObject entity);

    void insertReferences(List<StoredObjectReference> list);

    void deleteByObjectId(String id);

    int deleteByOwner(String referenceOwnerType, String referenceOwnerId);
}
