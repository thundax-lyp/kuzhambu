import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Card, Select } from "antd";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import { AiCandidatePanel } from "@/pages/classics/common/components/ai-candidate-panel";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import { ClassicsExportJobSection } from "@/pages/classics/common/components/classics-export-job-section";
import * as contentService from "@/pages/classics/common/classics-content-service";
import * as exportService from "@/pages/classics/common/classics-export-service";
import * as shareService from "@/pages/classics/common/classics-share-service";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { ClassicsContentTagPanel } from "@/pages/classics/common/components/classics-content-tag-panel";
import { ClassicsContentQaPanel } from "@/pages/classics/common/components/classics-content-qa-panel";
import * as currentUserService from "@/service/current-user-service";
import {
    hasClassicsContentPermission,
    type ClassicsBatchOperationRecord
} from "@/pages/classics/common/classics-content-types";
import type { ClassicsExportScopePayload } from "@/pages/classics/common/classics-export-types";
import { MingCustomsKeywordCloud } from "./components/ming-customs-keyword-cloud";
import { MingCustomsList } from "./components/ming-customs-list";
import { MingCustomsModel } from "./components/ming-customs-model";
import * as service from "./ming-customs-service";
import type { MingCustomsCommand, MingCustomsQuery } from "./ming-customs-service";
import type { MingCustomsRecord } from "./ming-customs-types";
import "./ming-customs-page.css";

type MingCustomsVisibilityFilter = "ALL" | "PUBLIC" | "PRIVATE";
type MingCustomsSortDirectionFilter = "ASC" | "DESC";

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

