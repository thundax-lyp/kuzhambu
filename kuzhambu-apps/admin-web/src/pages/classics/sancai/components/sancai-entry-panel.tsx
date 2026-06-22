import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Empty, List, Space, Tag, Typography } from "antd";
import { useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import type { KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import * as exportService from "@/pages/classics/common/classics-export-service";
import * as shareService from "@/pages/classics/common/classics-share-service";
import { AiCandidatePanel } from "@/pages/classics/common/components/ai-candidate-panel";
import { SancaiEntryList } from "./sancai-entry-list";
import { SancaiEntryModel } from "./sancai-entry-model";
import type { SancaiEntryFormValues } from "./sancai-form-values";
import { SancaiVersionHistoryPanel } from "./sancai-version-history-panel";
import * as entryService from "../services/sancai-entry-service";
import type {
    SancaiContentVersionRecord,
    SancaiEntryRecord,
    SancaiShowcaseRecord,
    SancaiExportJobRecord,
    SancaiVolumeRecord
} from "../sancai-types";

const { Text } = Typography;

const EXPORT_PAGE_SIZE = 8;
const SHOWCASE_PAGE_SIZE = 8;

const readEntryTitle = (entry: SancaiEntryRecord) => {
    return entry.title?.trim() || `条目 ${entry.id}`;
};

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "—";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hour = String(date.getHours()).padStart(2, "0");
    const minute = String(date.getMinutes()).padStart(2, "0");
    const second = String(date.getSeconds()).padStart(2, "0");
    return `${year}/${month}/${day} ${hour}:${minute}:${second}`;
};

const exportStatusTagType = (status?: string | null) => {
    switch (status) {
        case "COMPLETED":
            return "success";
        case "RUNNING":
        case "REQUESTED":
            return "processing";
        case "FAILED":
            return "error";
        case "EXPIRED":
            return "warning";
        default:
            return "default";
    }
};

const showcaseStatusTagType = (status?: string | null) => {
    switch (status) {
        case "COMPLETED":
            return "success";
        case "PROCESSING":
        case "REQUESTED":
            return "processing";
        case "FAILED":
            return "error";
        default:
            return "default";
    }
};

const isExpired = (expiresAt?: string | null) => {
    if (!expiresAt) {
        return false;
    }
    const expired = Number.isNaN(Date.parse(expiresAt))
        ? false
        : Date.parse(expiresAt) <= Date.now();
    return expired;
};

interface SancaiEntryPanelProps {
    categoryId: number | null;
    defaultCreateOpen?: boolean;
    isCatalogLoading: boolean;
    keyword?: string | null;
    lifecycleStatus?: string | null;
    refreshVersion: number;
    volumeId: number | null;
    volumes: SancaiVolumeRecord[];
}

export const SancaiEntryPanel = ({
    categoryId,
    defaultCreateOpen = false,
    isCatalogLoading,
    keyword,
    lifecycleStatus,
    refreshVersion,
    volumeId,
    volumes
}: SancaiEntryPanelProps) => {
    const { message: messageApi, modal: modalApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [isCreating, setIsCreating] = useState(defaultCreateOpen);
    const [isModelOpen, setIsModelOpen] = useState(defaultCreateOpen);
    const [editingEntry, setEditingEntry] = useState<SancaiEntryRecord | null>(null);
    const [selectedVersionId, setSelectedVersionId] = useState<number | null>(null);
    const entriesQuery = useQuery({
        queryKey: [
            "classics",
            "sancai",
            "entries",
            "list",
            categoryId,
            volumeId,
            keyword,
            lifecycleStatus,
            refreshVersion
        ],
        queryFn: () =>
            entryService.list({
                categoryId,
                volumeId,
                keyword,
                lifecycleStatus,
                sortDirection: "ASC"
            }),
        enabled: categoryId !== null && volumeId !== null,
        retry: false
    });
    const entries = entriesQuery.data || [];
    const detailQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "detail", editingEntry?.id],
        queryFn: () => entryService.get(editingEntry?.id ?? 0),
        enabled: isModelOpen && !isCreating && Boolean(editingEntry?.id),
        retry: false
    });
    const selectedEntry = isCreating ? undefined : (detailQuery.data ?? editingEntry ?? undefined);
    const versionsQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "versions", selectedEntry?.id],
        queryFn: () => entryService.listVersions(selectedEntry?.id ?? 0),
        enabled: isModelOpen && !isCreating && Boolean(selectedEntry?.id),
        retry: false
    });
    const versionDetailQuery = useQuery({
        queryKey: [
            "classics",
            "sancai",
            "entries",
            "version",
            selectedEntry?.id,
            selectedVersionId
        ],
        queryFn: () => entryService.getVersion(selectedEntry?.id ?? 0, selectedVersionId ?? 0),
        enabled: isModelOpen && Boolean(selectedEntry?.id && selectedVersionId),
        retry: false
    });
    const versions = versionsQuery.data || [];
    const selectedVersion =
        versionDetailQuery.data ||
        versions.find((version) => version.id === selectedVersionId) ||
        null;
    const exportsQuery = useQuery({
        queryKey: ["classics", "sancai", "exports", "jobs"],
        queryFn: () =>
            exportService.page({
                pageNo: 1,
                pageSize: EXPORT_PAGE_SIZE,
                contentType: "SANCAI_ENTRY",
                exportKind: "CONTENT_DATASET"
            }),
        retry: false
    });
    const exportJobs = exportsQuery.data?.records || [];
    const showcasesQuery = useQuery({
        queryKey: ["classics", "sancai", "showcases", "jobs"],
        queryFn: () =>
            entryService.pageShowcases({
                pageNo: 1,
                pageSize: SHOWCASE_PAGE_SIZE
            }),
        retry: false
    });
    const showcaseJobs = showcasesQuery.data?.records || [];
    let modelKey = "empty";
    if (isCreating) {
        modelKey = "create";
    } else if (selectedEntry) {
        modelKey = [
            selectedEntry.id,
            selectedEntry.currentVersionId ?? "no-version",
            selectedEntry.contentUpdatedAt ?? "no-content-time"
        ].join(":");
    }
    const isLoading = isCatalogLoading || entriesQuery.isLoading;
    const invalidateEntries = async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] }),
            queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "exports", "jobs"] })
        ]);
    };
    const invalidateExportJobs = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "sancai", "exports", "jobs"]
        });
    };
    const invalidateShowcaseJobs = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "sancai", "showcases", "jobs"]
        });
    };
    const refreshSancaiEntryDetail = async () => {
        if (!selectedEntry?.id) {
            return;
        }
        await Promise.all([
            queryClient.invalidateQueries({
                queryKey: ["classics", "sancai", "entries", "detail", selectedEntry.id]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "sancai", "entries", "versions", selectedEntry.id]
            })
        ]);
    };
    const addEntryMutation = useMutation({
        mutationFn: entryService.add,
        onSuccess: async () => {
            await invalidateEntries();
            setIsCreating(false);
            setIsModelOpen(false);
            setEditingEntry(null);
            setSelectedVersionId(null);
            messageApi.success("三才图会条目已新增");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "新增失败");
        }
    });
    const updateEntryMutation = useMutation({
        mutationFn: entryService.update,
        onSuccess: async () => {
            await invalidateEntries();
            setIsModelOpen(false);
            setEditingEntry(null);
            setSelectedVersionId(null);
            messageApi.success("三才图会条目已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });
    const deleteEntryMutation = useMutation({
        mutationFn: entryService.deleteById,
        onSuccess: async () => {
            await invalidateEntries();
            setEditingEntry(null);
            setSelectedVersionId(null);
            messageApi.success("三才图会条目已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });
    const sortEntryMutation = useMutation({
        mutationFn: entryService.sort,
        onSuccess: async () => {
            await invalidateEntries();
            messageApi.success("三才图会条目顺序已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "排序保存失败");
        }
    });
    const shareEntryMutation = useMutation({
        mutationFn: shareService.create,
        onSuccess: (share) => {
            if (typeof navigator.clipboard?.writeText === "function") {
                void navigator.clipboard.writeText(share.shareUrl);
                messageApi.success("分享链接已复制");
                return;
            }
            messageApi.success(`分享链接：${share.shareUrl}`);
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "分享创建失败");
        }
    });
    const resetVersionMutation = useMutation({
        mutationFn: ({ entryId, versionId }: { entryId: number; versionId: number }) =>
            entryService.resetVersion(entryId, versionId),
        onSuccess: async () => {
            setSelectedVersionId(null);
            await invalidateEntries();
            modalApi.success({
                title: "三才图会版本已恢复",
                content: "已生成新的正式版本，并已将条目移动到恢复快照所在卷目的末尾。"
            });
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "版本恢复失败");
        }
    });
    const exportEntryMutation = useMutation({
        mutationFn: (entry: SancaiEntryRecord) => {
            const title = `${readEntryTitle(entry)} 导出`;
            return exportService.create({
                contentType: "SANCAI_ENTRY",
                exportKind: "CONTENT_DATASET",
                exportFormat: "HTML",
                scopeType: "SELECTED_ITEMS",
                scopeJson: JSON.stringify({
                    title,
                    ids: [entry.id]
                })
            });
        },
        onSuccess: async () => {
            await invalidateExportJobs();
            messageApi.success("导出任务已提交，请到下方任务列表查看进度。");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "导出提交失败");
        }
    });
    const showcaseEntryMutation = useMutation({
        mutationFn: (entry: SancaiEntryRecord) => {
            const title = `${readEntryTitle(entry)} 静态展示`;
            return entryService.requestShowcase({
                scopeJson: JSON.stringify({
                    title,
                    entries: [
                        {
                            id: entry.id,
                            title: entry.title,
                            volumeId: entry.volumeId
                        }
                    ]
                }),
                entryCount: 1,
                visibilityRiskStatus: "PUBLIC_ONLY"
            });
        },
        onSuccess: async () => {
            await invalidateShowcaseJobs();
            messageApi.success("三才静态展示任务已提交，请到下方任务列表查看进度。");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "静态展示提交失败");
        }
    });

    const selectEntry = (entry: SancaiEntryRecord) => {
        setIsCreating(false);
        setEditingEntry(entry);
        setSelectedVersionId(null);
        setIsModelOpen(true);
    };

    const closeModel = () => {
        setIsCreating(false);
        setEditingEntry(null);
        setSelectedVersionId(null);
        setIsModelOpen(false);
    };

    const submitEntry = (form: SancaiEntryFormValues) => {
        if (isCreating) {
            if (!volumeId) {
                messageApi.warning("请先选择卷目");
                return;
            }
            addEntryMutation.mutate({
                volumeId,
                title: form.title,
                originalText: form.originalText,
                translationText: form.translationText,
                summary: form.summary,
                lifecycleStatus: "DRAFT",
                visibility: form.visibility,
                translationStatus: "PENDING",
                imageStatus: "PENDING",
                visualAssetStatus: "PENDING",
                refinementStatus: "PENDING"
            });
            return;
        }
        if (!selectedEntry) {
            return;
        }
        updateEntryMutation.mutate({
            id: selectedEntry.id,
            volumeId: selectedEntry.volumeId,
            title: form.title,
            originalText: form.originalText,
            translationText: form.translationText,
            summary: form.summary,
            lifecycleStatus: selectedEntry.lifecycleStatus,
            visibility: form.visibility,
            translationStatus: selectedEntry.translationStatus,
            imageStatus: selectedEntry.imageStatus,
            visualAssetStatus: selectedEntry.visualAssetStatus,
            refinementStatus: selectedEntry.refinementStatus
        });
    };

    const deleteEntry = (entry: SancaiEntryRecord) => {
        confirm.danger({
            title: "删除三才图会条目",
            message: `确认删除 ${entry.title?.trim() || `条目 ${entry.id}`}？`,
            description: "删除后该条目将不再出现在当前卷目下。",
            okText: "删除",
            onConfirm: () => deleteEntryMutation.mutateAsync(entry.id)
        });
    };

    const shareEntry = (entry: SancaiEntryRecord) => {
        const title = entry.title?.trim() || `条目 ${entry.id}`;
        shareEntryMutation.mutate({
            targets: [
                {
                    contentId: entry.id,
                    contentType: "SANCAI_ENTRY"
                }
            ],
            title: `${title} 分享`,
            visibility: "PUBLIC"
        });
    };

    const exportEntry = (entry: SancaiEntryRecord) => {
        exportEntryMutation.mutate(entry);
    };
    const showcaseEntry = (entry: SancaiEntryRecord) => {
        if (!entry.id) {
            return;
        }
        showcaseEntryMutation.mutate(entry);
    };

    const downloadExport = (job: SancaiExportJobRecord) => {
        if (!job.downloadUrl) {
            return;
        }
        window.open(job.downloadUrl, "_blank", "noopener,noreferrer");
    };
    const downloadShowcase = (job: SancaiShowcaseRecord) => {
        if (!job.downloadUrl) {
            return;
        }
        window.open(job.downloadUrl, "_blank", "noopener,noreferrer");
    };

    const resetVersion = (version: SancaiContentVersionRecord) => {
        if (!selectedEntry?.id) {
            return;
        }
        confirm.danger({
            title: "恢复三才图会版本",
            message: `确认恢复版本 ${version.versionNo ?? version.id}？`,
            description: "恢复后会产生新的正式版本，并刷新条目详情、列表和版本历史。",
            okText: "恢复",
            onConfirm: () =>
                resetVersionMutation.mutateAsync({
                    entryId: selectedEntry.id,
                    versionId: version.id
                })
        });
    };

    const sortEntry = (
        sourceEntry: SancaiEntryRecord,
        targetEntry: SancaiEntryRecord,
        position: KuzhambuTableSortPosition
    ) => {
        if (sourceEntry.id === targetEntry.id) {
            return;
        }
        const remainingEntries = entries.filter((entry) => entry.id !== sourceEntry.id);
        const targetIndex = remainingEntries.findIndex((entry) => entry.id === targetEntry.id);
        if (targetIndex < 0) {
            return;
        }
        const insertIndex = position === "before" ? targetIndex : targetIndex + 1;
        const sortedEntries = [...remainingEntries];
        sortedEntries.splice(insertIndex, 0, sourceEntry);
        sortEntryMutation.mutate({
            orderedIds: sortedEntries.map((entry) => entry.id),
            sortDirection: "ASC"
        });
    };
    const isDownloadableExport = (job: SancaiExportJobRecord) => {
        return job.status === "COMPLETED" && Boolean(job.downloadUrl) && !isExpired(job.expiresAt);
    };
    const isDownloadableShowcase = (job: SancaiShowcaseRecord) => {
        return job.status === "COMPLETED" && Boolean(job.downloadUrl);
    };
    const renderExportStatus = (status?: string | null, expiresAt?: string | null) => {
        const normalized = status || "UNKNOWN";
        if (isExpired(expiresAt)) {
            return <Tag color="warning">已过期</Tag>;
        }
        const displayStatus =
            {
                COMPLETED: "已完成",
                REQUESTED: "排队中",
                RUNNING: "进行中",
                FAILED: "失败",
                EXPIRED: "已过期"
            }[normalized] || normalized;
        return <Tag color={exportStatusTagType(normalized)}>{displayStatus}</Tag>;
    };
    const renderShowcaseStatus = (status?: string | null) => {
        const normalized = status || "UNKNOWN";
        const displayStatus =
            {
                COMPLETED: "已完成",
                REQUESTED: "排队中",
                PROCESSING: "进行中",
                FAILED: "失败"
            }[normalized] || normalized;
        return <Tag color={showcaseStatusTagType(normalized)}>{displayStatus}</Tag>;
    };

    return (
        <>
            {entriesQuery.isError ? (
                <Alert
                    className="sancai-alert"
                    type="warning"
                    showIcon
                    message="三才图会条目加载失败"
                    description="请确认后台条目接口可用后刷新页面。"
                />
            ) : null}
            {showcasesQuery.isError ? (
                <Alert
                    className="sancai-alert"
                    type="warning"
                    showIcon
                    message="静态展示任务列表加载失败"
                    description="请确认后台静态展示任务接口可用后刷新页面。"
                />
            ) : null}
            {exportsQuery.isError ? (
                <Alert
                    className="sancai-alert"
                    type="warning"
                    showIcon
                    message="导出任务列表加载失败"
                    description="请确认后台导出任务接口可用后刷新页面。"
                />
            ) : null}
            <section className="sancai-export-section">
                <Space align="center" className="sancai-export-section-head" size={12} wrap>
                    <Text strong>导出任务</Text>
                    <Button
                        size="small"
                        type="link"
                        onClick={() => {
                            void invalidateExportJobs();
                        }}
                    >
                        刷新
                    </Button>
                </Space>
                <List
                    size="small"
                    dataSource={exportJobs}
                    loading={exportsQuery.isLoading || exportEntryMutation.isPending}
                    locale={{
                        emptyText: (
                            <Empty
                                description="暂无导出任务"
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                            />
                        )
                    }}
                    renderItem={(job) => {
                        const expired = isExpired(job.expiresAt);
                        const downloadable = isDownloadableExport(job);
                        const statusText = expired ? "已过期" : formatDateTime(job.requestedAt);
                        return (
                            <List.Item
                                key={job.id ?? `export-job-${job.requestedAt}`}
                                extra={
                                    <Space size={8} wrap>
                                        {renderExportStatus(job.status, job.expiresAt)}
                                        {downloadable ? (
                                            <Button
                                                size="small"
                                                type="primary"
                                                onClick={() => downloadExport(job)}
                                            >
                                                下载
                                            </Button>
                                        ) : (
                                            <Button size="small" disabled>
                                                下载
                                            </Button>
                                        )}
                                    </Space>
                                }
                            >
                                <List.Item.Meta
                                    title={`任务 #${job.id ?? "草稿"}`}
                                    description={`${statusText} | 条目数：${job.itemCount ?? 0} | 资产数：${job.assetCount ?? 0}`}
                                />
                            </List.Item>
                        );
                    }}
                />
            </section>
            <section className="sancai-export-section">
                <Space align="center" className="sancai-export-section-head" size={12} wrap>
                    <Text strong>静态展示任务</Text>
                    <Button
                        size="small"
                        type="link"
                        onClick={() => {
                            void invalidateShowcaseJobs();
                        }}
                    >
                        刷新
                    </Button>
                </Space>
                <List
                    size="small"
                    dataSource={showcaseJobs}
                    loading={showcasesQuery.isLoading || showcaseEntryMutation.isPending}
                    locale={{
                        emptyText: (
                            <Empty
                                description="暂无静态展示任务"
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                            />
                        )
                    }}
                    renderItem={(job) => {
                        const downloadable = isDownloadableShowcase(job);
                        const statusText = formatDateTime(job.requestedAt);
                        return (
                            <List.Item
                                key={job.id ?? `showcase-job-${job.requestedAt}`}
                                extra={
                                    <Space size={8} wrap>
                                        {renderShowcaseStatus(job.status)}
                                        {downloadable ? (
                                            <Button
                                                size="small"
                                                type="primary"
                                                onClick={() => downloadShowcase(job)}
                                            >
                                                下载
                                            </Button>
                                        ) : (
                                            <Button size="small" disabled>
                                                下载
                                            </Button>
                                        )}
                                    </Space>
                                }
                            >
                                <List.Item.Meta
                                    title={`任务 #${job.id ?? "草稿"}`}
                                    description={`${statusText} | 条目数：${job.entryCount ?? 0} | 风险：${job.visibilityRiskStatus || "未知"}`}
                                />
                            </List.Item>
                        );
                    }}
                />
            </section>
            <SancaiEntryList
                entries={entries}
                isLoading={isLoading || sortEntryMutation.isPending}
                volumes={volumes}
                onDelete={deleteEntry}
                onExport={exportEntry}
                onShowcase={showcaseEntry}
                onShare={shareEntry}
                onSort={sortEntry}
                onView={selectEntry}
            />
            <SancaiEntryModel
                key={modelKey}
                entry={selectedEntry}
                isSubmitting={addEntryMutation.isPending || updateEntryMutation.isPending}
                mode={isCreating ? "create" : "edit"}
                open={isModelOpen && !isLoading}
                onCancel={closeModel}
                onSubmit={submitEntry}
                afterForm={
                    !isCreating && selectedEntry ? (
                        <>
                            <AiCandidatePanel
                                capabilities={["translate", "summary", "tags", "qa"]}
                                contentId={selectedEntry.id}
                                contentType="SANCAI_ENTRY"
                                onApplied={async () => {
                                    await Promise.all([
                                        refreshSancaiEntryDetail(),
                                        invalidateEntries()
                                    ]);
                                }}
                            />
                            <SancaiVersionHistoryPanel
                                currentEntry={selectedEntry}
                                detailLoading={versionDetailQuery.isLoading}
                                listLoading={versionsQuery.isLoading}
                                resetting={resetVersionMutation.isPending}
                                selectedVersion={selectedVersion}
                                versions={versions}
                                onSelectVersion={(version) => setSelectedVersionId(version.id)}
                                onResetVersion={resetVersion}
                            />
                        </>
                    ) : null
                }
            />
        </>
    );
};
