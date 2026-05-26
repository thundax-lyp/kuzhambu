package com.thundax.kuzhambu.biz.storage.entity;

import com.thundax.kuzhambu.biz.storage.entity.valueobject.MultipartUploadPartId;
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
