package com.thundax.kuzhambu.knowledge.application.refinement.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualityAnnotationPageQuery {
    private Long refinementTaskId;
    private String objectType;
    private int pageNo;
    private int pageSize;
}
