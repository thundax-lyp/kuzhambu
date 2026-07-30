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
@TableName("knowledge_quality_report_source_detail")
public class QualityReportSourceDetailDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private Long detailId;
    private Long reportId;
    private String sourceContentType;
    private Long sourceContentId;
    private String sourceCategoryCode;
    private String sourceCategoryName;
    private Long graphVersionId;
    private Instant appliedAt;
    private Long annotationCount;
    private Long issueCount;
    private String status;
    private String href;
    private Instant createdAt;
}
