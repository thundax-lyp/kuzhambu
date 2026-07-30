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
@TableName("knowledge_refinement_task")
public class RefinementTaskDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private Long refinementTaskId;
    private String taskType;
    private String sourceContentType;
    private Long sourceContentId;
    private String sourceCategoryCode;
    private String sourceCategoryName;
    private Long graphVersionId;
    private String status;
    private Long openedBy;
    private Instant openedAt;
    private Long submittedBy;
    private Instant submittedAt;
    private Long appliedBy;
    private Instant appliedAt;
    private Long cancelledBy;
    private Instant cancelledAt;
}
