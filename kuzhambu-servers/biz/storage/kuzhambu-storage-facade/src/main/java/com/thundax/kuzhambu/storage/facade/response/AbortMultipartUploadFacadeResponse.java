package com.thundax.kuzhambu.storage.facade.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AbortMultipartUploadFacadeResponse {

    private final String uploadId;
    private final String uploadStatus;
}
