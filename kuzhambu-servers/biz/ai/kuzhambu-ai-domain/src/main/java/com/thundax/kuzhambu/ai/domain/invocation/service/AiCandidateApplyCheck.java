package com.thundax.kuzhambu.ai.domain.invocation.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AiCandidateApplyCheck {

    private Long candidateId;
    private String contentType;
    private Long contentId;
    private String capability;
}
