package com.thundax.kuzhambu.storage.infra.configure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.storage")
public class StorageInfraProperties {

    private String bucketName = "local";
    private String contentPath = "/api/storage/object/";
}
