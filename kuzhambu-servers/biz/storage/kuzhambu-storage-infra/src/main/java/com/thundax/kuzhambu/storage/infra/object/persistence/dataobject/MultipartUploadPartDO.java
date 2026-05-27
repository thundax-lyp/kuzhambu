package com.thundax.kuzhambu.storage.infra.object.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("storage_multipart_upload_part")
public class MultipartUploadPartDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private String uploadId;
    private Integer partNumber;
    private String etag;
    private Long size;
}
