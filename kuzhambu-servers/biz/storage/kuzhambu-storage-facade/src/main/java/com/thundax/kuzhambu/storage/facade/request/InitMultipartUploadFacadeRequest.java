package com.thundax.kuzhambu.storage.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InitMultipartUploadFacadeRequest {

    private final String uploadId;
    private final String ownerId;
    private final String ownerType;
    private final String businessType;
    private final String originalFilename;
    private final String mimeType;
    private final String bucketName;
    private final String objectKey;
    private final String providerUploadId;
    private final Long totalSize;
    private final Long partSize;
}
