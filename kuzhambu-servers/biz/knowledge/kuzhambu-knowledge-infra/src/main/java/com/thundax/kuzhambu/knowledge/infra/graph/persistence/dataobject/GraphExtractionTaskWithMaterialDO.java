package com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GraphExtractionTaskWithMaterialDO extends GraphExtractionTaskDO {
    private String materialTitle;
}
