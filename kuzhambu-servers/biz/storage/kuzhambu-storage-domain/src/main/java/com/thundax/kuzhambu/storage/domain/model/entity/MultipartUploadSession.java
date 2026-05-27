package com.thundax.kuzhambu.storage.domain.model.entity;

import com.thundax.kuzhambu.storage.domain.model.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.storage.domain.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.model.valueobject.MultipartUploadSessionId;
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
