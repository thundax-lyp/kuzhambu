package com.thundax.kuzhambu.common.oss.support;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.thundax.kuzhambu.common.oss.client.ObjectStorageClient;
import com.thundax.kuzhambu.common.oss.model.ObjectStorageWriteResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class S3ObjectStorageClient implements ObjectStorageClient {

    private static final int BUFFER_SIZE = 8192;

    private final AmazonS3 amazonS3;
    private final String bucket;
    private final String locationPrefix;

    public S3ObjectStorageClient(AmazonS3 amazonS3, String bucket, String locationPrefix) {
        this.amazonS3 = amazonS3;
        this.bucket = bucket;
        this.locationPrefix = locationPrefix == null ? "" : locationPrefix;
    }

    @Override
    public ObjectStorageWriteResult put(String key, InputStream inputStream) throws IOException {
        byte[] bytes = toBytes(inputStream);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        amazonS3.putObject(bucket, key, new ByteArrayInputStream(bytes), metadata);
        ObjectStorageWriteResult result = new ObjectStorageWriteResult();
        result.setKey(key);
        result.setLocation(locationPrefix + key);
        result.setSize(bytes.length);
        return result;
    }

    @Override
    public InputStream get(String key) {
        return amazonS3.getObject(bucket, key).getObjectContent();
    }

    @Override
    public boolean exists(String key) {
        return amazonS3.doesObjectExist(bucket, key);
    }

    @Override
    public void delete(String key) {
        amazonS3.deleteObject(bucket, key);
    }

    private byte[] toBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }
}
