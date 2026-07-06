package com.thundax.kuzhambu.knowledge.domain.refinement.model.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualityReportIssue {
    private Long id;
    private Long issueId;
    private Long reportId;
    private String issueType;
    private String severity;
    private String objectType;
    private String objectKey;
    private String title;
    private String description;
    private String suggestion;
    private String href;
    private Integer priority;
    private Date createdAt;
}
