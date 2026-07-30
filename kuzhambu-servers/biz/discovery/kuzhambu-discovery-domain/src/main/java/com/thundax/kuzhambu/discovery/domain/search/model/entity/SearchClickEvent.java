package com.thundax.kuzhambu.discovery.domain.search.model.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchClickEvent {
    private Long id;
    private Long searchEventId;
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
    private Date createdAt;

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
            Date createdAt) {
        this.id = id == null ? parseId(searchClickEventId) : id;
        this.searchEventId = parseId(searchEventId);
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
        return id == null ? null : String.valueOf(id);
    }

    public void setSearchClickEventId(String searchClickEventId) {
        this.id = parseId(searchClickEventId);
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
