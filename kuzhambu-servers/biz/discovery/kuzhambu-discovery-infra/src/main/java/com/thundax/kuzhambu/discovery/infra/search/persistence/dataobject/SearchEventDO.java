package com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject;

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
@TableName("discovery_search_event")
public class SearchEventDO {

    @TableId(type = IdType.AUTO)
    private Long id;

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
    private Instant createdAt;

    public String getSearchEventId() {
        return id == null ? null : String.valueOf(id);
    }

    public void setSearchEventId(String searchEventId) {
        this.id = searchEventId == null ? null : Long.valueOf(searchEventId);
    }
}
