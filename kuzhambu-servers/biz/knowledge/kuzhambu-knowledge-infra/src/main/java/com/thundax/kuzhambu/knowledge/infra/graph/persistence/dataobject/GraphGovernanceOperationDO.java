package com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject;

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
@TableName("knowledge_graph_governance_operation")
public class GraphGovernanceOperationDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String operationType;
    private String targetType;
    private Long targetId;
    private String beforeSnapshotJson;
    private String afterSnapshotJson;
    private String reason;
    private Long auditLogId;
    private Instant operatedAt;
}
