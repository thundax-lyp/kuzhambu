package com.thundax.kuzhambu.discovery.domain.search.model.entity;

import com.thundax.kuzhambu.discovery.domain.search.codec.SearchClickEventIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.codec.SearchEventIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchClickEventId;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchEventId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchClickEvent {
    private SearchClickEventId id;
    private SearchEventId searchEventId;
    private String contentDomain;
    private String contentType;
    private String contentId;
    private String contentTitle;
    private String resultGroupKey;
    private Integer resultRank;
    private Integer groupRank;
    private String targetPath;
    private String operatorType;
    private String operatorId;
    private String requestId;
    private String traceId;
    private Instant createdAt;

    public SearchClickEvent(
            Long id,
            String searchClickEventId,
            String searchEventId,
            String contentDomain,
            String contentType,
            String contentId,
            String contentTitle,
            String resultGroupKey,
            Integer resultRank,
            Integer groupRank,
            String targetPath,
            String operatorType,
            String operatorId,
            String requestId,
            String traceId,
            Instant createdAt) {
        this.id = id == null
                ? SearchClickEventIdCodec.toDomain(searchClickEventId)
                : SearchClickEventIdCodec.toDomain(id);
        this.searchEventId = SearchEventIdCodec.toDomain(searchEventId);
        this.contentDomain = contentDomain;
        this.contentType = contentType;
        this.contentId = contentId;
        this.contentTitle = contentTitle;
        this.resultGroupKey = resultGroupKey;
        this.resultRank = resultRank;
        this.groupRank = groupRank;
        this.targetPath = targetPath;
        this.operatorType = operatorType;
        this.operatorId = operatorId;
        this.requestId = requestId;
        this.traceId = traceId;
        this.createdAt = createdAt;
    }

    public String getSearchClickEventId() {
        return SearchClickEventIdCodec.toStringValue(id);
    }

    public void setSearchClickEventId(String searchClickEventId) {
        this.id = SearchClickEventIdCodec.toDomain(searchClickEventId);
    }

    public void setId(SearchClickEventId id) {
        this.id = id;
    }

    public void setId(Long id) {
        this.id = SearchClickEventIdCodec.toDomain(id);
    }

    public void setSearchEventId(String searchEventId) {
        this.searchEventId = SearchEventIdCodec.toDomain(searchEventId);
    }

    public void setSearchEventId(Long searchEventId) {
        this.searchEventId = SearchEventIdCodec.toDomain(searchEventId);
    }

    public void setSearchEventId(SearchEventId searchEventId) {
        this.searchEventId = searchEventId;
    }
}
