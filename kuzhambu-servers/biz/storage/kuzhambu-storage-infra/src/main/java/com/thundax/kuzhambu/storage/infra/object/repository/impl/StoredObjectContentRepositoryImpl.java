package com.thundax.kuzhambu.storage.infra.object.repository.impl;

import com.thundax.kuzhambu.common.oss.client.ObjectStorageClient;
import com.thundax.kuzhambu.common.oss.model.ObjectStorageWriteResult;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectContentRepository;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

public class StoredObjectContentRepositoryImpl implements StoredObjectContentRepository {

    private static final ZoneId PATH_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter PATH_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMM").withZone(PATH_ZONE);

    private final ObjectStorageClient objectStorageClient;
    private final String bucketName;
    private final String contentPath;

    public StoredObjectContentRepositoryImpl(
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

    @Override
    public void delete(StoredObject storage) throws IOException {
        objectStorageClient.delete(objectKey(storage));
    }

    private String objectKey(StoredObject storage) {
        return storage.getObjectKey() == null ? storage.getPathName() : storage.getObjectKey();
    }

    private String writeObjectKey(StoredObject storage) {
        return writeObjectKey(storage, Instant.now());
    }

    String writeObjectKey(StoredObject storage, Instant now) {
        if (StringUtils.isNotBlank(storage.getObjectKey())) {
            return storage.getObjectKey();
        }
        if (storage.getId() != null) {
            return storage.getPathName();
        }
        String extendName = StringUtils.defaultIfBlank(storage.getExtendName(), "bin");
        return PATH_FORMATTER.format(now) + "/" + UUID.randomUUID() + "." + extendName;
    }
}
