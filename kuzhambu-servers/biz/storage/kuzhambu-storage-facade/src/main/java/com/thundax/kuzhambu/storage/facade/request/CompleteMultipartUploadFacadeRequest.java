package com.thundax.kuzhambu.storage.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CompleteMultipartUploadFacadeRequest {

    private final String uploadId;
    private final String bucketName;
    private final String objectKey;
    private final Long size;
    private final String accessEndpoint;
}
