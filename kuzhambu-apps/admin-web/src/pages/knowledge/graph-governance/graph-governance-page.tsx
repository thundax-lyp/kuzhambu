import { useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import { Input, Splitter } from "antd";
import { useCallback, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuGraph,
    KuzhambuPage,
    KuzhambuSelect,
    KuzhambuSpace,
    KuzhambuTable,
    KuzhambuTag
} from "@/components";
import type {
    KuzhambuGraphNodeItem,
    KuzhambuGraphSpoItem,
    KuzhambuTableColumn
} from "@/components";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { GovernanceDetailDrawer } from "./governance-detail-drawer";
import { GovernanceDeleteModal } from "./governance-delete-modal";
import { GovernanceEditorModal } from "./governance-editor-modal";
import { GovernanceMergeModal } from "./governance-merge-modal";
import * as service from "./graph-governance-service";
import type {
    GraphGovernanceAdjacencyRecord,
    GraphGovernanceNodeRecord,
    GraphGovernanceRelationRecord
} from "./graph-governance-types";
import "./graph-governance-page.css";

const NODE_TYPE_LABELS: Readonly<Record<string, string>> = {
    ANIMAL: "动物",
    BUILDING: "建筑",
    CELESTIAL_BODY: "天体",
    CONCEPT: "概念",
    DEITY: "神祇",
    DYNASTY: "朝代",
    EVENT: "事件",
    GROUP: "群体",
    MATERIAL: "材料",
    NATURAL_PHENOMENON: "自然现象",
    OBJECT: "器物",
    OFFICE: "官职",
    ORGANIZATION: "组织",
    PERSON: "人物",
    PLACE: "地点",
    PLANT: "植物",
    RITUAL: "仪式",
    WORK: "著作"
};
const NODE_TYPE_OPTIONS = [
    { label: "全部类型", value: "" },
    ...Object.entries(NODE_TYPE_LABELS).map(([value, label]) => ({ label, value }))
];
const RELATION_TYPE_LABELS: Readonly<Record<string, string>> = {
    ANCESTOR_OF: "祖先/后裔",
    ASSOCIATED_WITH: "相关",
    AUTHORED: "撰著",
    CAUSES: "导致/引起",
    COMPILED: "编纂",
    DEPICTS: "描绘",
    DESCRIBES: "记述",
    HOLDS_OFFICE: "任职",
    LOCATED_IN: "位于",
    MADE_OF: "制成材料",
    MEMBER_OF: "隶属/成员",
    MENTIONS: "提及",
    OCCURS_AT: "发生于",
    PARENT_OF: "父母/子女",
    PARTICIPATED_IN: "参与",
    PART_OF: "构成/隶属",
    PRACTICES: "实行/奉行",
    RELATED_TO: "相关",
    RULES: "统治/管辖",
    SPOUSE_OF: "配偶",
    SUCCEEDS: "继承/取代",
    USES: "使用/采用",
    WORSHIPS: "崇祀"
};

interface GraphGovernanceFilters {
    keyword: string;
    type: string;
}

const DEFAULT_FILTERS: GraphGovernanceFilters = {
    keyword: "",
    type: ""
};

const optionalValue = (value: string) => (value.trim() ? value.trim() : null);
const readNodeName = (node?: GraphGovernanceNodeRecord | null) => node?.name || node?.id || "-";
const readRelationName = (relation: GraphGovernanceRelationRecord) =>
    relation.relationType
        ? (RELATION_TYPE_LABELS[relation.relationType] ?? relation.relationType)
        : relation.id;
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

type GovernanceTreeRow = {
    children?: GovernanceTreeRow[];
    id: string;
    kind: "NODE" | "RELATION" | "LOAD_MORE";
    node?: GraphGovernanceNodeRecord;
    relationRecord?: GraphGovernanceAdjacencyRecord;
};

interface RelationPageKey {
    nodeId: string;
    pageNo: number;
}

export const GraphGovernancePage = () => {
    const canViewGraph = hasPermission("knowledge:graph:view");
    const canEditGraph = hasPermission("knowledge:graph:edit");
    const queryClient = useQueryClient();
    const [filters, setFilters] = useState<GraphGovernanceFilters>(DEFAULT_FILTERS);
    const [query, setQuery] = useState({
        ...DEFAULT_FILTERS,
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [localGraphNodes, setLocalGraphNodes] = useState<GraphGovernanceNodeRecord[]>([]);
    const [selectedObject, setSelectedObject] = useState<{
        id: string;
        type: "NODE" | "EDGE";
    } | null>(null);
    const [editorTarget, setEditorTarget] = useState<{
        id: string;
        type: "NODE" | "EDGE";
    } | null>(null);
    const [deleteTarget, setDeleteTarget] = useState<{
        id: string;
        lockVersion?: string | null;
        type: "NODE" | "EDGE";
    } | null>(null);
    const [mergeNode, setMergeNode] = useState<GraphGovernanceNodeRecord | null>(null);
    const [expandedRelationPages, setExpandedRelationPages] = useState<Record<string, number>>({});
    const pageQuery = useQuery({
        enabled: canViewGraph,
        queryFn: () =>
            service.pagePublishedNodes({
                keyword: optionalValue(query.keyword),
                nodeType: optionalValue(query.type),
                pageNo: query.pageNo,
                pageSize: query.pageSize,
                status: "ACTIVE"
            }),
        queryKey: ["knowledge", "graph-governance", "nodes", query]
    });
    const relationPageRequests = useMemo<RelationPageKey[]>(
        () =>
            Object.entries(expandedRelationPages).flatMap(([nodeId, pageCount]) =>
                Array.from({ length: pageCount }, (_, index) => ({
                    nodeId,
                    pageNo: index + 1
                }))
            ),
        [expandedRelationPages]
    );
    const relationPageQueries = useQueries({
        queries: relationPageRequests.map((request) => ({
            queryFn: () =>
                service.pagePublishedAdjacency({
                    includeIsolated: false,
                    pageNo: request.pageNo,
                    pageSize: 20,
                    subjectNodeId: request.nodeId
                }),
            queryKey: [
                "knowledge",
                "graph-governance",
                "node-relations",
                request.nodeId,
                request.pageNo
            ]
        }))
    });
    const localGraphAdjacencyQueries = useQueries({
        queries: localGraphNodes.map((node) => ({
            queryFn: () =>
                service.pagePublishedAdjacency({
                    includeIsolated: false,
                    pageNo: DEFAULT_PAGE_NO,
                    pageSize: 50,
                    subjectNodeId: node.id
                }),
            queryKey: ["knowledge", "graph-governance", "local-graph-adjacency", node.id]
        }))
    });
    const relationsByNode = useMemo(() => {
        const pages = new Map<string, GraphGovernanceAdjacencyRecord[]>();
        relationPageRequests.forEach((request, index) => {
            const records = relationPageQueries[index]?.data?.records;
            if (records) {
                pages.set(request.nodeId, [...(pages.get(request.nodeId) ?? []), ...records]);
            }
        });
        return pages;
    }, [relationPageQueries, relationPageRequests]);
    const lastRelationPageByNode = useMemo(() => {
        const pages = new Map<string, { pageNo: number; totalPage: number }>();
        relationPageRequests.forEach((request, index) => {
            const page = relationPageQueries[index]?.data;
            if (page) {
                pages.set(request.nodeId, { pageNo: page.pageNo, totalPage: page.totalPage });
            }
        });
        return pages;
    }, [relationPageQueries, relationPageRequests]);
    const adjacencyRecords = useMemo(() => {
        const records = new Map<string, GraphGovernanceAdjacencyRecord>();
        localGraphAdjacencyQueries.forEach((query) => {
            query.data?.records.forEach((record) => {
                records.set(record.relation?.id ?? record.subject.id, record);
            });
        });
        return Array.from(records.values());
    }, [localGraphAdjacencyQueries]);
    const graphNodes = useMemo<KuzhambuGraphNodeItem[]>(() => {
        const nodes = new Map<string, KuzhambuGraphNodeItem>();
        localGraphNodes.forEach((node) => {
            nodes.set(node.id, {
                group: "当前对象",
                id: node.id,
                label: readNodeName(node)
            });
        });
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
    }, [adjacencyRecords, localGraphNodes]);
    const graphItems = useMemo(() => toGraphItems(adjacencyRecords), [adjacencyRecords]);
    const nodeRecords = useMemo(() => pageQuery.data?.records ?? [], [pageQuery.data?.records]);
    const totalCount = pageQuery.data?.totalCount ?? pageQuery.data?.count ?? 0;

    const treeRecords = useMemo<GovernanceTreeRow[]>(
        () =>
            nodeRecords.map((node) => {
                const relationRows: GovernanceTreeRow[] = (relationsByNode.get(node.id) ?? []).map(
                    (relationRecord) => ({
                        id: `relation-${relationRecord.relation?.id ?? relationRecord.subject.id}`,
                        kind: "RELATION",
                        relationRecord
                    })
                );
                const lastPage = lastRelationPageByNode.get(node.id);
                if (lastPage && lastPage.pageNo < lastPage.totalPage) {
                    relationRows.push({
                        id: `load-more-${node.id}`,
                        kind: "LOAD_MORE",
                        node
                    });
                }
                return {
                    children: relationRows,
                    id: node.id,
                    kind: "NODE",
                    node
                };
            }),
        [lastRelationPageByNode, nodeRecords, relationsByNode]
    );

    const expandNodeRelations = (expanded: boolean, node: GovernanceTreeRow) => {
        if (expanded && node.kind === "NODE" && node.node) {
            setExpandedRelationPages((current) =>
                current[node.node!.id] ? current : { ...current, [node.node!.id]: 1 }
            );
        }
    };
    const isInLocalGraph = useCallback(
        (nodeId: string) => localGraphNodes.some((localGraphNode) => localGraphNode.id === nodeId),
        [localGraphNodes]
    );
    const toggleLocalGraphNode = useCallback(
        (node: GraphGovernanceNodeRecord) =>
            setLocalGraphNodes((current) =>
                current.some((localGraphNode) => localGraphNode.id === node.id)
                    ? current.filter((localGraphNode) => localGraphNode.id !== node.id)
                    : [...current, node]
            ),
        []
    );
    const loadMoreRelations = (nodeId: string) =>
        setExpandedRelationPages((current) => ({
            ...current,
            [nodeId]: (current[nodeId] ?? 1) + 1
        }));
    const openNodeDetail = (node: GraphGovernanceNodeRecord) =>
        setSelectedObject({ id: node.id, type: "NODE" });
    const selectRelation = (relation: GraphGovernanceRelationRecord) => {
        setSelectedObject({ id: relation.id, type: "EDGE" });
    };
    const openEditor = (id: string, type: "NODE" | "EDGE") => setEditorTarget({ id, type });
    const refreshGovernanceData = () =>
        queryClient.invalidateQueries({ queryKey: ["knowledge", "graph-governance"] });
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

    const treeColumns = useMemo<KuzhambuTableColumn<GovernanceTreeRow>[]>(
        () => [
            {
                key: "name",
                render: (_, record) => {
                    if (record.kind === "RELATION" && record.relationRecord) {
                        return renderRelation(record.relationRecord);
                    }
                    if (record.kind === "LOAD_MORE" && record.node) {
                        return (
                            <KuzhambuButton
                                testId={`knowledge-graph-governance-load-more-relations-${record.node.id}`}
                                onClick={() => loadMoreRelations(record.node!.id)}
                            >
                                继续加载关系
                            </KuzhambuButton>
                        );
                    }
                    if (record.kind === "NODE") {
                        return (
                            <KuzhambuSpace align="center" size={6} wrap={false}>
                                <span>{readNodeName(record.node)}</span>
                                <KuzhambuTag type="neutral">
                                    {NODE_TYPE_LABELS[record.node?.nodeType ?? ""] ??
                                        record.node?.nodeType ??
                                        "-"}
                                </KuzhambuTag>
                            </KuzhambuSpace>
                        );
                    }
                    return null;
                },
                title: "节点 / 关系"
            },
            {
                key: "actions",
                options: (record) => {
                    if (record.kind === "NODE" && record.node) {
                        return [
                            canEditGraph
                                ? {
                                      ariaLabel: `编辑节点 ${readNodeName(record.node)}`,
                                      key: "edit",
                                      onClick: () => openEditor(record.node!.id, "NODE"),
                                      text: "编辑",
                                      testId: `knowledge-graph-governance-edit-node-${record.node.id}`
                                  }
                                : {
                                      ariaLabel: `查看节点 ${readNodeName(record.node)}`,
                                      key: "view",
                                      onClick: () => openNodeDetail(record.node!),
                                      text: "查看",
                                      testId: `knowledge-graph-governance-view-node-${record.node.id}`
                                  },
                            {
                                key: "toggle-local-graph",
                                ariaLabel: `${
                                    isInLocalGraph(record.node.id) ? "移出" : "加入"
                                }局部关系图 ${readNodeName(record.node)}`,
                                onClick: () => toggleLocalGraphNode(record.node!),
                                text: isInLocalGraph(record.node.id) ? "移出" : "加入",
                                testId: `knowledge-graph-governance-toggle-node-${record.node.id}`
                            },
                            ...(canEditGraph
                                ? [
                                      {
                                          ariaLabel: `合并节点 ${readNodeName(record.node)}`,
                                          key: "merge",
                                          onClick: () => setMergeNode(record.node!),
                                          text: "合并",
                                          testId: `knowledge-graph-governance-merge-node-${record.node.id}`
                                      },
                                      {
                                          ariaLabel: `删除节点 ${readNodeName(record.node)}`,
                                          key: "delete",
                                          onClick: () =>
                                              setDeleteTarget({
                                                  id: record.node!.id,
                                                  lockVersion: record.node!.lockVersion,
                                                  type: "NODE"
                                              }),
                                          text: "删除",
                                          type: "danger" as const,
                                          testId: `knowledge-graph-governance-delete-node-${record.node.id}`
                                      }
                                  ]
                                : [])
                        ];
                    }
                    if (record.kind === "RELATION" && record.relationRecord?.relation) {
                        return [
                            canEditGraph
                                ? {
                                      ariaLabel: `编辑关系 ${readRelationName(
                                          record.relationRecord.relation
                                      )}`,
                                      key: "edit",
                                      onClick: () =>
                                          openEditor(record.relationRecord!.relation!.id, "EDGE"),
                                      text: "编辑",
                                      testId: `knowledge-graph-governance-edit-relation-${record.relationRecord.relation.id}`
                                  }
                                : {
                                      ariaLabel: `查看关系 ${readRelationName(
                                          record.relationRecord.relation
                                      )}`,
                                      key: "view",
                                      onClick: () =>
                                          selectRelation(record.relationRecord!.relation!),
                                      text: "查看",
                                      testId: `knowledge-graph-governance-view-relation-${record.relationRecord.relation.id}`
                                  },
                            ...(canEditGraph
                                ? [
                                      {
                                          ariaLabel: `删除关系 ${readRelationName(
                                              record.relationRecord.relation
                                          )}`,
                                          key: "delete",
                                          onClick: () =>
                                              setDeleteTarget({
                                                  id: record.relationRecord!.relation!.id,
                                                  lockVersion:
                                                      record.relationRecord!.relation!.lockVersion,
                                                  type: "EDGE"
                                              }),
                                          text: "删除",
                                          type: "danger" as const,
                                          testId: `knowledge-graph-governance-delete-relation-${record.relationRecord.relation.id}`
                                      }
                                  ]
                                : [])
                        ];
                    }
                    return [];
                }
            }
        ],
        [canEditGraph, isInLocalGraph, toggleLocalGraphNode]
    );
    const renderRelation = (record: GraphGovernanceAdjacencyRecord) => (
        <KuzhambuSpace align="center" size={6} wrap={false}>
            <span>{readNodeName(record.subject)}</span>
            <KuzhambuTag type="info">
                {record.relation ? readRelationName(record.relation) : "-"}
            </KuzhambuTag>
            <span>{readNodeName(record.object)}</span>
        </KuzhambuSpace>
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
                <KuzhambuCard>
                    <KuzhambuSpace wrap>
                        <Input
                            aria-label="搜索节点"
                            placeholder="搜索节点名称"
                            value={filters.keyword}
                            onChange={(event) =>
                                setFilters((current) => ({
                                    ...current,
                                    keyword: event.target.value
                                }))
                            }
                        />
                        <KuzhambuSelect
                            aria-label="筛选节点类型"
                            options={NODE_TYPE_OPTIONS}
                            value={filters.type}
                            onChange={(value) =>
                                setFilters((current) => ({ ...current, type: value }))
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
                    <Splitter.Panel defaultSize="50%" min="280px">
                        <KuzhambuCard title="节点结果">
                            <KuzhambuTable<GovernanceTreeRow>
                                ariaLabel="发布节点关系树"
                                columns={treeColumns}
                                dataSource={treeRecords}
                                loading={pageQuery.isLoading}
                                expandable={{
                                    onExpand: expandNodeRelations,
                                    rowExpandable: (record) => record.kind === "NODE"
                                }}
                                pagination={{
                                    current: query.pageNo,
                                    pageSize: query.pageSize,
                                    total: totalCount,
                                    onChange: changePage
                                }}
                                rowKey="id"
                            />
                        </KuzhambuCard>
                    </Splitter.Panel>
                    <Splitter.Panel>
                        <KuzhambuCard title="局部关系图" extra={<span>最多 200 个节点</span>}>
                            {localGraphNodes.length ? (
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
                                        已加入 {localGraphNodes.length}{" "}
                                        个节点，并加载各节点的一跳关系。
                                    </span>
                                </KuzhambuSpace>
                            ) : (
                                <KuzhambuAlert
                                    title="从左侧节点操作中加入节点后查看局部关系"
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
            <GovernanceEditorModal
                target={editorTarget}
                onCancel={() => setEditorTarget(null)}
                onSaved={refreshGovernanceData}
            />
            <GovernanceDeleteModal
                target={deleteTarget}
                onCancel={() => setDeleteTarget(null)}
                onDeleted={async () => {
                    if (deleteTarget?.type === "NODE") {
                        setLocalGraphNodes((current) =>
                            current.filter((node) => node.id !== deleteTarget.id)
                        );
                    }
                    await refreshGovernanceData();
                }}
            />
            <GovernanceMergeModal
                node={mergeNode}
                onCancel={() => setMergeNode(null)}
                onMerged={async () => {
                    setLocalGraphNodes((current) =>
                        current.filter((node) => node.id !== mergeNode?.id)
                    );
                    await refreshGovernanceData();
                }}
            />
        </KuzhambuPage>
    );
};
