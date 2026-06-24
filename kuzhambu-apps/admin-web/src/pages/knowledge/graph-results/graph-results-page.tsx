import { useQuery } from "@tanstack/react-query";
import { Alert, Card, Empty, Space, Tabs, Typography } from "antd";
import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
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

const { Paragraph, Text, Title } = Typography;
type GraphResultsTabKey = "versions" | "entities" | "relations" | "lineage";

export const GraphResultsPage = () => {
    const canViewGraph = hasPermission("knowledge:graph:view");
    const [activeTab, setActiveTab] = useState<GraphResultsTabKey>("versions");
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
    const versionDetailQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "version-detail", detailVersionId],
        queryFn: () => service.getVersionDetail({ versionId: detailVersionId || 0 }),
        enabled: detailOpen && detailVersionId !== null,
        retry: false
    });
    const entityPageQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "entities", entityQuery],
        queryFn: () => service.pageEntities(entityQuery),
        enabled: canViewGraph && activeTab === "entities" && entityQuery.versionId != null,
        retry: false
    });
    const relationPageQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "relations", relationQuery],
        queryFn: () => service.pageRelations(relationQuery),
        enabled: canViewGraph && activeTab === "relations" && relationQuery.versionId != null,
        retry: false
    });
    const lineageNodePageQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "lineage-nodes", lineageNodeQuery],
        queryFn: () => service.pageLineageNodes(lineageNodeQuery),
        enabled: canViewGraph && activeTab === "lineage" && lineageNodeQuery.versionId != null,
        retry: false
    });
    const lineageRelationPageQuery = useQuery({
        queryKey: ["knowledge", "graph-results", "lineage-relations", lineageRelationQuery],
        queryFn: () => service.pageLineageRelations(lineageRelationQuery),
        enabled: canViewGraph && activeTab === "lineage" && lineageRelationQuery.versionId != null,
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

    const versions = versionPageQuery.data?.records || [];
    const entities = entityPageQuery.data?.records || [];
    const relations = relationPageQuery.data?.records || [];
    const lineageNodes = lineageNodePageQuery.data?.records || [];
    const lineageRelations = lineageRelationPageQuery.data?.records || [];

    const openVersionDetail = (version: GraphVersionRecord) => {
        setDetailVersionId(version.versionId);
        setDetailOpen(true);
    };

    const selectVersionResults = (version: GraphVersionRecord) => {
        setSelectedVersion(version);
        setEntityQuery((current) => ({ ...current, versionId: version.versionId }));
        setRelationQuery((current) => ({ ...current, versionId: version.versionId }));
        setLineageNodeQuery((current) => ({ ...current, versionId: version.versionId }));
        setLineageRelationQuery((current) => ({ ...current, versionId: version.versionId }));
        setDetailOpen(false);
        setActiveTab("entities");
    };

    const renderVersionSelectionHint = (description: string) => {
        if (!canViewGraph) {
            return (
                <Empty
                    description="当前账号暂无知识图谱查看权限。"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
            );
        }
        if (!selectedVersion) {
            return <Empty description={description} image={Empty.PRESENTED_IMAGE_SIMPLE} />;
        }
        return null;
    };

    return (
        <KuzhambuPage
            className="graph-results-page knowledge-graph-results-page"
            description="以图谱版本为主入口，独立审阅正式实体、关系和世系结果。"
            eyebrow="Knowledge / Graph Results"
            title="正式结果读取"
        >
            <Space orientation="vertical" size={16} className="knowledge-graph-results-layout">
                <Alert
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
                                                versions={versions}
                                                onOpenDetail={openVersionDetail}
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
                                    <Space
                                        orientation="vertical"
                                        size={16}
                                        className="knowledge-graph-results-layout"
                                    >
                                        <Alert
                                            showIcon
                                            type="info"
                                            title={
                                                selectedVersion
                                                    ? `当前查看版本 #${selectedVersion.versionId} 的正式实体`
                                                    : "请先从图谱版本详情中选择一个版本"
                                            }
                                            description={
                                                selectedVersion
                                                    ? `任务 ${selectedVersion.taskId || "-"} / 状态 ${selectedVersion.status || "-"}`
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
                                    </Space>
                                )
                            },
                            {
                                key: "relations",
                                label: "正式关系",
                                children: (
                                    <Space
                                        orientation="vertical"
                                        size={16}
                                        className="knowledge-graph-results-layout"
                                    >
                                        <Alert
                                            showIcon
                                            type="info"
                                            title={
                                                selectedVersion
                                                    ? `当前查看版本 #${selectedVersion.versionId} 的正式关系`
                                                    : "请先从图谱版本详情中选择一个版本"
                                            }
                                            description={
                                                selectedVersion
                                                    ? `任务类型 ${selectedVersion.taskType || "-"} / 来源 ${selectedVersion.sourceContentType || "-"}`
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
                                    </Space>
                                )
                            },
                            {
                                key: "lineage",
                                label: "正式世系",
                                children: (
                                    <Space
                                        orientation="vertical"
                                        size={16}
                                        className="knowledge-graph-results-layout"
                                    >
                                        <Alert
                                            showIcon
                                            type="info"
                                            title={
                                                selectedVersion
                                                    ? `当前查看版本 #${selectedVersion.versionId} 的正式世系`
                                                    : "请先从图谱版本详情中选择一个版本"
                                            }
                                            description={
                                                selectedVersion
                                                    ? "世系节点与关系拆成独立读视图，强调版本来源和确认状态。"
                                                    : "点击图谱版本列表中的“查看详情”，再使用“查看此版本正式结果”进入下钻视图。"
                                            }
                                        />
                                        {renderVersionSelectionHint(
                                            "世系节点和关系会拆成独立读视图，强调版本来源和确认状态。"
                                        ) || (
                                            <>
                                                <section aria-labelledby="graph-results-lineage-nodes">
                                                    <Space
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
                                                    </Space>
                                                </section>
                                                <section aria-labelledby="graph-results-lineage-relations">
                                                    <Space
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
                                                    </Space>
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
                                    </Space>
                                )
                            }
                        ]}
                    />
                </Card>
            </Space>
        </KuzhambuPage>
    );
};
