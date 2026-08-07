import { UnorderedListOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Empty, Splitter } from "antd";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import {
    KuzhambuSpace,
    KuzhambuPage,
    KuzhambuButton,
    KuzhambuAlert,
    KuzhambuDrawer
} from "@/components";
import { isPositiveDecimalId, normalizeId } from "@/types/id";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";

import { GraphExtractionManuscriptDetail } from "./graph-extraction-manuscript-detail";
import { GraphExtractionManuscriptTree } from "./graph-extraction-manuscript-tree";
import { GraphExtractionTaskDetail } from "./graph-extraction-task-detail";
import { GraphExtractionTaskTable } from "./graph-extraction-task-table";
import * as service from "./graph-extraction-service";
import type {
    GraphExtractionRegenerateCommand,
    GraphExtractionTaskPageQuery
} from "./graph-extraction-service";
import type {
    GraphWorkbenchManuscriptNode,
    GraphExtractionTriggerSource,
    GraphExtractionTaskRecord
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

export const GraphExtractionPage = () => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const canViewGraph = hasPermission("knowledge:graph:view");
    const canEditGraph = hasPermission("knowledge:graph:edit");
    const canApplyGraph = hasPermission("knowledge:graph:apply");
    const canOpenRefinement = hasPermission("knowledge:refinement:edit");
    const [handoffRegenerateCommand] = useState<GraphExtractionRegenerateCommand | null>(() =>
        readRegenerateCommandFromSearch()
    );
    const [taskQuery] = useState<GraphExtractionTaskPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [detailTaskId, setDetailTaskId] = useState<string | null>(null);
    const [taskDetailDrawerOpen, setTaskDetailDrawerOpen] = useState(false);
    const [taskListDrawerOpen, setTaskListDrawerOpen] = useState(false);
    const [manuscriptChildrenByNodeKey, setManuscriptChildrenByNodeKey] = useState<
        Record<string, GraphWorkbenchManuscriptNode[]>
    >({});
    const [selectedManuscript, setSelectedManuscript] =
        useState<GraphWorkbenchManuscriptNode | null>(null);
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
        mutationFn: (taskType: string) => {
            return workbenchService.extractManuscript({
                sourceContentType:
                    selectedManuscript?.sourceContentType as GraphWorkbenchSourceContentType,
                sourceContentId: selectedManuscript?.sourceContentId || "",
                taskType
            });
        },
        onSuccess: async () => {
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
            messageApi.success("稿件图谱抽取任务已创建");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "稿件图谱抽取失败");
        }
    });
    const applyWorkbenchCandidateMutation = useMutation({
        mutationFn: (taskId: string) => workbenchService.applyCandidate({ taskId }),
        onSuccess: async () => {
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
    const selectedNodeKey = selectedManuscript?.nodeKey || null;

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

    const resizeWorkArea = useCallback((sizes: number[]) => {
        setTreePanelSize(sizes[0] ?? DEFAULT_TREE_PANEL_SIZE);
    }, []);

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
                                selectedNodeKey={selectedNodeKey}
                                onLoadChildren={loadManuscriptChildren}
                                onSelectManuscript={setSelectedManuscript}
                            />
                        </Splitter.Panel>
                        <Splitter.Panel className="knowledge-graph-extraction-detail-panel">
                            <GraphExtractionManuscriptDetail
                                applying={applyWorkbenchCandidateMutation.isPending}
                                canApply={canApplyGraph}
                                canEdit={canEditGraph}
                                canOpenRefinement={canOpenRefinement}
                                candidate={candidateQuery.data || null}
                                candidateLoading={candidateQuery.isLoading}
                                detail={manuscriptDetailQuery.data || null}
                                extracting={extractManuscriptMutation.isPending}
                                selectedNode={selectedManuscript}
                                onApplyCandidate={applyWorkbenchCandidateMutation.mutate}
                                onExtract={extractManuscriptMutation.mutate}
                            />
                        </Splitter.Panel>
                    </Splitter>
                </section>

                <KuzhambuDrawer
                    testId="knowledge-graph-extraction-task-list-drawer"
                    title="任务列表"
                    open={taskListDrawerOpen}
                    size="large"
                    onClose={() => setTaskListDrawerOpen(false)}
                >
                    <div className="knowledge-graph-extraction-task-list">
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
