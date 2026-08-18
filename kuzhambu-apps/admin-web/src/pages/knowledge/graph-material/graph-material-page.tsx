import { useQuery } from "@tanstack/react-query";
import { ReloadOutlined, SearchOutlined } from "@ant-design/icons";
import { Input, Splitter } from "antd";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuAlert, KuzhambuButton, KuzhambuPage, KuzhambuSpace } from "@/components";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { MaterialCatalogPanel } from "./material-catalog-panel";
import { MaterialListPanel } from "./material-list-panel";
import * as service from "./graph-material-service";
import type { GraphMaterialPageQuery } from "./graph-material-service";
import type {
    GraphMaterialListRecord,
    MaterialCatalogNode,
    GraphMaterialTreeNodeRecord
} from "./graph-material-types";
import "./graph-material-page.css";

const getErrorMessage = (error: unknown) =>
    error instanceof Error ? error.message : "请稍后重试。";

const EMPTY_MATERIAL_RECORDS: GraphMaterialListRecord[] = [];
const EMPTY_CATALOG_NODES: MaterialCatalogNode[] = [];
const SEARCH_DEBOUNCE_MS = 500;
const ROOT_CATALOG_KEY = "root";

const decodeCatalogNodeIdPart = (value: string) => {
    try {
        return decodeURIComponent(value.replaceAll("+", " "));
    } catch {
        return value;
    }
};

const normalizeKeyword = (value: string) => {
    const keyword = value.trim();
    return keyword || undefined;
};

const toCatalogNode = (record: GraphMaterialTreeNodeRecord): MaterialCatalogNode => ({
    children: record.leaf ? undefined : [],
    key: record.id,
    leaf: record.leaf,
    nodeType: record.nodeType,
    title: record.title
});

const loadInitialCatalog = async () => {
    const rootRecords = await service.listMaterialTree({ parentId: ROOT_CATALOG_KEY });
    const rootNodes = rootRecords.map(toCatalogNode);
    const firstLevelCatalogNodes = rootNodes.filter((node) => !node.leaf);
    const firstLevelChildren = await Promise.all(
        firstLevelCatalogNodes.map(async (node) => ({
            children: (await service.listMaterialTree({ parentId: node.key })).map(toCatalogNode),
            key: node.key
        }))
    );
    const childrenByParentKey = new Map(
        firstLevelChildren.map(({ children, key }) => [key, children])
    );
    return {
        expandedKeys: firstLevelCatalogNodes.map((node) => node.key),
        nodes: rootNodes.map((node) => ({
            ...node,
            children: childrenByParentKey.get(node.key) ?? node.children
        }))
    };
};

const findCatalogNode = (
    nodes: MaterialCatalogNode[],
    key: string
): MaterialCatalogNode | undefined => {
    for (const node of nodes) {
        if (node.key === key) {
            return node;
        }
        const child = findCatalogNode(node.children || [], key);
        if (child) {
            return child;
        }
    }
    return undefined;
};

const attachCatalogChildren = (
    nodes: MaterialCatalogNode[],
    parentKey: string,
    children: MaterialCatalogNode[]
): MaterialCatalogNode[] =>
    nodes.map((node) => {
        if (node.key === parentKey) {
            return { ...node, children };
        }
        if (!node.children?.length) {
            return node;
        }
        return { ...node, children: attachCatalogChildren(node.children, parentKey, children) };
    });

const toCatalogQuery = (
    node: MaterialCatalogNode,
    keyword: string | null,
    pageSize: number
): GraphMaterialPageQuery => {
    const parts = node.key.split(":");
    const query: GraphMaterialPageQuery = {
        keyword: keyword ?? undefined,
        pageNo: DEFAULT_PAGE_NO,
        pageSize
    };
    if (parts.length >= 2 && parts[0] === "type") {
        query.contentType = decodeCatalogNodeIdPart(parts[1]);
    }
    if ((node.nodeType === "category" || node.nodeType === "volume") && parts[2] === "category") {
        query.categoryCode = decodeCatalogNodeIdPart(parts[3] ?? "");
    }
    if (node.nodeType === "volume" && parts[4] === "volume") {
        query.volumeCode = decodeCatalogNodeIdPart(parts[5] ?? "");
    }
    return query;
};

