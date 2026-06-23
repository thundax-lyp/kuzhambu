package com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeAiExtractionResult {
    private Long callId;
    private Long candidateId;
    private String status;
    private String capability;
    private String resultFormat;
    private String resultPayload;
    private String errorType;
    private String errorMessage;
}
