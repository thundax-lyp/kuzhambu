package com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_entry_split_candidate")
public class EntrySplitCandidateDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long splitCandidateId;
    private Long candidateId;
    private String parentContentType;
    private Long parentContentId;
    private String title;
    private String originalText;
    private String translationText;
    private Long targetVolumeId;
    private Integer priority;
}
