package com.thundax.kuzhambu.storage.infra.object.support;

import com.thundax.kuzhambu.storage.infra.configure.StorageInfraProperties;
import org.apache.commons.lang3.StringUtils;

public record StorageObjectContentSettings(String bucketName, String contentPath) {

    private static final String DEFAULT_BUCKET_NAME = "local";
    private static final String DEFAULT_CONTENT_PATH = "/api/storage/object/";

    public static StorageObjectContentSettings from(StorageInfraProperties properties) {
        String contentPath = properties.getContentPath();
        return new StorageObjectContentSettings(
                StringUtils.defaultIfBlank(properties.getBucketName(), DEFAULT_BUCKET_NAME),
                normalizeContentPath(contentPath));
    }

    private static String normalizeContentPath(String contentPath) {
        if (StringUtils.isBlank(contentPath)) {
            return DEFAULT_CONTENT_PATH;
        }
        return contentPath.endsWith("/") ? contentPath : contentPath + "/";
    }
}
