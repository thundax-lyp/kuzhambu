package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.helper.GraphKeyHelper;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphMaterialEdge {
    private GraphMaterialEdgeId id;
    private ContentRef materialRef;
    private GraphMaterialNodeId sourceNodeId;
    private GraphMaterialNodeId targetNodeId;
    private GraphEdgeKey edgeKey;
    private String relationType;
    private GraphSourceType source;
    private String qualifiersJson;

    public void requireMaterial(ContentRef expectedMaterialRef) {
        if (expectedMaterialRef == null || !expectedMaterialRef.equals(materialRef)) {
            throw new DomainException("Graph material edge does not belong to expected material");
        }
    }

    public void validateRequiredFields() {
        if (materialRef == null
                || sourceNodeId == null
                || targetNodeId == null
                || isBlank(relationType)
                || source == null) {
            throw new DomainException("Graph material edge required fields are incomplete");
        }
    }

    public boolean connects(GraphMaterialNodeId nodeId) {
        return nodeId != null && (nodeId.equals(sourceNodeId) || nodeId.equals(targetNodeId));
    }

    public void replaceEndpoint(GraphMaterialNodeId sourceId, GraphMaterialNodeId targetId) {
        if (sourceId == null || targetId == null) {
            throw new DomainException("Graph material edge endpoints are required");
        }
        sourceNodeId = sourceId;
        targetNodeId = targetId;
    }

    public void refreshEdgeKey(
            GraphMaterialNode sourceNode,
            GraphMaterialNode targetNode,
            boolean directed,
            Map<String, String> keyQualifiers) {
        validateRequiredFields();
        if (sourceNode == null || targetNode == null) {
            throw new DomainException("Graph material edge endpoint nodes are required");
        }
        sourceNode.requireMaterial(materialRef);
        targetNode.requireMaterial(materialRef);
        if (!sourceNodeId.equals(sourceNode.getId()) || !targetNodeId.equals(targetNode.getId())) {
            throw new DomainException("Graph material edge endpoint ids do not match endpoint nodes");
        }
        edgeKey = GraphKeyHelper.generateEdgeKey(
                sourceNode.getNodeKey(), targetNode.getNodeKey(), relationType, directed, keyQualifiers);
    }

    public boolean sameBusinessKey(GraphMaterialEdge other) {
        return other != null && edgeKey != null && edgeKey.equals(other.edgeKey);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
