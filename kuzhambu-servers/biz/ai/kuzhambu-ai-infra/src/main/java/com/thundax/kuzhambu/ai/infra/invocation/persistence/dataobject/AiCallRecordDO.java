package com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_call_record")
public class AiCallRecordDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long callId;
    private Long batchId;
    private String scope;
    private String capability;
    private String contentType;
    private Long contentId;
    private Long objectId;
    private Long serviceId;
    private String serviceRole;
    private Long modelId;
    private String modelName;
    private Long promptVersionId;
    private String requestId;
    private String traceId;
    private String status;
    private Boolean streamUsed;
    private Boolean streamCompleted;
    private Boolean fallbackUsed;
    private Integer latencyMs;
    private Integer inputTokens;
    private Integer outputTokens;
    private BigDecimal costAmount;
    private String errorType;
    private String errorMessage;
    private String warningsJson;
    private Instant requestedAt;
    private Instant completedAt;
}
