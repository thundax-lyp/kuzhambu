package com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject;

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
    private Date openedAt;
    private Long submittedBy;
    private Date submittedAt;
    private Long appliedBy;
    private Date appliedAt;
    private Long cancelledBy;
    private Date cancelledAt;
}
