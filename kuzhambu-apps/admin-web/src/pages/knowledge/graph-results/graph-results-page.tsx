import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";
import { GraphResultsTabs } from "./components/graph-results-tabs";
import { GraphResultsToolbar } from "./components/graph-results-toolbar";
import * as service from "./graph-results-service";
import type { GraphVersionRecord } from "./graph-results-types";
import { useGraphResultsQueryState } from "./hooks/use-graph-results-query-state";
import "./graph-results-page.css";

export const GraphResultsPage = () => {
    const canViewGraph = hasPermission("knowledge:graph:view");
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
    const [detailVersionId, setDetailVersionId] = useState<number | null>(null);
    const [versionDetailDrawerOpen, setVersionDetailDrawerOpen] = useState(false);
    const [entityDetailId, setEntityDetailId] = useState<number | null>(null);
    const [entityDetailOpen, setEntityDetailOpen] = useState(false);
    const [relationDetailId, setRelationDetailId] = useState<number | null>(null);
    const [relationDetailOpen, setRelationDetailOpen] = useState(false);
    const [lineageNodeDetailId, setLineageNodeDetailId] = useState<number | null>(null);
    const [lineageNodeDetailOpen, setLineageNodeDetailOpen] = useState(false);
    const [lineageRelationDetailId, setLineageRelationDetailId] = useState<number | null>(null);
    const [lineageRelationDetailOpen, setLineageRelationDetailOpen] = useState(false);

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
        queryFn: () => service.getVersionDetail({ versionId: detailVersionId || 0 }),
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
        queryFn: () => service.getEntityDetail({ entityId: entityDetailId || 0 }),
        enabled: entityDetailOpen && entityDetailId !== null,
        retry: false
    });
    const relationDetailQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "relation-detail", relationDetailId],
        queryFn: () => service.getRelationDetail({ relationId: relationDetailId || 0 }),
        enabled: relationDetailOpen && relationDetailId !== null,
        retry: false
    });
    const lineageNodeDetailQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "lineage-node-detail", lineageNodeDetailId],
        queryFn: () => service.getLineageNodeDetail({ nodeId: lineageNodeDetailId || 0 }),
        enabled: lineageNodeDetailOpen && lineageNodeDetailId !== null,
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
            service.getLineageRelationDetail({ relationId: lineageRelationDetailId || 0 }),
        enabled: lineageRelationDetailOpen && lineageRelationDetailId !== null,
        retry: false
    });

    const entities = entityPageQuery.data?.records || [];
    const relations = relationPageQuery.data?.records || [];
    const lineageNodes = lineageNodePageQuery.data?.records || [];
    const lineageRelations = lineageRelationPageQuery.data?.records || [];

    const openVersionDetail = (version: GraphVersionRecord) => {
        setDetailVersionId(version.versionId);
        setVersionDetailDrawerOpen(true);
    };

    const openVersionResults = (version: GraphVersionRecord) => {
        selectVersionResults(version);
        setVersionDetailDrawerOpen(false);
    };

    return (
        <KuzhambuPage
            className="graph-results-page knowledge-graph-results-page"
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
                    canViewGraph={canViewGraph}
                    versionDetailDrawerOpen={versionDetailDrawerOpen}
                    entities={entities}
                    entityDetail={entityDetailQuery.data || null}
                    entityDetailLoading={entityDetailQuery.isLoading}
                    entityDetailOpen={entityDetailOpen}
                    entityLoading={entityPageQuery.isLoading}
                    lineageNodeDetail={lineageNodeDetailQuery.data || null}
                    lineageNodeDetailLoading={lineageNodeDetailQuery.isLoading}
                    lineageNodeDetailOpen={lineageNodeDetailOpen}
                    lineageNodeLoading={lineageNodePageQuery.isLoading}
                    lineageNodes={lineageNodes}
                    lineageRelationDetail={lineageRelationDetailQuery.data || null}
                    lineageRelationDetailLoading={lineageRelationDetailQuery.isLoading}
                    lineageRelationDetailOpen={lineageRelationDetailOpen}
                    lineageRelationLoading={lineageRelationPageQuery.isLoading}
                    lineageRelations={lineageRelations}
                    relationDetail={relationDetailQuery.data || null}
                    relationDetailLoading={relationDetailQuery.isLoading}
                    relationDetailOpen={relationDetailOpen}
                    relationLoading={relationPageQuery.isLoading}
                    relations={relations}
                    versionDetail={versionDetailQuery.data || null}
                    versionDetailLoading={versionDetailQuery.isLoading}
                    versionLoading={versionPageQuery.isLoading}
                    versions={versions}
                    onActiveTabChange={setActiveTab}
                    onCloseEntityDetail={() => setEntityDetailOpen(false)}
                    onCloseLineageNodeDetail={() => setLineageNodeDetailOpen(false)}
                    onCloseLineageRelationDetail={() => setLineageRelationDetailOpen(false)}
                    onCloseRelationDetail={() => setRelationDetailOpen(false)}
                    onCloseVersionDetail={() => setVersionDetailDrawerOpen(false)}
                    onOpenEntityDetail={(entity) => {
                        setEntityDetailId(entity.entityId);
                        setEntityDetailOpen(true);
                    }}
                    onOpenLineageNodeDetail={(node) => {
                        setLineageNodeDetailId(node.nodeId);
                        setLineageNodeDetailOpen(true);
                    }}
                    onOpenLineageRelationDetail={(relation) => {
                        setLineageRelationDetailId(relation.relationId);
                        setLineageRelationDetailOpen(true);
                    }}
                    onOpenRelationDetail={(relation) => {
                        setRelationDetailId(relation.relationId);
                        setRelationDetailOpen(true);
                    }}
                    onOpenVersionDetail={openVersionDetail}
                    onOpenVersionResults={openVersionResults}
                />
            </KuzhambuSpace>
        </KuzhambuPage>
    );
};
