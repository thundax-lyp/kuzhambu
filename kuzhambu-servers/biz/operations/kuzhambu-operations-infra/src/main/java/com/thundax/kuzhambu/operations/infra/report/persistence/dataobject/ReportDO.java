package com.thundax.kuzhambu.operations.infra.report.persistence.dataobject;

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
@TableName("operations_report")
public class ReportDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reportId;
    private String reportType;
    private String format;
    private Date periodStart;
    private Date periodEnd;
    private Long storageObjectId;
    private String reportStatus;
    private String failureReason;
    private Long requesterUserId;
    private Date requestedAt;
    private Date completedAt;
}
