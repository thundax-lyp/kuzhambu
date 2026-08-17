import { UnorderedListOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Empty } from "antd";
import { useMemo, useState } from "react";
import { usePermission } from "@/auth/hooks/use-permission";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuDrawer,
    KuzhambuPage,
    KuzhambuSegmented,
    KuzhambuSpace
} from "@/components";
import { normalizeId } from "@/types/id";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import * as service from "./graph-extraction-service";
import type {
    GraphExtractionRegenerateCommand,
    GraphExtractionTaskPageQuery,
    GraphExtractionTaskStateCommand
} from "./graph-extraction-service";
import { GraphExtractionTaskTable } from "./graph-extraction-task-table";
import { TaskBatchCreatePanel } from "./task-batch-create-panel";
import { TaskDetailDrawer } from "./task-detail-drawer";
import { TaskFilters } from "./task-filters";
import type {
    GraphContentRefRecord,
    GraphExtractionTaskDrawerSection,
    GraphExtractionTaskListMode,
    GraphExtractionTaskRecord,
    GraphExtractionTriggerSource,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
} from "./graph-extraction-types";
import "./graph-extraction-page.css";

const REFINEMENT_APPLIED_TRIGGER_SOURCE: GraphExtractionTriggerSource = "REFINEMENT_APPLIED";

type TaskActionKind = "retry" | "cancel" | "merge" | "replace" | "discard" | "regenerate";

interface TaskActionVariables {
    kind: TaskActionKind;
    task: GraphExtractionTaskRecord;
}

const normalizeSearchParam = (value: string | null) => {
    const text = value?.trim();
    return text || undefined;
};

const readPositiveIntegerSearchParam = (value: string | null, fallback: number) => {
    const numberValue = Number(value);
    if (!Number.isInteger(numberValue) || numberValue <= 0) {
        return fallback;
    }
    return numberValue;
};

const readBooleanSearchParam = (value: string | null, fallback: boolean) => {
    if (value === null) {
        return fallback;
    }
    return value === "true" || value === "1";
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
            return contentType && contentRefId ? [{ contentRefId, contentType }] : [];
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
    return contentType && contentRefId ? [{ contentRefId, contentType }] : undefined;
};

const compactTaskQuery = (query: GraphExtractionTaskPageQuery): GraphExtractionTaskPageQuery =>
    Object.fromEntries(
        Object.entries(query).filter(([, value]) => value !== undefined)
    ) as GraphExtractionTaskPageQuery;

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
        contentRefs: readContentRefsFromSearch(params),
        executionStatus: normalizeSearchParam(
            params.get("executionStatus")
        ) as GraphTaskExecutionStatus,
        keyword: normalizeSearchParam(params.get("keyword")),
        pageNo: readPositiveIntegerSearchParam(params.get("pageNo"), DEFAULT_PAGE_NO),
        pageSize: readPositiveIntegerSearchParam(params.get("pageSize"), DEFAULT_PAGE_SIZE)
    });
};

const readHandoffRegenerateCommand = (): GraphExtractionRegenerateCommand | null => {
    if (typeof window === "undefined") {
        return null;
    }
    const params = new URLSearchParams(window.location.search);
    if (!readBooleanSearchParam(params.get("regenerate"), false)) {
        return null;
    }
    const sourceTaskId = normalizeSearchParam(params.get("sourceTaskId"));
    if (!sourceTaskId || !/^\d+$/u.test(sourceTaskId)) {
        return null;
    }
    return {
        replaceUnconfirmedOnly: readBooleanSearchParam(params.get("replaceUnconfirmedOnly"), true),
        selectionScopeJson: normalizeSearchParam(params.get("selectionScopeJson")),
        sourceTaskId,
        taskType: normalizeSearchParam(params.get("taskType")) ?? "GRAPH",
        triggerSource:
            normalizeSearchParam(params.get("triggerSource")) ?? REFINEMENT_APPLIED_TRIGGER_SOURCE
    };
};

const createTaskStateCommand = (
    task: GraphExtractionTaskRecord,
    expectedExecutionStatus: GraphTaskExecutionStatus,
    expectedDisposition?: GraphTaskDisposition
): GraphExtractionTaskStateCommand => ({
    expectedDisposition,
    expectedExecutionStatus,
    replaceUnconfirmedOnly: task.replaceUnconfirmedOnly,
    requestedBy: task.requestedBy,
    selectionScopeJson: task.selectionScopeJson,
    sourceTaskId: task.taskId || task.id,
    taskId: task.taskId || task.id,
    taskLockVersion: task.lockVersion,
    taskType: task.taskType || "GRAPH",
    triggerSource: task.triggerSource
});

