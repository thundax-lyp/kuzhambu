package com.thundax.kuzhambu.discovery.application.search.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchPreviewQuery {
    private String contentType;
    private String contentId;
    private String operatorType;
    private String operatorId;
    private String requestId;
    private String traceId;
}
