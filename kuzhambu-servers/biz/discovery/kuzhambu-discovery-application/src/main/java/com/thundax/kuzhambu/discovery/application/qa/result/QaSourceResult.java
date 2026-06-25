package com.thundax.kuzhambu.discovery.application.qa.result;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaSourceResult {
    private Long sourceId;
    private String contentType;
    private Long contentId;
    private String knowledgeBase;
    private String titleSnapshot;
    private String locationLabel;
    private String snippet;
    private Integer sourceRank;
    private BigDecimal score;
    private String sourceStatus;
}
