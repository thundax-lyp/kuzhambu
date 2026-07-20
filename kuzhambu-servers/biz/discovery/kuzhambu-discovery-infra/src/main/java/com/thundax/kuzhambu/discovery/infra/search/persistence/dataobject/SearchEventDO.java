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
@TableName("discovery_search_event")
public class SearchEventDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String searchEventId;
    private String queryText;
    private String normalizedQueryText;
    private String displayQueryText;
    private String intentType;
    private String searchScopesJson;
    private Integer resultTotalCount;
    private Integer groupTotalCount;
    private Long searchLatencyMs;
    private String searchStatus;
    private String failureCode;
    private String failureMessage;
    private String operatorType;
    private String operatorId;
    private String requestId;
    private String traceId;
    private Date createdAt;
}
