package com.thundax.kuzhambu.discovery.application.search.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchClickEventCreateCommand {
    private String searchEventId;
    private String contentDomain;
    private String contentType;
    private String contentId;
    private String contentTitle;
    private String resultGroupKey;
    private int resultRank;
    private int groupRank;
    private String targetPath;
    private String operatorType;
    private String operatorId;
    private String requestId;
    private String traceId;
}
