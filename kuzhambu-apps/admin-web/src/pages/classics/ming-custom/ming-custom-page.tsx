import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App } from "antd";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { AiCandidateBatchDrawer } from "@/pages/classics/common/ai-candidate-batch-drawer";
import type { ClassicsContentQaTaskPair } from "@/pages/classics/common/classics-content-qa-ai-panel";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type {
    AiRefinementTaskCapability,
    AiRefinementTaskRecord
} from "@/pages/classics/common/ai-refinement-task-types";
import * as exportService from "@/pages/classics/common/classics-export-service";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { hasClassicsContentPermission } from "@/pages/classics/common/classics-content-types";
import type {
    ClassicsExportJobRecord,
    ClassicsExportScopePayload
} from "@/pages/classics/common/classics-export-types";
import { MingCustomsEditDrawer } from "./ming-customs-edit-drawer";
import { MingCustomsExportActions } from "./ming-customs-export-actions";
import { MingCustomsTable } from "./ming-customs-table";
import {
    MingCustomsToolbar,
    type MingCustomsFilters,
    type MingCustomsSelectedTagFilter
} from "./ming-customs-toolbar";
import { MingCustomsVersionPanel } from "./ming-customs-version-panel";
import { MingCustomsRefinementSection } from "./ming-customs-refinement-section";
import * as service from "./ming-custom-service";
import type { MingCustomsCommand, MingCustomsQuery } from "./ming-custom-service";
import type {
    MingCustomsContentVersionRecord,
    MingCustomsPublicationBatchRecord,
    MingCustomsRecord,
    MingCustomsTagCloudItem
} from "./ming-custom-types";
import "./ming-custom-page.css";

const EXPORT_PAGE_SIZE = 8;
const TASK_POLL_INTERVAL_MS = 3000;

const readTitle = (record?: MingCustomsRecord | null) => {
    return record?.title?.trim() || `明代习俗 ${record?.id ?? "未命名"}`;
};

const createEventId = (prefix: string) => {
    return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
};

const DEFAULT_MING_CUSTOMS_FILTERS: MingCustomsFilters = {
    category: "",
    sortDirection: "DESC"
};

interface RefinementTaskContext {
    existingQaPairs?: ClassicsContentQaTaskPair[];
    existingTags?: string[];
}

const buildInputPayloadJson = (
    entry: MingCustomsRecord,
    capability: string,
    context: RefinementTaskContext = {}
) => {
    const document = [entry.originalExcerpts, entry.content]
        .filter((value): value is string => Boolean(value?.trim()))
        .join("\n\n");
    const categoryPath = [entry.category, entry.chapter, entry.section]
        .filter((value): value is string => Boolean(value?.trim()))
        .join(" / ");
    return JSON.stringify({
        capability,
        contentType: "MING_CUSTOMS",
        document,
        title: entry.title || null,
        category: entry.category || null,
        categoryPath,
        chapter: entry.chapter || null,
        section: entry.section || null,
        summary: entry.summary || null,
        existingSummary: entry.summary || null,
        originalExcerpts: entry.originalExcerpts || null,
        originalText: entry.originalExcerpts || null,
        content: entry.content || null,
        bodyText: entry.content || null,
        contentFormat: entry.contentFormat || null,
        existingQaPairs: context.existingQaPairs || [],
        existingTags: context.existingTags || []
    });
};

const buildExportScopeJson = (entry: MingCustomsRecord) => {
    const title = readTitle(entry);
    const scopePayload: ClassicsExportScopePayload = {
        title: `${title} 导出`,
        contentType: "MING_CUSTOMS",
        scopeType: "SELECTED_ITEMS",
        items: [
            {
                id: entry.id,
                title,
                text: entry.content || entry.originalExcerpts || "",
                summary: entry.summary || null,
                category: entry.category || null
            }
        ]
    };

    return JSON.stringify(scopePayload);
};

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

