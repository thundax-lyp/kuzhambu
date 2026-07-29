package com.thundax.kuzhambu.storage.application.result;

import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import java.io.InputStream;
import lombok.Getter;

@Getter
public class StoredObjectContentResult {

    private final StoredObject storage;
    private final InputStream inputStream;

    public StoredObjectContentResult(StoredObject storage, InputStream inputStream) {
        this.storage = storage;
        this.inputStream = inputStream;
    }
}
