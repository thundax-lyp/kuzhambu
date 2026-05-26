package com.thundax.kuzhambu.biz.storage.service.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompleteMultipartUploadCommand {
    private String uploadId;
    private String bucketName;
    private String objectKey;
    private Long size;
    private String accessEndpoint;
}
