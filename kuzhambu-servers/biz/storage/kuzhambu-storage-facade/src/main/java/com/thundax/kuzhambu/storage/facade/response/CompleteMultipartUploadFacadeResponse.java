package com.thundax.kuzhambu.storage.facade.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CompleteMultipartUploadFacadeResponse {

    private final Long storageObjectId;
    private final String uploadId;
    private final String ownerType;
    private final String ownerId;
    private final String businessType;
    private final String originalFilename;
    private final String mimeType;
    private final String bucketName;
    private final String objectKey;
    private final Long size;
    private final String accessEndpoint;
    private final String objectStatus;
    private final String referenceStatus;
    private final String providerUploadId;
}
