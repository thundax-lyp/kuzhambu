package com.thundax.kuzhambu.classics.application.content.result;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiCandidateApplyContentResult {

    private ClassicsContentType contentType;
    private Long contentId;
    private Long versionId;
    private Integer versionNo;
}
