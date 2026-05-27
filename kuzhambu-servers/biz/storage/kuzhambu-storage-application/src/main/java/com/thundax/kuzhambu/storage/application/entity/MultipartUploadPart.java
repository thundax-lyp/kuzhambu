package com.thundax.kuzhambu.storage.application.entity;

import com.thundax.kuzhambu.storage.domain.model.valueobject.MultipartUploadPartId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MultipartUploadPart {

    private MultipartUploadPartId id;
    private String uploadId;
    private Integer partNumber;
    private String etag;
    private Long size;
}
