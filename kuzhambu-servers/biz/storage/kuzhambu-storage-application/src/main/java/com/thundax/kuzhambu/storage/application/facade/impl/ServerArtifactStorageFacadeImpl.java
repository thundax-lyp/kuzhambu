package com.thundax.kuzhambu.storage.application.facade.impl;

import com.thundax.kuzhambu.storage.application.facade.assembler.ServerArtifactStorageFacadeAssembler;
import com.thundax.kuzhambu.storage.application.service.ServerArtifactStorageApplicationService;
import com.thundax.kuzhambu.storage.facade.ServerArtifactStorageFacade;
import com.thundax.kuzhambu.storage.facade.request.StoreServerArtifactFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.StoreServerArtifactFacadeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServerArtifactStorageFacadeImpl implements ServerArtifactStorageFacade {

    private final ServerArtifactStorageApplicationService serverArtifactStorageApplicationService;
    private final ServerArtifactStorageFacadeAssembler facadeAssembler;

    public ServerArtifactStorageFacadeImpl(
            ServerArtifactStorageApplicationService serverArtifactStorageApplicationService,
            ServerArtifactStorageFacadeAssembler facadeAssembler) {
        this.serverArtifactStorageApplicationService = serverArtifactStorageApplicationService;
        this.facadeAssembler = facadeAssembler;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoreServerArtifactFacadeResponse storeServerArtifact(StoreServerArtifactFacadeRequest request) {
        return facadeAssembler.toResponse(
                serverArtifactStorageApplicationService.storeServerArtifact(facadeAssembler.toCommand(request)));
    }
}
