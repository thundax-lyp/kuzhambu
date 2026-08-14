package com.thundax.kuzhambu.knowledge.application.graph.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEdgeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEdgeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialImportCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeMergeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeSplitCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialVersionRestoreCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialImportQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialListQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialNodeMergeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialNodeSplitQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialChangeImpactResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialImportPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import java.util.List;

public interface GraphMaterialApplicationService {

    PageResult<GraphMaterial> pageMaterials(GraphMaterialListQuery query, PageQuery pageQuery);

    GraphMaterialResult getMaterialGraph(GraphMaterialQuery query);

    GraphMaterialNodeId createNode(GraphMaterialNodeCommand command);

    void updateNode(GraphMaterialNodeCommand command);

    void deleteNode(GraphMaterialNodeDeleteCommand command);

    GraphMaterialEdgeId createEdge(GraphMaterialEdgeCommand command);

    void updateEdge(GraphMaterialEdgeCommand command);

    void deleteEdge(GraphMaterialEdgeDeleteCommand command);

    GraphMaterialChangeImpactResult previewNodeMerge(GraphMaterialNodeMergeQuery query);

    GraphMaterialResult mergeNodes(GraphMaterialNodeMergeCommand command);

    GraphMaterialChangeImpactResult previewNodeSplit(GraphMaterialNodeSplitQuery query);

    GraphMaterialResult splitNode(GraphMaterialNodeSplitCommand command);

    List<GraphMaterialVersion> listVersions(GraphMaterialQuery query);

    GraphMaterialResult restoreVersion(GraphMaterialVersionRestoreCommand command);

    GraphMaterialImportPreviewResult previewImport(GraphMaterialImportQuery query);

    GraphMaterialResult importGraph(GraphMaterialImportCommand command);

    String exportGraph(GraphMaterialQuery query);
}