export const MingCustomPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [query, setQuery] = useState<MingCustomsQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE,
        sortDirection: DEFAULT_MING_CUSTOMS_FILTERS.sortDirection
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<MingCustomsFilters>(DEFAULT_MING_CUSTOMS_FILTERS);
    const [selectedTagFilter, setSelectedTagFilter] = useState<MingCustomsSelectedTagFilter | null>(
        null
    );
    const [mingCustomsEditDrawerMode, setMingCustomsEditDrawerMode] = useState<"create" | "edit">(
        "create"
    );
    const [mingCustomsEditDrawerOpen, setMingCustomsEditDrawerOpen] = useState(false);
    const [editingEntry, setEditingEntry] = useState<MingCustomsRecord | null>(null);
    const [selectedEntryRowsState, setSelectedEntryRowsState] = useState<{
        keys: string[];
        scopeKey: string;
    }>({
        keys: [],
        scopeKey: ""
    });
    const [batchCandidateEntryIds, setBatchCandidateEntryIds] = useState<string[]>([]);
    const [batchCandidateTitleById, setBatchCandidateTitleById] = useState<Record<string, string>>(
        {}
    );
    const [batchCandidateDrawerOpen, setBatchCandidateDrawerOpen] = useState(false);
    const [publicationBatchResult, setPublicationBatchResult] =
        useState<MingCustomsPublicationBatchRecord | null>(null);
    const [exportJobsDrawerOpen, setExportJobsDrawerOpen] = useState(false);
    const [creatingRefinementCapability, setCreatingRefinementCapability] =
        useState<AiRefinementTaskCapability | null>(null);
    const [summaryTrackingTask, setSummaryTrackingTask] = useState<AiRefinementTaskRecord | null>(
        null
    );
    const [selectedVersionId, setSelectedVersionId] = useState<string | null>(null);
    const handledSucceededTaskIdsRef = useRef<Set<string>>(new Set());
    const hasActiveFilters = Boolean(
        filters.category ||
        selectedTagFilter ||
        filters.sortDirection !== DEFAULT_MING_CUSTOMS_FILTERS.sortDirection
    );

    const mingCustomsQuery = useQuery({
        queryKey: ["ming-customs", "page", query],
        queryFn: () => service.page(query),
        retry: false
    });
    const categoryOptionsQuery = useQuery({
        queryKey: ["ming-customs", "category-options"],
        queryFn: service.listCategoryOptions,
        retry: false
    });
    const mingCustomsDetailQuery = useQuery({
        queryKey: ["ming-customs", "detail", editingEntry?.id],
        queryFn: () => service.get(editingEntry?.id ?? ""),
        enabled:
            mingCustomsEditDrawerOpen &&
            mingCustomsEditDrawerMode === "edit" &&
            Boolean(editingEntry?.id),
        retry: false
    });
    const versionsQuery = useQuery({
        queryKey: ["ming-customs", "versions", editingEntry?.id],
        queryFn: () => service.listVersions(editingEntry?.id ?? ""),
        enabled:
            mingCustomsEditDrawerOpen &&
            mingCustomsEditDrawerMode === "edit" &&
            Boolean(editingEntry?.id),
        retry: false
    });
    const mingCustomsVersionDetailQuery = useQuery({
        queryKey: ["ming-customs", "versions", "detail", editingEntry?.id, selectedVersionId],
        queryFn: () => service.getVersion(editingEntry?.id ?? "", selectedVersionId ?? ""),
        enabled:
            mingCustomsEditDrawerOpen && Boolean(editingEntry?.id) && Boolean(selectedVersionId),
        retry: false
    });
    const exportJobsQuery = useQuery({
        queryKey: ["classics", "ming-customs", "exports", "jobs"],
        queryFn: () =>
            exportService.page({
                pageNo: 1,
                pageSize: EXPORT_PAGE_SIZE,
                contentType: "MING_CUSTOMS",
                exportKind: "CONTENT_DATASET"
            }),
        retry: false
    });
    const refinementTasksQuery = useQuery({
        queryKey: ["classics", "ming-customs", "refinement", "tasks", editingEntry?.id],
        queryFn: () =>
            aiRefinementTaskService.pageTasks({
                pageNo: 1,
                pageSize: 10,
                contentType: "MING_CUSTOMS",
                contentId: editingEntry?.id
            }),
        enabled:
            mingCustomsEditDrawerOpen &&
            mingCustomsEditDrawerMode === "edit" &&
            Boolean(editingEntry?.id),
        retry: false,
        refetchInterval: (query) => {
            const tasks = query.state.data?.items || [];
            return tasks.some((task) => task.status === "PENDING" || task.status === "RUNNING")
                ? TASK_POLL_INTERVAL_MS
                : false;
        }
    });
    const pageResult = mingCustomsQuery.data;
    const records = useMemo(() => pageResult?.records || [], [pageResult?.records]);
    const categoryOptions = useMemo(
        () => categoryOptionsQuery.data || [],
        [categoryOptionsQuery.data]
    );
    const categoryLabels = useMemo(() => {
        return Object.fromEntries(categoryOptions.map((option) => [option.value, option.label]));
    }, [categoryOptions]);
    const totalCount = pageResult?.count ?? pageResult?.totalCount ?? 0;
    const currentPageNo = pageResult?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = pageResult?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;
    const editingEntryDetail = mingCustomsDetailQuery.data || editingEntry;
    const exportJobs = exportJobsQuery.data?.records || [];
    const currentPageSelectionScopeKey = useMemo(
        () => records.map((record) => String(record.id ?? "")).join("|"),
        [records]
    );
    const selectedEntryIds = useMemo(
        () =>
            selectedEntryRowsState.scopeKey === currentPageSelectionScopeKey
                ? selectedEntryRowsState.keys
                : [],
        [currentPageSelectionScopeKey, selectedEntryRowsState.keys, selectedEntryRowsState.scopeKey]
    );
    const selectedEntries = useMemo(
        () =>
            records.filter(
                (record) => record.id != null && selectedEntryIds.includes(String(record.id))
            ),
        [records, selectedEntryIds]
    );
    const canExportEntries = hasClassicsContentPermission("MING_CUSTOMS", "export", hasPermission);
    const canChangeEntryPublication = hasClassicsContentPermission(
        "MING_CUSTOMS",
        "edit",
        hasPermission
    );
    const refinementTasks = useMemo(
        () => refinementTasksQuery.data?.items || [],
        [refinementTasksQuery.data?.items]
    );
    const summaryRefinementTasks = useMemo(
        () =>
            refinementTasks.filter(
                (task) =>
                    aiRefinementTaskService.getNormalizedTaskCapability(task.capability) ===
                    "summary"
            ),
        [refinementTasks]
    );
    const versionHistory = useMemo(() => versionsQuery.data || [], [versionsQuery.data]);
    const selectedVersion = mingCustomsVersionDetailQuery.data || null;

    const invalidateMingCustoms = useCallback(async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["ming-customs", "page"] }),
            queryClient.invalidateQueries({ queryKey: ["ming-customs", "keyword-cloud"] }),
            queryClient.invalidateQueries({ queryKey: ["ming-customs", "tag-cloud"] }),
            queryClient.invalidateQueries({ queryKey: ["ming-customs", "detail"] }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "tags", "MING_CUSTOMS"]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "qa-pairs", "MING_CUSTOMS"]
            }),
            queryClient.invalidateQueries({ queryKey: ["ming-customs", "versions"] }),
            queryClient.invalidateQueries({
                queryKey: ["ai", "candidates", "MING_CUSTOMS", editingEntryDetail?.id]
            })
        ]);
    }, [editingEntryDetail?.id, queryClient]);

    const invalidateExportJobs = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "ming-customs", "exports", "jobs"]
        });
    };
    const invalidateRefinementTasks = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "ming-customs", "refinement", "tasks", editingEntryDetail?.id]
        });
    };

    const saveMutation = useMutation({
        mutationFn: (command: MingCustomsCommand) =>
            mingCustomsEditDrawerMode === "create" ? service.add(command) : service.update(command),
        onSuccess: async () => {
            setMingCustomsEditDrawerOpen(false);
            setEditingEntry(null);
            await invalidateMingCustoms();
            messageApi.success(
                mingCustomsEditDrawerMode === "create" ? "明代习俗已新增" : "明代习俗已保存"
            );
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });
    const deleteMutation = useMutation({
        mutationFn: service.deleteById,
        onSuccess: async () => {
            await invalidateMingCustoms();
            messageApi.success("明代习俗已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });
    const publicationMutation = useMutation({
        mutationFn: ({
            entry,
            action
        }: {
            entry: MingCustomsRecord;
            action: "PUBLISH" | "OFFLINE";
        }) =>
            action === "PUBLISH"
                ? service.publish({ id: entry.id })
                : service.submitOffline({ id: entry.id }),
        onSuccess: async () => {
            await invalidateMingCustoms();
            messageApi.success("发布状态变更请求已接受");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "发布状态变更失败");
        }
    });
    const publicationBatchMutation = useMutation({
        mutationFn: (action: "PUBLISH" | "OFFLINE") => {
            const command = { ids: selectedEntryIds };
            return action === "PUBLISH"
                ? service.publishBatch(command)
                : service.submitOfflineBatch(command);
        },
        onSuccess: async (result) => {
            setPublicationBatchResult(result);
            await invalidateMingCustoms();
            messageApi.success(
                `批量请求完成：接受 ${result.acceptedCount}，拒绝 ${result.rejectedCount}`
            );
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量发布状态变更失败");
        }
    });
    const exportMutation = useMutation({
        mutationFn: (entry: MingCustomsRecord) =>
            exportService.create({
                contentType: "MING_CUSTOMS",
                exportKind: "CONTENT_DATASET",
                exportFormat: "HTML",
                scopeType: "SELECTED_ITEMS",
                scopeJson: buildExportScopeJson(entry)
            }),
        onSuccess: async () => {
            await invalidateExportJobs();
            messageApi.success("导出任务已提交，请到任务抽屉查看进度。");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "导出提交失败");
        }
    });
    const deleteExportMutation = useMutation({
        mutationFn: exportService.deleteById,
        onSuccess: async () => {
            await invalidateExportJobs();
            messageApi.success("导出记录已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "导出记录删除失败");
        }
    });
    const createRefinementTaskMutation = useMutation({
        mutationFn: aiRefinementTaskService.createTask,
        onSuccess: async (task, command) => {
            if (
                aiRefinementTaskService.getNormalizedTaskCapability(command.capability) ===
                "summary"
            ) {
                setSummaryTrackingTask(task);
            }
            await invalidateRefinementTasks();
            messageApi.success("明代习俗精修任务已创建");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "精修任务创建失败");
        },
        onSettled: () => {
            setCreatingRefinementCapability(null);
        }
    });
    const resetVersionMutation = useMutation({
        mutationFn: ({ entryId, versionId }: { entryId: string; versionId: string }) =>
            service.resetVersion(entryId, versionId),
        onSuccess: async () => {
            setSelectedVersionId(null);
            await invalidateMingCustoms();
            messageApi.success("明代习俗版本已恢复");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "恢复失败");
        }
    });

    useEffect(() => {
        const completedTaskIds = refinementTasks
            .filter(
                (task) =>
                    (task.status === "SUCCEEDED" || task.status === "PARTIAL") &&
                    Boolean(task.taskId) &&
                    !handledSucceededTaskIdsRef.current.has(task.taskId)
            )
            .map((task) => task.taskId);
        if (!completedTaskIds.length) {
            return;
        }
        completedTaskIds.forEach((taskId) => handledSucceededTaskIdsRef.current.add(taskId));
        void invalidateMingCustoms();
    }, [invalidateMingCustoms, refinementTasks]);

    const searchMingCustoms = (value: string) => {
        setSearchText(value);
        setQuery((currentQuery) => ({
            ...currentQuery,
            keyword: normalizeSearch(value),
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const applyFilters = () => {
        setQuery((currentQuery) => ({
            ...currentQuery,
            category: filters.category || undefined,
            sortDirection: filters.sortDirection,
            tagId: selectedTagFilter?.tagId ?? undefined,
            tagNameSnapshot: selectedTagFilter?.tagNameSnapshot || undefined,
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const resetFilters = () => {
        setFilters(DEFAULT_MING_CUSTOMS_FILTERS);
        setQuery((currentQuery) => ({
            ...currentQuery,
            category: undefined,
            tagId: undefined,
            tagNameSnapshot: undefined,
            sortDirection: DEFAULT_MING_CUSTOMS_FILTERS.sortDirection,
            pageNo: DEFAULT_PAGE_NO
        }));
        setSelectedTagFilter(null);
    };

    const selectTag = (item: MingCustomsTagCloudItem) => {
        setSelectedTagFilter({
            count: item.count,
            tagId: item.tagId ?? null,
            tagNameSnapshot: item.tagNameSnapshot
        });
        setQuery((currentQuery) => ({
            ...currentQuery,
            tagId: item.tagId ?? undefined,
            tagNameSnapshot: item.tagNameSnapshot,
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const clearTagFilter = () => {
        setSelectedTagFilter(null);
        setQuery((currentQuery) => ({
            ...currentQuery,
            tagId: undefined,
            tagNameSnapshot: undefined,
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const openCreateMingCustomsDrawer = () => {
        setMingCustomsEditDrawerMode("create");
        setEditingEntry(null);
        setMingCustomsEditDrawerOpen(true);
        setSelectedVersionId(null);
        setSummaryTrackingTask(null);
    };

    const openEditMingCustomsDrawer = (entry: MingCustomsRecord) => {
        setMingCustomsEditDrawerMode("edit");
        setEditingEntry(entry);
        setMingCustomsEditDrawerOpen(true);
        setSelectedVersionId(null);
        setSummaryTrackingTask(null);
    };

    const closeMingCustomsEditDrawer = () => {
        if (saveMutation.isPending) {
            return;
        }
        setMingCustomsEditDrawerOpen(false);
        setEditingEntry(null);
        setSelectedVersionId(null);
        setSummaryTrackingTask(null);
        handledSucceededTaskIdsRef.current.clear();
    };

    const selectVersion = (version: MingCustomsContentVersionRecord) => {
        setSelectedVersionId(version.id);
    };

    const confirmResetVersion = (version: MingCustomsContentVersionRecord) => {
        if (!editingEntryDetail?.id) {
            return;
        }

        confirm.danger({
            title: "确认恢复明代习俗历史版本",
            message: "恢复后会生成新的正式版本，当前内容将被历史版本覆盖。",
            onConfirm: () =>
                resetVersionMutation.mutate({
                    entryId: editingEntryDetail.id,
                    versionId: version.id
                })
        });
    };

    const deleteEntry = (entry: MingCustomsRecord) => {
        const title = entry.title?.trim() || `条目 ${entry.id}`;
        confirm.danger({
            title: "删除明代习俗",
            message: `确认删除 ${title}？`,
            description: "删除后该条目将不再出现在明代习俗列表。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(entry.id)
        });
    };

    const changePublicationStatus = (entry: MingCustomsRecord, action: "PUBLISH" | "OFFLINE") => {
        const actionText = action === "PUBLISH" ? "发布" : "下线";
        confirm.danger({
            title: `${actionText}明代习俗`,
            message: `确认${actionText} ${readTitle(entry)}？`,
            description: "请求提交后由后台任务异步同步搜索与知识库状态。",
            okText: actionText,
            onConfirm: () => publicationMutation.mutateAsync({ entry, action })
        });
    };

    const changePublicationStatusBatch = (action: "PUBLISH" | "OFFLINE") => {
        const actionText = action === "PUBLISH" ? "发布" : "下线";
        confirm.danger({
            title: `批量${actionText}明代习俗`,
            message: `确认${actionText}选中的 ${selectedEntryIds.length} 条稿件？`,
            description: "服务端将逐条受理，并返回每条稿件的接受或拒绝原因。",
            okText: `批量${actionText}`,
            onConfirm: () => publicationBatchMutation.mutateAsync(action)
        });
    };

    const exportEntry = (entry: MingCustomsRecord) => {
        if (!canExportEntries) {
            messageApi.warning("当前账号缺少明代习俗导出权限");
            return;
        }
        exportMutation.mutate(entry);
    };

    const deleteExportJob = (job: ClassicsExportJobRecord) => {
        if (!job.id) {
            return;
        }
        confirm.danger({
            title: "删除导出记录",
            message: `确认删除导出任务 #${job.id}？`,
            description:
                "删除后该记录会从列表移除，并释放其导出产物引用；已无引用的文件对象会进入 Storage 删除流程。",
            okText: "删除",
            onConfirm: () => deleteExportMutation.mutateAsync(job.id ?? "")
        });
    };

    const deleteExportJobs = (jobs: ClassicsExportJobRecord[]) => {
        const ids = jobs.map((job) => job.id).filter((id): id is string => Boolean(id));
        if (!ids.length) {
            return;
        }
        confirm.danger({
            title: "批量删除导出记录",
            message: `确认删除 ${ids.length} 条导出记录？`,
            description: "批量删除逐条释放导出产物引用；单条仍被其他业务引用的文件不会被强制删除。",
            okText: "删除",
            onConfirm: async () => {
                await Promise.all(ids.map((id) => exportService.deleteById(id)));
                await invalidateExportJobs();
                messageApi.success(`已删除 ${ids.length} 条导出记录`);
            }
        });
    };

    const createRefinementTask = (
        entry: MingCustomsRecord,
        capability: AiRefinementTaskCapability,
        context: RefinementTaskContext = {}
    ) => {
        if (!entry.content?.trim() && !entry.originalExcerpts?.trim()) {
            messageApi.warning("正文与原文摘录均为空，无法创建 AI 精修任务");
            return;
        }
        setCreatingRefinementCapability(capability);
        const capabilityCode = aiRefinementTaskService.getBusinessCapabilityCode(capability);
        createRefinementTaskMutation.mutate({
            capability: capabilityCode,
            scope: "classics",
            contentType: "MING_CUSTOMS",
            contentId: entry.id,
            requestId: createEventId(`ming-customs-${capability}-request`),
            traceId: createEventId(`ming-customs-${capability}-trace`),
            inputPayloadJson: buildInputPayloadJson(entry, capabilityCode, context),
            locale: "zh-CN"
        });
    };

    const openBatchCandidateDrawer = () => {
        if (!canChangeEntryPublication) {
            messageApi.warning("当前账号缺少明代习俗编辑权限");
            return;
        }
        if (!selectedEntries.length) {
            messageApi.warning("请先选择当前页要批量治理的明代习俗");
            return;
        }
        setBatchCandidateEntryIds(selectedEntries.map((entry) => entry.id));
        setBatchCandidateTitleById(
            Object.fromEntries(selectedEntries.map((entry) => [entry.id, readTitle(entry)]))
        );
        setBatchCandidateDrawerOpen(true);
    };

    return (
        <div className="ming-custom-page-root">
            <MingCustomsToolbar
                categoryOptions={categoryOptions}
                filterActive={hasActiveFilters}
                filters={filters}
                onFilterApply={applyFilters}
                onFilterReset={resetFilters}
                onAdd={openCreateMingCustomsDrawer}
                onClearTagFilter={clearTagFilter}
                onFiltersChange={setFilters}
                onOpenExportJobs={() => setExportJobsDrawerOpen(true)}
                onSearchChange={searchMingCustoms}
                onSelectTag={selectTag}
                query={query}
                searchValue={searchText}
                selectedTagFilter={selectedTagFilter}
                content={
                    <>
                        <MingCustomsExportActions
                            canExportEntries={canExportEntries}
                            exportJobs={exportJobs}
                            hasExportJobsError={exportJobsQuery.isError}
                            loading={
                                exportJobsQuery.isLoading ||
                                exportMutation.isPending ||
                                deleteExportMutation.isPending
                            }
                            onBatchDeleteExportJobs={deleteExportJobs}
                            onCloseExportJobs={() => setExportJobsDrawerOpen(false)}
                            onDeleteExportJob={deleteExportJob}
                            onRefreshExportJobs={() => {
                                void invalidateExportJobs();
                            }}
                            openExportJobs={exportJobsDrawerOpen}
                        />
                        <MingCustomsTable
                            publicationBatchResult={publicationBatchResult}
                            canChangeEntryPublication={canChangeEntryPublication}
                            canExport={canExportEntries}
                            categoryLabels={categoryLabels}
                            loading={mingCustomsQuery.isLoading}
                            dataSource={records}
                            onBatchCandidate={openBatchCandidateDrawer}
                            onDelete={deleteEntry}
                            onExport={exportEntry}
                            onOpenEdit={openEditMingCustomsDrawer}
                            onPublicationAction={changePublicationStatus}
                            onPublicationBatch={changePublicationStatusBatch}
                            onSelectedEntryIdsChange={(ids) =>
                                setSelectedEntryRowsState({
                                    keys: ids,
                                    scopeKey: currentPageSelectionScopeKey
                                })
                            }
                            pagination={{
                                current: currentPageNo,
                                pageSize: currentPageSize,
                                total: totalCount,
                                onChange: (pageNo, pageSize) =>
                                    setQuery((currentQuery) => ({
                                        ...currentQuery,
                                        pageNo,
                                        pageSize
                                    }))
                            }}
                            selectedEntryIds={selectedEntryIds}
                            publicationChanging={
                                publicationMutation.isPending || publicationBatchMutation.isPending
                            }
                        />
                        <AiCandidateBatchDrawer
                            open={batchCandidateDrawerOpen}
                            contentType="MING_CUSTOMS"
                            contentIds={batchCandidateEntryIds}
                            capabilities={["summary", "tags", "qa"]}
                            contentTitleById={batchCandidateTitleById}
                            canEdit={canChangeEntryPublication}
                            onClose={() => setBatchCandidateDrawerOpen(false)}
                            onChanged={invalidateMingCustoms}
                        />
                    </>
                }
            />
            <MingCustomsEditDrawer
                categoryOptions={categoryOptions}
                entry={editingEntryDetail}
                loading={mingCustomsDetailQuery.isLoading}
                mode={mingCustomsEditDrawerMode}
                open={mingCustomsEditDrawerOpen}
                saving={saveMutation.isPending}
                onChanged={invalidateMingCustoms}
                onClose={closeMingCustomsEditDrawer}
                onSave={(command) => saveMutation.mutate(command)}
                summaryCreating={creatingRefinementCapability === "summary"}
                summaryTasks={summaryRefinementTasks}
                summaryTrackingTask={summaryTrackingTask}
                onCreateSummaryTask={() => {
                    if (editingEntryDetail) {
                        createRefinementTask(editingEntryDetail, "summary");
                    }
                }}
                onSummaryTaskChange={setSummaryTrackingTask}
                tagContent={
                    editingEntryDetail ? (
                        <MingCustomsRefinementSection
                            creatingRefinementCapability={creatingRefinementCapability}
                            entry={editingEntryDetail}
                            refinementTasks={refinementTasks}
                            section="tags"
                            onChanged={invalidateMingCustoms}
                            onCreateTask={(capability, context) =>
                                createRefinementTask(editingEntryDetail, capability, context)
                            }
                        />
                    ) : null
                }
                qaContent={
                    editingEntryDetail ? (
                        <MingCustomsRefinementSection
                            creatingRefinementCapability={creatingRefinementCapability}
                            entry={editingEntryDetail}
                            refinementTasks={refinementTasks}
                            section="qa"
                            onChanged={invalidateMingCustoms}
                            onCreateTask={(capability, context) =>
                                createRefinementTask(editingEntryDetail, capability, context)
                            }
                        />
                    ) : null
                }
                versionContent={
                    editingEntryDetail ? (
                        <MingCustomsVersionPanel
                            currentEntry={editingEntryDetail}
                            detailLoading={mingCustomsVersionDetailQuery.isLoading}
                            listLoading={versionsQuery.isLoading}
                            resetting={resetVersionMutation.isPending}
                            selectedVersion={selectedVersion}
                            versions={versionHistory}
                            onSelectVersion={selectVersion}
                            onResetVersion={confirmResetVersion}
                        />
                    ) : null
                }
            />
        </div>
    );
};
