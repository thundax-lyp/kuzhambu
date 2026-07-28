import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App } from "antd";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { AiCandidateBatchDrawer } from "@/pages/classics/common/components/ai-candidate-batch-drawer";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type { AiRefinementTaskCapability } from "@/pages/classics/common/ai-refinement-task-types";
import * as contentService from "@/pages/classics/common/classics-content-service";
import * as exportService from "@/pages/classics/common/classics-export-service";
import * as shareService from "@/pages/classics/common/classics-share-service";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import {
    hasClassicsContentPermission,
    type ClassicsBatchOperationRecord
} from "@/pages/classics/common/classics-content-types";
import type {
    ClassicsExportJobRecord,
    ClassicsExportScopePayload
} from "@/pages/classics/common/classics-export-types";
import { MingCustomsAiActions } from "./components/ming-customs-ai-actions";
import { MingCustomsEditDrawer } from "./components/ming-customs-edit-drawer";
import { MingCustomsExportActions } from "./components/ming-customs-export-actions";
import { MingCustomsTable } from "./components/ming-customs-table";
import {
    MingCustomsToolbar,
    type MingCustomsFilters,
    type MingCustomsSelectedTagFilter,
    type MingCustomsVisibilityFilter
} from "./components/ming-customs-toolbar";
import { MingCustomsVersionPanel } from "./components/ming-customs-version-panel";
import * as service from "./ming-customs-service";
import type { MingCustomsCommand, MingCustomsQuery } from "./ming-customs-service";
import type {
    MingCustomsContentVersionRecord,
    MingCustomsRecord,
    MingCustomsTagCloudItem
} from "./ming-customs-types";
import "./ming-customs-page.css";

const EXPORT_PAGE_SIZE = 8;
const TASK_POLL_INTERVAL_MS = 3000;
const DEFAULT_REFINEMENT_MODEL_ID = 1;
const DEFAULT_REFINEMENT_MODEL_NAME = "gpt-5.5";
const DEFAULT_REFINEMENT_SERVICE_ROLE = "PRIMARY";

const readTitle = (record?: MingCustomsRecord | null) => {
    return record?.title?.trim() || `明代习俗 ${record?.id ?? "未命名"}`;
};

const createEventId = (prefix: string) => {
    return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
};

const MING_CUSTOMS_REFINEMENT_PROMPT: Record<AiRefinementTaskCapability, string> = {
    summary: "你是明代风俗整理助理。请基于输入条目提炼准确、紧凑、便于后台回填的中文摘要。",
    tags: "你是明代风俗标签治理助理。请基于输入条目提取稳定、短小、适合后台统一标签库复用的中文标签。",
    qa: "你是明代风俗问答治理助理。请基于输入条目生成可用于知识库检索的中文问答对。"
};

const DEFAULT_MING_CUSTOMS_FILTERS: MingCustomsFilters = {
    category: "",
    sortDirection: "DESC",
    visibility: "ALL"
};

const buildPromptMessagesJson = (
    entry: MingCustomsRecord,
    capability: AiRefinementTaskCapability
) => {
    return JSON.stringify([
        {
            role: "system",
            content: MING_CUSTOMS_REFINEMENT_PROMPT[capability]
        },
        {
            role: "user",
            content: [
                `标题：${readTitle(entry)}`,
                `分类：${entry.category || "未分类"}`,
                `现有摘要：${entry.summary || "暂无"}`,
                `原文摘录：${entry.originalExcerpts || "暂无"}`,
                `正文：${entry.content || "暂无正文"}`
            ].join("\n")
        }
    ]);
};

