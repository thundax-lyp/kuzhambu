import { UnorderedListOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Empty, Splitter, Tag } from "antd";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { usePermission } from "@/auth/hooks/use-permission";
import {
    KuzhambuSpace,
    KuzhambuPage,
    KuzhambuButton,
    KuzhambuAlert,
    KuzhambuDrawer,
    KuzhambuSegmented,
    KuzhambuTable
} from "@/components";
import { isPositiveDecimalId, normalizeId } from "@/types/id";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";

import { GraphExtractionManuscriptDetail } from "./graph-extraction-manuscript-detail";
import { GraphExtractionCandidateModal } from "./graph-extraction-manuscript-detail/graph-extraction-candidate-modal";
import { GraphExtractionManuscriptTree } from "./graph-extraction-manuscript-tree";
import { GraphExtractionTaskDetail } from "./graph-extraction-task-detail";
import { GraphExtractionTaskTable } from "./graph-extraction-task-table";
import * as service from "./graph-extraction-service";
import { TaskBatchCreatePanel } from "./task-batch-create-panel";
import { TaskFilters } from "./task-filters";
import type {
    GraphExtractionRegenerateCommand,
    GraphExtractionTaskPageQuery
} from "./graph-extraction-service";
import type {
    GraphContentRefRecord,
    GraphExtractionTaskListMode,
    GraphExtractionTaskType,
    GraphTaskDisposition,
    GraphTaskExecutionStatus,
    GraphWorkbenchManuscriptNode,
    GraphExtractionTriggerSource,
    GraphExtractionTaskRecord,
    GraphWorkbenchStatus
} from "./graph-extraction-types";
import * as workbenchService from "./graph-workbench-service";
import type { GraphWorkbenchSourceContentType } from "./graph-extraction-types";

import "./graph-extraction-page.css";

const REGENERATE_TRIGGER_SOURCE: GraphExtractionTriggerSource = "REGENERATE";
const REFINEMENT_APPLIED_TRIGGER_SOURCE: GraphExtractionTriggerSource = "REFINEMENT_APPLIED";
const WORK_AREA_COMPACT_MEDIA_QUERY = "(max-width: 768px)";
const DEFAULT_TREE_PANEL_SIZE = 320;
const COLLAPSED_TREE_PANEL_SIZE = 0;

const isCompactWorkArea = () =>
    typeof window !== "undefined" && window.matchMedia(WORK_AREA_COMPACT_MEDIA_QUERY).matches;

const getInitialTreePanelSize = () =>
    isCompactWorkArea() ? COLLAPSED_TREE_PANEL_SIZE : DEFAULT_TREE_PANEL_SIZE;

const readBooleanSearchParam = (value: string | null, fallback: boolean) => {
    if (value === null) {
        return fallback;
    }
    return value === "true" || value === "1";
};

const readPositiveIntegerSearchParam = (value: string | null, fallback: number) => {
    const numberValue = Number(value);
    if (!Number.isInteger(numberValue) || numberValue <= 0) {
        return fallback;
    }
    return numberValue;
};

const normalizeSearchParam = (value: string | null) => {
    const text = value?.trim();
    return text || undefined;
};

const compactTaskQuery = (query: GraphExtractionTaskPageQuery): GraphExtractionTaskPageQuery =>
    Object.fromEntries(
        Object.entries(query).filter(([, value]) => value !== undefined)
    ) as GraphExtractionTaskPageQuery;

const EXECUTION_STATUS_VALUES: GraphTaskExecutionStatus[] = [
    "PENDING",
    "RUNNING",
    "SUCCEEDED",
    "FAILED",
    "CANCELLED"
];

const DISPOSITION_VALUES: GraphTaskDisposition[] = [
    "PENDING",
    "ADOPTED_MERGE",
    "ADOPTED_REPLACE",
    "DISCARDED",
    "SUPERSEDED"
];

const readEnumSearchParam = <TValue extends string>(
    value: string | null,
    allowedValues: readonly TValue[]
): TValue | undefined => {
    const text = normalizeSearchParam(value);
    return allowedValues.includes(text as TValue) ? (text as TValue) : undefined;
};

