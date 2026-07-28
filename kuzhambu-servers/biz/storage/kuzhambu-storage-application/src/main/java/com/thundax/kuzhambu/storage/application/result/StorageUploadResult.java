package com.thundax.kuzhambu.storage.application.result;

import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StorageUploadResult {
    private final StoredObject storage;
    private final String error;

    public boolean hasError() {
        return error != null && !error.isBlank();
    }
}
