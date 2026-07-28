import {
    FilterOutlined,
    PlusOutlined,
    ReloadOutlined,
    ScheduleOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Input } from "antd";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import {
    KuzhambuFilterPanel,
    KuzhambuPage,
    KuzhambuSpace,
    KuzhambuButton,
    KuzhambuSelect
} from "@/components";

import { AiCandidateBatchDrawer } from "@/pages/classics/common/components/ai-candidate-batch-drawer";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type {
    AiRefinementTaskCapability,
    AiRefinementTaskRecord
} from "@/pages/classics/common/ai-refinement-task-types";
import * as contentService from "@/pages/classics/common/classics-content-service";
import * as exportService from "@/pages/classics/common/classics-export-service";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import * as shareService from "@/pages/classics/common/classics-share-service";
import {
    hasClassicsContentPermission,
    type ClassicsBatchOperationRecord
} from "@/pages/classics/common/classics-content-types";
import type {
    ClassicsExportJobRecord,
    ClassicsExportScopePayload
} from "@/pages/classics/common/classics-export-types";
import { isSameId } from "@/types/id";
import { WangqiDocumentTable } from "./components/wangqi-document-table";
import { WangqiDocumentBatchResults } from "./components/wangqi-document-batch-results";
import { WangqiExportActions } from "./components/wangqi-export-actions";
import { WangqiDocumentEditDrawer } from "./components/wangqi-document-edit-drawer";
import type { WangqiQaTaskPair } from "./components/wangqi-qa-ai-modal";
import { WangqiRefinementActions } from "./components/wangqi-refinement-actions";
import { WangqiStorageFilePanel } from "./components/wangqi-storage-file-panel";
import { WangqiTimeline } from "./components/wangqi-timeline";
import { WangqiVersionPanel } from "./components/wangqi-version-panel";
import * as wangqiService from "./wangqi-service";
import type { WangqiDocumentCommand, WangqiDocumentQuery } from "./wangqi-service";
import type { WangqiContentVersionRecord, WangqiDocumentRecord } from "./wangqi-types";

import "./wangqi-page.css";

type WangqiVisibilityFilter = "ALL" | "PUBLIC" | "PRIVATE";
type WangqiSortDirectionFilter = "ASC" | "DESC";

interface WangqiFilters {
    sortDirection: WangqiSortDirectionFilter;
    visibility: WangqiVisibilityFilter;
}

const DEFAULT_WANGQI_FILTERS: WangqiFilters = {
    sortDirection: "DESC",
    visibility: "ALL"
};
const TASK_POLL_INTERVAL_MS = 3000;
const EXPORT_PAGE_SIZE = 8;
const DEFAULT_REFINEMENT_MODEL_ID = "1";
const DEFAULT_REFINEMENT_MODEL_NAME = "gpt-5.5";
const DEFAULT_REFINEMENT_SERVICE_ROLE = "PRIMARY";

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readVisibilityValue = (visibility: WangqiVisibilityFilter) => {
    return visibility === "ALL" ? undefined : visibility;
};

const createEventId = (prefix: string) => {
    return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
};

const WANGQI_REFINEMENT_PROMPT: Record<AiRefinementTaskCapability, string> = {
    summary: "你是古籍整理助理。请基于输入文稿生成简洁、准确、可直接回填到后台的中文摘要。",
    tags: '你是古籍标签治理助理。请基于输入文稿、已有摘要和已有标签提取稳定、短小、适合后台统一标签库复用的中文标签。只返回 JSON，格式为 {"tags":["标签"]}，不要解释。',
    qa: '你是古籍问答治理助理。请基于输入文稿、已有摘要和已有问答生成可用于知识库检索的中文问答对。只返回 JSON，格式为 {"qaPairs":[{"question":"问题","answer":"答案"}]}，不要解释。'
};

interface RefinementTaskContext {
    existingQaPairs?: WangqiQaTaskPair[];
    existingTags?: string[];
}

