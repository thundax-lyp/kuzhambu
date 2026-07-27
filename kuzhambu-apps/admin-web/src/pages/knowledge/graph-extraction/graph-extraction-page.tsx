import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Card, Col, Empty, Row, Typography } from "antd";
import { useCallback, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuSpace, KuzhambuPage, KuzhambuButton, KuzhambuAlert } from "@/components";
import * as currentUserService from "@/service/current-user-service";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";

import { GraphExtractionManuscriptDetail } from "./components/graph-extraction-manuscript-detail";
import { GraphExtractionManuscriptTree } from "./components/graph-extraction-manuscript-tree";
import { GraphExtractionTaskDetail } from "./components/graph-extraction-task-detail";
import { GraphExtractionTaskTable } from "./components/graph-extraction-task-table";
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

const { Text, Title } = Typography;

const REGENERATE_TRIGGER_SOURCE: GraphExtractionTriggerSource = "REGENERATE";
const REFINEMENT_APPLIED_TRIGGER_SOURCE: GraphExtractionTriggerSource = "REFINEMENT_APPLIED";

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
    const sourceTaskId = Number(params.get("sourceTaskId"));
    if (!Number.isFinite(sourceTaskId) || sourceTaskId <= 0) {
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

const updateNodeChildren = (
    nodes: GraphWorkbenchManuscriptNode[],
    nodeKey: string,
    children: GraphWorkbenchManuscriptNode[]
): GraphWorkbenchManuscriptNode[] =>
    nodes.map((node) => {
        if (node.nodeKey === nodeKey) {
            return {
                ...node,
                children
            };
        }
        return {
            ...node,
            children: node.children
                ? updateNodeChildren(node.children, nodeKey, children)
                : node.children
        };
    });

export const GraphExtractionPage = () => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const canViewGraph = hasPermission("knowledge:graph:view");
    const canEditGraph = hasPermission("knowledge:graph:edit");
    const canApplyGraph = hasPermission("knowledge:graph:apply");
    const [handoffRegenerateCommand] = useState<GraphExtractionRegenerateCommand | null>(() =>
        readRegenerateCommandFromSearch()
    );
    const [taskQuery] = useState<GraphExtractionTaskPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [detailTaskId, setDetailTaskId] = useState<number | null>(null);
    const [taskDetailDrawerOpen, setTaskDetailDrawerOpen] = useState(false);
    const [manuscriptTreeNodes, setManuscriptTreeNodes] = useState<GraphWorkbenchManuscriptNode[]>(
        []
    );
    const [manuscriptSearchText, setManuscriptSearchText] = useState("");
    const [selectedManuscript, setSelectedManuscript] =
        useState<GraphWorkbenchManuscriptNode | null>(null);

    const taskPageQuery = useQuery({
        queryKey: ["knowledge", "graph-extraction", "tasks", taskQuery],
        queryFn: () => service.pageTasks(taskQuery),
        enabled: canViewGraph || canEditGraph,
        retry: false
    });
    const taskDetailQuery = useQuery({
        queryKey: ["knowledge", "graph-extraction", "task-detail", detailTaskId],
        queryFn: () => service.getTaskDetail({ taskId: detailTaskId || 0 }),
        enabled: taskDetailDrawerOpen && detailTaskId !== null,
        retry: false
    });
    const currentUserQuery = useQuery({
        queryKey: ["sys", "current-user", "info"],
        queryFn: currentUserService.getCurrentUserInfo,
        enabled: canEditGraph,
        retry: false
    });

    const manuscriptTreeQuery = useQuery({
        queryKey: ["knowledge", "graph-workbench", "manuscript-tree", "roots"],
        queryFn: () => workbenchService.listManuscriptTree(),
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
                sourceContentId: selectedManuscript?.sourceContentId || 0
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
                sourceContentId: selectedManuscript?.sourceContentId || 0,
                taskType: "GRAPH"
            }),
        enabled:
            canViewGraph &&
            Boolean(selectedManuscript?.sourceContentType && selectedManuscript.sourceContentId),
        retry: false
    });

    const visibleManuscriptTreeNodes =
        manuscriptTreeNodes.length > 0 ? manuscriptTreeNodes : manuscriptTreeQuery.data || [];

    const loadManuscriptChildren = useCallback(
        async (nodeKey: string) => {
            const children = await workbenchService.listManuscriptTree({
                parentKey: nodeKey,
                keyword: manuscriptSearchText || undefined
            });
            setManuscriptTreeNodes((current) =>
                updateNodeChildren(
                    current.length > 0 ? current : manuscriptTreeQuery.data || [],
                    nodeKey,
                    children
                )
            );
        },
        [manuscriptSearchText, manuscriptTreeQuery.data]
    );

    const extractManuscriptMutation = useMutation({
        mutationFn: (taskType: string) => {
            const requestedBy = Number(currentUserQuery.data?.id ?? 0);
            if (!requestedBy) {
                throw new Error("当前用户信息未加载完成，请稍后重试");
            }
            return workbenchService.extractManuscript({
                sourceContentType:
                    selectedManuscript?.sourceContentType as GraphWorkbenchSourceContentType,
                sourceContentId: selectedManuscript?.sourceContentId || 0,
                taskType,
                requestedBy
            });
        },
        onSuccess: async () => {
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
        mutationFn: (taskId: number) => workbenchService.applyCandidate({ taskId }),
        onSuccess: async () => {
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
        mutationFn: (taskId: number) => service.applyTaskCandidate({ taskId }),
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
                batchJobId: task.batchJobId || 0,
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
    const selectedNodeKey = selectedManuscript?.nodeKey || null;

    const openTaskDetailDrawer = (task: GraphExtractionTaskRecord) => {
        const taskId = Number(task.taskId);
        if (Number.isNaN(taskId)) {
            return;
        }
        setDetailTaskId(taskId);
        setTaskDetailDrawerOpen(true);
    };

    const applyTask = (task: GraphExtractionTaskRecord) => {
        const taskId = Number(task.taskId);
        if (Number.isNaN(taskId)) {
            return;
        }
        applyTaskMutation.mutate(taskId);
    };

    const regenerateTask = (task: GraphExtractionTaskRecord) => {
        const taskId = Number(task.taskId);
        if (Number.isNaN(taskId)) {
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

    return (
        <KuzhambuPage
            className="graph-extraction-page knowledge-graph-extraction-page"
            description="统一管理 Knowledge 抽取任务、候选结果和正式应用动作。"
            title="知识抽取任务"
        >
            <KuzhambuSpace
                orientation="vertical"
                size={16}
                className="knowledge-graph-extraction-layout"
            >
                <KuzhambuAlert
                    banner
                    className="knowledge-graph-extraction-banner"
                    title="从稿件树选择三才、王圻或明俗稿件，系统会在后台自动创建图谱抽取任务。"
                    type="info"
                />

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

                <section aria-labelledby="graph-extraction-workbench-section">
                    <div className="knowledge-graph-extraction-section-header">
                        <Title id="graph-extraction-workbench-section" level={4}>
                            稿件图谱工作台
                        </Title>
                        <Text type="secondary">
                            稿件树是业务主入口，任务台账用于排查失败、重生成和审计。
                        </Text>
                    </div>
                    <Row gutter={[16, 16]}>
                        <Col xs={24} lg={8}>
                            <GraphExtractionManuscriptTree
                                loading={manuscriptTreeQuery.isLoading}
                                nodes={visibleManuscriptTreeNodes}
                                searchText={manuscriptSearchText}
                                selectedNodeKey={selectedNodeKey}
                                onLoadChildren={loadManuscriptChildren}
                                onSearchChange={setManuscriptSearchText}
                                onSelectManuscript={setSelectedManuscript}
                            />
                        </Col>
                        <Col xs={24} lg={16}>
                            <GraphExtractionManuscriptDetail
                                applying={applyWorkbenchCandidateMutation.isPending}
                                candidate={candidateQuery.data || null}
                                candidateLoading={candidateQuery.isLoading}
                                detail={manuscriptDetailQuery.data || null}
                                extracting={extractManuscriptMutation.isPending}
                                selectedNode={selectedManuscript}
                                onApplyCandidate={applyWorkbenchCandidateMutation.mutate}
                                onExtract={extractManuscriptMutation.mutate}
                            />
                        </Col>
                    </Row>
                </section>

                <section aria-labelledby="graph-extraction-task-section">
                    <div className="knowledge-graph-extraction-section-header">
                        <Title id="graph-extraction-task-section" level={4}>
                            任务列表
                        </Title>
                        <Text type="secondary">
                            将展示任务状态、AI 候选关联、失败原因和应用时间线。
                        </Text>
                    </div>
                    <Card className="knowledge-graph-extraction-placeholder" variant="borderless">
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
                                description="当前还没有抽取任务，可以先从稿件树选择稿件并抽取图谱。"
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                            />
                        )}
                    </Card>
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
                </section>
            </KuzhambuSpace>
        </KuzhambuPage>
    );
};
