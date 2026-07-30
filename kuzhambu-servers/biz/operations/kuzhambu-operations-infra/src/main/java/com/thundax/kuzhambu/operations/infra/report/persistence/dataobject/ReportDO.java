package com.thundax.kuzhambu.operations.infra.report.persistence.dataobject;

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
@TableName("operations_report")
public class ReportDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reportId;
    private String reportType;
    private String format;
    private Instant periodStart;
    private Instant periodEnd;
    private String requestId;
    private String traceId;
    private String templateVersion;
    private Long storageObjectId;
    private String artifactFilename;
    private String reportStatus;
    private String failureReason;
    private Long requesterUserId;
    private Instant requestedAt;
    private Instant completedAt;
}
