package com.thundax.kuzhambu.biz.storage.helper;

import com.thundax.kuzhambu.biz.storage.entity.StoredObject;
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
