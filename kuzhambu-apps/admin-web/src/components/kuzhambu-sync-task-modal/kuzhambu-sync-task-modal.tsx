import { CloseOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { useEffect } from "react";
import type { ReactNode } from "react";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuModal } from "@/components/kuzhambu-modal";
import type { KuzhambuModalProps } from "@/components/kuzhambu-modal";
import "./kuzhambu-sync-task-modal.css";

export type KuzhambuSyncTaskPhase =
    "idle" | "creating" | "tracking" | "waiting_result" | "result_ready" | "failed" | "cancelled";

/**
 * 把业务 task 映射成通用 modal 能理解的任务生命周期。
 *
 * 调用方必须保证 getPhase 能准确区分：
 * - tracking：远端任务仍在执行，modal 会继续轮询 fetchTask。
 * - waiting_result：任务已结束但候选结果还没可用，modal 会继续轮询 fetchTask/fetchResult。
 * - result_ready：任务已产出结果 key，modal 会拉取 fetchResult。
 * - failed/cancelled：终态，modal 停止任务轮询并展示失败/取消状态。
 */
export interface KuzhambuSyncTaskAdapter<TTask> {
    /** 用于定位远端任务，也是 fetchTask 的入参。 */
    getId: (task: TTask) => string;
    /** 状态提示的补充描述；不应包含业务表单写入逻辑。 */
    getMessage?: (task: TTask) => ReactNode;
    getPhase: (task: TTask) => KuzhambuSyncTaskPhase;
    /** 候选结果的稳定标识，例如 candidateId；变化后会刷新 result query。 */
    getResultKey?: (task: TTask) => number | string | null | undefined;
    /** 状态提示标题；不传则使用 modal 的通用文案。 */
    getStatusLabel?: (task: TTask) => ReactNode;
}

/**
 * 一个同步任务 modal 的完整工作流配置。
 *
 * 强约束原则：下游页面不要分散传 taskAdapter、fetchTask、onCreate 等零散 props。
 * 所有任务生命周期相关能力都必须集中到 workflow，代码审查时只看这一处就能确认：
 * 当前 task 从哪里来、怎么创建、怎么轮询、怎么取结果、怎么采用结果。
 */
export interface KuzhambuSyncTaskWorkflow<TTask, TResult> extends KuzhambuSyncTaskAdapter<TTask> {
    /** 当前要展示/跟踪的任务；创建任务成功后必须更新它，modal 才能开始 fetchTask 轮询。 */
    task: TTask | null;
    /** 只负责发起任务；任务创建成功后调用方应把返回 task 写回 workflow.task。 */
    createTask: () => void;
    /**
     * 拉取候选结果。会收到最新 task，包括 fetchTask 轮询返回的 task。
     * 若 task 尚未带 candidateId，调用方通常应返回 null，让 modal 继续轮询。
     */
    fetchResult?: (task: TTask | null) => Promise<TResult | null>;
    /** 根据 getId(task) 轮询远端任务状态。 */
    fetchTask?: (taskId: string) => Promise<TTask>;
    onResultChange?: (result: TResult | null) => void;
    onTaskChange?: (task: TTask | null) => void;
    /** 采用候选结果；业务保存、候选状态变更和表单字段回填都应放在这里。 */
    applyResult?: (result: TResult | null) => void | Promise<void>;
    pollIntervalMs?: number;
    resultQueryKey?: readonly unknown[];
    /** 是否启用 fetchTask 轮询；通常只有 task 存在时传 true。 */
    trackTask?: boolean;
}

/**
 * renderBody/renderStatus/applyDisabled/createDisabled 接收到的统一状态。
 *
 * 页面应优先使用 canCreate/canApply 控制按钮，并用 creating、tracking、resultLoading、
 * taskLoading、applying 等状态锁定会和任务结果冲突的输入框或操作。
 */
export interface KuzhambuSyncTaskModalState<TTask, TResult> {
    canApply: boolean;
    canCreate: boolean;
    creating: boolean;
    isBusy: boolean;
    message?: ReactNode;
    phase: KuzhambuSyncTaskPhase;
    refetchResult: () => void;
    refetchTask: () => void;
    result: TResult | null;
    resultError: Error | null;
    resultLoading: boolean;
    statusLabel?: ReactNode;
    task: TTask | null;
    taskError: Error | null;
    taskLoading: boolean;
    tracking: boolean;
}

export interface KuzhambuSyncTaskModalProps<TTask, TResult> extends Omit<
    KuzhambuModalProps,
    "children" | "footer" | "onCancel" | "title"
