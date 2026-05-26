package com.thundax.kuzhambu.biz.storage.entity;

import com.thundax.kuzhambu.biz.storage.entity.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.biz.storage.entity.enums.StorageOwnerType;
import com.thundax.kuzhambu.biz.storage.entity.valueobject.MultipartUploadSessionId;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MultipartUploadSession {

    private MultipartUploadSessionId id;
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
    private Integer uploadedPartCount = 0;
    private MultipartUploadStatus uploadStatus = MultipartUploadStatus.INITIATED;
    private Date completedDate;
    private Date abortedDate;
}
