package com.thundax.kuzhambu.storage.facade;

import com.thundax.kuzhambu.storage.facade.request.BindStorageObjectOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.GetReadableContentFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.MarkStorageObjectUsageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UnbindStorageObjectOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageObjectFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.GetReadableContentFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageObjectFacadeResponse;

public interface StorageFacade {

    boolean exists(GetReadableContentFacadeRequest request);

    GetReadableContentFacadeResponse open(GetReadableContentFacadeRequest request);

    UploadStorageObjectFacadeResponse upload(UploadStorageObjectFacadeRequest request);

    void bindOwner(BindStorageObjectOwnerFacadeRequest request);

    void unbindOwner(UnbindStorageObjectOwnerFacadeRequest request);

    void markInUse(MarkStorageObjectUsageFacadeRequest request);

    void markUnused(MarkStorageObjectUsageFacadeRequest request);
}
