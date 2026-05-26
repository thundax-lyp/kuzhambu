package com.thundax.kuzhambu.common.oss.client;

import com.thundax.kuzhambu.common.oss.model.ObjectStorageWriteResult;
import java.io.IOException;
import java.io.InputStream;

public interface ObjectStorageClient {

    ObjectStorageWriteResult put(String key, InputStream inputStream) throws IOException;

    InputStream get(String key) throws IOException;

    boolean exists(String key);

    void delete(String key) throws IOException;
}
