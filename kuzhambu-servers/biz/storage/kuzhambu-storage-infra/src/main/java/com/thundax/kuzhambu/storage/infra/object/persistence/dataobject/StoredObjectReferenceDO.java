package com.thundax.kuzhambu.storage.infra.object.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("storage_object_reference")
public class StoredObjectReferenceDO {
    /**
     * Composite identity in storage_object_reference:
     * (objectId, referenceOwnerType, referenceOwnerId).
     *
     * 该 DO 不使用单字段 @TableId；
     * 单字段对象 ID 与数据库约束（复合主键）不一致，
     * 以该复合键作为引用幂等/多引用能力的稳定口径。
     */
    private Long objectId;

    private String referenceOwnerId;
    private String referenceOwnerType;
    private String businessParams;
}
