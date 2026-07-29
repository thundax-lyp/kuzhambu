package com.thundax.kuzhambu.storage.domain.object.model.entity;

import com.thundax.kuzhambu.storage.domain.object.codec.MultipartPartNumberCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.MultipartUploadIdCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageByteSizeCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageObjectKeyCodec;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartNumber;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadId;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadPartId;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageByteSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageObjectKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultipartUploadPart {

    private MultipartUploadPartId id;
    private MultipartUploadId uploadId;
    private StorageObjectKey partPath;
    private MultipartPartNumber partNumber;
    private String etag;
    private StorageByteSize size;

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

    public String getPartPath() {
        return StorageObjectKeyCodec.toValue(partPath);
    }

    public void setPartPath(String partPath) {
        this.partPath = StorageObjectKeyCodec.toDomain(partPath);
    }

    public StorageObjectKey getPartPathRef() {
        return partPath;
    }

    public void setPartPathRef(StorageObjectKey partPath) {
        this.partPath = partPath;
    }

    public Integer getPartNumber() {
        return MultipartPartNumberCodec.toValue(partNumber);
    }

    public void setPartNumber(Integer partNumber) {
        this.partNumber = MultipartPartNumberCodec.toDomain(partNumber);
    }

    public MultipartPartNumber getPartNumberRef() {
        return partNumber;
    }

    public void setPartNumberRef(MultipartPartNumber partNumber) {
        this.partNumber = partNumber;
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
}
