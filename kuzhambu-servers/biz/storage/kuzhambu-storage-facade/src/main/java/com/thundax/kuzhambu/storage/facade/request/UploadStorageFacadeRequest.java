package com.thundax.kuzhambu.storage.facade.request;

import java.io.InputStream;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UploadStorageFacadeRequest {

    private final InputStream inputStream;
    private final String originalFilename;
    private final String contentType;
    private final Long sizeBytes;
    private final List<String> allowedSuffixes;
    private final String ownerType;
    private final String ownerId;
    private final String objectStatus;
    private final String referenceStatus;
    private final String remarks;
}
