package com.thundax.kuzhambu.storage.infra.configure;

import com.thundax.kuzhambu.common.oss.client.ObjectStorageClient;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectContentRepository;
import com.thundax.kuzhambu.storage.infra.object.repository.impl.StoredObjectContentRepositoryImpl;
import com.thundax.kuzhambu.storage.infra.object.support.StorageObjectContentSettings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageInfraProperties.class)
public class StorageInfraConfiguration {

    @Bean
    @ConditionalOnMissingBean(StoredObjectContentRepository.class)
    public StoredObjectContentRepository storedObjectContentRepository(
            ObjectStorageClient objectStorageClient, StorageInfraProperties properties) {
        StorageObjectContentSettings settings = StorageObjectContentSettings.from(properties);
        return new StoredObjectContentRepositoryImpl(
                objectStorageClient, settings.bucketName(), settings.contentPath());
    }
}
