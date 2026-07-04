package com.thundax.kuzhambu.discovery.application.qa.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SyncKnowledgeContentCommand {

    private String contentType;
    private Long contentId;
    private Integer currentVersionNo;
    private String requestId;
    private String traceId;
}
