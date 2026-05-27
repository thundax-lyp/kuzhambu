package com.thundax.kuzhambu.storage.infra.configure;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.storage")
public class StorageInfraProperties {

    private String bucketName = "local";
    private String contentPath = "/api/storage/object/";

    public String getBucketName() {
        return StringUtils.defaultIfBlank(bucketName, "local");
    }

    public String getContentPath() {
        if (StringUtils.isBlank(contentPath)) {
            return "/api/storage/object/";
        }
        return contentPath.endsWith("/") ? contentPath : contentPath + "/";
    }
}
