package com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject;

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
@TableName("ai_candidate")
public class AiCandidateDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long candidateId;
    private Long callId;
    private Long batchId;
    private String capability;
    private String contentType;
    private Long contentId;
    private Long objectId;
    private String resultFormat;
    private String resultPayload;
    private String status;
    private Long promptVersionId;
    private String modelName;
    private String errorType;
    private String errorMessage;
    private Instant requestedAt;
    private Instant appliedAt;
}
