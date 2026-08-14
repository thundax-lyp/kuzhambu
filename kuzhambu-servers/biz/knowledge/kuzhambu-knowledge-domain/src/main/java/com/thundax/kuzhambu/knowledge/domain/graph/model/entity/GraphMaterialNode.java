package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.helper.GraphKeyHelper;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphMaterialNode {
    private GraphMaterialNodeId id;
    private ContentRef materialRef;
    private GraphNodeKey nodeKey;
    private GraphNodeType nodeType;
    private String name;
    private GraphSourceType source;
    private String propertiesJson;

    public void requireMaterial(ContentRef expectedMaterialRef) {
        if (expectedMaterialRef == null || !expectedMaterialRef.equals(materialRef)) {
            throw new DomainException("Graph material node does not belong to expected material");
        }
    }

    public void validateRequiredFields() {
        if (materialRef == null || nodeType == null || isBlank(name) || source == null) {
            throw new DomainException("Graph material node required fields are incomplete");
        }
    }

    public void refreshNodeKey(String identityQualifier) {
        refreshNodeKeyFromFields(Map.of("identityQualifier", identityQualifier == null ? "" : identityQualifier));
    }

    public void refreshNodeKeyFromFields(Map<String, String> keyFields) {
        validateRequiredFields();
        nodeKey = GraphKeyHelper.generateNodeKey(nodeType, name, keyFields);
    }

    public boolean sameBusinessKey(GraphMaterialNode other) {
        return other != null && nodeKey != null && nodeKey.equals(other.nodeKey);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
