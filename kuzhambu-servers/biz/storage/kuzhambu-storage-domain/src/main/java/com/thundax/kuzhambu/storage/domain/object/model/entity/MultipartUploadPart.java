package com.thundax.kuzhambu.storage.domain.object.model.entity;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadId;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadPartId;
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
    private String partPath;
    private Integer partNumber;
    private String etag;
    private Long size;

    public String getUploadId() {
        return uploadId == null ? null : uploadId.value();
    }

    public MultipartUploadId getUploadIdRef() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = MultipartUploadId.ofNullable(uploadId);
    }
}
