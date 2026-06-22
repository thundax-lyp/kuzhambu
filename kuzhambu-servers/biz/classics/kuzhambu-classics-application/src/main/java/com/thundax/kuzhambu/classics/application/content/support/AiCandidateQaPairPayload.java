package com.thundax.kuzhambu.classics.application.content.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiCandidateQaPairPayload {

    private String question;
    private String answer;
}
