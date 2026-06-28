package com.thundax.kuzhambu.storage.facade.request;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BindStorageObjectOwnerFacadeRequest {

    private final List<Long> storageObjectIds;
    private final String ownerId;
    private final String ownerType;
    private final String ownerParams;
}
