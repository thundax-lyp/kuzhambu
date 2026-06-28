package com.thundax.kuzhambu.storage.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreServerArtifactFacadeRequest {

    private final byte[] contentBytes;
    private final String originalFilename;
    private final String contentType;
    private final Long sizeBytes;
}
