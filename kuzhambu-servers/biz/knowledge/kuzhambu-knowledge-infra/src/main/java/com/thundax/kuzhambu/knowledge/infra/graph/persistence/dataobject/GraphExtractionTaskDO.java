package com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject;

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
@TableName("knowledge_graph_extraction_task")
public class GraphExtractionTaskDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long taskId;
    private Long batchJobId;
    private String taskType;
    private String scopeType;
    private String scopeJson;
    private String triggerSource;
    private String selectionScopeJson;
    private Boolean replaceUnconfirmedOnly;
    private Long parentTaskId;
    private String sourceContentType;
    private Long sourceContentId;
    private Long aiCallId;
    private Long aiCandidateId;
    private String status;
    private String errorType;
    private String errorMessage;
    private Long requestedBy;
    private Date requestedAt;
    private Date completedAt;
    private Date appliedAt;
}
