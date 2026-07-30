package com.thundax.kuzhambu.discovery.application.search.command;

import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchEventId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchClickEventCreateCommand {
    private SearchEventId searchEventId;
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
    private RequestId requestId;
    private TraceId traceId;
}
