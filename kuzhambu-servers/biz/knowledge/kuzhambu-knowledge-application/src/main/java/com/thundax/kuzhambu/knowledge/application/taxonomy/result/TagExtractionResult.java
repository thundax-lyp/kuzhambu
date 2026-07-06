package com.thundax.kuzhambu.knowledge.application.taxonomy.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagExtractionResult {
    private Long aiCallId;
    private Long aiCandidateId;
    private String status;
    private String resultFormat;
    private String resultPayload;
    private String errorType;
    private String errorMessage;
}
