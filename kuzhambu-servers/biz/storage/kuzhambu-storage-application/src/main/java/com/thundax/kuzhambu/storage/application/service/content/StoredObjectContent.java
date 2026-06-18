package com.thundax.kuzhambu.storage.application.service.content;

import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import java.io.InputStream;
import lombok.Getter;

@Getter
public class StoredObjectContent {

    private final StoredObject storage;
    private final InputStream inputStream;

    public StoredObjectContent(StoredObject storage, InputStream inputStream) {
        this.storage = storage;
        this.inputStream = inputStream;
    }
}
