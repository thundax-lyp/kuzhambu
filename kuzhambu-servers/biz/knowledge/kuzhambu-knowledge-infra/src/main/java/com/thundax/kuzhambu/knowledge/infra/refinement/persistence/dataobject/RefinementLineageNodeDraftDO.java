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
@TableName("knowledge_refinement_lineage_node_draft")
public class RefinementLineageNodeDraftDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private Long draftId;
    private Long refinementTaskId;
    private Long nodeId;
    private String nodeKey;
    private String originType;
    private String operationType;
    private String name;
    private String nodeType;
    private Integer generation;
    private String gender;
    private String confirmationStatus;
    private String sourceRefsJson;
    private Integer sortOrder;
    private Long createdBy;
    private Instant createdAt;
    private Long updatedBy;
    private Instant updatedAt;
}
