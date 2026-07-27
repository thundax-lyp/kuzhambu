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
@TableName("ai_batch_job")
public class AiBatchJobDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String scope;
    private String capability;
    private String contentType;
    private String status;
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private Integer cancelledCount;
    private String failureSummaryJson;
    private Instant requestedAt;
    private Instant cancelledAt;
    private Instant completedAt;
}
