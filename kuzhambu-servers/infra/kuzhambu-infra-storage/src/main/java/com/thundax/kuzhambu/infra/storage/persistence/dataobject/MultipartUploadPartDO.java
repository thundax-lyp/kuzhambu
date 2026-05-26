package com.thundax.kuzhambu.infra.storage.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("assist_storage_multipart_upload_part")
public class MultipartUploadPartDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String uploadId;

    private Integer partNumber;

    private String etag;

    private Long size;
}
