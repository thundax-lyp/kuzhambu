package com.thundax.kuzhambu.storage.facade.response;

import com.thundax.kuzhambu.storage.facade.dto.ReadableStoredObjectFacadeDto;
import java.io.InputStream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetReadableContentFacadeResponse {

    private final ReadableStoredObjectFacadeDto storedObject;
    private final InputStream inputStream;
}
