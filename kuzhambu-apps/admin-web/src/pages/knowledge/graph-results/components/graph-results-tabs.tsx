import { Card, Empty, Tabs, Typography } from "antd";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { GraphEntityDetail } from "./graph-entity-detail";
import { GraphEntityTable } from "./graph-entity-table";
import { GraphLineageNodeDetail } from "./graph-lineage-node-detail";
import { GraphLineageNodeTable } from "./graph-lineage-node-table";
import { GraphLineageRelationDetail } from "./graph-lineage-relation-detail";
import { GraphLineageRelationTable } from "./graph-lineage-relation-table";
import { GraphRelationDetail } from "./graph-relation-detail";
import { GraphRelationTable } from "./graph-relation-table";
import { GraphVersionDetail } from "./graph-version-detail";
import { GraphVersionTable } from "./graph-version-table";
import type { GraphResultsTabKey } from "../hooks/use-graph-results-query-state";
import type {
    GraphEntityRecord,
    GraphLineageNodeRecord,
    GraphLineageRelationRecord,
    GraphRelationRecord,
    GraphVersionRecord
} from "../graph-results-types";

const { Title } = Typography;

interface GraphResultsTabsProps {
    activeTab: GraphResultsTabKey;
    activeVersion: GraphVersionRecord | null;
    activeVersionId: number | null;
    canViewGraph: boolean;
    detailOpen: boolean;
    entities: GraphEntityRecord[];
    entityDetail: GraphEntityRecord | null;
    entityDetailOpen: boolean;
    entityDetailLoading: boolean;
    entityLoading: boolean;
    lineageNodeDetail: GraphLineageNodeRecord | null;
    lineageNodeDetailOpen: boolean;
    lineageNodeDetailLoading: boolean;
    lineageNodeLoading: boolean;
    lineageNodes: GraphLineageNodeRecord[];
    lineageRelationDetail: GraphLineageRelationRecord | null;
    lineageRelationDetailOpen: boolean;
    lineageRelationDetailLoading: boolean;
    lineageRelationLoading: boolean;
    lineageRelations: GraphLineageRelationRecord[];
    relationDetail: GraphRelationRecord | null;
    relationDetailOpen: boolean;
    relationDetailLoading: boolean;
    relationLoading: boolean;
    relations: GraphRelationRecord[];
    versionDetail: GraphVersionRecord | null;
    versionDetailLoading: boolean;
    versionLoading: boolean;
    versions: GraphVersionRecord[];
    onActiveTabChange: (activeTab: GraphResultsTabKey) => void;
    onCloseEntityDetail: () => void;
    onCloseLineageNodeDetail: () => void;
    onCloseLineageRelationDetail: () => void;
    onCloseRelationDetail: () => void;
    onCloseVersionDetail: () => void;
    onOpenEntityDetail: (entity: GraphEntityRecord) => void;
    onOpenLineageNodeDetail: (node: GraphLineageNodeRecord) => void;
    onOpenLineageRelationDetail: (relation: GraphLineageRelationRecord) => void;
    onOpenRelationDetail: (relation: GraphRelationRecord) => void;
    onOpenVersionDetail: (version: GraphVersionRecord) => void;
    onOpenVersionResults: (version: GraphVersionRecord) => void;
}

export const GraphResultsTabs = ({
    activeTab,
    activeVersion,
    activeVersionId,
    canViewGraph,
    detailOpen,
    entities,
    entityDetail,
    entityDetailOpen,
    entityDetailLoading,
    entityLoading,
    lineageNodeDetail,
    lineageNodeDetailOpen,
    lineageNodeDetailLoading,
    lineageNodeLoading,
    lineageNodes,
    lineageRelationDetail,
    lineageRelationDetailOpen,
    lineageRelationDetailLoading,
    lineageRelationLoading,
    lineageRelations,
    relationDetail,
    relationDetailOpen,
    relationDetailLoading,
    relationLoading,
    relations,
    versionDetail,
    versionDetailLoading,
    versionLoading,
    versions,
    onActiveTabChange,
    onCloseEntityDetail,
    onCloseLineageNodeDetail,
    onCloseLineageRelationDetail,
    onCloseRelationDetail,
    onCloseVersionDetail,
    onOpenEntityDetail,
    onOpenLineageNodeDetail,
    onOpenLineageRelationDetail,
    onOpenRelationDetail,
    onOpenVersionDetail,
    onOpenVersionResults
}: GraphResultsTabsProps) => {
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
        <Card className="knowledge-graph-results-shell" variant="borderless">
            <Tabs
                activeKey={activeTab}
                onChange={(value) => onActiveTabChange(value as GraphResultsTabKey)}
                items={[
                    {
                        key: "versions",
                        label: "图谱版本",
                        children: (
                            <>
                                {canViewGraph && versions.length > 0 ? (
                                    <GraphVersionTable
                                        loading={versionLoading}
                                        selectedVersionId={activeVersionId}
                                        versions={versions}
                                        onOpenDetail={onOpenVersionDetail}
                                        onOpenResults={onOpenVersionResults}
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
                                        loading={versionDetailLoading}
                                        open={detailOpen}
                                        version={versionDetail}
                                        onOpenResults={onOpenVersionResults}
                                        onClose={onCloseVersionDetail}
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
                                            loading={entityLoading}
                                            onOpenDetail={onOpenEntityDetail}
                                        />
                                        <GraphEntityDetail
                                            entity={entityDetail}
                                            loading={entityDetailLoading}
                                            open={entityDetailOpen}
                                            onClose={onCloseEntityDetail}
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
                                            loading={relationLoading}
                                            onOpenDetail={onOpenRelationDetail}
                                            relations={relations}
                                        />
                                        <GraphRelationDetail
                                            loading={relationDetailLoading}
                                            open={relationDetailOpen}
                                            relation={relationDetail}
                                            onClose={onCloseRelationDetail}
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
                                                <Title id="graph-results-lineage-nodes" level={5}>
                                                    世系节点
                                                </Title>
                                                <GraphLineageNodeTable
                                                    loading={lineageNodeLoading}
                                                    nodes={lineageNodes}
                                                    onOpenDetail={onOpenLineageNodeDetail}
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
                                                    loading={lineageRelationLoading}
                                                    onOpenDetail={onOpenLineageRelationDetail}
                                                    relations={lineageRelations}
                                                />
                                            </KuzhambuSpace>
                                        </section>
                                        <GraphLineageNodeDetail
                                            loading={lineageNodeDetailLoading}
                                            node={lineageNodeDetail}
                                            open={lineageNodeDetailOpen}
                                            onClose={onCloseLineageNodeDetail}
                                        />
                                        <GraphLineageRelationDetail
                                            loading={lineageRelationDetailLoading}
                                            open={lineageRelationDetailOpen}
                                            relation={lineageRelationDetail}
                                            onClose={onCloseLineageRelationDetail}
                                        />
                                    </>
                                )}
                            </KuzhambuSpace>
                        )
                    }
                ]}
            />
        </Card>
    );
};
