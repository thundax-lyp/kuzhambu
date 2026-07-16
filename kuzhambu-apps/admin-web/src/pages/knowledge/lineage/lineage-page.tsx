import { useQuery } from "@tanstack/react-query";
import { Empty, Spin, Tabs, Typography } from "antd";
import { useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { LineageCanvas } from "./components/lineage-canvas";
import { LineageDetailPanel } from "./components/lineage-detail-panel";
import { LineageFilterBar } from "./components/lineage-filter-bar";
import { LineageNodeTable } from "./components/lineage-node-table";
import { LineageRelationTable } from "./components/lineage-relation-table";
import * as service from "./lineage-service";
import type { LineageCanvasQuery } from "./lineage-service";
import type {
    LineageAvailableFiltersRecord,
    LineageNodeRecord,
    LineageRelationRecord
} from "./lineage-types";
import "./lineage-page.css";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";

const { Text, Title } = Typography;

const EMPTY_FILTERS: LineageAvailableFiltersRecord = {
    versions: [],
    nodeTypes: [],
    relationTypes: [],
    confirmationStatuses: []
};

export const LineagePage = () => {
    const canViewLineage = hasPermission("knowledge:graph:view");
    const [query, setQuery] = useState<LineageCanvasQuery>({
        versionId: null,
        focusNodeId: null,
        focusRelationId: null,
        keyword: null,
        nodeType: null,
        relationType: null,
        confirmationStatus: null,
        depth: 2
    });

    const lineageQuery = useQuery({
        queryKey: ["knowledge", "lineage", "canvas", query],
        queryFn: () => service.getLineageCanvas(query),
        enabled: canViewLineage,
        retry: false
    });

    const canvas = lineageQuery.data;
    const filters = canvas?.availableFilters || EMPTY_FILTERS;
    const selectedNode = canvas?.selectedNode || null;
    const selectedRelation = canvas?.selectedRelation || null;
    const emptyDescription = canvas?.empty?.description || canvas?.empty?.title;
    const canvasStatus = useMemo(() => {
        if (lineageQuery.isError) {
            return "世系画布读取失败，请稍后重试。";
        }
        if (lineageQuery.isLoading) {
            return "正在读取世系画布";
        }
        if (emptyDescription) {
            return emptyDescription;
        }
        if (!canvas || canvas.nodes.length === 0) {
            return "请选择世系版本";
        }
        return null;
    }, [canvas, emptyDescription, lineageQuery.isError, lineageQuery.isLoading]);

    const changeQuery = (nextQuery: LineageCanvasQuery) => {
        setQuery(nextQuery);
    };

    const resetQuery = () => {
        setQuery((current) => ({
            versionId: current.versionId ?? null,
            focusNodeId: null,
            focusRelationId: null,
            keyword: null,
            nodeType: null,
            relationType: null,
            confirmationStatus: null,
            depth: 2
        }));
    };
    const selectNode = (node: LineageNodeRecord) => {
        setQuery((current) => ({
            ...current,
            focusNodeId: node.nodeId,
            focusRelationId: null
        }));
    };
    const selectRelation = (relation: LineageRelationRecord) => {
        setQuery((current) => ({
            ...current,
            focusNodeId: null,
            focusRelationId: relation.relationId
        }));
    };

    return (
        <KuzhambuPage
            className="lineage-page knowledge-lineage-page"
            description="以正式世系版本为入口浏览节点、关系和来源线索。"
            eyebrow="Knowledge / Lineage"
            title="世系图浏览"
        >
            {canViewLineage ? (
                <KuzhambuSpace
                    orientation="vertical"
                    size={16}
                    className="knowledge-lineage-layout"
                >
                    <section className="knowledge-lineage-toolbar" aria-label="世系图筛选">
                        <div>
                            <Title level={4}>画布筛选</Title>
                            <Text type="secondary">选择版本后查看正式世系图。</Text>
                        </div>
                        <LineageFilterBar
                            filters={filters}
                            loading={lineageQuery.isFetching}
                            query={query}
                            onChange={changeQuery}
                            onRefresh={() => void lineageQuery.refetch()}
                            onReset={resetQuery}
                        />
                    </section>
                    <section className="knowledge-lineage-workspace" aria-label="世系图画布">
                        <main className="knowledge-lineage-main">
                            <div className="knowledge-lineage-canvas-shell">
                                {canvasStatus ? (
                                    <Spin spinning={lineageQuery.isLoading}>
                                        <Empty
                                            description={canvasStatus}
                                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                                        />
                                    </Spin>
                                ) : (
                                    <LineageCanvas
                                        nodes={canvas?.nodes || []}
                                        relations={canvas?.relations || []}
                                        selectedNodeId={query.focusNodeId}
                                        selectedRelationId={query.focusRelationId}
                                        onSelectNode={selectNode}
                                        onSelectRelation={selectRelation}
                                    />
                                )}
                            </div>
                            <div className="knowledge-lineage-list-shell">
                                {lineageQuery.isFetching && !lineageQuery.isLoading ? (
                                    <KuzhambuAlert banner title="正在刷新世系画布" type="info" />
                                ) : null}
                                <Tabs
                                    items={[
                                        {
                                            key: "nodes",
                                            label: "节点列表",
                                            children: (
                                                <LineageNodeTable
                                                    loading={lineageQuery.isFetching}
                                                    nodes={canvas?.nodes || []}
                                                    selectedNodeId={query.focusNodeId}
                                                    onSelectNode={selectNode}
                                                />
                                            )
                                        },
                                        {
                                            key: "relations",
                                            label: "关系列表",
                                            children: (
                                                <LineageRelationTable
                                                    loading={lineageQuery.isFetching}
                                                    relations={canvas?.relations || []}
                                                    selectedRelationId={query.focusRelationId}
                                                    onSelectRelation={selectRelation}
                                                />
                                            )
                                        }
                                    ]}
                                />
                            </div>
                        </main>
                        <aside
                            className="knowledge-lineage-detail-shell"
                            aria-label="节点和关系详情"
                        >
                            <LineageDetailPanel node={selectedNode} relation={selectedRelation} />
                        </aside>
                    </section>
                </KuzhambuSpace>
            ) : (
                <Empty
                    description="当前账号暂无知识图谱查看权限。"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
            )}
        </KuzhambuPage>
    );
};
