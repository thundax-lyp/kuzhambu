package com.thundax.kuzhambu.storage.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ListStorageFacadeRequest {

    private final String ownerType;
    private final String ownerId;
    private final String objectStatus;
    private final String referenceStatus;
    private final String remarks;
}
