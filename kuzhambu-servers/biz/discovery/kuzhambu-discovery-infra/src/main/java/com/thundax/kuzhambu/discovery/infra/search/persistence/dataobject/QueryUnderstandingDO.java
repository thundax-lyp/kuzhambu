package com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject;

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
@TableName("discovery_query_understanding")
public class QueryUnderstandingDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String queryUnderstandingId;
    private String searchLogId;
    private String queryText;
    private String normalizedQueryText;
    private String rewrittenQueryText;
    private String intentType;
    private String recognizedEntitiesJson;
    private String expandedSynonymsJson;
    private String understandingStatus;
    private String failureCode;
    private String failureMessage;
    private String requestId;
    private String traceId;
    private Date createdAt;
}
