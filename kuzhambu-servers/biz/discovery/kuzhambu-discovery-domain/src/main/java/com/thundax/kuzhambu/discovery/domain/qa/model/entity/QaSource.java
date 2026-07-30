package com.thundax.kuzhambu.discovery.domain.qa.model.entity;

import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaSource {
    private Long id;
    private String sourceBusinessId;
    private Long messageId;
    private String contentType;
    private Long contentId;
    private String knowledgeBase;
    private String titleSnapshot;
    private String locationLabel;
    private String snippet;
    private String sourcePath;
    private Integer sourceRank;
    private BigDecimal score;
    private String sourceStatus;
    private Date referencedAt;

    public QaSource(
            Long id,
            Long sourceId,
            String sourceBusinessId,
            Long messageId,
            String contentType,
            Long contentId,
            String knowledgeBase,
            String titleSnapshot,
            String locationLabel,
            String snippet,
            String sourcePath,
            Integer sourceRank,
            BigDecimal score,
            String sourceStatus,
            Date referencedAt) {
        this.id = id == null ? sourceId : id;
        this.sourceBusinessId = sourceBusinessId;
        this.messageId = messageId;
        this.contentType = contentType;
        this.contentId = contentId;
        this.knowledgeBase = knowledgeBase;
        this.titleSnapshot = titleSnapshot;
        this.locationLabel = locationLabel;
        this.snippet = snippet;
        this.sourcePath = sourcePath;
        this.sourceRank = sourceRank;
        this.score = score;
        this.sourceStatus = sourceStatus;
        this.referencedAt = referencedAt;
    }

    public Long getSourceId() {
        return id;
    }

    public void setSourceId(Long sourceId) {
        this.id = sourceId;
    }
}
