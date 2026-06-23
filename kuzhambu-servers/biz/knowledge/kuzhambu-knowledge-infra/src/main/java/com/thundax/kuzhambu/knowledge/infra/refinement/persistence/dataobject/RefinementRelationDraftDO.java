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
@TableName("knowledge_refinement_relation_draft")
public class RefinementRelationDraftDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private Long draftId;
    private Long refinementTaskId;
    private Long relationId;
    private String relationKey;
    private String originType;
    private String operationType;
    private String sourceEntityKey;
    private String targetEntityKey;
    private String sourceName;
    private String targetName;
    private String relationType;
    private String evidence;
    private String confirmationStatus;
    private String sourceRefsJson;
    private Integer sortOrder;
    private Long createdBy;
    private Date createdAt;
    private Long updatedBy;
    private Date updatedAt;
}
