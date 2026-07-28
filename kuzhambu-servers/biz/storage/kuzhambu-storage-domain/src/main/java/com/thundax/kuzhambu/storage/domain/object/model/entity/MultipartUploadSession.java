package com.thundax.kuzhambu.storage.domain.object.model.entity;

import com.thundax.kuzhambu.storage.domain.object.codec.MultipartUploadIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadId;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadSessionId;
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
    private String mimeType;
    private String bucketName;
    private String objectKey;
    private String providerUploadId;
    private Long totalSize;
    private Long partSize;
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
