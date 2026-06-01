package com.thundax.kuzhambu.ai.domain.split.model.entity;

import com.thundax.kuzhambu.common.core.sort.Sortable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntrySplitCandidate implements Sortable {

    private Long id;
    private Long splitCandidateId;
    private Long candidateId;
    private String parentContentType;
    private Long parentContentId;
    private String title;
    private String originalText;
    private String translationText;
    private Long targetVolumeId;
    private int priority;

    public boolean belongsTo(String contentType, Long contentId) {
        return parentContentType != null
                && parentContentType.equals(contentType)
                && parentContentId != null
                && parentContentId.equals(contentId);
    }
}