const buildPromptMessagesJson = (
    document: WangqiDocumentRecord,
    capability: AiRefinementTaskCapability,
    context: RefinementTaskContext = {}
) => {
    return JSON.stringify([
        {
            role: "system",
            content: WANGQI_REFINEMENT_PROMPT[capability]
        },
        {
            role: "user",
            content: [
                `标题：${document.title || "未命名文档"}`,
                `现有摘要：${document.summary || "暂无"}`,
                ...(capability === "tags"
                    ? [
                          `已有标签：${
                              context.existingTags?.length
                                  ? context.existingTags.join("、")
                                  : "暂无"
                          }`
                      ]
                    : []),
                ...(capability === "qa"
                    ? [
                          `已有问答：${
                              context.existingQaPairs?.length
                                  ? context.existingQaPairs
                                        .map((pair) => `Q：${pair.question} A：${pair.answer}`)
                                        .join("；")
                                  : "暂无"
                          }`
                      ]
                    : []),
                `正文：${document.content || "暂无正文"}`
            ].join("\n")
        }
    ]);
};

const buildInputPayloadJson = (
    document: WangqiDocumentRecord,
    context: RefinementTaskContext = {}
) => {
    return JSON.stringify({
        title: document.title || null,
        summary: document.summary || null,
        content: document.content || null,
        contentFormat: document.contentFormat || null,
        existingQaPairs: context.existingQaPairs || [],
        existingTags: context.existingTags || []
    });
};

const readDocumentTitle = (document: WangqiDocumentRecord) => {
    return document.title?.trim() || `王圻文档 ${document.id}`;
};

const buildSingleDocumentQaUrl = (document: WangqiDocumentRecord) => {
    const search = new URLSearchParams({
        contextContentId: String(document.id),
        contextContentType: "WANGQI_DOCUMENT",
        contextMode: "SINGLE_DOCUMENT",
        title: readDocumentTitle(document)
    });

    return `/discovery/qa?${search.toString()}`;
};

const buildExportScopeJson = (document: WangqiDocumentRecord) => {
    const title = readDocumentTitle(document);
    const scopePayload: ClassicsExportScopePayload = {
        title: `${title} 导出`,
        contentType: "WANGQI_DOCUMENT",
        scopeType: "SELECTED_ITEMS",
        items: [
            {
                id: document.id,
                title,
                text: document.content || "",
                summary: document.summary || null,
                visibility: document.visibility || null,
                documentTime: document.documentTime || null,
                sourceFileStorageObjectId: document.storageObjectId || null
            }
        ]
    };

    return JSON.stringify(scopePayload);
};