> {
    /** 额外的采用按钮禁用条件；modal 已经会合并 applying 和 workflow.applyResult 是否存在。 */
    applyDisabled?: boolean | ((state: KuzhambuSyncTaskModalState<TTask, TResult>) => boolean);
    applyTestId?: string;
    /** 页面执行采用候选结果时传入，用于按钮 loading 和 busy 状态。 */
    applying?: boolean;
    applyText?: ReactNode;
    cancelTestId?: string;
    cancelText?: ReactNode;
    createAriaLabel?: string;
    /** 额外的创建按钮禁用条件；modal 已经会在创建、采用、跟踪和任务加载中自动禁用。 */
    createDisabled?: boolean | ((state: KuzhambuSyncTaskModalState<TTask, TResult>) => boolean);
    createIcon?: ReactNode;
    createTestId?: string;
    createText: ReactNode;
    /** 页面创建任务 mutation 的 pending 状态。 */
    creating?: boolean;
    onCancel: () => void;
    renderBody: (state: KuzhambuSyncTaskModalState<TTask, TResult>) => ReactNode;
    renderStatus?: (state: KuzhambuSyncTaskModalState<TTask, TResult>) => ReactNode;
    title: ReactNode;
    workflow: KuzhambuSyncTaskWorkflow<TTask, TResult>;
}

const TASK_PHASE_STATUS_TYPE: Record<
    KuzhambuSyncTaskPhase,
    "success" | "info" | "warning" | "error"
> = {
    idle: "info",
    creating: "info",
    tracking: "info",
    waiting_result: "info",
    result_ready: "success",
    failed: "error",
    cancelled: "warning"
};

const TASK_PHASE_LABELS: Record<KuzhambuSyncTaskPhase, string> = {
    idle: "暂无任务",
    creating: "正在创建任务",
    tracking: "任务执行中",
    waiting_result: "正在等待结果",
    result_ready: "任务结果已返回",
    failed: "任务失败",
    cancelled: "任务已取消"
};

const readError = (error: unknown) => {
    return error instanceof Error ? error : null;
};

const resolvePhase = <TResult,>({
    creating,
    hasFetchResult,
    result,
    resultFetching,
    taskFetching,
    taskPhase
}: {
    creating: boolean;
    hasFetchResult: boolean;
    result: TResult | null;
    resultFetching: boolean;
    taskFetching: boolean;
    taskPhase: KuzhambuSyncTaskPhase;
}) => {
    if (creating) {
        return "creating";
    }
    if (taskPhase === "result_ready" && hasFetchResult && !result && resultFetching) {
        return "waiting_result";
    }
    if (taskFetching || resultFetching) {
        return taskPhase;
    }
    return taskPhase;
};

