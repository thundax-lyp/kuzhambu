package com.thundax.kuzhambu.storage.domain.object.repository;

import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import java.io.IOException;
import java.io.InputStream;

public interface StoredObjectContentRepository {

    StoredObject save(StoredObject storage, InputStream inputStream) throws IOException;

    boolean exists(StoredObject storage);

    InputStream open(StoredObject storage) throws IOException;

    void delete(StoredObject storage) throws IOException;
}
