package com.thundax.kuzhambu.classics.application.content.command;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiCandidateApplyContentCommand {

    private Long candidateId;
    private ClassicsContentType contentType;
    private Long contentId;
    private Long objectId;
    private String capability;
    private String resultFormat;
    private String resultPayload;
    private String changeSummary;
    private String tagApplyMode;
}
