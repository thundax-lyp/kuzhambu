package com.thundax.kuzhambu.ai.infra.refinement.persistence.dataobject;

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
@TableName("ai_refinement_task")
public class AiRefinementTaskDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private String scope;
    private String capability;
    private String contentType;
    private Long contentId;
    private Long objectId;
    private Long requestedBy;
    private String requestId;
    private String traceId;
    private String status;
    private String serviceRole;
    private Long modelId;
    private String modelName;
    private Long promptVersionId;
    private Long callId;
    private Long candidateId;
    private String failureStage;
    private String errorType;
    private String errorMessage;
    private String resultFormat;
    private String resultPreview;
    private Boolean streamEnabled;
    private Instant requestedAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;
}
