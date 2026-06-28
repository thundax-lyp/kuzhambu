package com.thundax.kuzhambu.system.application.core.result;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAvatarResult {

    private final Long storageObjectId;
    private final String originalFilename;
    private final String contentType;
    private final String mimeType;
    private final Long sizeBytes;
    private final String ownerId;
    private final String ownerType;
    private final String objectStatus;
    private final String referenceStatus;
    private final String remarks;
}
