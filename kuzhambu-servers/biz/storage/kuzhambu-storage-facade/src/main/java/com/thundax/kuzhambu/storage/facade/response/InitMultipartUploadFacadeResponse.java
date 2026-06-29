package com.thundax.kuzhambu.storage.facade.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InitMultipartUploadFacadeResponse {

    private final String uploadId;
    private final String providerUploadId;
    private final String ownerType;
    private final String ownerId;
    private final String businessType;
    private final String originalFilename;
    private final String mimeType;
    private final String bucketName;
    private final String objectKey;
    private final Long totalSize;
    private final Long partSize;
    private final Integer uploadedPartCount;
    private final String uploadStatus;
}