export const WangqiPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [query, setQuery] = useState<WangqiDocumentQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE,
        sortDirection: DEFAULT_WANGQI_FILTERS.sortDirection
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<WangqiFilters>(DEFAULT_WANGQI_FILTERS);
    const [wangqiDocumentEditDrawerMode, setWangqiDocumentEditDrawerMode] = useState<
        "create" | "edit"
    >("create");
    const [wangqiDocumentEditDrawerOpen, setWangqiDocumentEditDrawerOpen] = useState(false);
    const [editingDocument, setEditingDocument] = useState<WangqiDocumentRecord | null>(null);
    const [selectedVersionId, setSelectedVersionId] = useState<string | null>(null);
    const [selectedDocumentIds, setSelectedDocumentIds] = useState<string[]>([]);
    const [batchCandidateDocumentIds, setBatchCandidateDocumentIds] = useState<string[]>([]);
    const [batchCandidateTitleById, setBatchCandidateTitleById] = useState<Record<string, string>>(
        {}
    );
    const [batchCandidateDrawerOpen, setBatchCandidateDrawerOpen] = useState(false);
    const [batchShareResult, setBatchShareResult] = useState<ClassicsBatchOperationRecord | null>(
        null
    );
    const [batchVisibilityResult, setBatchVisibilityResult] =
        useState<ClassicsBatchOperationRecord | null>(null);
    const [isFilterOpen, setIsFilterOpen] = useState(false);
    const [exportJobsDrawerOpen, setExportJobsDrawerOpen] = useState(false);
    const [creatingRefinementCapability, setCreatingRefinementCapability] =
        useState<AiRefinementTaskCapability | null>(null);
    const [summaryTrackingTask, setSummaryTrackingTask] = useState<AiRefinementTaskRecord | null>(
        null
    );
    const [tagTrackingTask, setTagTrackingTask] = useState<AiRefinementTaskRecord | null>(null);
    const [qaTrackingTask, setQaTrackingTask] = useState<AiRefinementTaskRecord | null>(null);
    const handledSucceededTaskIdsRef = useRef<Set<string>>(new Set());

    const hasActiveFilters = Boolean(
        filters.visibility !== "ALL" ||
        filters.sortDirection !== DEFAULT_WANGQI_FILTERS.sortDirection
    );

    const wangqiDocumentPageQuery = useQuery({
        queryKey: ["wangqi", "page", query],
        queryFn: () => wangqiService.page(query),
        retry: false
    });
    const wangqiDocumentDetailQuery = useQuery({
        queryKey: ["wangqi", "detail", editingDocument?.id],
        queryFn: () => wangqiService.get(editingDocument?.id ?? ""),
        enabled:
            wangqiDocumentEditDrawerOpen &&
            wangqiDocumentEditDrawerMode === "edit" &&
            Boolean(editingDocument?.id),
        retry: false
    });
    const editingDocumentData = wangqiDocumentDetailQuery.data || editingDocument;
    const exportJobsQuery = useQuery({
        queryKey: ["classics", "wangqi", "exports", "jobs"],
        queryFn: () =>
            exportService.page({
                pageNo: 1,
                pageSize: EXPORT_PAGE_SIZE,
                contentType: "WANGQI_DOCUMENT",
                exportKind: "CONTENT_DATASET"
            }),
        retry: false
    });
    const sourceFileQuery = useQuery({
        queryKey: [
            "wangqi",
            "source-file",
            editingDocumentData?.id,
            editingDocumentData?.storageObjectId
        ],
        queryFn: () => wangqiService.getSourceFile(editingDocumentData?.id ?? ""),
        enabled:
            wangqiDocumentEditDrawerOpen &&
            Boolean(editingDocumentData?.id && editingDocumentData?.storageObjectId),
        retry: false
    });
    const versionsQuery = useQuery({
        queryKey: ["wangqi", "versions", editingDocumentData?.id],
        queryFn: () => wangqiService.listVersions(editingDocumentData?.id ?? ""),
        enabled:
            wangqiDocumentEditDrawerOpen &&
            wangqiDocumentEditDrawerMode === "edit" &&
            Boolean(editingDocumentData?.id),
        retry: false
    });
    const versionDetailQuery = useQuery({
        queryKey: ["wangqi", "version", editingDocumentData?.id, selectedVersionId],
        queryFn: () =>
            wangqiService.getVersion(editingDocumentData?.id ?? "", selectedVersionId ?? ""),
        enabled:
            wangqiDocumentEditDrawerOpen && Boolean(editingDocumentData?.id && selectedVersionId),
        retry: false
    });
    const refinementTasksQuery = useQuery({
        queryKey: ["classics", "wangqi", "refinement", "tasks", editingDocumentData?.id],
        queryFn: () =>
            aiRefinementTaskService.pageTasks({
                pageNo: 1,
                pageSize: 10,
                contentType: "WANGQI_DOCUMENT",
                contentId: editingDocumentData?.id
            }),
        enabled:
            wangqiDocumentEditDrawerOpen &&
            wangqiDocumentEditDrawerMode === "edit" &&
            Boolean(editingDocumentData?.id),
        retry: false,
        refetchInterval: (query) => {
            const tasks = query.state.data?.items || [];
            return tasks.some((task) => task.status === "PENDING" || task.status === "RUNNING")
                ? TASK_POLL_INTERVAL_MS
                : false;
        }
    });

    const pageResult = wangqiDocumentPageQuery.data;
    const records = useMemo(() => pageResult?.records || [], [pageResult?.records]);
    const versions = useMemo(() => {
        return Array.isArray(versionsQuery.data) ? versionsQuery.data : [];
    }, [versionsQuery.data]);
    const totalCount = pageResult?.count ?? pageResult?.totalCount ?? 0;
    const currentPageNo = pageResult?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = pageResult?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;
    const selectedVersion =
        versionDetailQuery.data ||
        versions.find((version) => isSameId(version.id, selectedVersionId)) ||
        null;
    const refinementTasks = useMemo(
        () => refinementTasksQuery.data?.items || [],
        [refinementTasksQuery.data?.items]
    );
    const tagRefinementTasks = useMemo(
        () => refinementTasks.filter((task) => task.capability === "tags"),
        [refinementTasks]
    );
    const summaryRefinementTasks = useMemo(
        () => refinementTasks.filter((task) => task.capability === "summary"),
        [refinementTasks]
    );
    const qaRefinementTasks = useMemo(
        () => refinementTasks.filter((task) => task.capability === "qa"),
        [refinementTasks]
    );
    const exportJobs = exportJobsQuery.data?.records || [];
    const selectedDocuments = useMemo(
        () =>
            records.filter(
                (record) => record.id != null && selectedDocumentIds.includes(String(record.id))
            ),
        [records, selectedDocumentIds]
    );
    const canShareDocuments = hasClassicsContentPermission(
        "WANGQI_DOCUMENT",
        "share",
        hasPermission
    );
    const canChangeDocumentVisibility = hasClassicsContentPermission(
        "WANGQI_DOCUMENT",
        "edit",
        hasPermission
    );
    const canExportDocuments = hasClassicsContentPermission(
        "WANGQI_DOCUMENT",
        "export",
        hasPermission
    );
    const canOpenDiscoveryQa = hasPermission("discovery:qa:view");
    let singleDocumentQaDisabledReason: string | undefined;
    if (editingDocumentData && !editingDocumentData.id) {
        singleDocumentQaDisabledReason = "请先保存王圻文档";
    } else if (!canOpenDiscoveryQa) {
        singleDocumentQaDisabledReason = "缺少 Discovery 问答权限";
    }

    const openBatchCandidateDrawer = () => {
        if (!canChangeDocumentVisibility) {
            messageApi.warning("当前账号缺少王圻文档编辑权限");
            return;
        }
        if (!selectedDocuments.length) {
            messageApi.warning("请先选择要批量治理的王圻文档");
            return;
        }
        setBatchCandidateDocumentIds(selectedDocuments.map((document) => document.id));
        setBatchCandidateTitleById(
            Object.fromEntries(
                selectedDocuments.map((document) => [document.id, readDocumentTitle(document)])
            )
        );
        setBatchCandidateDrawerOpen(true);
    };

    const invalidateWangqi = useCallback(async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["wangqi", "page"] }),
            queryClient.invalidateQueries({ queryKey: ["wangqi", "detail"] }),
            queryClient.invalidateQueries({ queryKey: ["wangqi", "source-file"] }),
            queryClient.invalidateQueries({ queryKey: ["wangqi", "versions"] }),
            queryClient.invalidateQueries({ queryKey: ["wangqi", "version"] }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "tags", "WANGQI_DOCUMENT"]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "qa-pairs", "WANGQI_DOCUMENT"]
            }),
            queryClient.invalidateQueries({
                queryKey: ["ai", "candidates", "WANGQI_DOCUMENT", editingDocumentData?.id]
            })
        ]);
    }, [editingDocumentData?.id, queryClient]);

    const invalidateWangqiCandidates = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["ai", "candidates", "WANGQI_DOCUMENT", editingDocumentData?.id]
        });
    };

    const invalidateRefinementTasks = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "wangqi", "refinement", "tasks", editingDocumentData?.id]
        });
    };
    const invalidateExportJobs = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "wangqi", "exports", "jobs"]
        });
    };

    const updateQuery = (values: Partial<WangqiDocumentQuery>) => {
        setQuery((currentQuery) => ({
            ...currentQuery,
            ...values,
            pageNo: values.pageNo || DEFAULT_PAGE_NO,
            pageSize: values.pageSize || currentQuery.pageSize || DEFAULT_PAGE_SIZE
        }));
    };

    const saveMutation = useMutation({
        mutationFn: (command: WangqiDocumentCommand) =>
            wangqiDocumentEditDrawerMode === "create"
                ? wangqiService.add(command)
                : wangqiService.update(command),
        onSuccess: async () => {
            setWangqiDocumentEditDrawerOpen(false);
            setEditingDocument(null);
            setSelectedVersionId(null);
            await invalidateWangqi();
            messageApi.success(
                wangqiDocumentEditDrawerMode === "create" ? "王圻文档已新增" : "王圻文档已保存"
            );
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });
    const deleteMutation = useMutation({
        mutationFn: wangqiService.deleteById,
        onSuccess: async () => {
            await invalidateWangqi();
            messageApi.success("王圻文档已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });
    const uploadSourceFileMutation = useMutation({
        mutationFn: ({ documentId, file }: { documentId: string; file: File }) =>
            wangqiService.uploadSourceFile(documentId, file),
        onSuccess: async () => {
            await invalidateWangqi();
            messageApi.success("王圻原始文件已上传");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "原始文件上传失败");
        }
    });
    const resetVersionMutation = useMutation({
        mutationFn: ({ documentId, versionId }: { documentId: string; versionId: string }) =>
            wangqiService.resetVersion(documentId, versionId),
        onSuccess: async () => {
            setSelectedVersionId(null);
            await invalidateWangqi();
            messageApi.success("王圻版本已恢复");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "版本恢复失败");
        }
    });
    const shareDocumentMutation = useMutation({
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
            await invalidateWangqi();
            messageApi.success(
                `批量可见性完成：成功 ${result.successCount}，失败 ${result.failureCount}`
            );
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量可见性修改失败");
        }
    });
    const exportMutation = useMutation({
        mutationFn: (document: WangqiDocumentRecord) =>
            exportService.create({
                contentType: "WANGQI_DOCUMENT",
                exportKind: "CONTENT_DATASET",
                exportFormat: "HTML",
                scopeType: "SELECTED_ITEMS",
                scopeJson: buildExportScopeJson(document)
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
        onSuccess: async (task, command) => {
            if (command.capability === "summary") {
                setSummaryTrackingTask(task);
            }
            if (command.capability === "tags") {
                setTagTrackingTask(task);
            }
            if (command.capability === "qa") {
                setQaTrackingTask(task);
            }
            await invalidateRefinementTasks();
            messageApi.success("王圻精修任务已创建");
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
                    Boolean(task.taskId) &&
                    !handledSucceededTaskIdsRef.current.has(task.taskId)
            )
            .map((task) => task.taskId);
        if (!completedTaskIds.length) {
            return;
        }
        completedTaskIds.forEach((taskId) => handledSucceededTaskIdsRef.current.add(taskId));
        void invalidateWangqi();
    }, [invalidateWangqi, refinementTasks]);

    const searchWangqi = (value: string) => {
        setSearchText(value);
        updateQuery({ keyword: normalizeSearch(value) });
    };

    const applyFilters = () => {
        updateQuery({
            visibility: readVisibilityValue(filters.visibility),
            sortDirection: filters.sortDirection
        });
    };

    const sortWangqiDocuments = (sortDirection: WangqiSortDirectionFilter) => {
        setFilters((currentFilters) => ({
            ...currentFilters,
            sortDirection
        }));
        updateQuery({ sortDirection });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_WANGQI_FILTERS);
        updateQuery({
            visibility: undefined,
            sortDirection: DEFAULT_WANGQI_FILTERS.sortDirection
        });
    };

    const openCreateWangqiDocumentDrawer = () => {
        setWangqiDocumentEditDrawerMode("create");
        setEditingDocument(null);
        setSelectedVersionId(null);
        setSummaryTrackingTask(null);
        setTagTrackingTask(null);
        setQaTrackingTask(null);
        setWangqiDocumentEditDrawerOpen(true);
    };

    const openEditWangqiDocumentDrawer = (document: WangqiDocumentRecord) => {
        setWangqiDocumentEditDrawerMode("edit");
        setEditingDocument(document);
        setSelectedVersionId(null);
        setSummaryTrackingTask(null);
        setTagTrackingTask(null);
        setQaTrackingTask(null);
        setWangqiDocumentEditDrawerOpen(true);
    };

    const closeWangqiDocumentEditDrawer = () => {
        if (saveMutation.isPending) {
            return;
        }
        setWangqiDocumentEditDrawerOpen(false);
        setEditingDocument(null);
        setSelectedVersionId(null);
        setSummaryTrackingTask(null);
        setTagTrackingTask(null);
        setQaTrackingTask(null);
        handledSucceededTaskIdsRef.current.clear();
    };

    const deleteDocument = (document: WangqiDocumentRecord) => {
        const title = document.title?.trim() || `文档 ${document.id}`;
        confirm.danger({
            title: "删除王圻文档",
            message: `确认删除 ${title}？`,
            description: "删除后该文档、版本历史和关联引用会按后端流程清理。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(document.id)
        });
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

    const uploadSourceFile = (file: File) => {
        if (!editingDocumentData?.id) {
            messageApi.warning("请先保存王圻文档后再上传原始文件");
            return;
        }
        uploadSourceFileMutation.mutate({ documentId: editingDocumentData.id, file });
    };

    const resetVersion = (version: WangqiContentVersionRecord) => {
        if (!editingDocumentData?.id) {
            return;
        }
        confirm.danger({
            title: "恢复王圻版本",
            message: `确认恢复版本 ${version.versionNo ?? version.id}？`,
            description: "恢复后会产生新的历史恢复版本，并刷新详情、列表、时间线和原始文件元数据。",
            okText: "恢复",
            onConfirm: () =>
                resetVersionMutation.mutateAsync({
                    documentId: editingDocumentData.id,
                    versionId: version.id
                })
        });
    };

    const shareDocument = (document: WangqiDocumentRecord) => {
        if (!canShareDocuments) {
            messageApi.warning("当前账号缺少王圻文档分享权限");
            return;
        }
        const title = readDocumentTitle(document);
        shareDocumentMutation.mutate({
            targets: [
                {
                    contentId: document.id,
                    contentType: "WANGQI_DOCUMENT"
                }
            ],
            title: `${title} 分享`,
            visibility: "PUBLIC"
        });
    };

    const shareSelectedDocuments = () => {
        if (!canShareDocuments) {
            messageApi.warning("当前账号缺少王圻文档分享权限");
            return;
        }
        if (!selectedDocuments.length) {
            messageApi.warning("请先选择要批量分享的王圻文档");
            return;
        }
        batchShareMutation.mutate({
            privateContentConfirmed: false,
            status: "ACTIVE",
            targets: selectedDocuments.map((document) => ({
                contentId: document.id,
                contentType: "WANGQI_DOCUMENT"
            })),
            titlePrefix: "王圻批量分享 - ",
            visibility: "PUBLIC"
        });
    };

    const changeSelectedVisibility = (visibility: "PRIVATE" | "PUBLIC") => {
        if (!canChangeDocumentVisibility) {
            messageApi.warning("当前账号缺少王圻文档编辑权限");
            return;
        }
        if (!selectedDocuments.length) {
            messageApi.warning("请先选择要批量修改可见性的王圻文档");
            return;
        }
        batchVisibilityMutation.mutate({
            contentIds: selectedDocuments.map((document) => document.id),
            contentType: "WANGQI_DOCUMENT",
            visibility
        });
    };

    const exportDocument = (document: WangqiDocumentRecord) => {
        if (!canExportDocuments) {
            messageApi.warning("当前账号缺少王圻文档导出权限");
            return;
        }
        exportMutation.mutate(document);
    };

    const createRefinementTask = (
        document: WangqiDocumentRecord,
        capability: AiRefinementTaskCapability,
        context: RefinementTaskContext = {}
    ) => {
        if (!document.content?.trim()) {
            messageApi.warning("正文为空，无法创建 AI 精修任务");
            return;
        }
        setCreatingRefinementCapability(capability);
        createRefinementTaskMutation.mutate({
            capability,
            scope: "classics",
            contentType: "WANGQI_DOCUMENT",
            contentId: document.id,
            serviceRole: DEFAULT_REFINEMENT_SERVICE_ROLE,
            modelId: DEFAULT_REFINEMENT_MODEL_ID,
            modelName: DEFAULT_REFINEMENT_MODEL_NAME,
            requestId: createEventId(`wangqi-${capability}-request`),
            traceId: createEventId(`wangqi-${capability}-trace`),
            promptMessagesJson: buildPromptMessagesJson(document, capability, context),
            promptVariablesJson: JSON.stringify({
                capability,
                existingQaPairs: context.existingQaPairs || [],
                existingTags: context.existingTags || [],
                title: document.title || null
            }),
            inputPayloadJson: buildInputPayloadJson(document, context),
            locale: "zh-CN"
        });
    };

    const openSingleDocumentQa = (document: WangqiDocumentRecord) => {
        if (!document.id) {
            messageApi.warning("请先保存王圻文档后再发起单文档问答");
            return;
        }
        if (!canOpenDiscoveryQa) {
            messageApi.warning("当前账号缺少 Discovery 问答权限");
            return;
        }
        window.open(buildSingleDocumentQaUrl(document), "_blank", "noopener,noreferrer");
    };

    return (
        <>
            <KuzhambuPage
                className="wangqi-page"
                title="王圻文档"
                description="王圻古籍文档管理入口。"
                actions={
                    <KuzhambuSpace className="wangqi-page-actions">
                        <Input
                            allowClear
                            aria-label="搜索王圻文档"
                            className="wangqi-page-search"
                            placeholder="搜索标题、摘要或正文"
                            prefix={<SearchOutlined />}
                            value={searchText}
                            onChange={(event) => searchWangqi(event.target.value)}
                        />
                        <KuzhambuButton
                            testId="classics-wangqi-wangqi-filter-button"
                            className={
                                isFilterOpen || hasActiveFilters ? "wangqi-page-filter-active" : ""
                            }
                            icon={<FilterOutlined />}
                            aria-expanded={isFilterOpen}
                            onClick={() => setIsFilterOpen((open) => !open)}
                        >
                            筛选
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="classics-wangqi-wangqi-refresh-button"
                            icon={<ReloadOutlined />}
                            onClick={() => void wangqiDocumentPageQuery.refetch()}
                        >
                            刷新
                        </KuzhambuButton>
                        <WangqiTimeline
                            loading={wangqiDocumentPageQuery.isLoading}
                            dataSource={records}
                            onOpenDocument={openEditWangqiDocumentDrawer}
                        />
                        <KuzhambuButton
                            testId="classics-wangqi-wangqi-export-jobs-button"
                            icon={<ScheduleOutlined />}
                            onClick={() => setExportJobsDrawerOpen(true)}
                        >
                            任务
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="classics-wangqi-wangqi-document-create-button"
                            type="primary"
                            icon={<PlusOutlined />}
                            onClick={openCreateWangqiDocumentDrawer}
                        >
                            新增文档
                        </KuzhambuButton>
                    </KuzhambuSpace>
                }
            >
                <KuzhambuFilterPanel
                    actionsAlign="right"
                    open={isFilterOpen}
                    resetDisabled={!hasActiveFilters}
                    fields={[
                        {
                            name: "visibility",
                            label: "可见性",
                            render: () => (
                                <KuzhambuSelect
                                    aria-label="王圻文档可见性"
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
                                <KuzhambuSelect
                                    aria-label="王圻文档排序方向"
                                    value={filters.sortDirection}
                                    options={[
                                        { value: "DESC", label: "新到旧" },
                                        { value: "ASC", label: "旧到新" }
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
                    onApply={() => {
                        applyFilters();
                        setIsFilterOpen(false);
                    }}
                    onReset={resetFilters}
                />
                <div className="wangqi-document-panel">
                    <WangqiDocumentBatchResults
                        batchShareResult={batchShareResult}
                        batchVisibilityResult={batchVisibilityResult}
                    />
                    <WangqiDocumentTable
                        canChangeDocumentVisibility={canChangeDocumentVisibility}
                        canExport={canExportDocuments}
                        canShare={canShareDocuments}
                        isBatchSharing={batchShareMutation.isPending}
                        isBatchVisibilityChanging={batchVisibilityMutation.isPending}
                        loading={wangqiDocumentPageQuery.isLoading}
                        dataSource={records}
                        onChangeSelectedVisibility={changeSelectedVisibility}
                        onDelete={deleteDocument}
                        onExport={exportDocument}
                        onOpenEdit={openEditWangqiDocumentDrawer}
                        onOpenBatchCandidateDrawer={openBatchCandidateDrawer}
                        onShare={shareDocument}
                        onShareSelectedDocuments={shareSelectedDocuments}
                        onSelectedDocumentIdsChange={setSelectedDocumentIds}
                        onSortDirectionChange={sortWangqiDocuments}
                        pagination={{
                            current: currentPageNo,
                            pageSize: currentPageSize,
                            total: totalCount,
                            onChange: (pageNo, pageSize) => updateQuery({ pageNo, pageSize })
                        }}
                        sortDirection={query.sortDirection || DEFAULT_WANGQI_FILTERS.sortDirection}
                        selectedDocumentIds={selectedDocumentIds}
                    />
                </div>
            </KuzhambuPage>
            <WangqiExportActions
                canExportDocuments={canExportDocuments}
                exportJobs={exportJobs}
                loading={
                    exportJobsQuery.isLoading ||
                    exportMutation.isPending ||
                    deleteExportMutation.isPending
                }
                open={exportJobsDrawerOpen}
                onBatchDelete={deleteExportJobs}
                onClose={() => setExportJobsDrawerOpen(false)}
                onDelete={deleteExportJob}
                onRefresh={() => {
                    void invalidateExportJobs();
                }}
            />
            <AiCandidateBatchDrawer
                open={batchCandidateDrawerOpen}
                contentType="WANGQI_DOCUMENT"
                contentIds={batchCandidateDocumentIds}
                capabilities={["summary", "tags", "qa"]}
                contentTitleById={batchCandidateTitleById}
                canEdit={canChangeDocumentVisibility}
                onClose={() => setBatchCandidateDrawerOpen(false)}
                onChanged={invalidateWangqi}
            />
            <WangqiDocumentEditDrawer
                document={editingDocumentData}
                loading={wangqiDocumentDetailQuery.isLoading}
                mode={wangqiDocumentEditDrawerMode}
                open={wangqiDocumentEditDrawerOpen}
                saving={saveMutation.isPending}
                creatingSummaryTask={creatingRefinementCapability === "summary"}
                summaryTasks={summaryRefinementTasks}
                summaryTrackingTask={summaryTrackingTask}
                onCreateSummaryTask={
                    wangqiDocumentEditDrawerMode === "edit" && editingDocumentData
                        ? () => createRefinementTask(editingDocumentData, "summary")
                        : undefined
                }
                onSummaryTaskChange={setSummaryTrackingTask}
                onClose={closeWangqiDocumentEditDrawer}
                onSave={(command) => saveMutation.mutate(command)}
                tagContent={
                    wangqiDocumentEditDrawerMode === "edit" && editingDocumentData ? (
                        <WangqiRefinementActions
                            canOpenDiscoveryQa={canOpenDiscoveryQa}
                            creatingRefinementCapability={creatingRefinementCapability}
                            document={editingDocumentData}
                            qaTasks={qaRefinementTasks}
                            qaTrackingTask={qaTrackingTask}
                            section="tags"
                            singleDocumentQaDisabledReason={singleDocumentQaDisabledReason}
                            tagTasks={tagRefinementTasks}
                            tagTrackingTask={tagTrackingTask}
                            onChanged={invalidateWangqi}
                            onCreateQaTask={(existingQaPairs) =>
                                createRefinementTask(editingDocumentData, "qa", {
                                    existingQaPairs
                                })
                            }
                            onCreateTagTask={(existingTags) =>
                                createRefinementTask(editingDocumentData, "tags", {
                                    existingTags
                                })
                            }
                            onOpenSingleDocumentQa={openSingleDocumentQa}
                            onQaTaskChange={setQaTrackingTask}
                            onRejectedCandidate={invalidateWangqiCandidates}
                            onTagTaskChange={setTagTrackingTask}
                        />
                    ) : null
                }
                qaContent={
                    wangqiDocumentEditDrawerMode === "edit" && editingDocumentData ? (
                        <WangqiRefinementActions
                            canOpenDiscoveryQa={canOpenDiscoveryQa}
                            creatingRefinementCapability={creatingRefinementCapability}
                            document={editingDocumentData}
                            qaTasks={qaRefinementTasks}
                            qaTrackingTask={qaTrackingTask}
                            section="qa"
                            singleDocumentQaDisabledReason={singleDocumentQaDisabledReason}
                            tagTasks={tagRefinementTasks}
                            tagTrackingTask={tagTrackingTask}
                            onChanged={invalidateWangqi}
                            onCreateQaTask={(existingQaPairs) =>
                                createRefinementTask(editingDocumentData, "qa", {
                                    existingQaPairs
                                })
                            }
                            onCreateTagTask={(existingTags) =>
                                createRefinementTask(editingDocumentData, "tags", {
                                    existingTags
                                })
                            }
                            onOpenSingleDocumentQa={openSingleDocumentQa}
                            onQaTaskChange={setQaTrackingTask}
                            onRejectedCandidate={invalidateWangqiCandidates}
                            onTagTaskChange={setTagTrackingTask}
                        />
                    ) : null
                }
                sourceFileContent={
                    wangqiDocumentEditDrawerMode === "edit" && editingDocumentData ? (
                        <WangqiStorageFilePanel
                            document={editingDocumentData}
                            loading={sourceFileQuery.isLoading || sourceFileQuery.isFetching}
                            sourceFile={sourceFileQuery.data}
                            uploading={uploadSourceFileMutation.isPending}
                            onRefresh={() => void sourceFileQuery.refetch()}
                            onUpload={uploadSourceFile}
                        />
                    ) : null
                }
                versionContent={
                    wangqiDocumentEditDrawerMode === "edit" && editingDocumentData ? (
                        <WangqiVersionPanel
                            currentDocument={editingDocumentData}
                            detailLoading={versionDetailQuery.isLoading}
                            listLoading={versionsQuery.isLoading}
                            resetting={resetVersionMutation.isPending}
                            selectedVersion={selectedVersion}
                            versions={versions}
                            onSelectVersion={(version) => setSelectedVersionId(version.id)}
                            onResetVersion={resetVersion}
                        />
                    ) : null
                }
            />
        </>
    );
};
