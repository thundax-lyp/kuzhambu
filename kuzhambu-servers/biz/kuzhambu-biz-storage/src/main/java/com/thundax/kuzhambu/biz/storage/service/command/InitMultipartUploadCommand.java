package com.thundax.kuzhambu.biz.storage.service.command;

import com.thundax.kuzhambu.biz.storage.entity.enums.StorageOwnerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InitMultipartUploadCommand {
    private String uploadId;
    private String ownerId;
    private StorageOwnerType ownerType;
    private String businessType;
    private String originalFilename;
    private String mimeType;
    private String bucketName;
    private String objectKey;
    private String providerUploadId;
    private Long totalSize;
    private Long partSize;
}
