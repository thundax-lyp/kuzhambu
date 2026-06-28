package com.thundax.kuzhambu.storage.facade;

import com.thundax.kuzhambu.storage.facade.request.StoreServerArtifactFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.StoreServerArtifactFacadeResponse;

public interface ServerArtifactStorageFacade {

    StoreServerArtifactFacadeResponse storeServerArtifact(StoreServerArtifactFacadeRequest request);
}
