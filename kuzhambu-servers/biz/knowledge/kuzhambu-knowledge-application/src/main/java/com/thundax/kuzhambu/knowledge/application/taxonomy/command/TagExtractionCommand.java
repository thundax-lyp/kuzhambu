package com.thundax.kuzhambu.knowledge.application.taxonomy.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagExtractionCommand {
    private String sourceContentType;
    private Long sourceContentId;
    private String contentTitle;
    private String contentText;
    private Long modelId;
    private String modelName;
    private Long promptVersionId;
    private Integer maxTags;
    private Boolean allowNewTags;
    private Long requestedBy;
}
