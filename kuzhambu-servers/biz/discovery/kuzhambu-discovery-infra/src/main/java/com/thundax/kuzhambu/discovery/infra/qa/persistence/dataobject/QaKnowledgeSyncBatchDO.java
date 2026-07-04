package com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject;

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
@TableName("discovery_qa_knowledge_sync_batch")
public class QaKnowledgeSyncBatchDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long batchId;
    private String triggerType;
    private String provider;
    private Integer totalCount;
    private Integer successCount;
    private Integer failureCount;
    private Date startedAt;
    private Date finishedAt;
}
