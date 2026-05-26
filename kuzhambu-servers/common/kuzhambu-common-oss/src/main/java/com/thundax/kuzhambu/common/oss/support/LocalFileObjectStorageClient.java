package com.thundax.kuzhambu.common.oss.support;

import com.thundax.kuzhambu.common.oss.client.ObjectStorageClient;
import com.thundax.kuzhambu.common.oss.model.ObjectStorageWriteResult;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class LocalFileObjectStorageClient implements ObjectStorageClient {

    private final Path rootPath;
    private final String locationPrefix;

    public LocalFileObjectStorageClient(String rootPath, String locationPrefix) {
        this.rootPath = Paths.get(rootPath).toAbsolutePath().normalize();
        this.locationPrefix = locationPrefix;
    }

    @Override
    public ObjectStorageWriteResult put(String key, InputStream inputStream) throws IOException {
        Path target = resolveKey(key);
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        long size = Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        ObjectStorageWriteResult result = new ObjectStorageWriteResult();
        result.setKey(key);
        result.setLocation(locationPrefix + key);
        result.setSize(size);
        return result;
    }

    @Override
    public InputStream get(String key) throws IOException {
        return Files.newInputStream(resolveKey(key));
    }

    @Override
    public boolean exists(String key) {
        return Files.exists(resolveKey(key));
    }

    @Override
    public void delete(String key) throws IOException {
        Files.deleteIfExists(resolveKey(key));
    }

    private Path resolveKey(String key) {
        Path path = rootPath.resolve(key).normalize();
        if (!path.startsWith(rootPath)) {
            throw new IllegalArgumentException("Object key escapes storage root: " + key);
        }
        return path;
    }
}