const parseContentRefsJson = (value: string | null): GraphContentRefRecord[] | undefined => {
    if (!value) {
        return undefined;
    }
    try {
        const parsedValue: unknown = JSON.parse(value);
        if (!Array.isArray(parsedValue)) {
            return undefined;
        }
        const contentRefs = parsedValue.flatMap((item) => {
            if (typeof item !== "object" || item === null) {
                return [];
            }
            const record = item as Partial<GraphContentRefRecord>;
            const contentType = record.contentType?.trim();
            const contentRefId = record.contentRefId?.trim();
            return contentType && contentRefId ? [{ contentType, contentRefId }] : [];
        });
        return contentRefs.length ? contentRefs : undefined;
    } catch {
        return undefined;
    }
};

const readContentRefsFromSearch = (
    params: URLSearchParams
): GraphContentRefRecord[] | undefined => {
    const jsonContentRefs = parseContentRefsJson(params.get("contentRefs"));
    if (jsonContentRefs) {
        return jsonContentRefs;
    }

    const contentType = normalizeSearchParam(
        params.get("contentType") ?? params.get("sourceContentType")
    );
    const contentRefId = normalizeSearchParam(
        params.get("contentRefId") ?? params.get("sourceContentId")
    );
    if (!contentType || !contentRefId) {
        return undefined;
    }
    return [{ contentType, contentRefId }];
};

const readTaskQueryFromSearch = (): GraphExtractionTaskPageQuery => {
    const defaultQuery: GraphExtractionTaskPageQuery = {
        groupBy: "NONE",
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    };
    if (typeof window === "undefined") {
        return defaultQuery;
    }
    const params = new URLSearchParams(window.location.search);
    return compactTaskQuery({
        ...defaultQuery,
        batchId: normalizeSearchParam(params.get("batchId")),
        categoryCode: normalizeSearchParam(params.get("categoryCode")),
        contentRefs: readContentRefsFromSearch(params),
        contentType: normalizeSearchParam(
            params.get("contentType") ?? params.get("sourceContentType")
        ),
        disposition: readEnumSearchParam(params.get("disposition"), DISPOSITION_VALUES),
        executionStatus: readEnumSearchParam(
            params.get("executionStatus"),
            EXECUTION_STATUS_VALUES
        ),
        keyword: normalizeSearchParam(params.get("keyword")),
        pageNo: readPositiveIntegerSearchParam(params.get("pageNo"), DEFAULT_PAGE_NO),
        pageSize: readPositiveIntegerSearchParam(params.get("pageSize"), DEFAULT_PAGE_SIZE),
        volumeCode: normalizeSearchParam(params.get("volumeCode"))
    });
};

const readRegenerateCommandFromSearch = (): GraphExtractionRegenerateCommand | null => {
    if (typeof window === "undefined") {
        return null;
    }
    const params = new URLSearchParams(window.location.search);
    if (params.get("regenerate") !== "1") {
        return null;
    }
    const sourceTaskId = params.get("sourceTaskId")?.trim();
    if (!isPositiveDecimalId(sourceTaskId)) {
        return null;
    }
    return {
        taskType: params.get("taskType") || "GRAPH",
        sourceTaskId,
        triggerSource: params.get("triggerSource") || REFINEMENT_APPLIED_TRIGGER_SOURCE,
        selectionScopeJson: params.get("selectionScopeJson"),
        replaceUnconfirmedOnly: readBooleanSearchParam(params.get("replaceUnconfirmedOnly"), true)
    };
};

const mergeLoadedNodeChildren = (
    nodes: GraphWorkbenchManuscriptNode[],
    childrenByNodeKey: Record<string, GraphWorkbenchManuscriptNode[]>
): GraphWorkbenchManuscriptNode[] =>
    nodes.map((node) => {
        const children = childrenByNodeKey[node.nodeKey] || node.children;
        return {
            ...node,
            children: children ? mergeLoadedNodeChildren(children, childrenByNodeKey) : children
        };
    });

const isManuscriptNode = (node?: GraphWorkbenchManuscriptNode | null) =>
    node?.nodeType === "MANUSCRIPT" && node.sourceContentType && node.sourceContentId;

