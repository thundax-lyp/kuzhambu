import { useQuery } from "@tanstack/react-query";
import { Card, Empty, Tabs, Typography } from "antd";
import { useCallback, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { GraphEntityDetail } from "./components/graph-entity-detail";
import { GraphEntityTable } from "./components/graph-entity-table";
import { GraphLineageNodeDetail } from "./components/graph-lineage-node-detail";
import { GraphLineageNodeTable } from "./components/graph-lineage-node-table";
import { GraphLineageRelationDetail } from "./components/graph-lineage-relation-detail";
import { GraphLineageRelationTable } from "./components/graph-lineage-relation-table";
import { GraphRelationDetail } from "./components/graph-relation-detail";
import { GraphRelationTable } from "./components/graph-relation-table";
import { GraphVersionDetail } from "./components/graph-version-detail";
import { GraphVersionTable } from "./components/graph-version-table";
import * as service from "./graph-results-service";
import type {
    GraphEntityPageQuery,
    GraphLineageNodePageQuery,
    GraphLineageRelationPageQuery,
    GraphRelationPageQuery,
    GraphVersionPageQuery,
    GraphVersionRecord
} from "./graph-results-types";
import "./graph-results-page.css";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";

const { Paragraph, Text, Title } = Typography;
type GraphResultsTabKey = "versions" | "entities" | "relations" | "lineage";

const readGraphVersionIdFromSearch = () => {
    if (typeof window === "undefined") {
        return null;
    }
    const versionId = Number(new URLSearchParams(window.location.search).get("graphVersionId"));
    return Number.isFinite(versionId) && versionId > 0 ? versionId : null;
};

export const GraphResultsPage = () => {
    const canViewGraph = hasPermission("knowledge:graph:view");
    const [focusVersionId] = useState<number | null>(() => readGraphVersionIdFromSearch());
    const [activeTab, setActiveTab] = useState<GraphResultsTabKey>(
        focusVersionId ? "entities" : "versions"
    );
    const [versionQuery] = useState<GraphVersionPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [entityQuery, setEntityQuery] = useState<GraphEntityPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [relationQuery, setRelationQuery] = useState<GraphRelationPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [lineageNodeQuery, setLineageNodeQuery] = useState<GraphLineageNodePageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [lineageRelationQuery, setLineageRelationQuery] = useState<GraphLineageRelationPageQuery>(
        {
            pageNo: DEFAULT_PAGE_NO,
            pageSize: DEFAULT_PAGE_SIZE
        }
    );
    const [detailVersionId, setDetailVersionId] = useState<number | null>(null);
    const [detailOpen, setDetailOpen] = useState(false);
    const [selectedVersion, setSelectedVersion] = useState<GraphVersionRecord | null>(null);
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
    const focusedVersion = focusVersionId
        ? versions.find((version) => version.versionId === focusVersionId) || null
        : null;
    const activeVersion = selectedVersion || focusedVersion;
    const activeVersionId = activeVersion?.versionId || focusVersionId || null;
    const effectiveEntityQuery = useMemo(
        () => ({
            ...entityQuery,
            versionId: activeVersionId ?? entityQuery.versionId ?? null
        }),
        [activeVersionId, entityQuery]
    );
    const effectiveRelationQuery = useMemo(
        () => ({
            ...relationQuery,
            versionId: activeVersionId ?? relationQuery.versionId ?? null
        }),
        [activeVersionId, relationQuery]
    );
    const effectiveLineageNodeQuery = useMemo(
        () => ({
            ...lineageNodeQuery,
            versionId: activeVersionId ?? lineageNodeQuery.versionId ?? null
        }),
        [activeVersionId, lineageNodeQuery]
    );
    const effectiveLineageRelationQuery = useMemo(
        () => ({
            ...lineageRelationQuery,
            versionId: activeVersionId ?? lineageRelationQuery.versionId ?? null
        }),
        [activeVersionId, lineageRelationQuery]
    );
    const versionDetailQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "version-detail", detailVersionId],
        queryFn: () => service.getVersionDetail({ versionId: detailVersionId || 0 }),
        enabled: detailOpen && detailVersionId !== null,
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
        setDetailOpen(true);
    };

    const selectVersionResults = useCallback((version: GraphVersionRecord) => {
        setSelectedVersion(version);
        setEntityQuery((current) => ({
            ...current,
            pageNo: DEFAULT_PAGE_NO,
            versionId: version.versionId
        }));
        setRelationQuery((current) => ({
            ...current,
            pageNo: DEFAULT_PAGE_NO,
            versionId: version.versionId
        }));
        setLineageNodeQuery((current) => ({
            ...current,
            pageNo: DEFAULT_PAGE_NO,
            versionId: version.versionId
        }));
        setLineageRelationQuery((current) => ({
            ...current,
            pageNo: DEFAULT_PAGE_NO,
            versionId: version.versionId
        }));
        setDetailOpen(false);
        setActiveTab("entities");
    }, []);

    const renderVersionSelectionHint = (description: string) => {
        if (!canViewGraph) {
            return (
                <Empty
                    description="当前账号暂无知识图谱查看权限。"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
            );
        }
        if (!activeVersion) {
            return <Empty description={description} image={Empty.PRESENTED_IMAGE_SIMPLE} />;
        }
        return null;
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

                <section aria-labelledby="graph-results-overview-section">
                    <div className="knowledge-graph-results-section-header">
                        <Title id="graph-results-overview-section" level={4}>
                            结果入口
                        </Title>
                        <Text type="secondary">
                            图谱版本列表会作为主入口，再下钻查看正式实体、关系和世系结果。
                        </Text>
                    </div>
                    <Paragraph className="knowledge-graph-results-helper">
                        当前页已以图谱版本作为主入口，管理员可以从版本详情下钻审阅实体、关系和世系正式结果。
                    </Paragraph>
                </section>

                <Card className="knowledge-graph-results-shell" variant="borderless">
                    <Tabs
                        activeKey={activeTab}
                        onChange={(value) => setActiveTab(value as GraphResultsTabKey)}
                        items={[
                            {
                                key: "versions",
                                label: "图谱版本",
                                children: (
                                    <>
                                        {canViewGraph && versions.length > 0 ? (
                                            <GraphVersionTable
                                                loading={versionPageQuery.isLoading}
                                                selectedVersionId={activeVersionId}
                                                versions={versions}
                                                onOpenDetail={openVersionDetail}
                                                onOpenResults={selectVersionResults}
                                            />
                                        ) : (
                                            <Empty
                                                description={
                                                    canViewGraph
                                                        ? "当前还没有图谱版本，可先在抽取任务台应用候选结果。"
                                                        : "当前账号暂无知识图谱查看权限。"
                                                }
                                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                                            />
                                        )}
                                        {detailOpen ? (
                                            <GraphVersionDetail
                                                loading={versionDetailQuery.isLoading}
                                                open={detailOpen}
                                                version={versionDetailQuery.data || null}
                                                onOpenResults={selectVersionResults}
                                                onClose={() => setDetailOpen(false)}
                                            />
                                        ) : null}
                                    </>
                                )
                            },
                            {
                                key: "entities",
                                label: "正式实体",
                                children: (
                                    <KuzhambuSpace
                                        orientation="vertical"
                                        size={16}
                                        className="knowledge-graph-results-layout"
                                    >
                                        <KuzhambuAlert
                                            showIcon
                                            type="info"
                                            title={
                                                activeVersion
                                                    ? `当前查看版本 #${activeVersion.versionId} 的正式实体`
                                                    : "请先从图谱版本详情中选择一个版本"
                                            }
                                            description={
                                                activeVersion
                                                    ? `任务 ${activeVersion.taskId || "-"} / 状态 ${activeVersion.status || "-"}`
                                                    : "点击图谱版本列表中的“查看详情”，再使用“查看此版本正式结果”进入下钻视图。"
                                            }
                                        />
                                        {renderVersionSelectionHint(
                                            "实体列表会从版本详情下钻进入，默认展示确认状态、版本关联和来源引用。"
                                        ) || (
                                            <>
                                                <GraphEntityTable
                                                    entities={entities}
                                                    loading={entityPageQuery.isLoading}
                                                    onOpenDetail={(entity) => {
                                                        setEntityDetailId(entity.entityId);
                                                        setEntityDetailOpen(true);
                                                    }}
                                                />
                                                <GraphEntityDetail
                                                    entity={entityDetailQuery.data || null}
                                                    loading={entityDetailQuery.isLoading}
                                                    open={entityDetailOpen}
                                                    onClose={() => setEntityDetailOpen(false)}
                                                />
                                            </>
                                        )}
                                    </KuzhambuSpace>
                                )
                            },
                            {
                                key: "relations",
                                label: "正式关系",
                                children: (
                                    <KuzhambuSpace
                                        orientation="vertical"
                                        size={16}
                                        className="knowledge-graph-results-layout"
                                    >
                                        <KuzhambuAlert
                                            showIcon
                                            type="info"
                                            title={
                                                activeVersion
                                                    ? `当前查看版本 #${activeVersion.versionId} 的正式关系`
                                                    : "请先从图谱版本详情中选择一个版本"
                                            }
                                            description={
                                                activeVersion
                                                    ? `任务类型 ${activeVersion.taskType || "-"} / 来源 ${activeVersion.sourceContentType || "-"}`
                                                    : "点击图谱版本列表中的“查看详情”，再使用“查看此版本正式结果”进入下钻视图。"
                                            }
                                        />
                                        {renderVersionSelectionHint(
                                            "关系列表会与版本详情联动，重点展示关系类型、证据和确认状态。"
                                        ) || (
                                            <>
                                                <GraphRelationTable
                                                    loading={relationPageQuery.isLoading}
                                                    onOpenDetail={(relation) => {
                                                        setRelationDetailId(relation.relationId);
                                                        setRelationDetailOpen(true);
                                                    }}
                                                    relations={relations}
                                                />
                                                <GraphRelationDetail
                                                    loading={relationDetailQuery.isLoading}
                                                    open={relationDetailOpen}
                                                    relation={relationDetailQuery.data || null}
                                                    onClose={() => setRelationDetailOpen(false)}
                                                />
                                            </>
                                        )}
                                    </KuzhambuSpace>
                                )
                            },
                            {
                                key: "lineage",
                                label: "正式世系",
                                children: (
                                    <KuzhambuSpace
                                        orientation="vertical"
                                        size={16}
                                        className="knowledge-graph-results-layout"
                                    >
                                        <KuzhambuAlert
                                            showIcon
                                            type="info"
                                            title={
                                                activeVersion
                                                    ? `当前查看版本 #${activeVersion.versionId} 的正式世系`
                                                    : "请先从图谱版本详情中选择一个版本"
                                            }
                                            description={
                                                activeVersion
                                                    ? "世系节点与关系拆成独立读视图，强调版本来源和确认状态。"
                                                    : "点击图谱版本列表中的“查看详情”，再使用“查看此版本正式结果”进入下钻视图。"
                                            }
                                        />
                                        {renderVersionSelectionHint(
                                            "世系节点和关系会拆成独立读视图，强调版本来源和确认状态。"
                                        ) || (
                                            <>
                                                <section aria-labelledby="graph-results-lineage-nodes">
                                                    <KuzhambuSpace
                                                        orientation="vertical"
                                                        size={12}
                                                        className="knowledge-graph-results-layout"
                                                    >
                                                        <Title
                                                            id="graph-results-lineage-nodes"
                                                            level={5}
                                                        >
                                                            世系节点
                                                        </Title>
                                                        <GraphLineageNodeTable
                                                            loading={lineageNodePageQuery.isLoading}
                                                            nodes={lineageNodes}
                                                            onOpenDetail={(node) => {
                                                                setLineageNodeDetailId(node.nodeId);
                                                                setLineageNodeDetailOpen(true);
                                                            }}
                                                        />
                                                    </KuzhambuSpace>
                                                </section>
                                                <section aria-labelledby="graph-results-lineage-relations">
                                                    <KuzhambuSpace
                                                        orientation="vertical"
                                                        size={12}
                                                        className="knowledge-graph-results-layout"
                                                    >
                                                        <Title
                                                            id="graph-results-lineage-relations"
                                                            level={5}
                                                        >
                                                            世系关系
                                                        </Title>
                                                        <GraphLineageRelationTable
                                                            loading={
                                                                lineageRelationPageQuery.isLoading
                                                            }
                                                            onOpenDetail={(relation) => {
                                                                setLineageRelationDetailId(
                                                                    relation.relationId
                                                                );
                                                                setLineageRelationDetailOpen(true);
                                                            }}
                                                            relations={lineageRelations}
                                                        />
                                                    </KuzhambuSpace>
                                                </section>
                                                <GraphLineageNodeDetail
                                                    loading={lineageNodeDetailQuery.isLoading}
                                                    node={lineageNodeDetailQuery.data || null}
                                                    open={lineageNodeDetailOpen}
                                                    onClose={() => setLineageNodeDetailOpen(false)}
                                                />
                                                <GraphLineageRelationDetail
                                                    loading={lineageRelationDetailQuery.isLoading}
                                                    open={lineageRelationDetailOpen}
                                                    relation={
                                                        lineageRelationDetailQuery.data || null
                                                    }
                                                    onClose={() =>
                                                        setLineageRelationDetailOpen(false)
                                                    }
                                                />
                                            </>
                                        )}
                                    </KuzhambuSpace>
                                )
                            }
                        ]}
                    />
                </Card>
            </KuzhambuSpace>
        </KuzhambuPage>
    );
};
