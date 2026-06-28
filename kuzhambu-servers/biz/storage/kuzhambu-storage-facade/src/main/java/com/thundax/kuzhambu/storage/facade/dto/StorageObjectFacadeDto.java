package com.thundax.kuzhambu.storage.facade.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StorageObjectFacadeDto {

    private final Long id;
    private final String originalFilename;
    private final String contentType;
    private final String ownerId;
    private final String ownerType;
    private final Long size;
    private final String objectStatus;
    private final String referenceStatus;
    private final String remarks;
}
