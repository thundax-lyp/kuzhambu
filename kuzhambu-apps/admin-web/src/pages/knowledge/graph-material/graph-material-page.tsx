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
    GraphMaterialRecord
} from "./graph-material-types";
import "./graph-material-page.css";

const getErrorMessage = (error: unknown) =>
    error instanceof Error ? error.message : "请稍后重试。";

const EMPTY_MATERIAL_RECORDS: GraphMaterialListRecord[] = [];
const SEARCH_DEBOUNCE_MS = 500;
const ROOT_CATALOG_KEY = "all";
const CATALOG_PAGE_SIZE = 500;

const SOURCE_TYPE_LABELS: Readonly<Record<string, string>> = {
    MING_CUSTOMS: "明代风俗",
    SANCAI_ENTRY: "三才图会",
    WANGQI_DOCUMENT: "王祺文献"
};

const readSourceTypeLabel = (contentType: string) => SOURCE_TYPE_LABELS[contentType] || contentType;

const normalizeKeyword = (value: string) => {
    const keyword = value.trim();
    return keyword || undefined;
};

const sanitizeCatalogKeyPart = (value: string) => encodeURIComponent(value);

const buildMaterialCatalogNodes = (records: GraphMaterialListRecord[]): MaterialCatalogNode[] => {
    const root: MaterialCatalogNode = {
        key: ROOT_CATALOG_KEY,
        nodeType: "all",
        title: "全部素材"
    };
    const typeNodes = new Map<string, MaterialCatalogNode>();

    records.forEach((record) => {
        const { contentType } = record.source;
        const typeKey = `type:${sanitizeCatalogKeyPart(contentType)}`;
        let typeNode = typeNodes.get(typeKey);
        if (!typeNode) {
            typeNode = {
                children: [],
                contentType,
                key: typeKey,
                nodeType: "contentType",
                title: readSourceTypeLabel(contentType)
            };
            typeNodes.set(typeKey, typeNode);
        }

        const categoryCode = record.source.category?.trim();
        const volumeCode = record.source.volume?.trim();
        if (!categoryCode) {
            return;
        }

        const categoryKey = `${typeKey}:category:${sanitizeCatalogKeyPart(categoryCode)}`;
        let categoryNode = typeNode.children?.find((node) => node.key === categoryKey);
        if (!categoryNode) {
            categoryNode = {
                categoryCode,
                children: [],
                contentType,
                key: categoryKey,
                nodeType: "category",
                title: categoryCode
            };
            typeNode.children?.push(categoryNode);
        }

        if (!volumeCode) {
            return;
        }

        const volumeKey = `${categoryKey}:volume:${sanitizeCatalogKeyPart(volumeCode)}`;
        if (!categoryNode.children?.some((node) => node.key === volumeKey)) {
            categoryNode.children?.push({
                categoryCode,
                contentType,
                key: volumeKey,
                nodeType: "volume",
                title: volumeCode,
                volumeCode
            });
        }
    });

    return [
        {
            ...root,
            children: Array.from(typeNodes.values())
        }
    ];
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

const collectExpandableCatalogKeys = (nodes: MaterialCatalogNode[]): string[] =>
    nodes.flatMap((node) => [
        ...(node.children?.length ? [node.key] : []),
        ...collectExpandableCatalogKeys(node.children || [])
    ]);

const toCatalogQuery = (
    node: MaterialCatalogNode,
    keyword: string | null,
    pageSize: number
): GraphMaterialPageQuery => ({
    categoryCode: node.categoryCode,
    contentType: node.contentType,
    keyword: keyword ?? undefined,
    pageNo: DEFAULT_PAGE_NO,
    pageSize,
    volumeCode: node.volumeCode
});

const isSamePageQuery = (left: GraphMaterialPageQuery, right: GraphMaterialPageQuery) =>
    left.categoryCode === right.categoryCode &&
    left.contentType === right.contentType &&
    left.keyword === right.keyword &&
    left.pageNo === right.pageNo &&
    left.pageSize === right.pageSize &&
    left.volumeCode === right.volumeCode;

const isLeafCatalogNode = (node?: MaterialCatalogNode) => {
    return Boolean(
        node &&
        (node.nodeType === "category" || node.nodeType === "volume") &&
        (!node.children || node.children.length === 0)
    );
};

export const GraphMaterialPage = () => {
    const navigate = useNavigate();
    const canViewGraph = hasPermission("knowledge:graph:view");
    const canEditGraph = hasPermission("knowledge:graph:edit");
    const [searchText, setSearchText] = useState("");
    const [appliedKeyword, setAppliedKeyword] = useState<string | null>(null);
    const [selectedCatalogKey, setSelectedCatalogKey] = useState(ROOT_CATALOG_KEY);
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
        queryFn: () =>
            service.pageMaterials({
                pageNo: DEFAULT_PAGE_NO,
                pageSize: CATALOG_PAGE_SIZE
            }),
        queryKey: ["knowledge", "graph-material", "catalog"]
    });
    const catalogRecords = materialCatalogQuery.data?.records ?? EMPTY_MATERIAL_RECORDS;
    const catalogNodes = useMemo(() => buildMaterialCatalogNodes(catalogRecords), [catalogRecords]);
    const defaultCatalogExpandedKeys = useMemo(
        () => collectExpandableCatalogKeys(catalogNodes),
        [catalogNodes]
    );
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
                        onClick={() => void materialCatalogQuery.refetch()}
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
                                    ? materialCatalogQuery.refetch()
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
                            onRefresh={() => void materialCatalogQuery.refetch()}
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
