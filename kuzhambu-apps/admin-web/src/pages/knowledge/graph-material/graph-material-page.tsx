import { useMutation, useQuery } from "@tanstack/react-query";
import { ReloadOutlined, SearchOutlined } from "@ant-design/icons";
import { Input, Splitter } from "antd";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuAlert, KuzhambuButton, KuzhambuPage, KuzhambuSpace } from "@/components";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { MaterialCatalogPanel } from "./material-catalog-panel";
import { MaterialDetailDrawer } from "./material-detail-drawer";
import { MaterialTable } from "./material-table";
import * as service from "./graph-material-service";
import type { GraphMaterialPageQuery } from "./graph-material-service";
import type {
    GraphMaterialDrawerSection,
    GraphMaterialListRecord,
    MaterialCatalogNode,
    GraphMaterialTreeNodeRecord,
    GraphMaterialRecord
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
    return {
        expandedKeys: [],
        nodes: rootRecords.map(toCatalogNode)
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
    const navigate = useNavigate();
    const canViewGraph = hasPermission("knowledge:graph:view");
    const canEditGraph = hasPermission("knowledge:graph:edit");
    const [searchText, setSearchText] = useState("");
    const [appliedKeyword, setAppliedKeyword] = useState<string | null>(null);
    const [selectedCatalogKey, setSelectedCatalogKey] = useState(ROOT_CATALOG_KEY);
    const [loadedCatalogNodes, setLoadedCatalogNodes] = useState<MaterialCatalogNode[] | null>(
        null
    );
    const [catalogExpandedKeys, setCatalogExpandedKeys] = useState<string[] | null>(null);
    const [activeMaterial, setActiveMaterial] = useState<GraphMaterialRecord | null>(null);
    const [activeMaterialSection, setActiveMaterialSection] =
        useState<GraphMaterialDrawerSection>("OVERVIEW");
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
    const materialDetailQuery = useQuery({
        enabled: activeMaterial !== null,
        queryFn: () => {
            if (!activeMaterial) {
                throw new Error("未选择素材");
            }
            return service.getMaterial({ contentRef: activeMaterial.contentRef });
        },
        queryKey: ["knowledge", "graph-material", "detail", activeMaterial?.contentRef]
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
    const batchExtractionMutation = useMutation({
        mutationFn: service.createBatchExtraction,
        onSuccess: () => {
            void materialPageQuery.refetch();
        }
    });
    const openMaterialDetailDrawer = (material: GraphMaterialRecord) => {
        setActiveMaterial(material);
        setActiveMaterialSection("OVERVIEW");
    };
    const closeMaterialDetailDrawer = () => {
        setActiveMaterial(null);
        setActiveMaterialSection("OVERVIEW");
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
                    {!selectedLeafCatalogNode ? (
                        <div
                            className="graph-material-list-placeholder"
                            aria-label="图谱素材列表占位"
                        >
                            请选择左侧目录叶子节点查看素材列表
                        </div>
                    ) : !isInitialError ? (
                        <MaterialTable
                            canOpenMaterial={canViewGraph}
                            canExtractMaterial={canEditGraph}
                            canViewTasks={canViewGraph}
                            dataSource={records}
                            loading={materialPageQuery.isLoading}
                            onOpenMaterial={openMaterialDetailDrawer}
                            onExtract={(contentRef) =>
                                batchExtractionMutation.mutateAsync({ contentRefs: [contentRef] })
                            }
                            onViewTasks={navigate}
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
                    ) : null}
                </Splitter.Panel>
            </Splitter>
            <MaterialDetailDrawer
                activeSection={activeMaterialSection}
                detail={materialDetailQuery.data ?? null}
                error={materialDetailQuery.error}
                loading={materialDetailQuery.isFetching}
                material={activeMaterial}
                open={activeMaterial !== null}
                onClose={closeMaterialDetailDrawer}
                onRetry={() => void materialDetailQuery.refetch()}
                onDeletePrecheck={(contentRef) => service.precheckDeletion({ contentRef })}
                onPublish={async (detail) => {
                    if (!detail.material?.lockVersion) {
                        throw new Error("素材缺少锁版本，无法发布。");
                    }
                    const preview = await service.previewPublication({
                        contentRef: detail.material.contentRef
                    });
                    if (!preview.publishable) {
                        throw new Error(preview.issues[0]?.message ?? "发布预检未通过。");
                    }
                    await service.publishMaterial({
                        conflictDecisions: [],
                        contentRef: preview.materialRef,
                        materialLockVersion: preview.materialLockVersion,
                        previewToken: preview.previewToken
                    });
                    await Promise.all([materialPageQuery.refetch(), materialDetailQuery.refetch()]);
                }}
                onWithdraw={async (detail) => {
                    if (!detail.material?.lockVersion) {
                        throw new Error("素材缺少锁版本，无法撤回。");
                    }
                    await service.previewWithdrawal({ contentRef: detail.material.contentRef });
                    await service.withdrawMaterial({
                        contentRef: detail.material.contentRef,
                        materialLockVersion: detail.material.lockVersion
                    });
                    await Promise.all([materialPageQuery.refetch(), materialDetailQuery.refetch()]);
                }}
                onSectionChange={setActiveMaterialSection}
            />
        </KuzhambuPage>
    );
};
