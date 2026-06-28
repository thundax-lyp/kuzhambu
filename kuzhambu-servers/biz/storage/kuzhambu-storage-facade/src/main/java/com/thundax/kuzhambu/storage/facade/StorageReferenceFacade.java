package com.thundax.kuzhambu.storage.facade;

import com.thundax.kuzhambu.storage.facade.request.AddStorageReferencesFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.ChangeStorageReferenceStatusFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.RemoveStorageReferencesFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.StorageReferenceFacadeResponse;

public interface StorageReferenceFacade {

    StorageReferenceFacadeResponse addReferences(AddStorageReferencesFacadeRequest request);

    StorageReferenceFacadeResponse removeReferences(RemoveStorageReferencesFacadeRequest request);

    StorageReferenceFacadeResponse changeReferenceStatus(ChangeStorageReferenceStatusFacadeRequest request);
}
