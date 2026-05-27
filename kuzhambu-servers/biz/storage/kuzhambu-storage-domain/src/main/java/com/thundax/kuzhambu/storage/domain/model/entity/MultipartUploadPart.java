package com.thundax.kuzhambu.storage.domain.model.entity;

import com.thundax.kuzhambu.storage.domain.model.valueobject.MultipartUploadPartId;
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
    private String uploadId;
    private Integer partNumber;
    private String etag;
    private Long size;
}
