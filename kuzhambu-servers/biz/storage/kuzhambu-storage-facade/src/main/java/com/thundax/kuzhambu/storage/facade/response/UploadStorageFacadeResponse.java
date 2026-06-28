package com.thundax.kuzhambu.storage.facade.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UploadStorageFacadeResponse {

    private final Long storageObjectId;
    private final String originalFilename;
    private final String contentType;
    private final String name;
    private final String extendName;
    private final String mimeType;
    private final String bucketName;
    private final String objectKey;
    private final Long sizeBytes;
    private final String accessEndpoint;
    private final String objectStatus;
    private final String referenceStatus;
    private final String remarks;
}
