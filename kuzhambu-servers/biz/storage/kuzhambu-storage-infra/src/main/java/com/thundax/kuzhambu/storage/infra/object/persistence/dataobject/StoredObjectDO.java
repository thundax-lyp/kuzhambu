package com.thundax.kuzhambu.storage.infra.object.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("storage_object")
public class StoredObjectDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private String name;
    private String extendName;
    private String mimeType;
    private String bucketName;
    private String objectKey;
    private Long size;
    private String accessEndpoint;
    private Instant storedAt;
    private String objectStatus;
    private String referenceStatus;
    private Integer priority;
    private String remarks;
}
