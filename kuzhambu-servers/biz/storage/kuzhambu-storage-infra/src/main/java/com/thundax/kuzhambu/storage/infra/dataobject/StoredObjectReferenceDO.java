package com.thundax.kuzhambu.storage.infra.dataobject;

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
@TableName("storage_object_reference")
public class StoredObjectReferenceDO {

    @TableId(type = IdType.INPUT)
    private Long fileId;

    private String referenceOwnerId;

    private String referenceOwnerType;

    private String businessParams;

    private String referenceStatus;
}
