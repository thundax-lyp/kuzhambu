package com.thundax.kuzhambu.storage.infra.store;

import com.thundax.kuzhambu.common.oss.client.ObjectStorageClient;
import com.thundax.kuzhambu.common.oss.model.ObjectStorageWriteResult;
import com.thundax.kuzhambu.storage.application.store.StoredObjectStore;
import com.thundax.kuzhambu.storage.domain.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

public class ObjectStorageStoredObjectStore implements StoredObjectStore {

    private static final String PATH_FORMAT = "yyyyMM";

    private final ObjectStorageClient objectStorageClient;
    private final String bucketName;
    private final String contentPath;

    public ObjectStorageStoredObjectStore(
            ObjectStorageClient objectStorageClient, String bucketName, String contentPath) {
        this.objectStorageClient = objectStorageClient;
        this.bucketName = bucketName;
        this.contentPath = contentPath;
    }

    @Override
    public StoredObject save(StoredObject storage, InputStream inputStream) throws IOException {
        ObjectStorageWriteResult result = objectStorageClient.put(writeObjectKey(storage), inputStream);
        StoredObject storedObject = new StoredObject();
        storedObject.setBucketName(bucketName);
        storedObject.setObjectKey(result.getKey());
        storedObject.setSize(result.getSize());
        if (storage.getId() != null) {
            storedObject.setAccessEndpoint(contentPath + StoredObjectIdCodec.toValue(storage.getId()) + "/content");
        }
        return storedObject;
    }

    @Override
    public boolean exists(StoredObject storage) {
        return objectStorageClient.exists(objectKey(storage));
    }

    @Override
    public InputStream open(StoredObject storage) throws IOException {
        return objectStorageClient.get(objectKey(storage));
    }

    private String objectKey(StoredObject storage) {
        return storage.getObjectKey() == null ? storage.getPathName() : storage.getObjectKey();
    }

    private String writeObjectKey(StoredObject storage) {
        if (StringUtils.isNotBlank(storage.getObjectKey())) {
            return storage.getObjectKey();
        }
        if (storage.getId() != null) {
            return storage.getPathName();
        }
        String extendName = StringUtils.defaultIfBlank(storage.getExtendName(), "bin");
        return new SimpleDateFormat(PATH_FORMAT).format(new Date()) + "/" + UUID.randomUUID() + "." + extendName;
    }
}
