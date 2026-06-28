package com.thundax.kuzhambu.storage.application.facade.assembler;

import com.thundax.kuzhambu.storage.application.service.command.UploadServerArtifactCommand;
import com.thundax.kuzhambu.storage.application.service.result.ServerArtifactStoredResult;
import com.thundax.kuzhambu.storage.facade.request.StoreServerArtifactFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.StoreServerArtifactFacadeResponse;
import org.springframework.stereotype.Component;

@Component
public class ServerArtifactStorageFacadeAssembler {

    public UploadServerArtifactCommand toCommand(StoreServerArtifactFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new UploadServerArtifactCommand(
                request.getContentBytes(),
                request.getOriginalFilename(),
                request.getContentType(),
                request.getSizeBytes());
    }

    public StoreServerArtifactFacadeResponse toResponse(ServerArtifactStoredResult result) {
        if (result == null) {
            return null;
        }
        return StoreServerArtifactFacadeResponse.builder()
                .storageObjectId(
                        result.getStorageObjectId() == null
                                ? null
                                : result.getStorageObjectId().value())
                .originalFilename(result.getOriginalFilename())
                .contentType(result.getContentType())
                .sizeBytes(result.getSizeBytes())
                .build();
    }
}
