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
@TableName("assist_storage_business")
public class StoredObjectReferenceDO {

    @TableId(type = IdType.INPUT)
    private Long fileId;

    private String referenceOwnerId;

    private String referenceOwnerType;

    private String businessParams;

    private String referenceStatus;
}
