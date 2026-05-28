package com.thundax.kuzhambu.classics.infra.content.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("classics_content_export_job")
public class ClassicsContentExportJobDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String exportKind;
    private String contentType;
    private String exportFormat;
    private String scopeType;
    private String scopeJson;
    private Date requestedAt;
    private Date expiresAt;
    private String status;
    private Long storageObjectId;
    private Integer itemCount;
    private Integer assetCount;
    private String visibilityRiskStatus;
    private Boolean contentChanged;
}