export const GraphExtractionPage = () => {
    const { message } = App.useApp();
    const queryClient = useQueryClient();
    const canViewGraph = usePermission("knowledge:graph:view");
    const canEditGraph = usePermission("knowledge:graph:edit");
    const [taskQuery, setTaskQuery] = useState<GraphExtractionTaskPageQuery>(() =>
        readTaskQueryFromSearch()
    );
    const [taskListDrawerOpen, setTaskListDrawerOpen] = useState(false);
    const [detailTaskId, setDetailTaskId] = useState<string | null>(null);
    const [taskDetailDrawerOpen, setTaskDetailDrawerOpen] = useState(false);
    const [activeTaskDetailSection, setActiveTaskDetailSection] =
        useState<GraphExtractionTaskDrawerSection>("OVERVIEW");
    const handoffRegenerateCommand = useMemo(() => readHandoffRegenerateCommand(), []);
    const canUseTaskQueue = canViewGraph || canEditGraph;

    const taskPageQuery = useQuery({
        enabled: canUseTaskQueue,
        queryFn: () => service.pageTasks(taskQuery),
        queryKey: ["knowledge", "graph-extraction", "tasks", taskQuery],
        retry: false
    });
    const taskDetailQuery = useQuery({
        enabled: taskDetailDrawerOpen && detailTaskId !== null,
        queryFn: () => service.getTask({ taskId: detailTaskId || "" }),
        queryKey: ["knowledge", "graph-extraction", "task-detail", detailTaskId],
        retry: false
    });
    const invalidateTaskQueries = async () => {
        await Promise.all([
            queryClient.invalidateQueries({
                queryKey: ["knowledge", "graph-extraction", "tasks"]
            }),
            queryClient.invalidateQueries({
                queryKey: ["knowledge", "graph-extraction", "task-detail", detailTaskId]
            }),
            queryClient.invalidateQueries({
                queryKey: ["knowledge", "graph-material"]
            })
        ]);
    };
    const taskActionMutation = useMutation({
        mutationFn: ({ kind, task }: TaskActionVariables) => {
            switch (kind) {
                case "retry":
                    return service.retryTask(createTaskStateCommand(task, "FAILED"));
                case "cancel":
                    return service.cancelTask(
                        createTaskStateCommand(task, task.executionStatus || "RUNNING")
                    );
                case "merge":
                    return service.applyCandidate({
                        ...createTaskStateCommand(task, "SUCCEEDED", "PENDING"),
                        applyMode: "MERGE",
                        materialLockVersion: task.lockVersion
                    });
                case "replace":
                    return service.applyCandidate({
                        ...createTaskStateCommand(task, "SUCCEEDED", "PENDING"),
                        applyMode: "REPLACE",
                        materialLockVersion: task.lockVersion
                    });
                case "discard":
                    return service.discardCandidate(
                        createTaskStateCommand(task, "SUCCEEDED", "PENDING")
                    );
                case "regenerate":
                    return service.regenerateTask(createTaskStateCommand(task, "SUCCEEDED"));
            }
        },
        onSuccess: async (result, variables) => {
            await invalidateTaskQueries();
            if (result.conflict) {
                message.warning(result.conflict.message);
                return;
            }
            const successMessages: Record<TaskActionKind, string> = {
                cancel: "任务已取消",
                discard: "候选结果已丢弃",
                merge: "候选结果已合并",
                regenerate: "重新抽取任务已创建",
                replace: "候选结果已覆盖",
                retry: "任务已重试"
            };
            message.success(successMessages[variables.kind]);
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "任务操作失败");
        }
    });
    const regenerateTaskMutation = useMutation({
        mutationFn: service.regenerateTask,
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: ["knowledge", "graph-extraction", "tasks"]
            });
            message.success("重生成任务已创建");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "重生成任务创建失败");
        }
    });

    const tasks = taskPageQuery.data?.records || [];
    const taskTotalCount = taskPageQuery.data?.totalCount || 0;
    const taskListMode = taskQuery.groupBy ?? "NONE";
    const changeTaskListMode = (groupBy: GraphExtractionTaskListMode) => {
        setTaskQuery((currentQuery) => ({
            ...currentQuery,
            groupBy,
            pageNo: DEFAULT_PAGE_NO
        }));
    };
    const openTaskDetailDrawer = (task: GraphExtractionTaskRecord) => {
        const taskId = normalizeId(task.taskId || task.id).trim();
        if (!taskId) {
            return;
        }
        setActiveTaskDetailSection("OVERVIEW");
        setDetailTaskId(taskId);
        setTaskDetailDrawerOpen(true);
    };
    const applyTask = (task: GraphExtractionTaskRecord) => {
        taskActionMutation.mutate({ kind: "merge", task });
    };

    if (!canUseTaskQueue) {
        return (
            <KuzhambuPage
                className="graph-extraction-page knowledge-graph-extraction-page"
                description="需要知识图谱查看或编辑权限。"
                title="知识抽取"
            >
                <KuzhambuAlert title="无权查看知识抽取任务" type="warning" showIcon />
            </KuzhambuPage>
        );
    }

    const taskListContent = (
        <div className="knowledge-graph-extraction-task-list">
            <KuzhambuSegmented<GraphExtractionTaskListMode>
                aria-label="任务列表模式"
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
                testId="knowledge-graph-extraction-task-list-mode"
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
                    applyingTaskId={taskActionMutation.variables?.task.taskId || null}
                    canApply={canEditGraph}
                    canEdit={canEditGraph}
                    cancellingBatchId={null}
                    loading={taskPageQuery.isLoading}
                    regeneratingTaskId={taskActionMutation.variables?.task.taskId || null}
                    tasks={tasks}
                    onApply={applyTask}
                    onCancelBatch={(task) => taskActionMutation.mutate({ kind: "cancel", task })}
                    onOpenDetail={openTaskDetailDrawer}
                    onRegenerate={(task) => taskActionMutation.mutate({ kind: "regenerate", task })}
                />
            ) : (
                <Empty
                    className="knowledge-graph-extraction-task-empty"
                    description="当前还没有抽取任务。"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
            )}
        </div>
    );

    return (
        <KuzhambuPage
            actions={
                <KuzhambuButton
                    ariaLabel={`任务列表(${taskTotalCount})`}
                    icon={<UnorderedListOutlined />}
                    testId="knowledge-graph-extraction-task-list-button"
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
                className="knowledge-graph-extraction-layout"
                orientation="vertical"
                size={16}
            >
                {handoffRegenerateCommand ? (
                    <KuzhambuAlert
                        action={
                            <KuzhambuButton
                                disabled={!canEditGraph}
                                loading={regenerateTaskMutation.isPending}
                                testId="knowledge-graph-extraction-regenerate-handoff-button"
                                onClick={() =>
                                    regenerateTaskMutation.mutate(handoffRegenerateCommand)
                                }
                            >
                                提交重生成
                            </KuzhambuButton>
                        }
                        className="knowledge-graph-extraction-banner"
                        showIcon
                        title="精修应用后的图谱重生成参数已载入"
                        type="warning"
                    />
                ) : null}

                <KuzhambuCard title="任务队列">{taskListContent}</KuzhambuCard>
                <TaskBatchCreatePanel
                    canCreate={canEditGraph}
                    contentRefs={taskQuery.contentRefs ?? []}
                    volumeCode={taskQuery.volumeCode}
                    volumeTitle={taskQuery.volumeCode}
                    onCreated={async () => {
                        await queryClient.invalidateQueries({
                            queryKey: ["knowledge", "graph-extraction", "tasks"]
                        });
                    }}
                />
            </KuzhambuSpace>
            <KuzhambuDrawer
                open={taskListDrawerOpen}
                size="large"
                testId="knowledge-graph-extraction-task-list-drawer"
                title="任务列表"
                onClose={() => setTaskListDrawerOpen(false)}
            >
                {taskListContent}
            </KuzhambuDrawer>
            <TaskDetailDrawer
                activeSection={activeTaskDetailSection}
                detail={taskDetailQuery.data || null}
                loading={taskDetailQuery.isLoading}
                open={taskDetailDrawerOpen}
                onCancel={(task) => taskActionMutation.mutate({ kind: "cancel", task })}
                onClose={() => setTaskDetailDrawerOpen(false)}
                onDiscard={(task) => taskActionMutation.mutate({ kind: "discard", task })}
                onMerge={(task) => taskActionMutation.mutate({ kind: "merge", task })}
                onRegenerate={(task) => taskActionMutation.mutate({ kind: "regenerate", task })}
                onReplace={(task) => taskActionMutation.mutate({ kind: "replace", task })}
                onRetry={(task) => taskActionMutation.mutate({ kind: "retry", task })}
                onSectionChange={setActiveTaskDetailSection}
            />
        </KuzhambuPage>
    );
};