const isVolumeNode = (node?: GraphWorkbenchManuscriptNode | null) => node?.nodeType === "VOLUME";

const toTableManuscriptNode = (node: GraphWorkbenchManuscriptNode) => {
    const tableNode = { ...node };
    delete tableNode.children;
    return tableNode;
};

const STATUS_LABELS = new Map<GraphWorkbenchStatus, string>([
    ["NOT_EXTRACTED", "未抽取"],
    ["EXTRACTING", "抽取中"],
    ["EXTRACTION_FAILED", "未抽取"],
    ["CANDIDATE_READY", "已抽取"],
    ["APPLIED", "已抽取"],
    ["REFINING", "抽取中"],
    ["REFINED", "已抽取"],
    ["QUALITY_ISSUE", "已抽取"]
]);

const STATUS_COLORS = new Map<GraphWorkbenchStatus, string>([
    ["NOT_EXTRACTED", "default"],
    ["EXTRACTING", "processing"],
    ["EXTRACTION_FAILED", "default"],
    ["CANDIDATE_READY", "success"],
    ["APPLIED", "success"],
    ["REFINING", "processing"],
    ["REFINED", "success"],
    ["QUALITY_ISSUE", "success"]
]);

const statusLabel = (status?: GraphWorkbenchStatus | null) =>
    STATUS_LABELS.get(status || "") || status || "未知";

const statusColor = (status?: GraphWorkbenchStatus | null) =>
    STATUS_COLORS.get(status || "") || "default";

type ExtractManuscriptVariables = {
    node: GraphWorkbenchManuscriptNode;
    taskType: GraphExtractionTaskType;
};

