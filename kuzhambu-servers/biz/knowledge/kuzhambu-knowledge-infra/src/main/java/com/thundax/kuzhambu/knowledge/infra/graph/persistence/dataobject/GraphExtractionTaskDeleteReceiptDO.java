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
@TableName("knowledge_graph_extraction_task_delete_receipt")
public class GraphExtractionTaskDeleteReceiptDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long operatorId;
    private String idempotencyKey;
    private Long deletedTaskId;
    private Instant completedAt;
}
