package com.thundax.kuzhambu.knowledge.application.graph.support;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.application.graph.assembler.GraphApplicationAssembler;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GraphExtractionApplyExecutor {

    private final GraphMaterialGraphLoader graphLoader;
    private final GraphDocumentPlanner documentPlanner;
    private final GraphMaterialGraphSaver graphSaver;

    public GraphExtractionApplyExecutor(
            GraphMaterialGraphLoader graphLoader,
            GraphDocumentPlanner documentPlanner,
            GraphMaterialGraphSaver graphSaver) {
        this.graphLoader = graphLoader;
        this.documentPlanner = documentPlanner;
        this.graphSaver = graphSaver;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public GraphMaterialResult apply(ContentRef materialRef, GraphDocument document, long expectedLockVersion) {
        GraphMaterialGraph graph = graphLoader.require(materialRef);
        graph.material().requireLockVersion(expectedLockVersion);
        graph.material().requireEditable();
        GraphDocumentPlan plan = documentPlanner.plan(graph, document, GraphSourceType.AI, "MERGE");
        return GraphApplicationAssembler.toMaterialResult(graphSaver.applyDocument(graph, plan, expectedLockVersion));
    }
}
