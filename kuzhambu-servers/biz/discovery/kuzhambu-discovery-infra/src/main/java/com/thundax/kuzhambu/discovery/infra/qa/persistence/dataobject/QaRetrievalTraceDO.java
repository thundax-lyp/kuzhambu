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
    private Long callId;
    private String rawQuestion;
    private String rewrittenQuestion;
    private String scope;
    private String filtersJson;
    private String expandedTermsJson;
    private String linkedEntitiesJson;
    private Integer candidateCount;
    private String contextSnapshot;
    private Date retrievedAt;
}
