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
@TableName("assist_storage")
public class StoredObjectDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String name;

    private String extendName;

    private String mimeType;

    private String ownerId;

    private String ownerType;

    private String bucketName;

    private String objectKey;

    private Long size;

    private String accessEndpoint;

    private String objectStatus;

    private String referenceStatus;

    private Integer priority;
    private String remarks;
}
