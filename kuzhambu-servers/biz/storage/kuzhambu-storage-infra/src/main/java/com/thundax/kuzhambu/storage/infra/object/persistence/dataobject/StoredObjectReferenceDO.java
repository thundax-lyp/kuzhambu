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
@TableName("storage_object_reference")
public class StoredObjectReferenceDO {
    @TableId(type = IdType.INPUT)
    private Long objectId;

    private String referenceOwnerId;
    private String referenceOwnerType;
    private String businessParams;
    private String referenceStatus;
}
