package com.thundax.kuzhambu.knowledge.infra.graph.persistence.projection;

import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphExtractionTaskDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GraphExtractionTaskWithMaterialProjection extends GraphExtractionTaskDO {
    private String materialTitle;
}
