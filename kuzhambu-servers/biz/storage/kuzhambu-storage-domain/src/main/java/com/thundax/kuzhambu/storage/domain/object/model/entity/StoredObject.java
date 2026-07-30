package com.thundax.kuzhambu.storage.domain.object.model.entity;

import com.thundax.kuzhambu.common.core.sort.Sortable;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageBucketNameCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageByteSizeCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageMimeTypeCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageObjectKeyCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageBucketName;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageByteSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageObjectKey;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoredObject implements Sortable {
    private static final ZoneId PATH_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter PATH_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMM").withZone(PATH_ZONE);
    private static final String DOT = ".";
    private static final String PATH_SEPARATOR = "/";

    private StoredObjectId id;
    private String originalFilename;
    private String contentType;
    private String name;
    private String extendName;
    private StorageMimeType mimeType;
    private StorageBucketName bucketName;
    private StorageObjectKey objectKey;
    private StorageByteSize size;
    private String accessEndpoint;
    private Instant storedAt;
    private StoredObjectStatus objectStatus = StoredObjectStatus.ACTIVE;
    private StoredObjectReferenceStatus referenceStatus = StoredObjectReferenceStatus.UNREFERENCED;
    private String referenceOwnerType;
    private int priority;
    private String remarks;

    public String getOriginalFilename() {
        return isBlank(originalFilename) ? getOriginalFileName() : originalFilename;
    }

    public String getContentType() {
        return isBlank(contentType) ? getMimeType() : contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
        setMimeType(contentType);
    }

    public String getMimeType() {
        return StorageMimeTypeCodec.toValue(mimeType);
    }

    public void setMimeType(String mimeType) {
        this.mimeType = StorageMimeTypeCodec.toDomain(mimeType);
    }

    public StorageMimeType getMimeTypeRef() {
        return mimeType;
    }

    public void setMimeTypeRef(StorageMimeType mimeType) {
        this.mimeType = mimeType;
    }

    public String getBucketName() {
        return StorageBucketNameCodec.toValue(bucketName);
    }

    public void setBucketName(String bucketName) {
        this.bucketName = StorageBucketNameCodec.toDomain(bucketName);
    }

    public StorageBucketName getBucketNameRef() {
        return bucketName;
    }

    public void setBucketNameRef(StorageBucketName bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectKey() {
        return StorageObjectKeyCodec.toValue(objectKey);
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = StorageObjectKeyCodec.toDomain(objectKey);
    }

    public StorageObjectKey getObjectKeyRef() {
        return objectKey;
    }

    public void setObjectKeyRef(StorageObjectKey objectKey) {
        this.objectKey = objectKey;
    }

    public Long getSize() {
        return StorageByteSizeCodec.toValue(size);
    }

    public void setSize(Long size) {
        this.size = StorageByteSizeCodec.toDomain(size);
    }

    public StorageByteSize getSizeRef() {
        return size;
    }

    public void setSizeRef(StorageByteSize size) {
        this.size = size;
    }

    public boolean isEnable() {
        return StoredObjectStatus.ACTIVE == getObjectStatus();
    }

    public String getFileName() {
        return StoredObjectIdCodec.toValue(getId()) + DOT + this.getExtendName();
    }

    public String getOriginalFileName() {
        if (isNotBlank(originalFilename)) {
            return originalFilename;
        }
        if (isBlank(this.getExtendName())) {
            return this.getName();
        }
        return this.getName() + DOT + this.getExtendName();
    }

    public String getPathName() {
        return getPathName(Instant.now());
    }

    String getPathName(Instant now) {
        return PATH_FORMATTER.format(now) + PATH_SEPARATOR + this.getFileName();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean isNotBlank(String value) {
        return !isBlank(value);
    }
}
