package com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("classics_sancai_showcase")
public class SancaiShowcaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDateTime requestedAt;
    private String status;
    private String scopeJson;
    private Long storageObjectId;
    private Integer entryCount;
    private String visibilityRiskStatus;
}
