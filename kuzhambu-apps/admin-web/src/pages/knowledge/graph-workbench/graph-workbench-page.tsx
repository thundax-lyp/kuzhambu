import { useQuery } from "@tanstack/react-query";
import { Checkbox, Input, Tag } from "antd";
import type { TablePaginationConfig } from "antd";
import { useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuPage,
    KuzhambuSelect,
    KuzhambuSpace,
    KuzhambuTable
} from "@/components";
import type { KuzhambuTableColumn } from "@/components";
import { normalizeId } from "@/types/id";

import * as service from "./graph-workbench-service";
import type {
    GraphPublishedAdjacencyRecord,
    GraphPublishedNodeRecord
} from "./graph-workbench-types";
import "./graph-workbench-page.css";

const NODE_TYPE_OPTIONS = [
    { label: "全部类型", value: "" },
    { label: "人物", value: "PERSON" },
    { label: "地点", value: "PLACE" },
    { label: "作品", value: "WORK" },
    { label: "事件", value: "EVENT" },
    { label: "器物", value: "OBJECT" },
    { label: "概念", value: "CONCEPT" }
];

const STATUS_OPTIONS = [
    { label: "全部状态", value: "" },
    { label: "有效", value: "ACTIVE" },
    { label: "已删除", value: "DELETED" }
];

const SOURCE_OPTIONS = [
    { label: "全部来源", value: "" },
    { label: "素材应用", value: "MATERIAL" },
    { label: "人工维护", value: "MANUAL" }
];

interface GraphAdjacencyFilters {
    subjectKeyword: string;
    subjectType: string;
    subjectStatus: string;
    relationType: string;
    relationStatus: string;
    relationSource: string;
    objectKeyword: string;
    objectType: string;
    includeIsolated: boolean;
}

const DEFAULT_FILTERS: GraphAdjacencyFilters = {
    subjectKeyword: "",
    subjectType: "",
    subjectStatus: "ACTIVE",
    relationType: "",
    relationStatus: "ACTIVE",
    relationSource: "",
    objectKeyword: "",
    objectType: "",
    includeIsolated: true
};

const DEFAULT_PAGE_SIZE = 20;

const readNodeLabel = (node?: GraphPublishedNodeRecord | null) => {
    if (!node) {
        return "-";
    }
    return node.name || node.id;
};

const readNodeType = (node?: GraphPublishedNodeRecord | null) => {
    if (!node) {
        return "-";
    }
    return node.nodeType || "-";
};

const optionalValue = (value: string) => {
    return value.trim() ? value.trim() : null;
};

const buildQuery = (
    filters: GraphAdjacencyFilters,
    pageNo: number,
    pageSize: number
): service.GraphPublishedAdjacencyQuery => ({
    includeIsolated: filters.includeIsolated,
    objectKeyword: optionalValue(filters.objectKeyword),
    objectType: optionalValue(filters.objectType),
    pageNo,
    pageSize,
    relationSource: optionalValue(filters.relationSource),
    relationStatus: optionalValue(filters.relationStatus),
    relationType: optionalValue(filters.relationType),
    subjectKeyword: optionalValue(filters.subjectKeyword),
    subjectStatus: optionalValue(filters.subjectStatus),
    subjectType: optionalValue(filters.subjectType)
});

const readRelationStatusColor = (status?: string | null) => {
    switch (status) {
        case "ACTIVE":
            return "green";
        case "DELETED":
            return "red";
        default:
            return "default";
    }
};

