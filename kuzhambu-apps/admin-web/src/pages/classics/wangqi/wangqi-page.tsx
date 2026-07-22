import {
    FilterOutlined,
    PlusOutlined,
    ReloadOutlined,
    ScheduleOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Card, Input, Select, Tooltip } from "antd";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuFilterPanel } from "@/components/kuzhambu-filter-panel";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace, KuzhambuSpaceCompact } from "@/components/kuzhambu-space";
import { AiCandidateBatchDrawer } from "@/pages/classics/common/components/ai-candidate-batch-drawer";
import { AiCandidatePanel } from "@/pages/classics/common/components/ai-candidate-panel";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type {
    AiRefinementTaskCapability,
    AiRefinementTaskRecord
} from "@/pages/classics/common/ai-refinement-task-types";
import * as contentService from "@/pages/classics/common/classics-content-service";
import * as exportService from "@/pages/classics/common/classics-export-service";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { ClassicsContentQaPanel } from "@/pages/classics/common/components/classics-content-qa-panel";
import { ClassicsContentTagPanel } from "@/pages/classics/common/components/classics-content-tag-panel";
import { ClassicsExportJobSection } from "@/pages/classics/common/components/classics-export-job-section";
import * as shareService from "@/pages/classics/common/classics-share-service";
import * as currentUserService from "@/service/current-user-service";
import {
    hasClassicsContentPermission,
    type ClassicsBatchOperationRecord
} from "@/pages/classics/common/classics-content-types";
import type {
    ClassicsExportJobRecord,
    ClassicsExportScopePayload
} from "@/pages/classics/common/classics-export-types";
import { WangqiDocumentTable } from "./components/wangqi-document-table";
import { WangqiDocumentToolbar } from "./components/wangqi-document-toolbar";
import { WangqiDocumentEditDrawer } from "./components/wangqi-document-edit-drawer";
import { WangqiQaAiModal, type WangqiQaTaskPair } from "./components/wangqi-qa-ai-modal";
import { WangqiStorageFilePanel } from "./components/wangqi-storage-file-panel";
import { WangqiTagAiModal } from "./components/wangqi-tag-ai-modal";
import { WangqiTimeline } from "./components/wangqi-timeline";
import { WangqiVersionHistoryPanel } from "./components/wangqi-version-history-panel";
import * as wangqiService from "./wangqi-service";
import type { WangqiDocumentCommand, WangqiDocumentQuery } from "./wangqi-service";
import type { WangqiContentVersionRecord, WangqiDocumentRecord } from "./wangqi-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
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
const DEFAULT_REFINEMENT_MODEL_ID = 1;
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
    const [editorMode, setEditorMode] = useState<"create" | "edit">("create");
    const [editorOpen, setEditorOpen] = useState(false);
    const [editingDocument, setEditingDocument] = useState<WangqiDocumentRecord | null>(null);
    const [selectedVersionId, setSelectedVersionId] = useState<number | null>(null);
    const [selectedDocumentIds, setSelectedDocumentIds] = useState<number[]>([]);
    const [batchCandidateDocumentIds, setBatchCandidateDocumentIds] = useState<number[]>([]);
    const [batchCandidateTitleById, setBatchCandidateTitleById] = useState<Record<number, string>>(
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
    const handledSucceededTaskIdsRef = useRef<Set<number>>(new Set());

    const hasActiveFilters = Boolean(
        filters.visibility !== "ALL" ||
        filters.sortDirection !== DEFAULT_WANGQI_FILTERS.sortDirection
    );

    const pageQuery = useQuery({
        queryKey: ["wangqi", "page", query],
        queryFn: () => wangqiService.page(query),
        retry: false
    });
    const detailQuery = useQuery({
        queryKey: ["wangqi", "detail", editingDocument?.id],
        queryFn: () => wangqiService.get(editingDocument?.id ?? 0),
        enabled: editorOpen && editorMode === "edit" && Boolean(editingDocument?.id),
        retry: false
    });
    const activeDocument = detailQuery.data || editingDocument;
    const currentUserQuery = useQuery({
        queryKey: ["sys", "current-user", "info"],
        queryFn: currentUserService.getCurrentUserInfo,
        retry: false
    });
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
    const currentUserId = Number(currentUserQuery.data?.id ?? 0);
    const sourceFileQuery = useQuery({
        queryKey: ["wangqi", "source-file", activeDocument?.id, activeDocument?.storageObjectId],
        queryFn: () => wangqiService.getSourceFile(activeDocument?.id ?? 0),
        enabled: editorOpen && Boolean(activeDocument?.id && activeDocument?.storageObjectId),
        retry: false
    });
    const versionsQuery = useQuery({
        queryKey: ["wangqi", "versions", activeDocument?.id],
        queryFn: () => wangqiService.listVersions(activeDocument?.id ?? 0),
        enabled: editorOpen && editorMode === "edit" && Boolean(activeDocument?.id),
        retry: false
    });
    const versionDetailQuery = useQuery({
        queryKey: ["wangqi", "version", activeDocument?.id, selectedVersionId],
        queryFn: () => wangqiService.getVersion(activeDocument?.id ?? 0, selectedVersionId ?? 0),
        enabled: editorOpen && Boolean(activeDocument?.id && selectedVersionId),
        retry: false
    });
    const refinementTasksQuery = useQuery({
        queryKey: ["classics", "wangqi", "refinement", "tasks", activeDocument?.id],
        queryFn: () =>
            aiRefinementTaskService.pageTasks({
                pageNo: 1,
                pageSize: 10,
                contentType: "WANGQI_DOCUMENT",
                contentId: activeDocument?.id
            }),
        enabled: editorOpen && editorMode === "edit" && Boolean(activeDocument?.id),
        retry: false,
        refetchInterval: (query) => {
            const tasks = query.state.data?.items || [];
            return tasks.some((task) => task.status === "PENDING" || task.status === "RUNNING")
                ? TASK_POLL_INTERVAL_MS
                : false;
        }
    });

    const pageResult = pageQuery.data;
    const records = useMemo(() => pageResult?.records || [], [pageResult?.records]);
    const versions = useMemo(() => {
        return Array.isArray(versionsQuery.data) ? versionsQuery.data : [];
    }, [versionsQuery.data]);
    const totalCount = pageResult?.count ?? pageResult?.totalCount ?? 0;
    const currentPageNo = pageResult?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = pageResult?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;
    const selectedVersion =
        versionDetailQuery.data ||
        versions.find((version) => version.id === selectedVersionId) ||
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
        () => records.filter((record) => selectedDocumentIds.includes(record.id)),
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
    if (activeDocument && !activeDocument.id) {
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
                queryKey: ["ai", "candidates", "WANGQI_DOCUMENT", activeDocument?.id]
            })
        ]);
    }, [activeDocument?.id, queryClient]);

    const invalidateWangqiCandidates = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["ai", "candidates", "WANGQI_DOCUMENT", activeDocument?.id]
        });
    };

    const invalidateRefinementTasks = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "wangqi", "refinement", "tasks", activeDocument?.id]
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
            editorMode === "create" ? wangqiService.add(command) : wangqiService.update(command),
        onSuccess: async () => {
            setEditorOpen(false);
            setEditingDocument(null);
            setSelectedVersionId(null);
            await invalidateWangqi();
            messageApi.success(editorMode === "create" ? "王圻文档已新增" : "王圻文档已保存");
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
        mutationFn: ({ documentId, file }: { documentId: number; file: File }) =>
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
        mutationFn: ({ documentId, versionId }: { documentId: number; versionId: number }) =>
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
                    typeof task.taskId === "number" &&
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

    const openCreateEditor = () => {
        setEditorMode("create");
        setEditingDocument(null);
        setSelectedVersionId(null);
        setSummaryTrackingTask(null);
        setTagTrackingTask(null);
        setQaTrackingTask(null);
        setEditorOpen(true);
    };

    const openEditEditor = (document: WangqiDocumentRecord) => {
        setEditorMode("edit");
        setEditingDocument(document);
        setSelectedVersionId(null);
        setSummaryTrackingTask(null);
        setTagTrackingTask(null);
        setQaTrackingTask(null);
        setEditorOpen(true);
    };

    const closeEditor = () => {
        if (saveMutation.isPending) {
            return;
        }
        setEditorOpen(false);
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

    const uploadSourceFile = (file: File) => {
        if (!activeDocument?.id) {
            messageApi.warning("请先保存王圻文档后再上传原始文件");
            return;
        }
        uploadSourceFileMutation.mutate({ documentId: activeDocument.id, file });
    };

    const resetVersion = (version: WangqiContentVersionRecord) => {
        if (!activeDocument?.id) {
            return;
        }
        confirm.danger({
            title: "恢复王圻版本",
            message: `确认恢复版本 ${version.versionNo ?? version.id}？`,
            description: "恢复后会产生新的历史恢复版本，并刷新详情、列表、时间线和原始文件元数据。",
            okText: "恢复",
            onConfirm: () =>
                resetVersionMutation.mutateAsync({
                    documentId: activeDocument.id,
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
        const requestedBy = currentUserQuery.data?.id;
        if (!requestedBy) {
            messageApi.warning("当前用户信息未加载完成，请稍后重试");
            return;
        }
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
            requestedBy: currentUserId,
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
                            onClick={() => void pageQuery.refetch()}
                        >
                            刷新
                        </KuzhambuButton>
                        <WangqiTimeline
                            loading={pageQuery.isLoading}
                            dataSource={records}
                            onOpenDocument={openEditEditor}
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
                            onClick={openCreateEditor}
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
                                <Select
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
                                <Select
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
                    <WangqiDocumentToolbar
                        batchShareResult={batchShareResult}
                        batchVisibilityResult={batchVisibilityResult}
                        canChangeDocumentVisibility={canChangeDocumentVisibility}
                        canShareDocuments={canShareDocuments}
                        isBatchSharing={batchShareMutation.isPending}
                        isBatchVisibilityChanging={batchVisibilityMutation.isPending}
                        recordCount={records.length}
                        selectedCount={selectedDocuments.length}
                        onChangeSelectedVisibility={changeSelectedVisibility}
                        onOpenBatchCandidateDrawer={openBatchCandidateDrawer}
                        onShareSelectedDocuments={shareSelectedDocuments}
                    />
                    <WangqiDocumentTable
                        canExport={canExportDocuments}
                        canShare={canShareDocuments}
                        loading={pageQuery.isLoading}
                        dataSource={records}
                        onDelete={deleteDocument}
                        onExport={exportDocument}
                        onOpenEdit={openEditEditor}
                        onShare={shareDocument}
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
            <KuzhambuDrawer
                testId="classics-wangqi-wangqi-drawer"
                aria-label="王圻导出任务"
                destroyOnHidden
                open={exportJobsDrawerOpen}
                size="large"
                title="导出任务"
                onClose={() => setExportJobsDrawerOpen(false)}
            >
                <ClassicsExportJobSection
                    items={exportJobs}
                    loading={
                        exportJobsQuery.isLoading ||
                        exportMutation.isPending ||
                        deleteExportMutation.isPending
                    }
                    onDownload={(job) => {
                        if (job.downloadUrl) {
                            window.open(job.downloadUrl, "_blank", "noopener,noreferrer");
                        }
                    }}
                    onDelete={canExportDocuments ? deleteExportJob : undefined}
                    onBatchDelete={canExportDocuments ? deleteExportJobs : undefined}
                    onRefresh={() => {
                        void invalidateExportJobs();
                    }}
                />
            </KuzhambuDrawer>
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
                document={activeDocument}
                loading={detailQuery.isLoading}
                mode={editorMode}
                open={editorOpen}
                saving={saveMutation.isPending}
                creatingSummaryTask={creatingRefinementCapability === "summary"}
                summaryTasks={summaryRefinementTasks}
                summaryTrackingTask={summaryTrackingTask}
                onCreateSummaryTask={
                    editorMode === "edit" && activeDocument
                        ? () => createRefinementTask(activeDocument, "summary")
                        : undefined
                }
                onClose={closeEditor}
                onSave={(command) => saveMutation.mutate(command)}
                tagContent={
                    editorMode === "edit" && activeDocument ? (
                        <div className="wangqi-page-drawer-panels">
                            <ClassicsContentTagPanel
                                contentId={activeDocument.id}
                                contentType="WANGQI_DOCUMENT"
                                showHeader={false}
                                toolbarExtra={
                                    <WangqiTagAiModal
                                        creatingTagTask={creatingRefinementCapability === "tags"}
                                        document={activeDocument}
                                        tagTasks={tagRefinementTasks}
                                        tagTrackingTask={tagTrackingTask}
                                        onChanged={invalidateWangqi}
                                        onCreateTagTask={(existingTags) =>
                                            createRefinementTask(activeDocument, "tags", {
                                                existingTags
                                            })
                                        }
                                    />
                                }
                                onChanged={invalidateWangqi}
                            />
                            <AiCandidatePanel
                                capabilities={["tags"]}
                                contentId={activeDocument.id}
                                contentType="WANGQI_DOCUMENT"
                                onApplied={async () => {
                                    await invalidateWangqi();
                                }}
                                onRejected={async () => {
                                    await invalidateWangqiCandidates();
                                }}
                            />
                        </div>
                    ) : null
                }
                qaContent={
                    editorMode === "edit" && activeDocument ? (
                        <div className="wangqi-page-drawer-panels">
                            <Card
                                size="small"
                                title="问答生成"
                                extra={
                                    <KuzhambuSpaceCompact>
                                        <Tooltip title={singleDocumentQaDisabledReason}>
                                            <KuzhambuButton
                                                testId="classics-wangqi-wangqi-action-button-2"
                                                disabled={!activeDocument.id || !canOpenDiscoveryQa}
                                                onClick={() => openSingleDocumentQa(activeDocument)}
                                            >
                                                单文档问答
                                            </KuzhambuButton>
                                        </Tooltip>
                                        <WangqiQaAiModal
                                            creatingQaTask={creatingRefinementCapability === "qa"}
                                            document={activeDocument}
                                            qaTasks={qaRefinementTasks}
                                            qaTrackingTask={qaTrackingTask}
                                            onChanged={invalidateWangqi}
                                            onCreateQaTask={(existingQaPairs) =>
                                                createRefinementTask(activeDocument, "qa", {
                                                    existingQaPairs
                                                })
                                            }
                                        />
                                    </KuzhambuSpaceCompact>
                                }
                            >
                                <div className="wangqi-refinement-task-list">
                                    {qaRefinementTasks.length ? (
                                        qaRefinementTasks.slice(0, 3).map((task) => (
                                            <div key={task.taskId}>
                                                问答：{task.status}
                                                {task.resultPreview
                                                    ? ` · ${task.resultPreview}`
                                                    : ""}
                                            </div>
                                        ))
                                    ) : (
                                        <div>暂无问答任务</div>
                                    )}
                                </div>
                            </Card>
                            <AiCandidatePanel
                                capabilities={["qa"]}
                                contentId={activeDocument.id}
                                contentType="WANGQI_DOCUMENT"
                                onApplied={async () => {
                                    await invalidateWangqi();
                                }}
                                onRejected={async () => {
                                    await invalidateWangqiCandidates();
                                }}
                            />
                            <ClassicsContentQaPanel
                                panelTitle="王圻问答对"
                                contentId={activeDocument.id}
                                contentType="WANGQI_DOCUMENT"
                                onChanged={invalidateWangqi}
                            />
                        </div>
                    ) : null
                }
                sourceFileContent={
                    editorMode === "edit" && activeDocument ? (
                        <WangqiStorageFilePanel
                            document={activeDocument}
                            loading={sourceFileQuery.isLoading || sourceFileQuery.isFetching}
                            sourceFile={sourceFileQuery.data}
                            uploading={uploadSourceFileMutation.isPending}
                            onRefresh={() => void sourceFileQuery.refetch()}
                            onUpload={uploadSourceFile}
                        />
                    ) : null
                }
                versionContent={
                    editorMode === "edit" && activeDocument ? (
                        <WangqiVersionHistoryPanel
                            currentDocument={activeDocument}
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
