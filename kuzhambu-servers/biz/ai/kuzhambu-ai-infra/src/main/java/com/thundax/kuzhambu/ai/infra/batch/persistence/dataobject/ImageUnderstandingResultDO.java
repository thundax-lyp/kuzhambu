package com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject;

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
@TableName("ai_image_understanding")
public class ImageUnderstandingResultDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long understandingId;
    private Long storageObjectId;
    private String contentHash;
    private String analysisMarkdown;
    private Long callId;
    private Long promptVersionId;
    private String modelName;
    private Instant requestedAt;
}
