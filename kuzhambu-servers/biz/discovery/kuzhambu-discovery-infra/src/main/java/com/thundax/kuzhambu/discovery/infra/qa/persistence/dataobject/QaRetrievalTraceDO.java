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
@TableName("discovery_qa_retrieval_trace")
public class QaRetrievalTraceDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long traceId;
    private Long messageId;
    private String rawQuestion;
    private String provider;
    private String externalKnowledgeBaseId;
    private String externalKnowledgeItemIds;
    private String externalChatId;
    private String providerRequestId;
    private Long latencyMs;
    private String failureReason;
    private String raw;
    private Date retrievedAt;
}
