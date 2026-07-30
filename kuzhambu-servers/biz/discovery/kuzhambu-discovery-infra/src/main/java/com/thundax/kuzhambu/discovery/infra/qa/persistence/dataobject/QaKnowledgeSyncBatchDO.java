package com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject;

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
@TableName("discovery_qa_knowledge_sync_batch")
public class QaKnowledgeSyncBatchDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String triggerType;
    private String provider;
    private Integer totalCount;
    private Integer successCount;
    private Integer failureCount;
    private Instant startedAt;
    private Instant finishedAt;
}
