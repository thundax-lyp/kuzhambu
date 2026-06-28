package com.thundax.kuzhambu.storage.facade;

import com.thundax.kuzhambu.storage.facade.request.UploadStorageObjectFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageObjectFacadeResponse;

public interface StorageUploadFacade {

    UploadStorageObjectFacadeResponse uploadStorageObject(UploadStorageObjectFacadeRequest request);
}
