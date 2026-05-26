package com.thundax.kuzhambu.infra.storage.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.thundax.kuzhambu.biz.storage.entity.StoredObject;
import com.thundax.kuzhambu.biz.storage.entity.enums.StorageOwnerType;
import com.thundax.kuzhambu.biz.storage.entity.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.biz.storage.entity.enums.StoredObjectStatus;
import com.thundax.kuzhambu.biz.storage.entity.valueobject.StoredObjectIdCodec;
import com.thundax.kuzhambu.common.cache.CacheDTO;
import com.thundax.kuzhambu.common.cache.KuzhambuCacheNames;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class StorageCacheSupport {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final String CACHE_SECTION = KuzhambuCacheNames.PREFIX + "assist.storage.";

    @CreateCache(
            name = CACHE_SECTION,
            cacheType = CacheType.BOTH,
            expire = OBJECT_EXPIRE_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    private Cache<String, Object> cache;

    public StoredObject getById(String id) {
        return toDomain((StoredObjectCacheDTO) cache.get(String.valueOf(id)));
    }

    public void putById(StoredObject storage) {
        if (storage != null && StringUtils.isNotBlank(StoredObjectIdCodec.toStringValue(storage.getId()))) {
            cache.put(
                    StoredObjectIdCodec.toStringValue(storage.getId()),
                    toCacheDTO(storage),
                    OBJECT_EXPIRE_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    public void removeById(String id) {
        cache.remove(id);
    }

    private static StoredObject toDomain(StoredObjectCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectIdCodec.toDomain(cacheDTO.id));
        storage.setOriginalFilename(cacheDTO.originalFilename);
        storage.setContentType(cacheDTO.contentType);
        storage.setName(cacheDTO.name);
        storage.setExtendName(cacheDTO.extendName);
        storage.setMimeType(cacheDTO.mimeType);
        storage.setOwnerId(cacheDTO.ownerId);
        storage.setOwnerType(cacheDTO.ownerType == null ? null : StorageOwnerType.from(cacheDTO.ownerType));
        storage.setBucketName(cacheDTO.bucketName);
        storage.setObjectKey(cacheDTO.objectKey);
        storage.setSize(cacheDTO.size);
        storage.setAccessEndpoint(cacheDTO.accessEndpoint);
        storage.setObjectStatus(cacheDTO.objectStatus == null ? null : StoredObjectStatus.from(cacheDTO.objectStatus));
        storage.setReferenceStatus(
                cacheDTO.referenceStatus == null ? null : StoredObjectReferenceStatus.from(cacheDTO.referenceStatus));
        storage.setPriority(cacheDTO.priority == null ? 0 : cacheDTO.priority);
        storage.setRemarks(cacheDTO.remarks);
        return storage;
    }

    private static StoredObjectCacheDTO toCacheDTO(StoredObject storage) {
        StoredObjectCacheDTO cacheDTO = new StoredObjectCacheDTO();
        cacheDTO.id = StoredObjectIdCodec.toValue(storage.getId());
        cacheDTO.originalFilename = storage.getOriginalFilename();
        cacheDTO.contentType = storage.getContentType();
        cacheDTO.name = storage.getName();
        cacheDTO.extendName = storage.getExtendName();
        cacheDTO.mimeType = storage.getMimeType();
        cacheDTO.ownerId = storage.getOwnerId();
        cacheDTO.ownerType =
                storage.getOwnerType() == null ? null : storage.getOwnerType().value();
        cacheDTO.bucketName = storage.getBucketName();
        cacheDTO.objectKey = storage.getObjectKey();
        cacheDTO.size = storage.getSize();
        cacheDTO.accessEndpoint = storage.getAccessEndpoint();
        cacheDTO.objectStatus = storage.getObjectStatus() == null
                ? null
                : storage.getObjectStatus().value();
        cacheDTO.referenceStatus = storage.getReferenceStatus() == null
                ? null
                : storage.getReferenceStatus().value();
        cacheDTO.priority = storage.getPriority();
        cacheDTO.remarks = storage.getRemarks();
        return cacheDTO;
    }

    private static class StoredObjectCacheDTO implements CacheDTO {
        private Long id;
        private String originalFilename;
        private String contentType;
        private String name;
        private String extendName;
        private String mimeType;
        private String ownerId;
        private String ownerType;
        private String bucketName;
        private String objectKey;
        private Long size;
        private String accessEndpoint;
        private String objectStatus;
        private String referenceStatus;
        private Integer priority;
        private String remarks;
    }
}
