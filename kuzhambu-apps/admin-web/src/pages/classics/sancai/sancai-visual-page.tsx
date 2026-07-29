import {
    ArrowLeftOutlined,
    BookOutlined,
    FileImageOutlined,
    FileTextOutlined,
    FolderOutlined,
    SwapOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Empty, Skeleton, Tag, Tree, Typography } from "antd";
import type { DataNode } from "antd/es/tree";
import type { Key } from "react";
import { useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuModal,
    KuzhambuPage,
    KuzhambuSpace
} from "@/components";
import { SancaiEntryVisualSection } from "./components/sancai-entry-edit-drawer/sancai-entry-visual-section";
import { useSancaiCatalogState } from "./hooks/use-sancai-catalog-state";
import * as entryService from "./sancai-entry-service";
import type {
    SancaiCatalogTreeNode,
    SancaiEntryRecord,
    SancaiVisualAssetRecord
} from "./sancai-types";

import "./sancai-page.css";
import "./sancai-visual-page.css";

const { Paragraph, Text } = Typography;

const readEntryTitle = (entry: SancaiEntryRecord | null | undefined) => {
    if (!entry) {
        return "未选择稿件";
    }
    return entry.title?.trim() || `条目 ${entry.id}`;
};

const readEntrySummary = (entry: SancaiEntryRecord) => {
    return entry.summary?.trim() || entry.originalText?.trim() || "暂无摘要";
};

const statusTagMeta: Record<string, { color: string; label: string }> = {
    ARCHIVED: { color: "default", label: "已下线" },
    DRAFT: { color: "gold", label: "草稿" },
    PUBLISHED: { color: "green", label: "已发布" }
};

const renderStatusTag = (status?: string | null) => {
    const normalizedStatus = status || "UNKNOWN";
    const meta = statusTagMeta[normalizedStatus] ?? {
        color: "blue",
        label: normalizedStatus
    };
    return <Tag color={meta.color}>{meta.label}</Tag>;
};

const toEntryPickerEntryKey = (entryId: string) => `entry:${entryId}`;

const flattenCatalogNodes = (nodes: SancaiCatalogTreeNode[]): SancaiCatalogTreeNode[] => {
    return nodes.flatMap((node) => [node, ...flattenCatalogNodes(node.children || [])]);
};