const isSamePageQuery = (left: GraphMaterialPageQuery, right: GraphMaterialPageQuery) =>
    left.categoryCode === right.categoryCode &&
    left.contentType === right.contentType &&
    left.keyword === right.keyword &&
    left.pageNo === right.pageNo &&
    left.pageSize === right.pageSize &&
    left.volumeCode === right.volumeCode;

const isLeafCatalogNode = (node?: MaterialCatalogNode) => Boolean(node?.leaf);

export const GraphMaterialPage = () => {
    const canViewGraph = hasPermission("knowledge:graph:view");
    const canEditGraph = hasPermission("knowledge:graph:edit");
    const [searchText, setSearchText] = useState("");
    const [appliedKeyword, setAppliedKeyword] = useState<string | null>(null);
    const [selectedCatalogKey, setSelectedCatalogKey] = useState(ROOT_CATALOG_KEY);
    const [loadedCatalogNodes, setLoadedCatalogNodes] = useState<MaterialCatalogNode[] | null>(
        null
    );
    const [catalogExpandedKeys, setCatalogExpandedKeys] = useState<string[] | null>(null);
    const [query, setQuery] = useState<GraphMaterialPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const materialCatalogQuery = useQuery({
        enabled: canViewGraph,
        queryFn: loadInitialCatalog,
        queryKey: ["knowledge", "graph-material", "catalog"]
    });
    const catalogNodes = useMemo(
        () => loadedCatalogNodes ?? materialCatalogQuery.data?.nodes ?? EMPTY_CATALOG_NODES,
        [loadedCatalogNodes, materialCatalogQuery.data?.nodes]
    );
    const defaultCatalogExpandedKeys = materialCatalogQuery.data?.expandedKeys ?? [];
    const visibleCatalogExpandedKeys = catalogExpandedKeys ?? defaultCatalogExpandedKeys;
    const selectedCatalogNode = useMemo(
        () => findCatalogNode(catalogNodes, selectedCatalogKey),
        [catalogNodes, selectedCatalogKey]
    );
    const selectedLeafCatalogNode = isLeafCatalogNode(selectedCatalogNode)
        ? selectedCatalogNode
        : undefined;
    const materialPageQuery = useQuery({
        enabled: canViewGraph && selectedLeafCatalogNode !== undefined,
        queryFn: () => service.pageMaterials(query),
        queryKey: ["knowledge", "graph-material", "page", query]
    });
    const pageResult = materialPageQuery.data;
    const records = pageResult?.records ?? EMPTY_MATERIAL_RECORDS;
    const totalCount = pageResult?.totalCount ?? pageResult?.count ?? 0;
    const isInitialError = materialPageQuery.isError && records.length === 0;

    useEffect(() => {
        const timeoutId = window.setTimeout(() => {
            const nextKeyword = normalizeKeyword(searchText) ?? null;
            setAppliedKeyword(nextKeyword);
            if (selectedLeafCatalogNode) {
                setQuery((currentQuery) => {
                    const nextQuery = toCatalogQuery(
                        selectedLeafCatalogNode,
                        nextKeyword,
                        currentQuery.pageSize || DEFAULT_PAGE_SIZE
                    );
                    return isSamePageQuery(currentQuery, nextQuery) ? currentQuery : nextQuery;
                });
            }
        }, SEARCH_DEBOUNCE_MS);
        return () => window.clearTimeout(timeoutId);
    }, [searchText, selectedLeafCatalogNode]);

    const updateQuery = (nextQuery: GraphMaterialPageQuery) => {
        setQuery(nextQuery);
    };
    const refreshCatalog = async () => {
        setLoadedCatalogNodes(null);
        setCatalogExpandedKeys(null);
        await materialCatalogQuery.refetch();
    };
    const loadCatalogChildren = async (node: MaterialCatalogNode) => {
        if (node.leaf || node.children?.length) {
            return;
        }
        const childRecords = await service.listMaterialTree({ parentId: node.key });
        const childNodes = childRecords.map(toCatalogNode);
        setLoadedCatalogNodes((currentNodes) =>
            attachCatalogChildren(
                currentNodes ?? materialCatalogQuery.data?.nodes ?? [],
                node.key,
                childNodes
            )
        );
    };
    if (!canViewGraph) {
        return (
            <KuzhambuPage
                className="graph-material-page"
                description="需要知识图谱查看权限。"
                title="图谱素材库"
            >
                <KuzhambuAlert title="无权查看图谱素材库" type="warning" showIcon />
            </KuzhambuPage>
        );
    }

    return (
        <KuzhambuPage
            className="graph-material-page"
            description="按素材目录查看图谱发布状态、抽取状态和草稿入口。"
            title="图谱素材库"
            actions={
                <KuzhambuSpace className="graph-material-page-actions">
                    <Input
                        allowClear
                        aria-label="搜索图谱素材"
                        className="graph-material-page-search"
                        placeholder="搜索素材标题或摘要"
                        prefix={<SearchOutlined />}
                        value={searchText}
                        onChange={(event) => setSearchText(event.target.value)}
                    />
                    <KuzhambuButton
                        testId="knowledge-graph-material-refresh-button"
                        icon={<ReloadOutlined />}
                        onClick={() => void refreshCatalog()}
                    >
                        刷新
                    </KuzhambuButton>
                </KuzhambuSpace>
            }
        >
            {materialCatalogQuery.isError || materialPageQuery.isError ? (
                <KuzhambuAlert
                    className="graph-material-alert"
                    action={
                        <KuzhambuButton
                            testId="knowledge-graph-material-retry-page-button"
                            size="small"
                            onClick={() =>
                                void (materialCatalogQuery.isError
                                    ? refreshCatalog()
                                    : materialPageQuery.refetch())
                            }
                        >
                            重试加载素材
                        </KuzhambuButton>
                    }
                    description={getErrorMessage(
                        materialCatalogQuery.error ?? materialPageQuery.error
                    )}
                    title={materialCatalogQuery.isError ? "素材目录加载失败" : "素材列表加载失败"}
                    type="error"
                    showIcon
                />
            ) : null}
            <Splitter
                className="graph-material-work-area"
                classNames={{
                    dragger: "graph-material-work-area-dragger"
                }}
            >
                <Splitter.Panel
                    className="graph-material-work-area-panel"
                    defaultSize={320}
                    min={260}
                    max={520}
                >
                    <aside className="graph-material-catalog-panel">
                        <MaterialCatalogPanel
                            expandedKeys={visibleCatalogExpandedKeys}
                            isLoading={materialCatalogQuery.isLoading}
                            isRefreshing={materialCatalogQuery.isFetching}
                            nodes={catalogNodes}
                            selectedKey={selectedCatalogKey}
                            onExpandedKeysChange={setCatalogExpandedKeys}
                            onLoadChildren={loadCatalogChildren}
                            onRefresh={() => void refreshCatalog()}
                            onSelectNode={(node) => {
                                setSelectedCatalogKey(node.key);
                                if (isLeafCatalogNode(node)) {
                                    updateQuery(
                                        toCatalogQuery(
                                            node,
                                            appliedKeyword,
                                            query.pageSize || DEFAULT_PAGE_SIZE
                                        )
                                    );
                                } else {
                                    setCatalogExpandedKeys((currentKeys) =>
                                        Array.from(
                                            new Set([
                                                ...(currentKeys ?? visibleCatalogExpandedKeys),
                                                node.key
                                            ])
                                        )
                                    );
                                    void loadCatalogChildren(node);
                                }
                            }}
                        />
                    </aside>
                </Splitter.Panel>
                <Splitter.Panel className="graph-material-work-panel">
                    <MaterialListPanel
                        canExtractMaterial={canEditGraph}
                        dataSource={records}
                        loading={materialPageQuery.isLoading}
                        onRefreshMaterials={() => materialPageQuery.refetch()}
                        showPlaceholder={!selectedLeafCatalogNode || isInitialError}
                        pagination={{
                            current: query.pageNo || DEFAULT_PAGE_NO,
                            pageSize: query.pageSize || DEFAULT_PAGE_SIZE,
                            total: totalCount,
                            onChange: (pageNo, pageSize) =>
                                updateQuery({
                                    ...query,
                                    pageNo,
                                    pageSize
                                })
                        }}
                    />
                </Splitter.Panel>
            </Splitter>
        </KuzhambuPage>
    );
};