// 标准接入流程：
// 1. 页面打开 modal，并在 workflow.task 传入当前最新任务；没有任务时传 null。
// 2. 用户点击创建按钮后，workflow.createTask 只负责触发创建任务 mutation。
// 3. 创建任务成功后，页面必须把后端返回的 task 写回父级状态或 query cache，并重新传给 workflow.task。
// 4. modal 根据 workflow.getId(task) 调用 workflow.fetchTask，并在 tracking/waiting_result 阶段轮询。
// 5. task 进入 result_ready 或 waiting_result 后，modal 调用 workflow.fetchResult 拉取候选结果。
// 6. 页面通过 workflow.onTaskChange/onResultChange 同步最新 task/result 到本地 draft 或父级状态。
// 7. 用户点击采用后，workflow.applyResult 负责业务保存、候选状态变更和表单字段回填。
//
// 使用边界：这个组件只负责通用任务生命周期。业务结果解析、候选校验、保存入库、
// 表单字段映射都应留在页面组件里，通过 workflow、renderBody 和 renderStatus 接入。
export const KuzhambuSyncTaskModal = <TTask, TResult>({
    applyDisabled = false,
    applyTestId,
    applying = false,
    applyText = "采用",
    cancelTestId,
    cancelText = "取消",
    className,
    createAriaLabel,
    createDisabled = false,
    createIcon,
    createTestId,
    createText,
    creating = false,
    destroyOnHidden = true,
    onCancel,
    open,
    renderBody,
    renderStatus,
    title,
    workflow,
    ...modalProps
}: KuzhambuSyncTaskModalProps<TTask, TResult>) => {
    const {
        applyResult,
        createTask,
        fetchResult,
        fetchTask,
        onResultChange,
        onTaskChange,
        pollIntervalMs = 3000,
        resultQueryKey = [],
        task,
        trackTask = true
    } = workflow;
    const taskId = task ? workflow.getId(task) : null;
    const taskQuery = useQuery({
        queryKey: ["sync-task-modal", modalProps.testId, "task", taskId],
        queryFn: () => fetchTask?.(taskId ?? ""),
        enabled: open && trackTask && Boolean(fetchTask) && taskId != null,
        retry: false,
        refetchInterval: (query) => {
            const latestTask = (query.state.data ?? task) as TTask | null;
            if (!latestTask) {
                return false;
            }
            const phase = workflow.getPhase(latestTask);
            return phase === "tracking" || phase === "waiting_result" ? pollIntervalMs : false;
        }
    });
    const effectiveTask = (taskQuery.data ?? task) as TTask | null;
    const taskPhase = effectiveTask ? workflow.getPhase(effectiveTask) : "idle";
    const resultKey = effectiveTask ? workflow.getResultKey?.(effectiveTask) : null;
    const resultQuery = useQuery({
        queryKey: [
            "sync-task-modal",
            modalProps.testId,
            "result",
            ...resultQueryKey,
            taskId,
            resultKey ?? null
        ],
        queryFn: () => fetchResult?.(effectiveTask),
        enabled: open && Boolean(fetchResult),
        retry: false,
        refetchInterval: (query) => {
            const latestResult = query.state.data as TResult | null | undefined;
            if (latestResult) {
                return false;
            }
            if (creating || taskPhase === "tracking" || taskPhase === "waiting_result") {
                return pollIntervalMs;
            }
            if (taskPhase === "result_ready" && latestResult === undefined) {
                return pollIntervalMs;
            }
            return false;
        }
    });
    const result = (resultQuery.data ?? null) as TResult | null;
    const phase = resolvePhase({
        creating,
        hasFetchResult: Boolean(fetchResult),
        result,
        resultFetching: resultQuery.isFetching,
        taskFetching: taskQuery.isFetching,
        taskPhase
    });
    const baseState: Omit<KuzhambuSyncTaskModalState<TTask, TResult>, "canApply" | "canCreate"> = {
        creating,
        isBusy: creating || taskQuery.isFetching || resultQuery.isFetching || applying,
        message: effectiveTask ? workflow.getMessage?.(effectiveTask) : undefined,
        phase,
        refetchResult: () => {
            void resultQuery.refetch();
        },
        refetchTask: () => {
            void taskQuery.refetch();
        },
        result,
        resultError: readError(resultQuery.error),
        resultLoading: resultQuery.isFetching,
        statusLabel: effectiveTask ? workflow.getStatusLabel?.(effectiveTask) : undefined,
        task: effectiveTask,
        taskError: readError(taskQuery.error),
        taskLoading: taskQuery.isFetching,
        tracking: phase === "tracking" || phase === "waiting_result"
    };
    const applyDisabledValue =
        typeof applyDisabled === "function"
            ? applyDisabled({ ...baseState, canApply: false, canCreate: false })
            : applyDisabled;
    const createDisabledValue =
        typeof createDisabled === "function"
            ? createDisabled({ ...baseState, canApply: false, canCreate: false })
            : createDisabled;
    const state: KuzhambuSyncTaskModalState<TTask, TResult> = {
        ...baseState,
        canApply: Boolean(applyResult) && !applying && !applyDisabledValue,
        canCreate:
            !creating &&
            !applying &&
            !baseState.tracking &&
            !baseState.taskLoading &&
            !createDisabledValue
    };

    useEffect(() => {
        onResultChange?.(result);
    }, [onResultChange, result]);

    useEffect(() => {
        onTaskChange?.(effectiveTask);
    }, [effectiveTask, onTaskChange]);

    let statusContent: ReactNode = null;
    if (renderStatus) {
        statusContent = renderStatus(state);
    } else if (phase !== "idle" || state.resultError || state.taskError) {
        let errorTitle: string | null = null;
        if (state.taskError) {
            errorTitle = "任务状态获取失败";
        } else if (state.resultError) {
            errorTitle = "任务结果获取失败";
        }
        statusContent = (
            <KuzhambuAlert
                showIcon
                className="kuzhambu-sync-task-modal-status"
                type={errorTitle ? "error" : TASK_PHASE_STATUS_TYPE[phase]}
                title={errorTitle || state.statusLabel || TASK_PHASE_LABELS[phase]}
                description={
                    state.taskError?.message ||
                    state.resultError?.message ||
                    state.message ||
                    undefined
                }
            />
        );
    }

    return (
        <KuzhambuModal
            {...modalProps}
            className={["kuzhambu-sync-task-modal", className].filter(Boolean).join(" ")}
            closable={false}
            destroyOnHidden={destroyOnHidden}
            footer={
                <div className="kuzhambu-sync-task-modal-footer">
                    <KuzhambuButton
                        testId={cancelTestId || `${modalProps.testId}-cancel-button`}
                        onClick={onCancel}
                    >
                        {cancelText}
                    </KuzhambuButton>
                    {applyResult ? (
                        <KuzhambuButton
                            testId={applyTestId || `${modalProps.testId}-apply-button`}
                            type="primary"
                            disabled={!state.canApply}
                            loading={applying}
                            onClick={() => void applyResult(result)}
                        >
                            {applyText}
                        </KuzhambuButton>
                    ) : null}
                </div>
            }
            open={open}
            title={
                <div className="kuzhambu-sync-task-modal-title">
                    <span>{title}</span>
                    <div className="kuzhambu-sync-task-modal-title-actions">
                        <KuzhambuButton
                            testId={createTestId || `${modalProps.testId}-create-button`}
                            type="primary"
                            ariaLabel={createAriaLabel}
                            disabled={!state.canCreate}
                            icon={createIcon}
                            loading={creating}
                            onClick={createTask}
                        >
                            {createText}
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId={`${modalProps.testId}-close-button`}
                            type="text"
                            ariaLabel="关闭弹窗"
                            icon={<CloseOutlined />}
                            onClick={onCancel}
                        />
                    </div>
                </div>
            }
            onCancel={onCancel}
        >
            {statusContent}
            {renderBody(state)}
        </KuzhambuModal>
    );
};
