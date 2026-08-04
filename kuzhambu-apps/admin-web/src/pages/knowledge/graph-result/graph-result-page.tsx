import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuSpace, KuzhambuPage, KuzhambuAlert } from "@/components";

import { GraphResultsTabs } from "./graph-results-tabs";
import { GraphResultsToolbar } from "./graph-results-toolbar";
import * as service from "./graph-result-service";
import type { GraphVersionRecord } from "./graph-result-types";
import { useGraphResultsQueryState } from "./hooks/use-graph-results-query-state";
import "./graph-result-page.css";

export const GraphResultPage = () => {
    const canViewGraph = hasPermission("knowledge:graph:view");
    const canOpenRefinement = hasPermission("knowledge:refinement:edit");
    const {
        activeTab,
        effectiveEntityQuery,
        effectiveLineageNodeQuery,
        effectiveLineageRelationQuery,
        effectiveRelationQuery,
        focusVersionId,
        resolveActiveVersion,
        selectVersionResults,
        setActiveTab,
        versionQuery
    } = useGraphResultsQueryState();
    const [detailVersionId, setDetailVersionId] = useState<string | null>(null);
    const [versionDetailDrawerOpen, setVersionDetailDrawerOpen] = useState(false);
    const [entityDetailId, setEntityDetailId] = useState<string | null>(null);
    const [entityDetailDrawerOpen, setEntityDetailDrawerOpen] = useState(false);
    const [relationDetailId, setRelationDetailId] = useState<string | null>(null);
    const [relationDetailDrawerOpen, setRelationDetailDrawerOpen] = useState(false);
    const [lineageNodeDetailId, setLineageNodeDetailId] = useState<string | null>(null);
    const [lineageNodeDetailDrawerOpen, setLineageNodeDetailDrawerOpen] = useState(false);
    const [lineageRelationDetailId, setLineageRelationDetailId] = useState<string | null>(null);
    const [lineageRelationDetailDrawerOpen, setLineageRelationDetailDrawerOpen] = useState(false);

    const versionPageQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "versions", versionQuery],
        queryFn: () => service.pageVersions(versionQuery),
        enabled: canViewGraph,
        retry: false
    });
    const versions = versionPageQuery.data?.records || [];
    const activeVersion = resolveActiveVersion(versions);
    const activeVersionId = activeVersion?.versionId || focusVersionId || null;
    const versionDetailQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "version-detail", detailVersionId],
        queryFn: () => service.getVersionDetail({ versionId: detailVersionId || "" }),
        enabled: versionDetailDrawerOpen && detailVersionId !== null,
        retry: false
    });
    const entityPageQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "entities", effectiveEntityQuery],
        queryFn: () => service.pageEntities(effectiveEntityQuery),
        enabled: canViewGraph && activeTab === "entities" && activeVersionId != null,
        retry: false
    });
    const relationPageQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "relations", effectiveRelationQuery],
        queryFn: () => service.pageRelations(effectiveRelationQuery),
        enabled: canViewGraph && activeTab === "relations" && activeVersionId != null,
        retry: false
    });
    const lineageNodePageQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "lineage-nodes", effectiveLineageNodeQuery],
        queryFn: () => service.pageLineageNodes(effectiveLineageNodeQuery),
        enabled: canViewGraph && activeTab === "lineage" && activeVersionId != null,
        retry: false
    });
    const lineageRelationPageQuery = useQuery({
        queryKey: [
            "knowledge",
            "graph-results",
            "lineage-relations",
            effectiveLineageRelationQuery
        ],
        queryFn: () => service.pageLineageRelations(effectiveLineageRelationQuery),
        enabled: canViewGraph && activeTab === "lineage" && activeVersionId != null,
        retry: false
    });
    const entityDetailQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "entity-detail", entityDetailId],
        queryFn: () => service.getEntityDetail({ entityId: entityDetailId || "" }),
        enabled: entityDetailDrawerOpen && entityDetailId !== null,
        retry: false
    });
    const relationDetailQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "relation-detail", relationDetailId],
        queryFn: () => service.getRelationDetail({ relationId: relationDetailId || "" }),
        enabled: relationDetailDrawerOpen && relationDetailId !== null,
        retry: false
    });
    const lineageNodeDetailQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "lineage-node-detail", lineageNodeDetailId],
        queryFn: () => service.getLineageNodeDetail({ nodeId: lineageNodeDetailId || "" }),
        enabled: lineageNodeDetailDrawerOpen && lineageNodeDetailId !== null,
        retry: false
    });
    const lineageRelationDetailQuery = useQuery({
        queryKey: [
            "knowledge",
            "graph-results",
            "lineage-relation-detail",
            lineageRelationDetailId
        ],
        queryFn: () =>
            service.getLineageRelationDetail({ relationId: lineageRelationDetailId || "" }),
        enabled: lineageRelationDetailDrawerOpen && lineageRelationDetailId !== null,
        retry: false
    });

    const entities = entityPageQuery.data?.records || [];
    const relations = relationPageQuery.data?.records || [];
    const lineageNodes = lineageNodePageQuery.data?.records || [];
    const lineageRelations = lineageRelationPageQuery.data?.records || [];

    const openVersionDetailDrawer = (version: GraphVersionRecord) => {
        setDetailVersionId(version.versionId);
        setVersionDetailDrawerOpen(true);
    };

    const openVersionResultsTab = (version: GraphVersionRecord) => {
        selectVersionResults(version);
        setVersionDetailDrawerOpen(false);
    };

    return (
        <KuzhambuPage
            className="graph-result-page knowledge-graph-result-page"
            description="以图谱版本为主入口，独立审阅正式实体、关系和世系结果。"
            title="正式结果读取"
        >
            <KuzhambuSpace
                orientation="vertical"
                size={16}
                className="knowledge-graph-results-layout"
            >
                <KuzhambuAlert
                    banner
                    className="knowledge-graph-results-banner"
                    title="本页将作为正式结果审阅台，与 taxonomy 治理台和抽取任务台保持独立边界。"
                    type="info"
                />

                <GraphResultsToolbar />

                <GraphResultsTabs
                    activeTab={activeTab}
                    activeVersion={activeVersion}
                    activeVersionId={activeVersionId}
                    canOpenRefinement={canOpenRefinement}
                    canViewGraph={canViewGraph}
                    versionDetailDrawerOpen={versionDetailDrawerOpen}
                    entities={entities}
                    entityDetail={entityDetailQuery.data || null}
                    entityDetailLoading={entityDetailQuery.isLoading}
                    entityDetailDrawerOpen={entityDetailDrawerOpen}
                    entityLoading={entityPageQuery.isLoading}
                    lineageNodeDetail={lineageNodeDetailQuery.data || null}
                    lineageNodeDetailLoading={lineageNodeDetailQuery.isLoading}
                    lineageNodeDetailDrawerOpen={lineageNodeDetailDrawerOpen}
                    lineageNodeLoading={lineageNodePageQuery.isLoading}
                    lineageNodes={lineageNodes}
                    lineageRelationDetail={lineageRelationDetailQuery.data || null}
                    lineageRelationDetailLoading={lineageRelationDetailQuery.isLoading}
                    lineageRelationDetailDrawerOpen={lineageRelationDetailDrawerOpen}
                    lineageRelationLoading={lineageRelationPageQuery.isLoading}
                    lineageRelations={lineageRelations}
                    relationDetail={relationDetailQuery.data || null}
                    relationDetailLoading={relationDetailQuery.isLoading}
                    relationDetailDrawerOpen={relationDetailDrawerOpen}
                    relationLoading={relationPageQuery.isLoading}
                    relations={relations}
                    versionDetail={versionDetailQuery.data || null}
                    versionDetailLoading={versionDetailQuery.isLoading}
                    versionLoading={versionPageQuery.isLoading}
                    versions={versions}
                    onActiveTabChange={setActiveTab}
                    onCloseEntityDetail={() => setEntityDetailDrawerOpen(false)}
                    onCloseLineageNodeDetail={() => setLineageNodeDetailDrawerOpen(false)}
                    onCloseLineageRelationDetail={() => setLineageRelationDetailDrawerOpen(false)}
                    onCloseRelationDetail={() => setRelationDetailDrawerOpen(false)}
                    onCloseVersionDetail={() => setVersionDetailDrawerOpen(false)}
                    onOpenEntityDetail={(entity) => {
                        setEntityDetailId(entity.entityId);
                        setEntityDetailDrawerOpen(true);
                    }}
                    onOpenLineageNodeDetail={(node) => {
                        setLineageNodeDetailId(node.nodeId);
                        setLineageNodeDetailDrawerOpen(true);
                    }}
                    onOpenLineageRelationDetail={(relation) => {
                        setLineageRelationDetailId(relation.relationId);
                        setLineageRelationDetailDrawerOpen(true);
                    }}
                    onOpenRelationDetail={(relation) => {
                        setRelationDetailId(relation.relationId);
                        setRelationDetailDrawerOpen(true);
                    }}
                    onOpenVersionDetail={openVersionDetailDrawer}
                    onOpenVersionResults={openVersionResultsTab}
                />
            </KuzhambuSpace>
        </KuzhambuPage>
    );
};
