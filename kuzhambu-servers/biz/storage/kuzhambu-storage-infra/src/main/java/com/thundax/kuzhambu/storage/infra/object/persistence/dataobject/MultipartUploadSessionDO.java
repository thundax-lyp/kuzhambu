package com.thundax.kuzhambu.storage.infra.object.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("storage_multipart_upload")
public class MultipartUploadSessionDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private String uploadId;
    private String ownerId;
    private String ownerType;
    private String businessType;
    private String originalFilename;
    private String mimeType;
    private String bucketName;
    private String objectKey;
    private String providerUploadId;
    private Long totalSize;
    private Long partSize;
    private Integer uploadedPartCount;
    private String uploadStatus;
    private Date completedDate;
    private Date abortedDate;
}
