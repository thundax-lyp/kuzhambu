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
    private Long sourceId;
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
}
