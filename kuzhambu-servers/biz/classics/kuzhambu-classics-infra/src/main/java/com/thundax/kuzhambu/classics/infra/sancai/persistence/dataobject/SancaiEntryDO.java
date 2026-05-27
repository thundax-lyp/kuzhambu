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
@TableName("classics_sancai_entry")
public class SancaiEntryDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long volumeId;
    private String title;
    private String originalText;
    private String translationText;
    private String summary;
    private String lifecycleStatus;
    private String visibility;
    private String translationStatus;
    private String imageStatus;
    private String visualAssetStatus;
    private String refinementStatus;
    private Integer priority;
}
