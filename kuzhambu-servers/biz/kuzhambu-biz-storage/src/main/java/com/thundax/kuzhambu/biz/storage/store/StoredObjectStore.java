package com.thundax.kuzhambu.biz.storage.store;

import com.thundax.kuzhambu.biz.storage.entity.StoredObject;
import java.io.IOException;
import java.io.InputStream;

public interface StoredObjectStore {

    StoredObject save(StoredObject storage, InputStream inputStream) throws IOException;

    boolean exists(StoredObject storage);

    InputStream open(StoredObject storage) throws IOException;
}
