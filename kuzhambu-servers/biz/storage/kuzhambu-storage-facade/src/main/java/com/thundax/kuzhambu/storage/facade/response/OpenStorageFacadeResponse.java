package com.thundax.kuzhambu.storage.facade.response;

import com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto;
import java.io.InputStream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenStorageFacadeResponse {

    private final StorageObjectFacadeDto storedObject;
    private final InputStream inputStream;
}
