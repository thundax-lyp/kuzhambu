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
    private String searchClickEventId;
    private String searchEventId;
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
}