export const SancaiVisualPage = () => {
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();
    const queryClient = useQueryClient();
    const { message: messageApi } = App.useApp();
    const selectedEntryId = searchParams.get("entryId");
    const [entryPickerOpen, setEntryPickerOpen] = useState(false);
    const [pendingEntry, setPendingEntry] = useState<SancaiEntryRecord | null>(null);
    const {
        actualSelectedKey,
        hasError: hasCatalogError,
        isLoading: isCatalogLoading,
        selectedCategory,
        selectedVolume,
        selectCatalogNode,
        setExpandedKeys,
        treeExpandedKeys,
        treeNodes
    } = useSancaiCatalogState({ enabled: entryPickerOpen });
    const entriesQuery = useQuery({
        queryKey: [
            "classics",
            "sancai",
            "visual",
            "entries",
            selectedCategory?.id,
            selectedVolume?.id
        ],
        queryFn: () =>
            entryService.list({
                categoryId: selectedCategory?.id ?? null,
                volumeId: selectedVolume?.id ?? null,
                sortDirection: "ASC"
            }),
        enabled: entryPickerOpen && Boolean(selectedVolume?.id),
        retry: false
    });
    const entries = useMemo(() => entriesQuery.data || [], [entriesQuery.data]);
    const catalogNodeByKey = useMemo(() => {
        return new Map(flattenCatalogNodes(treeNodes).map((node) => [node.key, node]));
    }, [treeNodes]);
    const entryByPickerKey = useMemo(() => {
        return new Map(entries.map((entry) => [toEntryPickerEntryKey(entry.id), entry]));
    }, [entries]);
    const entryDetailQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "detail", selectedEntryId],
        queryFn: () => entryService.get(selectedEntryId ?? ""),
        enabled: Boolean(selectedEntryId),
        retry: false
    });
    const selectedEntry = entryDetailQuery.data ?? null;
    const updateVisualAssetMutation = useMutation({
        mutationFn: entryService.updateVisualAsset,
        onSuccess: async () => {
            await refreshVisualPageData();
            messageApi.success("三才视觉处理已采纳");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "视觉处理采纳失败");
        }
    });
    const changeCurrentVisualAssetMutation = useMutation({
        mutationFn: entryService.changeCurrentVisualAsset,
        onSuccess: async () => {
            await refreshVisualPageData();
            messageApi.success("当前视觉处理版本已切换");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "视觉处理切换失败");
        }
    });

    const selectEntry = (entry: SancaiEntryRecord) => {
        setSearchParams({ entryId: entry.id });
        setEntryPickerOpen(false);
        setPendingEntry(null);
    };

    const refreshVisualPageData = async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "visual"] }),
            queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] })
        ]);
    };

    const updateVisualAsset = (asset: SancaiVisualAssetRecord) => {
        return updateVisualAssetMutation.mutateAsync({
            visualAssetId: asset.visualAssetId ?? asset.id ?? null,
            entryId: asset.entryId ?? selectedEntry?.id ?? null,
            versionNo: asset.versionNo,
            status: asset.status,
            sourceImageStorageObjectId: asset.sourceImageStorageObjectId,
            generatedImageStorageObjectId: asset.generatedImageStorageObjectId,
            currentUsed: asset.currentUsed,
            textWeight: asset.textWeight,
            imageWeight: asset.imageWeight,
            imageAnalysisMarkdown: asset.imageAnalysisMarkdown,
            fusionDescription: asset.fusionDescription,
            visualDescription: asset.visualDescription,
            generationParamsJson: asset.generationParamsJson
        });
    };

    const switchVisualAsset = (asset: SancaiVisualAssetRecord) => {
        const visualAssetId = asset.visualAssetId ?? asset.id;
        const entryId = asset.entryId ?? selectedEntry?.id;
        if (!visualAssetId || !entryId) {
            return;
        }
        changeCurrentVisualAssetMutation.mutate({ entryId, visualAssetId });
    };

    const entryPickerTreeData = useMemo(() => {
        const entryChildren: DataNode[] = entries.map((entry) => ({
            icon: <FileTextOutlined />,
            key: toEntryPickerEntryKey(entry.id),
            title: readEntryTitle(entry)
        }));
        const buildTreeData = (nodes: SancaiCatalogTreeNode[]): DataNode[] =>
            nodes.map((node) => {
                const isSelectedVolume =
                    node.key === actualSelectedKey && node.nodeType === "volume";
                let children = node.children ? buildTreeData(node.children) : undefined;
                if (isSelectedVolume) {
                    if (entriesQuery.isLoading) {
                        children = [
                            {
                                disabled: true,
                                key: `${node.key}:loading`,
                                title: "加载稿件中"
                            }
                        ];
                    } else if (entryChildren.length) {
                        children = entryChildren;
                    } else {
                        children = [
                            {
                                disabled: true,
                                key: `${node.key}:empty`,
                                title: "暂无稿件"
                            }
                        ];
                    }
                }
                return {
                    children,
                    icon: node.nodeType === "volume" ? <BookOutlined /> : <FolderOutlined />,
                    key: node.key,
                    title: node.title
                };
            });
        return buildTreeData(treeNodes);
    }, [actualSelectedKey, entries, entriesQuery.isLoading, treeNodes]);
    const entryPickerSelectedKeys = [
        pendingEntry ? toEntryPickerEntryKey(pendingEntry.id) : actualSelectedKey
    ].filter(Boolean);
    const selectEntryPickerNode = (keys: Key[]) => {
        const key = String(keys[0] ?? "");
        const entry = entryByPickerKey.get(key);
        if (entry) {
            setPendingEntry(entry);
            return;
        }
        const catalogNode = catalogNodeByKey.get(key);
        if (catalogNode) {
            setPendingEntry(null);
            selectCatalogNode(catalogNode);
            if (catalogNode.nodeType === "volume") {
                setExpandedKeys((keys) => Array.from(new Set([...keys, catalogNode.key])));
            }
        }
    };
    const confirmPendingEntry = () => {
        if (pendingEntry) {
            selectEntry(pendingEntry);
        }
    };

    return (
        <KuzhambuPage
            className="sancai-page sancai-visual-page"
            title={
                <span className="sancai-visual-page-title">
                    三才图会 <small>视觉处理</small>
                </span>
            }
            description="选择稿件和来源图片，完成图片理解、图文融合、视觉描述和生图。"
            actions={
                <KuzhambuSpace className="sancai-page-actions">
                    <KuzhambuButton
                        testId="classics-sancai-visual-back-button"
                        icon={<ArrowLeftOutlined />}
                        onClick={() => navigate("/classics/sancai")}
                    >
                        返回
                    </KuzhambuButton>
                </KuzhambuSpace>
            }
        >
            {entryDetailQuery.isError ? (
                <KuzhambuAlert
                    className="sancai-alert"
                    type="warning"
                    showIcon
                    title="当前稿件加载失败"
                    description="请从稿件表重新进入，或选择其他稿件继续处理。"
                />
            ) : null}
            <section className="sancai-visual-main-panel" aria-label="三才图会视觉处理工作台">
                {selectedEntry ? (
                    <>
                        <div className="sancai-visual-entry-context">
                            <div>
                                <KuzhambuSpace wrap>
                                    <FileImageOutlined />
                                    <Text strong>{readEntryTitle(selectedEntry)}</Text>
                                    {renderStatusTag(selectedEntry.lifecycleStatus)}
                                </KuzhambuSpace>
                                <Paragraph
                                    className="sancai-visual-entry-summary"
                                    type="secondary"
                                    ellipsis={{ rows: 2, expandable: true, symbol: "展开" }}
                                >
                                    {readEntrySummary(selectedEntry)}
                                </Paragraph>
                            </div>
                            <KuzhambuButton
                                testId="classics-sancai-visual-entry-context-switch-button"
                                icon={<SwapOutlined />}
                                onClick={() => setEntryPickerOpen(true)}
                            >
                                选择稿件
                            </KuzhambuButton>
                        </div>
                        <SancaiEntryVisualSection
                            entry={selectedEntry}
                            isUpdatingVisualAsset={updateVisualAssetMutation.isPending}
                            onPreviewStateChange={() => undefined}
                            onRefinementChanged={refreshVisualPageData}
                            onUpdateVisualAsset={updateVisualAsset}
                            onUseVisualAsset={switchVisualAsset}
                        />
                    </>
                ) : (
                    <Empty
                        className="sancai-visual-empty"
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                        description="请先选择要处理的稿件"
                    >
                        <KuzhambuButton
                            testId="classics-sancai-visual-empty-entry-picker-button"
                            type="primary"
                            icon={<SwapOutlined />}
                            onClick={() => setEntryPickerOpen(true)}
                        >
                            选择稿件
                        </KuzhambuButton>
                    </Empty>
                )}
            </section>
            <KuzhambuModal
                testId="classics-sancai-visual-entry-picker-modal"
                className="sancai-visual-entry-picker-modal"
                width={760}
                title="选择视觉处理稿件"
                footer={
                    <div className="sancai-modal-footer">
                        <KuzhambuButton
                            testId="classics-sancai-visual-entry-picker-cancel-button"
                            onClick={() => setEntryPickerOpen(false)}
                        >
                            取消
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="classics-sancai-visual-entry-picker-confirm-button"
                            type="primary"
                            disabled={!pendingEntry}
                            onClick={confirmPendingEntry}
                        >
                            选择
                        </KuzhambuButton>
                    </div>
                }
                open={entryPickerOpen}
                onCancel={() => setEntryPickerOpen(false)}
            >
                {entriesQuery.isError || hasCatalogError ? (
                    <KuzhambuAlert
                        className="sancai-alert"
                        type="warning"
                        showIcon
                        title="稿件选择数据加载失败"
                        description="请确认目录和稿件接口可用后重试。"
                    />
                ) : null}
                <div className="sancai-visual-entry-picker" aria-label="三才图会视觉处理稿件选择">
                    <div className="sancai-visual-entry-picker-tree">
                        {isCatalogLoading ? (
                            <Skeleton active paragraph={{ rows: 10 }} />
                        ) : (
                            <Tree
                                blockNode
                                showIcon
                                expandedKeys={treeExpandedKeys}
                                selectedKeys={entryPickerSelectedKeys}
                                treeData={entryPickerTreeData}
                                onExpand={(keys: Key[]) => setExpandedKeys(keys.map(String))}
                                onSelect={selectEntryPickerNode}
                            />
                        )}
                    </div>
                    <div className="sancai-visual-entry-picker-hint">
                        {!selectedVolume && !isCatalogLoading ? (
                            <Text type="secondary">请先选择卷目，再选择稿件。</Text>
                        ) : pendingEntry ? (
                            <Text type="secondary">{`已选择：${readEntryTitle(pendingEntry)}`}</Text>
                        ) : (
                            <Text type="secondary">请选择一条稿件后确认。</Text>
                        )}
                    </div>
                </div>
            </KuzhambuModal>
        </KuzhambuPage>
    );
};
