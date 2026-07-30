package com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject;

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
@TableName("classics_sancai_showcase")
public class SancaiShowcaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Instant requestedAt;
    private Instant completedAt;
    private String status;
    private String scopeJson;
    private String scopeTitle;
    private Long storageObjectId;
    private Integer entryCount;
    private Integer assetCount;
    private String visibilityRiskStatus;
    private String filename;
    private String contentType;
    private Long sizeBytes;
    private String sha256;
    private String failureType;
    private String failureMessage;
}
