import { useQueries, useQuery } from "@tanstack/react-query";
import { Input, Segmented, Splitter } from "antd";
import { useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuGraph,
    KuzhambuPage,
    KuzhambuSelect,
    KuzhambuSpace,
    KuzhambuTable
} from "@/components";
import type {
    KuzhambuGraphNodeItem,
    KuzhambuGraphSpoItem,
    KuzhambuTableColumn
} from "@/components";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { GovernanceDetailDrawer } from "./governance-detail-drawer";
import * as service from "./graph-governance-service";
import type {
    GraphGovernanceAdjacencyRecord,
    GraphGovernanceNodeRecord,
    GraphGovernanceObjectType,
    GraphGovernanceRelationRecord
} from "./graph-governance-types";
import "./graph-governance-page.css";

const OBJECT_TYPE_OPTIONS = [
    { label: "节点", value: "NODE" },
    { label: "关系", value: "EDGE" }
];
const STATUS_OPTIONS = [
    { label: "有效", value: "ACTIVE" },
    { label: "已删除", value: "DELETED" },
    { label: "全部状态", value: "" }
];
const SOURCE_OPTIONS = [
    { label: "全部来源", value: "" },
    { label: "素材发布", value: "MATERIAL" },
    { label: "人工维护", value: "MANUAL" }
];

interface GraphGovernanceFilters {
    keyword: string;
    source: string;
    status: string;
    type: string;
}

const DEFAULT_FILTERS: GraphGovernanceFilters = {
    keyword: "",
    source: "",
    status: "ACTIVE",
    type: ""
};

const optionalValue = (value: string) => (value.trim() ? value.trim() : null);
const readNodeName = (node?: GraphGovernanceNodeRecord | null) => node?.name || node?.id || "-";
const readRelationName = (relation: GraphGovernanceRelationRecord) =>
    relation.relationType || relation.id;

const toGraphItems = (records: readonly GraphGovernanceAdjacencyRecord[]): KuzhambuGraphSpoItem[] =>
    records.flatMap((record) => {
        if (!record.relation || !record.object) {
            return [];
        }
        return [
            {
                object: readNodeName(record.object),
                objectId: record.object.id,
                predicate: readRelationName(record.relation),
                subject: readNodeName(record.subject),
                subjectId: record.subject.id
            }
        ];
    });