const buildPromptMessagesJson = (entry: MingCustomsRecord) => {
    return JSON.stringify([
        {
            role: "system",
            content: "你是明代风俗整理助理。请基于输入条目提炼准确、紧凑、便于后台回填的中文摘要。"
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

interface MingCustomsFilters {
    category: string;
    sortDirection: MingCustomsSortDirectionFilter;
    visibility: MingCustomsVisibilityFilter;
}

const DEFAULT_MING_CUSTOMS_FILTERS: MingCustomsFilters = {
    category: "",
    sortDirection: "DESC",
    visibility: "ALL"
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
    const [editorMode, setEditorMode] = useState<"create" | "edit">("create");
    const [editorOpen, setEditorOpen] = useState(false);
    const [editingEntry, setEditingEntry] = useState<MingCustomsRecord | null>(null);
    const [selectedEntryIds, setSelectedEntryIds] = useState<number[]>([]);
    const [batchShareResult, setBatchShareResult] = useState<ClassicsBatchOperationRecord | null>(
        null
    );
    const [batchVisibilityResult, setBatchVisibilityResult] =
        useState<ClassicsBatchOperationRecord | null>(null);
    const [creatingRefinementCapability, setCreatingRefinementCapability] = useState<
        "summary" | null
    >(null);
    const handledSucceededTaskIdsRef = useRef<Set<number>>(new Set());
    const hasActiveFilters = Boolean(
        filters.category ||
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
    const detailQuery = useQuery({
        queryKey: ["ming-customs", "detail", editingEntry?.id],
        queryFn: () => service.get(editingEntry?.id ?? 0),
        enabled: editorOpen && editorMode === "edit" && Boolean(editingEntry?.id),
        retry: false
    });
    const currentUserQuery = useQuery({
        queryKey: ["sys", "current-user", "info"],
        queryFn: currentUserService.getCurrentUserInfo,
        retry: false
    });
    const currentUserId = Number(currentUserQuery.data?.id ?? 0);
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
                contentId: editingEntry?.id,
                capability: "summary"
            }),
        enabled: editorOpen && editorMode === "edit" && Boolean(editingEntry?.id),
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
    const editorEntry = detailQuery.data || editingEntry;
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

    const invalidateMingCustoms = useCallback(async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["ming-customs", "page"] }),
            queryClient.invalidateQueries({ queryKey: ["ming-customs", "keyword-cloud"] }),
            queryClient.invalidateQueries({ queryKey: ["ming-customs", "detail"] }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "tags", "MING_CUSTOMS"]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "qa-pairs", "MING_CUSTOMS"]
            }),
            queryClient.invalidateQueries({
                queryKey: ["ai", "candidates", "MING_CUSTOMS", editorEntry?.id]
            })
        ]);
    }, [editorEntry?.id, queryClient]);
    const invalidateExportJobs = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "ming-customs", "exports", "jobs"]
        });
    };
    const invalidateRefinementTasks = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "ming-customs", "refinement", "tasks", editorEntry?.id]
        });
    };

    const saveMutation = useMutation({
        mutationFn: (command: MingCustomsCommand) =>
            editorMode === "create" ? service.add(command) : service.update(command),
        onSuccess: async () => {
            setEditorOpen(false);
            setEditingEntry(null);
            await invalidateMingCustoms();
            messageApi.success(editorMode === "create" ? "明代习俗已新增" : "明代习俗已保存");
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
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const resetFilters = () => {
        setFilters(DEFAULT_MING_CUSTOMS_FILTERS);
        setQuery((currentQuery) => ({
            ...currentQuery,
            category: undefined,
            visibility: undefined,
            sortDirection: DEFAULT_MING_CUSTOMS_FILTERS.sortDirection,
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const selectKeyword = (keyword: string) => {
        setSearchText(keyword);
        setQuery((currentQuery) => ({
            ...currentQuery,
            keyword,
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const openCreateEditor = () => {
        setEditorMode("create");
        setEditingEntry(null);
        setEditorOpen(true);
    };

    const openEditEditor = (entry: MingCustomsRecord) => {
        setEditorMode("edit");
        setEditingEntry(entry);
        setEditorOpen(true);
    };

    const closeEditor = () => {
        if (saveMutation.isPending) {
            return;
        }
        setEditorOpen(false);
        setEditingEntry(null);
        handledSucceededTaskIdsRef.current.clear();
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

    const createRefinementTask = (entry: MingCustomsRecord) => {
        const requestedBy = currentUserQuery.data?.id;
        if (!requestedBy) {
            messageApi.warning("当前用户信息未加载完成，请稍后重试");
            return;
        }
        if (!entry.content?.trim() && !entry.originalExcerpts?.trim()) {
            messageApi.warning("正文与原文摘录均为空，无法创建摘要精修任务");
            return;
        }
        setCreatingRefinementCapability("summary");
        createRefinementTaskMutation.mutate({
            capability: "summary",
            scope: "classics",
            contentType: "MING_CUSTOMS",
            contentId: entry.id,
            requestedBy: currentUserId,
            serviceRole: DEFAULT_REFINEMENT_SERVICE_ROLE,
            modelId: DEFAULT_REFINEMENT_MODEL_ID,
            modelName: DEFAULT_REFINEMENT_MODEL_NAME,
            requestId: createEventId("ming-customs-summary-request"),
            traceId: createEventId("ming-customs-summary-trace"),
            promptMessagesJson: buildPromptMessagesJson(entry),
            promptVariablesJson: JSON.stringify({ title: entry.title || null }),
            inputPayloadJson: buildInputPayloadJson(entry),
            locale: "zh-CN"
        });
    };

    return (
        <>
            <KuzhambuListPage<MingCustomsRecord>
                pageClassName="ming-customs-page"
                title="明代习俗"
                description="明代习俗专题条目治理入口。"
                subjectName="明代习俗"
                enableSearch
                enableFilter
                enableAdd
                addText="新增明代习俗"
                filterActive={hasActiveFilters}
                filterFields={[
                    {
                        name: "category",
                        label: "分类",
                        render: () => (
                            <Select
                                allowClear
                                aria-label="明代习俗分类"
                                placeholder="全部分类"
                                value={filters.category || undefined}
                                options={categoryOptions.map((option) => ({
                                    value: option.value,
                                    label: option.label
                                }))}
                                onChange={(value) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        category: value || ""
                                    }))
                                }
                            />
                        )
                    },
                    {
                        name: "visibility",
                        label: "可见性",
                        render: () => (
                            <Select
                                aria-label="明代习俗可见性"
                                value={filters.visibility}
                                options={[
                                    { value: "ALL", label: "全部" },
                                    { value: "PUBLIC", label: "公开" },
                                    { value: "PRIVATE", label: "私有" }
                                ]}
                                onChange={(value) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        visibility: value
                                    }))
                                }
                            />
                        )
                    },
                    {
                        name: "sortDirection",
                        label: "排序",
                        render: () => (
                            <Select
                                aria-label="明代习俗排序方向"
                                value={filters.sortDirection}
                                options={[
                                    { value: "DESC", label: "最新优先" },
                                    { value: "ASC", label: "最早优先" }
                                ]}
                                onChange={(value) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        sortDirection: value
                                    }))
                                }
                            />
                        )
                    }
                ]}
                onFilterApply={applyFilters}
                onFilterReset={resetFilters}
                onAdd={openCreateEditor}
                pageActions={
                    <MingCustomsKeywordCloud
                        visibility={query.visibility}
                        onSelect={selectKeyword}
                    />
                }
                searchValue={searchText}
                onSearchChange={searchMingCustoms}
                content={
                    <>
                        {exportJobsQuery.isError ? (
                            <Alert
                                type="warning"
                                showIcon
                                title="导出任务列表加载失败"
                                description="请确认后台导出任务接口可用后重试。"
                            />
                        ) : null}
                        <ClassicsExportJobSection
                            items={exportJobs}
                            loading={exportJobsQuery.isLoading || exportMutation.isPending}
                            onDownload={(job) => {
                                if (job.downloadUrl) {
                                    window.open(job.downloadUrl, "_blank", "noopener,noreferrer");
                                }
                            }}
                            onRefresh={() => {
                                void invalidateExportJobs();
                            }}
                        />
                        <div style={{ marginBottom: 12 }}>
                            <Button
                                disabled={!selectedEntries.length || !canShareEntries}
                                loading={batchShareMutation.isPending}
                                onClick={shareSelectedEntries}
                            >
                                批量分享
                            </Button>
                            <Button
                                disabled={!selectedEntries.length || !canChangeEntryVisibility}
                                loading={batchVisibilityMutation.isPending}
                                style={{ marginLeft: 8 }}
                                onClick={() => changeSelectedVisibility("PUBLIC")}
                            >
                                批量公开
                            </Button>
                            <Button
                                disabled={!selectedEntries.length || !canChangeEntryVisibility}
                                loading={batchVisibilityMutation.isPending}
                                style={{ marginLeft: 8 }}
                                onClick={() => changeSelectedVisibility("PRIVATE")}
                            >
                                批量私有
                            </Button>
                        </div>
                        {batchShareResult ? (
                            <Alert
                                showIcon
                                type={batchShareResult.failureCount > 0 ? "warning" : "success"}
                                style={{ marginBottom: 12 }}
                                message={`批量分享结果：成功 ${batchShareResult.successCount}，失败 ${batchShareResult.failureCount}`}
                                description={
                                    batchShareResult.failures.length
                                        ? batchShareResult.failures
                                              .map(
                                                  (item) =>
                                                      `${item.contentType}#${item.contentId}: ${item.failureReason || item.failureCode || "未知失败"}`
                                              )
                                              .join("；")
                                        : "全部选中明代习俗已创建分享记录。"
                                }
                            />
                        ) : null}
                        {batchVisibilityResult ? (
                            <Alert
                                showIcon
                                type={
                                    batchVisibilityResult.failureCount > 0 ? "warning" : "success"
                                }
                                style={{ marginBottom: 12 }}
                                message={`批量可见性结果：成功 ${batchVisibilityResult.successCount}，失败 ${batchVisibilityResult.failureCount}`}
                                description={
                                    batchVisibilityResult.failures.length
                                        ? batchVisibilityResult.failures
                                              .map(
                                                  (item) =>
                                                      `${item.contentType}#${item.contentId}: ${item.failureReason || item.failureCode || "未知失败"}`
                                              )
                                              .join("；")
                                        : "全部选中明代习俗已更新可见性。"
                                }
                            />
                        ) : null}
                        <MingCustomsList
                            canExport={canExportEntries}
                            canShare={canShareEntries}
                            categoryLabels={categoryLabels}
                            loading={mingCustomsQuery.isLoading}
                            dataSource={records}
                            onDelete={deleteEntry}
                            onExport={exportEntry}
                            onOpenEdit={openEditEditor}
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
                    </>
                }
            />
            <MingCustomsModel
                categoryOptions={categoryOptions}
                entry={editorEntry}
                loading={detailQuery.isLoading}
                mode={editorMode}
                open={editorOpen}
                saving={saveMutation.isPending}
                onClose={closeEditor}
                onSave={(command) => saveMutation.mutate(command)}
                afterForm={
                    editorMode === "edit" && editorEntry ? (
                        <>
                            <Card
                                size="small"
                                title="AI 精修任务"
                                extra={
                                    <Button
                                        type="primary"
                                        onClick={() => createRefinementTask(editorEntry)}
                                        loading={creatingRefinementCapability === "summary"}
                                    >
                                        创建摘要任务
                                    </Button>
                                }
                            >
                                {refinementTasks.length ? (
                                    refinementTasks.slice(0, 4).map((task) => (
                                        <div key={task.taskId}>
                                            {task.capability}：{task.status}
                                            {task.resultPreview ? ` · ${task.resultPreview}` : ""}
                                        </div>
                                    ))
                                ) : (
                                    <div>暂无精修任务</div>
                                )}
                            </Card>
                            <AiCandidatePanel
                                capabilities={["summary", "tags", "qa"]}
                                contentId={editorEntry.id}
                                contentType="MING_CUSTOMS"
                                onApplied={async () => {
                                    await invalidateMingCustoms();
                                }}
                            />
                            <ClassicsContentTagPanel
                                contentId={editorEntry.id}
                                contentType="MING_CUSTOMS"
                                onChanged={invalidateMingCustoms}
                            />
                            <ClassicsContentQaPanel
                                contentId={editorEntry.id}
                                contentType="MING_CUSTOMS"
                                onChanged={invalidateMingCustoms}
                            />
                        </>
                    ) : null
                }
            />
        </>
    );
};
