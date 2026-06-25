package com.thundax.kuzhambu.discovery.application.qa.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenQaSessionCommand {
    private Long ownerUserId;
    private String title;
    private String scope;
    private String contextMode;
    private String contextContentType;
    private Long contextContentId;
    private String requestId;
    private String traceId;
}
