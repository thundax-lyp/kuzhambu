package com.thundax.kuzhambu.storage.application.dao;

import com.thundax.kuzhambu.storage.domain.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.model.entity.StoredObjectReference;
import java.util.List;

public interface StoredObjectReferenceDao {

    List<String> listReferenceOwnerTypes();

    List<StoredObjectReference> listReferences(StoredObject entity);

    void insertReferences(List<StoredObjectReference> list);

    void deleteByObjectId(String id);

    int deleteByOwner(String referenceOwnerType, String referenceOwnerId);
}