export const GraphExtractionPage = () => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const canViewGraph = usePermission("knowledge:graph:view");
    const canEditGraph = usePermission("knowledge:graph:edit");
    const canApplyGraph = usePermission("knowledge:graph:apply");
    const [handoffRegenerateCommand] = useState<GraphExtractionRegenerateCommand | null>(() =>
        readRegenerateCommandFromSearch()
    );
    const [taskQuery, setTaskQuery] =
        useState<GraphExtractionTaskPageQuery>(readTaskQueryFromSearch);
    const [detailTaskId, setDetailTaskId] = useState<string | null>(null);
    const [taskDetailDrawerOpen, setTaskDetailDrawerOpen] = useState(false);
    const [taskListDrawerOpen, setTaskListDrawerOpen] = useState(false);
    const [manuscriptDetailDrawerOpen, setManuscriptDetailDrawerOpen] = useState(false);
    const [candidateModalOpen, setCandidateModalOpen] = useState(false);
    const [candidateModalTask, setCandidateModalTask] = useState<GraphExtractionTaskRecord | null>(
        null
    );
    const [manuscriptChildrenByNodeKey, setManuscriptChildrenByNodeKey] = useState<
        Record<string, GraphWorkbenchManuscriptNode[]>
    >({});
    const [selectedManuscript, setSelectedManuscript] =
        useState<GraphWorkbenchManuscriptNode | null>(null);
    const [selectedVolume, setSelectedVolume] = useState<GraphWorkbenchManuscriptNode | null>(null);
    const [selectedManuscriptKeys, setSelectedManuscriptKeys] = useState<string[]>([]);
    const [treePanelSize, setTreePanelSize] = useState<number>(getInitialTreePanelSize);
    const [compactWorkArea, setCompactWorkArea] = useState(isCompactWorkArea);
    const loadedManuscriptNodeKeysRef = useRef<Set<string>>(new Set());
    const loadingManuscriptNodeKeysRef = useRef<Set<string>>(new Set());

    useEffect(() => {
        if (typeof window === "undefined" || typeof window.matchMedia !== "function") {
            return undefined;
        }

        const mediaQueryList = window.matchMedia(WORK_AREA_COMPACT_MEDIA_QUERY);
        const syncTreePanelSize = (compact: boolean) => {
            setCompactWorkArea(compact);
            setTreePanelSize(compact ? COLLAPSED_TREE_PANEL_SIZE : DEFAULT_TREE_PANEL_SIZE);
        };
        const handleMediaChange = (event: MediaQueryListEvent) => {
            syncTreePanelSize(event.matches);
        };

        syncTreePanelSize(mediaQueryList.matches);
        mediaQueryList.addEventListener("change", handleMediaChange);
        return () => {
            mediaQueryList.removeEventListener("change", handleMediaChange);
        };
    }, []);

    const taskPageQuery = useQuery({
        queryKey: ["knowledge", "graph-extraction", "tasks", taskQuery],
        queryFn: () => service.pageTasks(taskQuery),
        enabled: canViewGraph || canEditGraph,
        retry: false
    });
    const taskDetailQuery = useQuery({
        queryKey: ["knowledge", "graph-extraction", "task-detail", detailTaskId],
        queryFn: () => service.getTaskDetail({ taskId: detailTaskId || "" }),
        enabled: taskDetailDrawerOpen && detailTaskId !== null,
        retry: false
    });
    const manuscriptTreeQuery = useQuery({
        queryKey: ["knowledge", "graph-workbench", "manuscript-tree", "roots"],
        queryFn: () => workbenchService.listManuscriptTree({}),
        enabled: canViewGraph,
        retry: false
    });
    const manuscriptDetailQuery = useQuery({
        queryKey: [
            "knowledge",
            "graph-workbench",
            "manuscript",
            selectedManuscript?.sourceContentType,
            selectedManuscript?.sourceContentId
        ],
        queryFn: () =>
            workbenchService.getManuscript({
                sourceContentType:
                    selectedManuscript?.sourceContentType as GraphWorkbenchSourceContentType,
                sourceContentId: selectedManuscript?.sourceContentId || ""
            }),
        enabled:
            canViewGraph &&
            Boolean(selectedManuscript?.sourceContentType && selectedManuscript.sourceContentId),
        retry: false
    });
    const volumeManuscriptsQuery = useQuery({
        queryKey: ["knowledge", "graph-workbench", "volume-manuscripts", selectedVolume?.nodeKey],
        queryFn: () =>
            workbenchService.listManuscriptTree({
                parentKey: selectedVolume?.nodeKey || ""
            }),
        enabled: canViewGraph && Boolean(selectedVolume?.nodeKey),
        retry: false
    });
    const candidateQuery = useQuery({
        queryKey: [
            "knowledge",
            "graph-workbench",
            "candidate",
            selectedManuscript?.sourceContentType,
            selectedManuscript?.sourceContentId
        ],
        queryFn: () =>
            workbenchService.getLatestCandidate({
                sourceContentType:
                    selectedManuscript?.sourceContentType as GraphWorkbenchSourceContentType,
                sourceContentId: selectedManuscript?.sourceContentId || "",
                taskType: "GRAPH"
            }),
        enabled:
            canViewGraph &&
            Boolean(selectedManuscript?.sourceContentType && selectedManuscript.sourceContentId),
        retry: false
    });
    const visibleManuscriptTreeNodes = useMemo(
        () => mergeLoadedNodeChildren(manuscriptTreeQuery.data || [], manuscriptChildrenByNodeKey),
        [manuscriptChildrenByNodeKey, manuscriptTreeQuery.data]
    );
    const volumeManuscripts = useMemo(
        () => (volumeManuscriptsQuery.data || []).map(toTableManuscriptNode),
        [volumeManuscriptsQuery.data]
    );
    const selectedVolumeManuscripts = useMemo(
        () => volumeManuscripts.filter((node) => selectedManuscriptKeys.includes(node.nodeKey)),
        [selectedManuscriptKeys, volumeManuscripts]
    );
    const selectedVolumeContentRefs = useMemo(
        () =>
            selectedVolumeManuscripts.flatMap((node) =>
                node.sourceContentType && node.sourceContentId
                    ? [
                          {
                              contentRefId: node.sourceContentId,
                              contentType: node.sourceContentType
                          }
                      ]
                    : []
            ),
        [selectedVolumeManuscripts]
    );

    const loadManuscriptChildren = useCallback(async (nodeKey: string) => {
        if (
            loadedManuscriptNodeKeysRef.current.has(nodeKey) ||
            loadingManuscriptNodeKeysRef.current.has(nodeKey)
        ) {
            return;
        }
        loadingManuscriptNodeKeysRef.current.add(nodeKey);
        try {
            const children = await workbenchService.listManuscriptTree({
                parentKey: nodeKey
            });
            loadedManuscriptNodeKeysRef.current.add(nodeKey);
            setManuscriptChildrenByNodeKey((current) => {
                if (current[nodeKey]) {
                    return current;
                }
                return {
                    ...current,
                    [nodeKey]: children
                };
            });
        } finally {
            loadingManuscriptNodeKeysRef.current.delete(nodeKey);
        }
    }, []);

    const extractManuscriptMutation = useMutation({
        mutationFn: ({ node, taskType }: ExtractManuscriptVariables) => {
            return workbenchService.extractManuscript({
                sourceContentType: node.sourceContentType as GraphWorkbenchSourceContentType,
                sourceContentId: node.sourceContentId || "",
                taskType
            });
        },
        onSuccess: async (task) => {
            setCandidateModalTask(task);
            loadedManuscriptNodeKeysRef.current.clear();
            loadingManuscriptNodeKeysRef.current.clear();
            setManuscriptChildrenByNodeKey({});
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "graph-extraction", "tasks"]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "graph-workbench"]
                })
            ]);
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "稿件图谱抽取失败");
        }
    });
    const applyWorkbenchCandidateMutation = useMutation({
        mutationFn: ({ applyMode, taskId }: { applyMode: string; taskId: string }) =>
            workbenchService.applyCandidate({ applyMode, taskId }),
        onSuccess: async () => {
            loadedManuscriptNodeKeysRef.current.clear();
            loadingManuscriptNodeKeysRef.current.clear();
            setManuscriptChildrenByNodeKey({});
            setCandidateModalOpen(false);
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "graph-extraction", "tasks"]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "graph-workbench"]
                })
            ]);
            messageApi.success("稿件候选结果已应用");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "稿件候选应用失败");
        }
    });
    const applyTaskMutation = useMutation({
        mutationFn: (taskId: string) => service.applyTaskCandidate({ taskId }),
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "graph-extraction", "tasks"]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "graph-extraction", "task-detail", detailTaskId]
                })
            ]);
            messageApi.success("候选结果已应用");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "候选结果应用失败");
        }
    });
    const regenerateTaskMutation = useMutation({
        mutationFn: service.regenerateTask,
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: ["knowledge", "graph-extraction", "tasks"]
            });
            messageApi.success("重生成任务已创建");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "重生成任务创建失败");
        }
    });
    const cancelBatchTaskMutation = useMutation({
        mutationFn: (task: GraphExtractionTaskRecord) =>
            service.cancelBatchTask({
                batchJobId: task.batchJobId || "",
                requestedBy: task.requestedBy
            }),
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "graph-extraction", "tasks"]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "graph-extraction", "task-detail", detailTaskId]
                })
            ]);
            messageApi.success("批任务已取消");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批任务取消失败");
        }
    });

    const tasks = taskPageQuery.data?.records || [];
    const taskTotalCount = taskPageQuery.data?.totalCount || 0;
    const taskListMode = taskQuery.groupBy ?? "NONE";
    const selectedTreeNodeKey = selectedManuscript?.nodeKey || selectedVolume?.nodeKey || null;

    const readTaskId = (task: GraphExtractionTaskRecord) => normalizeId(task.taskId).trim();

    const openTaskDetailDrawer = (task: GraphExtractionTaskRecord) => {
        const taskId = readTaskId(task);
        if (!taskId) {
            return;
        }
        setDetailTaskId(taskId);
        setTaskDetailDrawerOpen(true);
    };

    const applyTask = (task: GraphExtractionTaskRecord) => {
        const taskId = readTaskId(task);
        if (!taskId) {
            return;
        }
        applyTaskMutation.mutate(taskId);
    };

    const regenerateTask = (task: GraphExtractionTaskRecord) => {
        const taskId = readTaskId(task);
        if (!taskId) {
            return;
        }
        regenerateTaskMutation.mutate({
            taskType: task.taskType || "GRAPH",
            sourceTaskId: taskId,
            triggerSource: REGENERATE_TRIGGER_SOURCE,
            selectionScopeJson: task.selectionScopeJson,
            replaceUnconfirmedOnly: task.replaceUnconfirmedOnly ?? true,
            requestedBy: task.requestedBy
        });
    };

    const cancelBatchTask = (task: GraphExtractionTaskRecord) => {
        if (!task.batchJobId) {
            return;
        }
        cancelBatchTaskMutation.mutate(task);
    };

    const changeTaskListMode = (groupBy: GraphExtractionTaskListMode) => {
        setTaskQuery((currentQuery) => ({
            ...currentQuery,
            groupBy,
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const resizeWorkArea = useCallback((sizes: number[]) => {
        setTreePanelSize(sizes[0] ?? DEFAULT_TREE_PANEL_SIZE);
    }, []);

    const selectTreeNode = (node: GraphWorkbenchManuscriptNode) => {
        if (isVolumeNode(node)) {
            setSelectedVolume(node);
            setSelectedManuscript(null);
            setSelectedManuscriptKeys([]);
            setManuscriptDetailDrawerOpen(false);
            setCandidateModalOpen(false);
            setCandidateModalTask(null);
            return;
        }
        if (isManuscriptNode(node)) {
            setSelectedManuscript(node);
        }
    };

    const openManuscriptDetail = (node: GraphWorkbenchManuscriptNode) => {
        setSelectedManuscript(node);
        setManuscriptDetailDrawerOpen(true);
    };

    const openCandidateModal = (node?: GraphWorkbenchManuscriptNode) => {
        if (node) {
            setSelectedManuscript(node);
            setManuscriptDetailDrawerOpen(true);
        }
        setCandidateModalTask(null);
        setCandidateModalOpen(true);
    };

    return (
        <KuzhambuPage
            actions={
                <KuzhambuButton
                    ariaLabel={`任务列表(${taskTotalCount})`}
                    testId="knowledge-graph-extraction-task-list-button"
                    icon={<UnorderedListOutlined />}
                    type="primary"
                    onClick={() => setTaskListDrawerOpen(true)}
                >
                    任务列表({taskTotalCount})
                </KuzhambuButton>
            }
            className="graph-extraction-page knowledge-graph-extraction-page"
            description="统一管理 Knowledge 抽取任务、候选结果和正式应用动作。"
            title="知识抽取"
        >
            <KuzhambuSpace
                orientation="vertical"
                size={16}
                className="knowledge-graph-extraction-layout"
            >
                {handoffRegenerateCommand ? (
                    <KuzhambuAlert
                        showIcon
                        className="knowledge-graph-extraction-banner"
                        title="精修应用后的图谱重生成参数已载入"
                        type="warning"
                        action={
                            <KuzhambuButton
                                testId="knowledge-graph-extraction-regenerate-handoff-button"
                                disabled={!canEditGraph}
                                loading={regenerateTaskMutation.isPending}
                                onClick={() =>
                                    regenerateTaskMutation.mutate(handoffRegenerateCommand)
                                }
                            >
                                提交重生成
                            </KuzhambuButton>
                        }
                    />
                ) : null}

                <section>
                    <Splitter
                        className="knowledge-graph-extraction-work-area"
                        collapsible={{ motion: true }}
                        onResize={resizeWorkArea}
                    >
                        <Splitter.Panel
                            className="knowledge-graph-extraction-tree-panel"
                            size={treePanelSize}
                            min={compactWorkArea ? COLLAPSED_TREE_PANEL_SIZE : 260}
                            max={520}
                            collapsible
                        >
                            <GraphExtractionManuscriptTree
                                loading={manuscriptTreeQuery.isLoading}
                                nodes={visibleManuscriptTreeNodes}
                                selectedNodeKey={selectedTreeNodeKey}
                                onLoadChildren={loadManuscriptChildren}
                                onSelectManuscript={selectTreeNode}
                            />
                        </Splitter.Panel>
                        <Splitter.Panel className="knowledge-graph-extraction-detail-panel">
                            {selectedVolume ? (
                                <KuzhambuSpace orientation="vertical" size={12}>
                                    <TaskBatchCreatePanel
                                        canCreate={canEditGraph}
                                        contentRefs={selectedVolumeContentRefs}
                                        volumeCode={selectedVolume.sourceContentId || undefined}
                                        volumeTitle={selectedVolume.title}
                                        onCreated={async () => {
                                            loadedManuscriptNodeKeysRef.current.clear();
                                            loadingManuscriptNodeKeysRef.current.clear();
                                            setManuscriptChildrenByNodeKey({});
                                            setSelectedManuscriptKeys([]);
                                            await Promise.all([
                                                queryClient.invalidateQueries({
                                                    queryKey: [
                                                        "knowledge",
                                                        "graph-extraction",
                                                        "tasks"
                                                    ]
                                                }),
                                                queryClient.invalidateQueries({
                                                    queryKey: ["knowledge", "graph-workbench"]
                                                })
                                            ]);
                                        }}
                                    />
                                    <KuzhambuTable<GraphWorkbenchManuscriptNode>
                                        ariaLabel="卷目稿件列表"
                                        rowKey="nodeKey"
                                        className="graph-extraction-manuscript-table"
                                        dataSource={volumeManuscripts}
                                        loading={volumeManuscriptsQuery.isLoading}
                                        pagination={{
                                            defaultPageSize: DEFAULT_PAGE_SIZE,
                                            showSizeChanger: true
                                        }}
                                        toolbar={{
                                            leading: (
                                                <span>
                                                    当前页已选 {selectedManuscriptKeys.length} 条
                                                </span>
                                            )
                                        }}
                                        rowSelection={{
                                            selectedRowKeys: selectedManuscriptKeys,
                                            onChange: (keys) =>
                                                setSelectedManuscriptKeys(keys.map(String))
                                        }}
                                        size="small"
                                        columns={[
                                            {
                                                title: "稿件",
                                                dataIndex: "title",
                                                key: "title"
                                            },
                                            {
                                                title: "图谱状态",
                                                dataIndex: "graphStatus",
                                                key: "graphStatus",
                                                width: 120,
                                                render: (status?: GraphWorkbenchStatus | null) => (
                                                    <Tag color={statusColor(status)}>
                                                        {statusLabel(status)}
                                                    </Tag>
                                                )
                                            },
                                            {
                                                key: "actions",
                                                options: (record) => [
                                                    {
                                                        key: "view",
                                                        text: "查看",
                                                        testId: "knowledge-graph-extraction-manuscript-view-button",
                                                        onClick: openManuscriptDetail
                                                    },
                                                    {
                                                        key: "extract",
                                                        text: "抽取",
                                                        testId: "knowledge-graph-extraction-manuscript-table-extract-button",
                                                        disabled:
                                                            !canEditGraph ||
                                                            extractManuscriptMutation.isPending,
                                                        onClick: () => openCandidateModal(record)
                                                    }
                                                ]
                                            }
                                        ]}
                                        locale={{
                                            emptyText: selectedVolume
                                                ? "当前卷目下没有稿件"
                                                : "请选择左侧卷目"
                                        }}
                                    />
                                </KuzhambuSpace>
                            ) : (
                                <Empty
                                    description="请选择左侧卷目查看稿件"
                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                />
                            )}
                        </Splitter.Panel>
                    </Splitter>
                </section>

                <KuzhambuDrawer
                    testId="knowledge-graph-extraction-manuscript-detail-drawer"
                    title={selectedManuscript?.title || "稿件图谱详情"}
                    open={manuscriptDetailDrawerOpen}
                    size="large"
                    onClose={() => {
                        setManuscriptDetailDrawerOpen(false);
                        setCandidateModalOpen(false);
                    }}
                >
                    <GraphExtractionManuscriptDetail
                        canEdit={canEditGraph}
                        currentGraphLoading={false}
                        currentGraphRelations={[]}
                        detail={manuscriptDetailQuery.data || null}
                        selectedNode={selectedManuscript}
                        onOpenExtractionDialog={() => openCandidateModal()}
                    />
                </KuzhambuDrawer>

                <GraphExtractionCandidateModal
                    applying={applyWorkbenchCandidateMutation.isPending}
                    canApply={canApplyGraph}
                    canEdit={canEditGraph}
                    candidate={candidateQuery.data || null}
                    candidateLoading={candidateQuery.isLoading}
                    detail={manuscriptDetailQuery.data || null}
                    extracting={
                        extractManuscriptMutation.isPending &&
                        extractManuscriptMutation.variables?.node.nodeKey ===
                            selectedManuscript?.nodeKey
                    }
                    open={candidateModalOpen}
                    task={candidateModalTask}
                    onApplyCandidate={(taskId, applyMode) =>
                        applyWorkbenchCandidateMutation.mutate({ applyMode, taskId })
                    }
                    onCancel={() => setCandidateModalOpen(false)}
                    onFetchCandidate={() => {
                        if (
                            !selectedManuscript?.sourceContentType ||
                            !selectedManuscript.sourceContentId
                        ) {
                            return Promise.resolve(null);
                        }
                        return workbenchService.getLatestCandidate({
                            sourceContentType:
                                selectedManuscript.sourceContentType as GraphWorkbenchSourceContentType,
                            sourceContentId: selectedManuscript.sourceContentId,
                            taskType: "GRAPH"
                        });
                    }}
                    onFetchTask={(taskId) => service.getTaskDetail({ taskId })}
                    onExtract={(taskType) => {
                        if (selectedManuscript) {
                            extractManuscriptMutation.mutate({
                                node: selectedManuscript,
                                taskType
                            });
                        }
                    }}
                    onTaskChange={setCandidateModalTask}
                />

                <KuzhambuDrawer
                    testId="knowledge-graph-extraction-task-list-drawer"
                    title="任务列表"
                    open={taskListDrawerOpen}
                    size="large"
                    onClose={() => setTaskListDrawerOpen(false)}
                >
                    <div className="knowledge-graph-extraction-task-list">
                        <KuzhambuSegmented<GraphExtractionTaskListMode>
                            aria-label="任务列表模式"
                            testId="knowledge-graph-extraction-task-list-mode"
                            options={[
                                {
                                    label: "全局队列",
                                    value: "NONE"
                                },
                                {
                                    label: "按素材分组",
                                    value: "MATERIAL"
                                }
                            ]}
                            value={taskListMode}
                            onChange={changeTaskListMode}
                        />
                        <TaskFilters
                            loading={taskPageQuery.isLoading}
                            query={taskQuery}
                            total={taskTotalCount}
                            onChange={setTaskQuery}
                        />
                        {tasks.length > 0 ? (
                            <GraphExtractionTaskTable
                                applyingTaskId={applyTaskMutation.variables?.toString() || null}
                                canApply={canApplyGraph}
                                canEdit={canEditGraph}
                                cancellingBatchId={
                                    cancelBatchTaskMutation.variables?.batchJobId || null
                                }
                                loading={taskPageQuery.isLoading}
                                regeneratingTaskId={
                                    regenerateTaskMutation.variables?.sourceTaskId?.toString() ||
                                    null
                                }
                                tasks={tasks}
                                onApply={applyTask}
                                onCancelBatch={cancelBatchTask}
                                onOpenDetail={openTaskDetailDrawer}
                                onRegenerate={regenerateTask}
                            />
                        ) : (
                            <Empty
                                className="knowledge-graph-extraction-task-empty"
                                description="当前还没有抽取任务，可以先从稿件树选择稿件并抽取图谱。"
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                            />
                        )}
                    </div>
                </KuzhambuDrawer>
                <GraphExtractionTaskDetail
                    applying={applyTaskMutation.isPending}
                    canApply={canApplyGraph}
                    loading={taskDetailQuery.isLoading}
                    open={taskDetailDrawerOpen}
                    task={taskDetailQuery.data || null}
                    onApply={() => {
                        if (detailTaskId !== null) {
                            applyTaskMutation.mutate(detailTaskId);
                        }
                    }}
                    onClose={() => setTaskDetailDrawerOpen(false)}
                />
            </KuzhambuSpace>
        </KuzhambuPage>
    );
};