export const GraphGovernancePage = () => {
    const canViewGraph = hasPermission("knowledge:graph:view");
    const [objectType, setObjectType] = useState<GraphGovernanceObjectType>("NODE");
    const [filters, setFilters] = useState<GraphGovernanceFilters>(DEFAULT_FILTERS);
    const [query, setQuery] = useState({
        ...DEFAULT_FILTERS,
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [selectedNode, setSelectedNode] = useState<GraphGovernanceNodeRecord | null>(null);
    const [selectedObject, setSelectedObject] = useState<{
        id: string;
        type: GraphGovernanceObjectType;
    } | null>(null);
    const [expandedNodeIds, setExpandedNodeIds] = useState<string[]>([]);
    const pageQuery = useQuery({
        enabled: canViewGraph,
        queryFn: () =>
            objectType === "NODE"
                ? service.pagePublishedNodes({
                      keyword: optionalValue(query.keyword),
                      nodeType: optionalValue(query.type),
                      pageNo: query.pageNo,
                      pageSize: query.pageSize,
                      source: optionalValue(query.source),
                      status: optionalValue(query.status)
                  })
                : service.pagePublishedRelations({
                      keyword: optionalValue(query.keyword),
                      pageNo: query.pageNo,
                      pageSize: query.pageSize,
                      relationType: optionalValue(query.type),
                      source: optionalValue(query.source),
                      status: optionalValue(query.status)
                  }),
        queryKey: ["knowledge", "graph-governance", objectType, query]
    });
    const selectedNodeQuery = useQuery({
        enabled: Boolean(selectedNode),
        queryFn: () => service.getPublishedNode(selectedNode?.id ?? ""),
        queryKey: ["knowledge", "graph-governance", "selected-node", selectedNode?.id]
    });
    const focusNodes = useMemo(() => {
        const nodes = new Map<string, GraphGovernanceNodeRecord>();
        if (selectedNode) {
            nodes.set(selectedNode.id, selectedNode);
        }
        selectedNodeQuery.data?.incidentEdges.forEach((edge) => {
            if (edge.sourceNodeId && edge.sourceNodeId !== selectedNode?.id) {
                nodes.set(edge.sourceNodeId, { id: edge.sourceNodeId, name: edge.sourceNodeId });
            }
            if (edge.targetNodeId && edge.targetNodeId !== selectedNode?.id) {
                nodes.set(edge.targetNodeId, { id: edge.targetNodeId, name: edge.targetNodeId });
            }
        });
        return Array.from(nodes.values());
    }, [selectedNode, selectedNodeQuery.data?.incidentEdges]);
    const expandedNodes = useMemo(
        () => focusNodes.filter((node) => expandedNodeIds.includes(node.id)),
        [expandedNodeIds, focusNodes]
    );
    const adjacencyQueries = useQueries({
        queries: expandedNodes.slice(0, 12).map((node) => ({
            enabled: Boolean(node.name),
            queryFn: () =>
                service.pagePublishedAdjacency({
                    includeIsolated: false,
                    pageNo: DEFAULT_PAGE_NO,
                    pageSize: 50,
                    subjectKeyword: node.name ?? node.id
                }),
            queryKey: ["knowledge", "graph-governance", "adjacency", node.id, node.name]
        }))
    });
    const adjacencyRecords = useMemo(
        () =>
            adjacencyQueries.flatMap((adjacencyQuery) =>
                (adjacencyQuery.data?.records ?? []).filter((record) =>
                    expandedNodeIds.includes(record.subject.id)
                )
            ),
        [adjacencyQueries, expandedNodeIds]
    );
    const graphNodes = useMemo<KuzhambuGraphNodeItem[]>(() => {
        const nodes = new Map<string, KuzhambuGraphNodeItem>();
        if (selectedNode) {
            nodes.set(selectedNode.id, {
                group: "当前对象",
                id: selectedNode.id,
                label: readNodeName(selectedNode)
            });
        }
        adjacencyRecords.forEach((record) => {
            nodes.set(record.subject.id, {
                group: record.subject.nodeType || "节点",
                id: record.subject.id,
                label: readNodeName(record.subject)
            });
            if (record.object) {
                nodes.set(record.object.id, {
                    group: record.object.nodeType || "节点",
                    id: record.object.id,
                    label: readNodeName(record.object)
                });
            }
        });
        return Array.from(nodes.values()).slice(0, 200);
    }, [adjacencyRecords, selectedNode]);
    const graphItems = useMemo(() => toGraphItems(adjacencyRecords), [adjacencyRecords]);
    const nodeRecords = objectType === "NODE" ? (pageQuery.data?.records ?? []) : [];
    const relationRecords = objectType === "EDGE" ? (pageQuery.data?.records ?? []) : [];
    const totalCount = pageQuery.data?.totalCount ?? pageQuery.data?.count ?? 0;

    const selectNode = (node: GraphGovernanceNodeRecord) => {
        setSelectedNode(node);
        setSelectedObject({ id: node.id, type: "NODE" });
        setExpandedNodeIds([node.id]);
    };
    const selectRelation = (relation: GraphGovernanceRelationRecord) => {
        setSelectedObject({ id: relation.id, type: "EDGE" });
    };
    const applyFilters = () =>
        setQuery({ ...filters, pageNo: DEFAULT_PAGE_NO, pageSize: query.pageSize });
    const resetFilters = () => {
        setFilters(DEFAULT_FILTERS);
        setQuery({ ...DEFAULT_FILTERS, pageNo: DEFAULT_PAGE_NO, pageSize: query.pageSize });
    };
    const changePage = (pageNo: number, pageSize: number) =>
        setQuery((currentQuery) => ({
            ...currentQuery,
            pageNo,
            pageSize
        }));

    const nodeColumns = useMemo<KuzhambuTableColumn<GraphGovernanceNodeRecord>[]>(
        () => [
            {
                dataIndex: "name",
                key: "name",
                render: (name, node) => name || node.id,
                title: "节点"
            },
            { dataIndex: "nodeType", key: "nodeType", title: "类型", width: 108 },
            { dataIndex: "source", key: "source", title: "来源", width: 96 },
            {
                key: "actions",
                options: (node) => [
                    {
                        key: "view",
                        onClick: () => selectNode(node),
                        text: "查看",
                        testId: `knowledge-graph-governance-view-node-${node.id}`
                    }
                ]
            }
        ],
        []
    );
    const relationColumns = useMemo<KuzhambuTableColumn<GraphGovernanceRelationRecord>[]>(
        () => [
            {
                dataIndex: "relationType",
                key: "relationType",
                render: (type, relation) => type || relation.id,
                title: "关系"
            },
            { dataIndex: "source", key: "source", title: "来源", width: 96 },
            { dataIndex: "status", key: "status", title: "状态", width: 96 },
            {
                key: "actions",
                options: (relation) => [
                    {
                        key: "view",
                        onClick: () => selectRelation(relation),
                        text: "查看",
                        testId: `knowledge-graph-governance-view-relation-${relation.id}`
                    }
                ]
            }
        ],
        []
    );

    if (!canViewGraph) {
        return (
            <KuzhambuPage
                className="graph-governance-page"
                description="需要知识图谱查看权限。"
                title="图谱治理"
            >
                <KuzhambuAlert title="无权查看图谱治理" type="warning" showIcon />
            </KuzhambuPage>
        );
    }

    return (
        <KuzhambuPage
            className="graph-governance-page"
            description="查询和维护发布空间；只加载当前筛选结果与选中对象的局部关系。"
            title="图谱治理"
        >
            <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                <KuzhambuCard title="发布图谱">
                    <KuzhambuSpace wrap>
                        <Segmented
                            aria-label="切换发布对象"
                            options={OBJECT_TYPE_OPTIONS}
                            value={objectType}
                            onChange={(value) => {
                                setObjectType(value as GraphGovernanceObjectType);
                                setSelectedNode(null);
                                setSelectedObject(null);
                                setExpandedNodeIds([]);
                            }}
                        />
                        <Input
                            aria-label="搜索发布对象"
                            placeholder={objectType === "NODE" ? "搜索节点名称" : "搜索关系类型"}
                            value={filters.keyword}
                            onChange={(event) =>
                                setFilters((current) => ({
                                    ...current,
                                    keyword: event.target.value
                                }))
                            }
                        />
                        <Input
                            aria-label={objectType === "NODE" ? "筛选节点类型" : "筛选关系类型"}
                            placeholder={objectType === "NODE" ? "节点类型" : "关系类型"}
                            value={filters.type}
                            onChange={(event) =>
                                setFilters((current) => ({ ...current, type: event.target.value }))
                            }
                        />
                        <KuzhambuSelect
                            aria-label="筛选对象来源"
                            options={SOURCE_OPTIONS}
                            value={filters.source}
                            onChange={(value) =>
                                setFilters((current) => ({ ...current, source: value }))
                            }
                        />
                        <KuzhambuSelect
                            aria-label="筛选对象状态"
                            options={STATUS_OPTIONS}
                            value={filters.status}
                            onChange={(value) =>
                                setFilters((current) => ({ ...current, status: value }))
                            }
                        />
                        <KuzhambuButton
                            testId="knowledge-graph-governance-apply-filters"
                            type="primary"
                            onClick={applyFilters}
                        >
                            查询
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="knowledge-graph-governance-reset-filters"
                            onClick={resetFilters}
                        >
                            重置
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </KuzhambuCard>
                <Splitter className="graph-governance-work-area">
                    <Splitter.Panel defaultSize="34%" min="280px">
                        <KuzhambuCard title={objectType === "NODE" ? "节点结果" : "关系结果"}>
                            {objectType === "NODE" ? (
                                <KuzhambuTable
                                    ariaLabel="发布节点分页列表"
                                    columns={nodeColumns}
                                    dataSource={nodeRecords as GraphGovernanceNodeRecord[]}
                                    loading={pageQuery.isLoading}
                                    pagination={{
                                        current: query.pageNo,
                                        pageSize: query.pageSize,
                                        total: totalCount,
                                        onChange: changePage
                                    }}
                                    rowKey="id"
                                />
                            ) : (
                                <KuzhambuTable
                                    ariaLabel="发布关系分页列表"
                                    columns={relationColumns}
                                    dataSource={relationRecords as GraphGovernanceRelationRecord[]}
                                    loading={pageQuery.isLoading}
                                    pagination={{
                                        current: query.pageNo,
                                        pageSize: query.pageSize,
                                        total: totalCount,
                                        onChange: changePage
                                    }}
                                    rowKey="id"
                                />
                            )}
                        </KuzhambuCard>
                    </Splitter.Panel>
                    <Splitter.Panel min="420px">
                        <KuzhambuCard title="局部关系图" extra={<span>最多 200 个节点</span>}>
                            {selectedNode ? (
                                <KuzhambuSpace
                                    orientation="vertical"
                                    size={12}
                                    style={{ width: "100%" }}
                                >
                                    <KuzhambuGraph
                                        height={420}
                                        nodeList={graphNodes}
                                        spoList={graphItems}
                                    />
                                    <span>
                                        已以“{readNodeName(selectedNode)}”为焦点加载一跳关系。
                                    </span>
                                    {graphNodes.filter((node) => !expandedNodeIds.includes(node.id))
                                        .length ? (
                                        <KuzhambuSpace wrap>
                                            {graphNodes
                                                .filter(
                                                    (node) => !expandedNodeIds.includes(node.id)
                                                )
                                                .slice(0, 8)
                                                .map((node) => (
                                                    <KuzhambuButton
                                                        key={node.id}
                                                        testId={`knowledge-graph-governance-expand-${node.id}`}
                                                        onClick={() =>
                                                            setExpandedNodeIds((current) => [
                                                                ...current,
                                                                node.id
                                                            ])
                                                        }
                                                    >
                                                        展开 {node.label}
                                                    </KuzhambuButton>
                                                ))}
                                        </KuzhambuSpace>
                                    ) : null}
                                </KuzhambuSpace>
                            ) : (
                                <KuzhambuAlert
                                    title="从左侧选择一个节点后查看局部关系"
                                    type="info"
                                    showIcon
                                />
                            )}
                        </KuzhambuCard>
                    </Splitter.Panel>
                </Splitter>
            </KuzhambuSpace>
            <GovernanceDetailDrawer
                objectId={selectedObject?.id}
                objectType={selectedObject?.type}
                onClose={() => setSelectedObject(null)}
            />
        </KuzhambuPage>
    );
};
