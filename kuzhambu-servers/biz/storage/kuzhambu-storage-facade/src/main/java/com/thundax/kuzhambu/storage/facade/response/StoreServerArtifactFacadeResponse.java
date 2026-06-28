package com.thundax.kuzhambu.storage.facade.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreServerArtifactFacadeResponse {

    private final Long storageObjectId;
    private final String originalFilename;
    private final String contentType;
    private final Long sizeBytes;
}
