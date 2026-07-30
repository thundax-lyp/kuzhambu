package com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject;

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
@TableName("knowledge_quality_report_issue")
public class QualityReportIssueDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private Long issueId;
    private Long reportId;
    private String issueType;
    private String severity;
    private String objectType;
    private String objectKey;
    private String title;
    private String description;
    private String suggestion;
    private String href;
    private Integer priority;
    private Instant createdAt;
}
