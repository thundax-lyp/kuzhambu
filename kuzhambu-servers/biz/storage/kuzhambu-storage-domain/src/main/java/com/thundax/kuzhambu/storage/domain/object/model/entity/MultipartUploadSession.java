package com.thundax.kuzhambu.storage.domain.object.model.entity;

import com.thundax.kuzhambu.storage.domain.object.codec.MultipartPartSizeCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.MultipartUploadIdCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageBucketNameCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageByteSizeCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageMimeTypeCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageObjectKeyCodec;
import com.thundax.kuzhambu.storage.domain.object.model.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadId;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadSessionId;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageBucketName;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageByteSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageObjectKey;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultipartUploadSession {

    private MultipartUploadSessionId id;
    private MultipartUploadId uploadId;
    private StorageOwnerRef ownerRef;
    private String businessType;
    private String originalFilename;
    private StorageMimeType mimeType;
    private StorageBucketName bucketName;
    private StorageObjectKey objectKey;
    private String providerUploadId;
    private StorageByteSize totalSize;
    private MultipartPartSize partSize;
    private Integer uploadedPartCount = 0;
    private MultipartUploadStatus uploadStatus = MultipartUploadStatus.INITIATED;
    private Date completedDate;
    private Date abortedDate;

    public String getUploadId() {
        return uploadId == null ? null : uploadId.value();
    }

    public MultipartUploadId getUploadIdRef() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = MultipartUploadIdCodec.toDomain(uploadId);
    }

    public void setUploadIdRef(MultipartUploadId uploadId) {
        this.uploadId = uploadId;
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

    public Long getTotalSize() {
        return StorageByteSizeCodec.toValue(totalSize);
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = StorageByteSizeCodec.toDomain(totalSize);
    }

    public StorageByteSize getTotalSizeRef() {
        return totalSize;
    }

    public void setTotalSizeRef(StorageByteSize totalSize) {
        this.totalSize = totalSize;
    }

    public Long getPartSize() {
        return MultipartPartSizeCodec.toValue(partSize);
    }

    public void setPartSize(Long partSize) {
        this.partSize = MultipartPartSizeCodec.toDomain(partSize);
    }

    public MultipartPartSize getPartSizeRef() {
        return partSize;
    }

    public void setPartSizeRef(MultipartPartSize partSize) {
        this.partSize = partSize;
    }

    public StorageOwnerType getOwnerType() {
        return ownerRef == null ? null : ownerRef.ownerType();
    }

    public void setOwnerType(StorageOwnerType ownerType) {
        this.ownerRef =
                ownerRef == null ? StorageOwnerRef.ofNullable(ownerType, null) : ownerRef.withOwnerType(ownerType);
    }

    public String getOwnerId() {
        return ownerRef == null ? null : ownerRef.ownerId();
    }

    public void setOwnerId(String ownerId) {
        this.ownerRef = ownerRef == null ? StorageOwnerRef.ofNullable(null, ownerId) : ownerRef.withOwnerId(ownerId);
    }
}