export const GraphWorkbenchPage = () => {
    const canViewGraph = hasPermission("knowledge:graph:view");
    const [filters, setFilters] = useState<GraphAdjacencyFilters>(DEFAULT_FILTERS);
    const [query, setQuery] = useState(() => buildQuery(DEFAULT_FILTERS, 1, DEFAULT_PAGE_SIZE));

    const adjacencyPageQuery = useQuery({
        enabled: canViewGraph,
        queryFn: () => service.pagePublishedAdjacency(query),
        queryKey: ["knowledge", "graph-workbench", "published-adjacency", query],
        retry: false
    });

    const pageResult = adjacencyPageQuery.data;
    const records = pageResult?.records || [];

    const columns = useMemo<KuzhambuTableColumn<GraphPublishedAdjacencyRecord>[]>(
        () => [
            {
                key: "subjectName",
                render: (_, record) => readNodeLabel(record.subject),
                title: "主语"
            },
            {
                key: "subjectType",
                render: (_, record) => readNodeType(record.subject),
                title: "主语类型",
                width: 112
            },
            {
                key: "relationType",
                render: (_, record) => record.relation?.relationType || "-",
                title: "谓词",
                width: 144
            },
            {
                key: "objectName",
                render: (_, record) => readNodeLabel(record.object),
                title: "宾语"
            },
            {
                key: "objectType",
                render: (_, record) => readNodeType(record.object),
                title: "宾语类型",
                width: 112
            },
            {
                key: "relationStatus",
                render: (_, record) =>
                    record.relation ? (
                        <Tag color={readRelationStatusColor(record.relation.status)}>
                            {record.relation.status || "-"}
                        </Tag>
                    ) : (
                        <Tag>孤立节点</Tag>
                    ),
                title: "关系状态",
                width: 120
            },
            {
                key: "source",
                render: (_, record) => record.relation?.source || record.subject.source || "-",
                title: "来源",
                width: 112
            }
        ],
        []
    );

    const applyFilters = () => {
        setQuery(buildQuery(filters, 1, query.pageSize || DEFAULT_PAGE_SIZE));
    };

    const resetFilters = () => {
        setFilters(DEFAULT_FILTERS);
        setQuery(buildQuery(DEFAULT_FILTERS, 1, DEFAULT_PAGE_SIZE));
    };

    const handleTableChange = (pagination: TablePaginationConfig) => {
        setQuery((currentQuery) => ({
            ...currentQuery,
            pageNo: pagination.current || 1,
            pageSize: pagination.pageSize || currentQuery.pageSize || DEFAULT_PAGE_SIZE
        }));
    };

    if (!canViewGraph) {
        return (
            <KuzhambuPage
                className="graph-workbench-page knowledge-graph-workbench-page"
                description="需要知识图谱查看权限。"
                title="图谱工作台"
            >
                <KuzhambuAlert title="无权查看图谱工作台" type="warning" showIcon />
            </KuzhambuPage>
        );
    }

    return (
        <KuzhambuPage
            className="graph-workbench-page knowledge-graph-workbench-page"
            description="按主语、谓词、宾语查看正式图当前态的一跳关系，包含暂无关系的孤立节点。"
            title="图谱工作台"
        >
            <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                <KuzhambuSpace wrap>
                    <Input
                        aria-label="筛选主语关键词"
                        className="graph-workbench-filter-input"
                        placeholder="主语名称"
                        value={filters.subjectKeyword}
                        onChange={(event) =>
                            setFilters((current) => ({
                                ...current,
                                subjectKeyword: event.target.value
                            }))
                        }
                    />
                    <KuzhambuSelect
                        aria-label="筛选主语类型"
                        className="graph-workbench-filter-select"
                        options={NODE_TYPE_OPTIONS}
                        value={filters.subjectType}
                        onChange={(value) =>
                            setFilters((current) => ({ ...current, subjectType: value }))
                        }
                    />
                    <Input
                        aria-label="筛选谓词"
                        className="graph-workbench-filter-input"
                        placeholder="谓词"
                        value={filters.relationType}
                        onChange={(event) =>
                            setFilters((current) => ({
                                ...current,
                                relationType: event.target.value
                            }))
                        }
                    />
                    <Input
                        aria-label="筛选宾语关键词"
                        className="graph-workbench-filter-input"
                        placeholder="宾语名称"
                        value={filters.objectKeyword}
                        onChange={(event) =>
                            setFilters((current) => ({
                                ...current,
                                objectKeyword: event.target.value
                            }))
                        }
                    />
                    <KuzhambuSelect
                        aria-label="筛选宾语类型"
                        className="graph-workbench-filter-select"
                        options={NODE_TYPE_OPTIONS}
                        value={filters.objectType}
                        onChange={(value) =>
                            setFilters((current) => ({ ...current, objectType: value }))
                        }
                    />
                    <KuzhambuSelect
                        aria-label="筛选主语状态"
                        className="graph-workbench-filter-select"
                        options={STATUS_OPTIONS}
                        value={filters.subjectStatus}
                        onChange={(value) =>
                            setFilters((current) => ({ ...current, subjectStatus: value }))
                        }
                    />
                    <KuzhambuSelect
                        aria-label="筛选关系状态"
                        className="graph-workbench-filter-select"
                        options={STATUS_OPTIONS}
                        value={filters.relationStatus}
                        onChange={(value) =>
                            setFilters((current) => ({ ...current, relationStatus: value }))
                        }
                    />
                    <KuzhambuSelect
                        aria-label="筛选关系来源"
                        className="graph-workbench-filter-select"
                        options={SOURCE_OPTIONS}
                        value={filters.relationSource}
                        onChange={(value) =>
                            setFilters((current) => ({ ...current, relationSource: value }))
                        }
                    />
                    <Checkbox
                        checked={filters.includeIsolated}
                        onChange={(event) =>
                            setFilters((current) => ({
                                ...current,
                                includeIsolated: event.target.checked
                            }))
                        }
                    >
                        包含孤立节点
                    </Checkbox>
                    <KuzhambuButton
                        testId="knowledge-graph-workbench-apply-filters-button"
                        type="primary"
                        onClick={applyFilters}
                    >
                        查询
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-graph-workbench-reset-filters-button"
                        onClick={resetFilters}
                    >
                        重置
                    </KuzhambuButton>
                </KuzhambuSpace>
                <KuzhambuTable<GraphPublishedAdjacencyRecord>
                    aria-label="知识图谱单跳邻接表"
                    columns={columns}
                    dataSource={records}
                    loading={adjacencyPageQuery.isLoading}
                    onChange={handleTableChange}
                    pagination={{
                        current: pageResult?.pageNo || query.pageNo || 1,
                        pageSize: pageResult?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE,
                        total: pageResult?.totalCount || pageResult?.count || 0
                    }}
                    rowKey={(record) =>
                        `${normalizeId(record.subject.id)}-${normalizeId(record.relation?.id || "isolated")}`
                    }
                />
            </KuzhambuSpace>
        </KuzhambuPage>
    );
};
