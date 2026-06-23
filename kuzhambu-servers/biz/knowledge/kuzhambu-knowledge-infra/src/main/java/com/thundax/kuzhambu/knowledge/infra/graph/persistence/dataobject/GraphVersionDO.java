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
@TableName("knowledge_graph_version")
public class GraphVersionDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long versionId;
    private Long taskId;
    private Long candidateId;
    private String taskType;
    private String scopeType;
    private String scopeJson;
    private String sourceContentType;
    private Long sourceContentId;
    private String sourceCategoryCode;
    private String sourceCategoryName;
    private Integer versionNo;
    private String status;
    private Date appliedAt;
}
