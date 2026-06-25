package com.thundax.kuzhambu.discovery.application.qa.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AskQuestionCommand {
    private Long sessionId;
    private String question;
    private Integer contextTurnCount;
    private String operatorType;
    private String operatorId;
    private String requestId;
    private String traceId;
}
