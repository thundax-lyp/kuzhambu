package com.thundax.kuzhambu.storage.facade.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UploadMultipartPartFacadeResponse {

    private final String uploadId;
    private final Integer partNumber;
    private final String etag;
    private final Long size;
    private final String uploadStatus;
}
