package com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("classics_sancai_visual_asset")
public class SancaiVisualAssetDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long entryId;
    private Integer versionNo;
    private String status;
    private Long sourceImageStorageObjectId;
    private Long generatedImageStorageObjectId;
    private Boolean currentUsed;
    private Integer textWeight;
    private Integer imageWeight;
    private String imageAnalysisMarkdown;
    private String fusionDescription;
    private String visualDescription;
    private String generationParamsJson;
}