const buildInputPayloadJson = (entry: MingCustomsRecord) => {
    return JSON.stringify({
        title: entry.title || null,
        category: entry.category || null,
        chapter: entry.chapter || null,
        section: entry.section || null,
        summary: entry.summary || null,
        originalExcerpts: entry.originalExcerpts || null,
        content: entry.content || null,
        contentFormat: entry.contentFormat || null
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
                visibility: entry.visibility || null,
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

const readVisibilityValue = (visibility: MingCustomsVisibilityFilter) => {
    return visibility === "ALL" ? undefined : visibility;
};

export const MingCustomsPage = () => {
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
    const [selectedEntryIds, setSelectedEntryIds] = useState<number[]>([]);
    const [batchCandidateEntryIds, setBatchCandidateEntryIds] = useState<number[]>([]);
    const [batchCandidateTitleById, setBatchCandidateTitleById] = useState<Record<number, string>>(
        {}
    );
    const [batchCandidateDrawerOpen, setBatchCandidateDrawerOpen] = useState(false);
    const [batchShareResult, setBatchShareResult] = useState<ClassicsBatchOperationRecord | null>(
        null
    );
    const [batchVisibilityResult, setBatchVisibilityResult] =
        useState<ClassicsBatchOperationRecord | null>(null);
    const [creatingRefinementCapability, setCreatingRefinementCapability] =
        useState<AiRefinementTaskCapability | null>(null);
    const [selectedVersionId, setSelectedVersionId] = useState<number | null>(null);
    const handledSucceededTaskIdsRef = useRef<Set<number>>(new Set());
    const hasActiveFilters = Boolean(
        filters.category ||
        selectedTagFilter ||
        filters.visibility !== "ALL" ||
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
        queryFn: () => service.get(editingEntry?.id ?? 0),
        enabled:
            mingCustomsEditDrawerOpen &&
            mingCustomsEditDrawerMode === "edit" &&
            Boolean(editingEntry?.id),
        retry: false
    });
    const versionsQuery = useQuery({
        queryKey: ["ming-customs", "versions", editingEntry?.id],
        queryFn: () => service.listVersions(editingEntry?.id ?? 0),
        enabled:
            mingCustomsEditDrawerOpen &&
            mingCustomsEditDrawerMode === "edit" &&
            Boolean(editingEntry?.id),
        retry: false
    });
    const mingCustomsVersionDetailQuery = useQuery({
        queryKey: ["ming-customs", "versions", "detail", editingEntry?.id, selectedVersionId],
        queryFn: () => service.getVersion(editingEntry?.id ?? 0, selectedVersionId ?? 0),
        enabled:
            mingCustomsEditDrawerOpen &&
            Boolean(editingEntry?.id) &&
            typeof selectedVersionId === "number" &&
            Number.isInteger(selectedVersionId),
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
    const selectedEntries = useMemo(
        () => records.filter((record) => selectedEntryIds.includes(record.id)),
        [records, selectedEntryIds]
    );
    const canShareEntries = hasClassicsContentPermission("MING_CUSTOMS", "share", hasPermission);
    const canExportEntries = hasClassicsContentPermission("MING_CUSTOMS", "export", hasPermission);
    const canChangeEntryVisibility = hasClassicsContentPermission(
        "MING_CUSTOMS",
        "edit",
        hasPermission
    );
    const refinementTasks = useMemo(
        () => refinementTasksQuery.data?.items || [],
        [refinementTasksQuery.data?.items]
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

    const invalidateMingCandidates = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["ai", "candidates", "MING_CUSTOMS", editingEntryDetail?.id]
        });
    };
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
    const shareMutation = useMutation({
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
    const batchShareMutation = useMutation({
        mutationFn: shareService.createBatch,
        onSuccess: (result) => {
            setBatchShareResult(result);
            messageApi.success(
                `批量分享完成：成功 ${result.successCount}，失败 ${result.failureCount}`
            );
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量分享创建失败");
        }
    });
    const batchVisibilityMutation = useMutation({
        mutationFn: contentService.changeVisibilityBatch,
        onSuccess: async (result) => {
            setBatchVisibilityResult(result);
            await invalidateMingCustoms();
            messageApi.success(
                `批量可见性完成：成功 ${result.successCount}，失败 ${result.failureCount}`
            );
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量可见性修改失败");
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
            messageApi.success("导出任务已提交，请到下方任务列表查看进度。");
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
        onSuccess: async () => {
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
        mutationFn: ({ entryId, versionId }: { entryId: number; versionId: number }) =>
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
                    typeof task.taskId === "number" &&
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
            visibility: readVisibilityValue(filters.visibility),
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
            visibility: undefined,
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
    };

    const openEditMingCustomsDrawer = (entry: MingCustomsRecord) => {
        setMingCustomsEditDrawerMode("edit");
        setEditingEntry(entry);
        setMingCustomsEditDrawerOpen(true);
        setSelectedVersionId(null);
    };

    const closeMingCustomsEditDrawer = () => {
        if (saveMutation.isPending) {
            return;
        }
        setMingCustomsEditDrawerOpen(false);
        setEditingEntry(null);
        setSelectedVersionId(null);
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

    const shareEntry = (entry: MingCustomsRecord) => {
        if (!canShareEntries) {
            messageApi.warning("当前账号缺少明代习俗分享权限");
            return;
        }
        const title = entry.title?.trim() || `条目 ${entry.id}`;
        shareMutation.mutate({
            targets: [
                {
                    contentId: entry.id,
                    contentType: "MING_CUSTOMS"
                }
            ],
            title: `${title} 分享`,
            visibility: "PUBLIC"
        });
    };
    const shareSelectedEntries = () => {
        if (!canShareEntries) {
            messageApi.warning("当前账号缺少明代习俗分享权限");
            return;
        }
        if (!selectedEntries.length) {
            messageApi.warning("请先选择要批量分享的明代习俗");
            return;
        }
        batchShareMutation.mutate({
            privateContentConfirmed: false,
            status: "ACTIVE",
            targets: selectedEntries.map((entry) => ({
                contentId: entry.id,
                contentType: "MING_CUSTOMS"
            })),
            titlePrefix: "明代习俗批量分享 - ",
            visibility: "PUBLIC"
        });
    };

    const changeSelectedVisibility = (visibility: "PRIVATE" | "PUBLIC") => {
        if (!canChangeEntryVisibility) {
            messageApi.warning("当前账号缺少明代习俗编辑权限");
            return;
        }
        if (!selectedEntries.length) {
            messageApi.warning("请先选择要批量修改可见性的明代习俗");
            return;
        }
        batchVisibilityMutation.mutate({
            contentIds: selectedEntries.map((entry) => entry.id),
            contentType: "MING_CUSTOMS",
            visibility
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
            onConfirm: () => deleteExportMutation.mutateAsync(job.id as number)
        });
    };

    const deleteExportJobs = (jobs: ClassicsExportJobRecord[]) => {
        const ids = jobs.map((job) => job.id).filter((id): id is number => id != null);
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
        capability: AiRefinementTaskCapability
    ) => {
        if (!entry.content?.trim() && !entry.originalExcerpts?.trim()) {
            messageApi.warning("正文与原文摘录均为空，无法创建 AI 精修任务");
            return;
        }
        setCreatingRefinementCapability(capability);
        createRefinementTaskMutation.mutate({
            capability,
            scope: "classics",
            contentType: "MING_CUSTOMS",
            contentId: entry.id,
            serviceRole: DEFAULT_REFINEMENT_SERVICE_ROLE,
            modelId: DEFAULT_REFINEMENT_MODEL_ID,
            modelName: DEFAULT_REFINEMENT_MODEL_NAME,
            requestId: createEventId(`ming-customs-${capability}-request`),
            traceId: createEventId(`ming-customs-${capability}-trace`),
            promptMessagesJson: buildPromptMessagesJson(entry, capability),
            promptVariablesJson: JSON.stringify({ capability, title: entry.title || null }),
            inputPayloadJson: buildInputPayloadJson(entry),
            locale: "zh-CN"
        });
    };

    const openBatchCandidateDrawer = () => {
        if (!canChangeEntryVisibility) {
            messageApi.warning("当前账号缺少明代习俗编辑权限");
            return;
        }
        if (!selectedEntries.length) {
            messageApi.warning("请先选择要批量治理的明代习俗");
            return;
        }
        setBatchCandidateEntryIds(selectedEntries.map((entry) => entry.id));
        setBatchCandidateTitleById(
            Object.fromEntries(selectedEntries.map((entry) => [entry.id, readTitle(entry)]))
        );
        setBatchCandidateDrawerOpen(true);
    };

    return (
        <div className="ming-customs-page-root">
            <MingCustomsToolbar
                categoryOptions={categoryOptions}
                filterActive={hasActiveFilters}
                filters={filters}
                onFilterApply={applyFilters}
                onFilterReset={resetFilters}
                onAdd={openCreateMingCustomsDrawer}
                onClearTagFilter={clearTagFilter}
                onFiltersChange={setFilters}
                onSearchChange={searchMingCustoms}
                onSelectTag={selectTag}
                query={query}
                searchValue={searchText}
                selectedTagFilter={selectedTagFilter}
                content={
                    <>
                        <MingCustomsExportActions
                            batchShareResult={batchShareResult}
                            batchVisibilityResult={batchVisibilityResult}
                            canChangeEntryVisibility={canChangeEntryVisibility}
                            canExportEntries={canExportEntries}
                            canShareEntries={canShareEntries}
                            exportJobs={exportJobs}
                            hasExportJobsError={exportJobsQuery.isError}
                            loading={
                                exportJobsQuery.isLoading ||
                                exportMutation.isPending ||
                                deleteExportMutation.isPending
                            }
                            onBatchCandidate={openBatchCandidateDrawer}
                            onBatchDeleteExportJobs={deleteExportJobs}
                            onChangeSelectedVisibility={changeSelectedVisibility}
                            onDeleteExportJob={deleteExportJob}
                            onRefreshExportJobs={() => {
                                void invalidateExportJobs();
                            }}
                            onShareSelectedEntries={shareSelectedEntries}
                            selectedEntriesCount={selectedEntries.length}
                            sharing={batchShareMutation.isPending}
                            visibilityChanging={batchVisibilityMutation.isPending}
                        />
                        <MingCustomsTable
                            canExport={canExportEntries}
                            canShare={canShareEntries}
                            categoryLabels={categoryLabels}
                            loading={mingCustomsQuery.isLoading}
                            dataSource={records}
                            onDelete={deleteEntry}
                            onExport={exportEntry}
                            onOpenEdit={openEditMingCustomsDrawer}
                            onSelectedEntryIdsChange={setSelectedEntryIds}
                            onShare={shareEntry}
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
                        />
                        <AiCandidateBatchDrawer
                            open={batchCandidateDrawerOpen}
                            contentType="MING_CUSTOMS"
                            contentIds={batchCandidateEntryIds}
                            capabilities={["summary", "tags", "qa"]}
                            contentTitleById={batchCandidateTitleById}
                            canEdit={canChangeEntryVisibility}
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
                onClose={closeMingCustomsEditDrawer}
                onSave={(command) => saveMutation.mutate(command)}
                afterForm={
                    mingCustomsEditDrawerMode === "edit" && editingEntryDetail ? (
                        <>
                            <MingCustomsAiActions
                                creatingRefinementCapability={creatingRefinementCapability}
                                entry={editingEntryDetail}
                                onCandidateApplied={invalidateMingCustoms}
                                onCandidateRejected={invalidateMingCandidates}
                                onContentChanged={invalidateMingCustoms}
                                onCreateRefinementTask={createRefinementTask}
                                refinementTasks={refinementTasks}
                            />
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
                        </>
                    ) : null
                }
            />
        </div>
    );
};
