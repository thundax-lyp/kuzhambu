package com.thundax.kuzhambu.knowledge.application.graph.operator;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import org.springframework.stereotype.Component;

@Component
public class GraphMaterialGraphLoader {

    private final GraphMaterialRepository materialRepository;
    private final GraphMaterialNodeRepository nodeRepository;
    private final GraphMaterialEdgeRepository edgeRepository;

    public GraphMaterialGraphLoader(
            GraphMaterialRepository materialRepository,
            GraphMaterialNodeRepository nodeRepository,
            GraphMaterialEdgeRepository edgeRepository) {
        this.materialRepository = materialRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
    }

    public GraphMaterialGraph require(ContentRef materialRef) {
        GraphMaterial material = materialRepository.getByContentRef(materialRef);
        if (material == null) {
            throw new BizException("Graph material does not exist");
        }
        return load(material);
    }

    public GraphMaterialGraph getOrCreate(ContentRef materialRef, String contentTitleSnapshot) {
        GraphMaterial material = materialRepository.getByContentRef(materialRef);
        if (material == null) {
            material = new GraphMaterial(materialRef, contentTitleSnapshot, GraphMaterialStatus.DRAFT, null, 0L);
            materialRepository.insert(material);
        }
        return load(material);
    }

    private GraphMaterialGraph load(GraphMaterial material) {
        ContentRef materialRef = material.getContentRef();
        return GraphMaterialGraph.of(
                material, nodeRepository.listByMaterial(materialRef), edgeRepository.listByMaterial(materialRef));
    }
}
