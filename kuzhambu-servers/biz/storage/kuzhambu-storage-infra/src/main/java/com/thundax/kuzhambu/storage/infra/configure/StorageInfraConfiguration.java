package com.thundax.kuzhambu.storage.infra.configure;

import com.thundax.kuzhambu.common.oss.client.ObjectStorageClient;
import com.thundax.kuzhambu.storage.application.store.StoredObjectStore;
import com.thundax.kuzhambu.storage.infra.store.ObjectStorageStoredObjectStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageInfraProperties.class)
public class StorageInfraConfiguration {

    @Bean
    @ConditionalOnMissingBean(StoredObjectStore.class)
    public StoredObjectStore storedObjectStore(
            ObjectStorageClient objectStorageClient, StorageInfraProperties properties) {
        return new ObjectStorageStoredObjectStore(
                objectStorageClient, properties.getBucketName(), properties.getContentPath());
    }
}
