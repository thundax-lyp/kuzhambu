package com.thundax.kuzhambu.biz.storage.helper;

import com.thundax.kuzhambu.biz.storage.entity.StoredObject;
import com.thundax.kuzhambu.biz.storage.entity.enums.StorageOwnerType;
import com.thundax.kuzhambu.biz.storage.service.StorageService;
import com.thundax.kuzhambu.biz.storage.service.command.CreateStorageCommand;
import com.thundax.kuzhambu.biz.storage.store.StoredObjectStore;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class StorageUploadStreamHelper {

    private final StorageService storageService;
    private final StoredObjectStore storedObjectStore;

    public StorageUploadStreamHelper(StorageService storageService, StoredObjectStore storedObjectStore) {
        this.storageService = storageService;
        this.storedObjectStore = storedObjectStore;
    }

    public StorageUploadResult upload(
            InputStream inputStream,
            String originalFilename,
            String contentType,
            long size,
            List<String> allowedSuffixes,
            StorageOwnerType ownerType,
            String ownerId) {
        StorageUploadResult validatedResult = validateUploadFile(inputStream, originalFilename, size, allowedSuffixes);
        if (validatedResult.hasError()) {
            return validatedResult;
        }

        StoredObject storage = new StoredObject();
        storage.setOwnerType(ownerType);
        storage.setOwnerId(ownerId);
        applyFileMetadata(originalFilename, contentType, storage);
        try {
            applyStoredObject(storage, storedObjectStore.save(storage, inputStream));
        } catch (IOException e) {
            return StorageUploadResult.builder().error(e.getMessage()).build();
        }
        storage.setId(storageService.create(toCreateStorageCommand(storage)));
        return StorageUploadResult.builder().storage(storage).build();
    }

    private StorageUploadResult validateUploadFile(
            InputStream inputStream, String originalFilename, long size, List<String> allowedSuffixes) {
        if (inputStream == null || size <= 0L) {
            return StorageUploadResult.builder().error("文件不能为空").build();
        }
        String extendName = StringUtils.lowerCase(FilenameUtils.getExtension(originalFilename));
        if (allowedSuffixes != null && !allowedSuffixes.isEmpty() && !allowedSuffixes.contains(extendName)) {
            return StorageUploadResult.builder().error("无效的后缀名").build();
        }
        return StorageUploadResult.builder().build();
    }

    private void applyFileMetadata(String originalFilename, String contentType, StoredObject storage) {
        storage.setOriginalFilename(originalFilename);
        storage.setName(FilenameUtils.getBaseName(originalFilename));
        String extendName = StringUtils.lowerCase(FilenameUtils.getExtension(originalFilename));
        storage.setExtendName(extendName);
        storage.setContentType(contentType);
        storage.setMimeType(contentType);
    }

    private void applyStoredObject(StoredObject storage, StoredObject object) {
        storage.setBucketName(object.getBucketName());
        storage.setObjectKey(object.getObjectKey());
        storage.setSize(object.getSize());
        storage.setAccessEndpoint(object.getAccessEndpoint());
    }

    private CreateStorageCommand toCreateStorageCommand(StoredObject storage) {
        CreateStorageCommand command = new CreateStorageCommand();
        command.setId(storage.getId());
        command.setOriginalFilename(storage.getOriginalFilename());
        command.setContentType(storage.getContentType());
        command.setName(storage.getName());
        command.setExtendName(storage.getExtendName());
        command.setMimeType(storage.getMimeType());
        command.setOwnerId(storage.getOwnerId());
        command.setOwnerType(storage.getOwnerType());
        command.setBucketName(storage.getBucketName());
        command.setObjectKey(storage.getObjectKey());
        command.setSize(storage.getSize());
        command.setAccessEndpoint(storage.getAccessEndpoint());
        command.setObjectStatus(storage.getObjectStatus());
        command.setReferenceStatus(storage.getReferenceStatus());
        command.setRemarks(storage.getRemarks());
        return command;
    }
}
