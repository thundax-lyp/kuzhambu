package com.thundax.kuzhambu.storage.facade;

import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.ListStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.MarkStorageUsageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.RemoveStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UnbindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.ListStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;

public interface StorageFacade {

    boolean exists(OpenStorageFacadeRequest request);

    OpenStorageFacadeResponse open(OpenStorageFacadeRequest request);

    ListStorageFacadeResponse list(ListStorageFacadeRequest request);

    UploadStorageFacadeResponse upload(UploadStorageFacadeRequest request);

    void remove(RemoveStorageFacadeRequest request);

    void bindOwner(BindStorageOwnerFacadeRequest request);

    void unbindOwner(UnbindStorageOwnerFacadeRequest request);

    void markInUse(MarkStorageUsageFacadeRequest request);

    void markUnused(MarkStorageUsageFacadeRequest request);
}
