package com.thundax.kuzhambu.storage.application.helper;

import com.thundax.kuzhambu.storage.domain.model.entity.StoredObject;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StorageUploadResult {
    private final StoredObject storage;
    private final String error;

    public boolean hasError() {
        return error != null;
    }
}
