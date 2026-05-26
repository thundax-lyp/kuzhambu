package com.thundax.kuzhambu.biz.storage.dao;

import com.thundax.kuzhambu.biz.storage.entity.StoredObject;
import com.thundax.kuzhambu.biz.storage.entity.StoredObjectReference;
import java.util.List;

public interface StoredObjectReferenceDao {

    List<String> listReferenceOwnerTypes();

    List<StoredObjectReference> listReferences(StoredObject entity);

    void insertReferences(List<StoredObjectReference> list);

    void deleteByObjectId(String id);

    int deleteByOwner(String referenceOwnerType, String referenceOwnerId);
}
