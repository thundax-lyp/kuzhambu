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

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long searchEventId;
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

    public String getQueryUnderstandingId() {
        return id == null ? null : String.valueOf(id);
    }

    public void setQueryUnderstandingId(String queryUnderstandingId) {
        this.id = parseId(queryUnderstandingId);
    }

    public void setSearchEventId(String searchEventId) {
        this.searchEventId = parseId(searchEventId);
    }

    public void setSearchEventId(Long searchEventId) {
        this.searchEventId = searchEventId;
    }

    private Long parseId(String value) {
        return value == null ? null : Long.valueOf(value);
    }
}
