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

export interface KuzhambuSyncTaskAdapter<TTask> {
    getId: (task: TTask) => number | string;
    getMessage?: (task: TTask) => ReactNode;
    getPhase: (task: TTask) => KuzhambuSyncTaskPhase;
    getResultKey?: (task: TTask) => number | string | null | undefined;
    getStatusLabel?: (task: TTask) => ReactNode;
}

export interface KuzhambuSyncTaskModalState<TTask, TResult> {
    canApply: boolean;
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
    applyDisabled?: boolean | ((state: KuzhambuSyncTaskModalState<TTask, TResult>) => boolean);
    applyTestId?: string;
    applying?: boolean;
    applyText?: ReactNode;
    cancelTestId?: string;
    cancelText?: ReactNode;
    createAriaLabel?: string;
    createIcon?: ReactNode;
    createTestId?: string;
    createText: ReactNode;
    creating?: boolean;
    fetchResult?: (task: TTask | null) => Promise<TResult | null>;
    fetchTask?: (taskId: number | string) => Promise<TTask>;
    onApply?: (result: TResult | null) => void | Promise<void>;
    onCancel: () => void;
    onCreate: () => void;
    onResultChange?: (result: TResult | null) => void;
    pollIntervalMs?: number;
    renderBody: (state: KuzhambuSyncTaskModalState<TTask, TResult>) => ReactNode;
    renderStatus?: (state: KuzhambuSyncTaskModalState<TTask, TResult>) => ReactNode;
    resultQueryKey?: readonly unknown[];
    task?: TTask | null;
    taskAdapter: KuzhambuSyncTaskAdapter<TTask>;
    title: ReactNode;
    trackTask?: boolean;
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
    if (taskPhase === "result_ready" && hasFetchResult && !result) {
        return "waiting_result";
    }
    if (taskFetching || resultFetching) {
        return taskPhase;
    }
    return taskPhase;
};

// AI NOTE: This component owns only the generic lifecycle for
// "open modal -> create task -> poll task -> load result -> let page apply result".
// Do not put business result parsing, candidate validation, persistence, or form-field mapping here.
// Keep those semantics in page-domain components via taskAdapter, fetchResult, renderBody, and onApply.
export const KuzhambuSyncTaskModal = <TTask, TResult>({
    applyDisabled = false,
    applyTestId,
    applying = false,
    applyText = "采用",
    cancelTestId,
    cancelText = "取消",
    className,
    createAriaLabel,
    createIcon,
    createTestId,
    createText,
    creating = false,
    destroyOnHidden = true,
    fetchResult,
    fetchTask,
    onApply,
    onCancel,
    onCreate,
    onResultChange,
    open,
    pollIntervalMs = 3000,
    renderBody,
    renderStatus,
    resultQueryKey = [],
    task = null,
    taskAdapter,
    title,
    trackTask = true,
    ...modalProps
}: KuzhambuSyncTaskModalProps<TTask, TResult>) => {
    const taskId = task ? taskAdapter.getId(task) : null;
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
            const phase = taskAdapter.getPhase(latestTask);
            return phase === "tracking" || phase === "waiting_result" ? pollIntervalMs : false;
        }
    });
    const effectiveTask = (taskQuery.data ?? task) as TTask | null;
    const taskPhase = effectiveTask ? taskAdapter.getPhase(effectiveTask) : "idle";
    const resultKey = effectiveTask ? taskAdapter.getResultKey?.(effectiveTask) : null;
    const shouldPollResult = creating || taskPhase === "tracking" || taskPhase === "waiting_result";
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
        refetchInterval: () => (shouldPollResult ? pollIntervalMs : false)
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
    const baseState: Omit<KuzhambuSyncTaskModalState<TTask, TResult>, "canApply"> = {
        creating,
        isBusy: creating || taskQuery.isFetching || resultQuery.isFetching || applying,
        message: effectiveTask ? taskAdapter.getMessage?.(effectiveTask) : undefined,
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
        statusLabel: effectiveTask ? taskAdapter.getStatusLabel?.(effectiveTask) : undefined,
        task: effectiveTask,
        taskError: readError(taskQuery.error),
        taskLoading: taskQuery.isFetching,
        tracking: phase === "tracking" || phase === "waiting_result"
    };
    const applyDisabledValue =
        typeof applyDisabled === "function"
            ? applyDisabled({ ...baseState, canApply: false })
            : applyDisabled;
    const state: KuzhambuSyncTaskModalState<TTask, TResult> = {
        ...baseState,
        canApply: Boolean(onApply) && !applying && !applyDisabledValue
    };

    useEffect(() => {
        onResultChange?.(result);
    }, [onResultChange, result]);

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
            destroyOnHidden={destroyOnHidden}
            footer={
                <div className="kuzhambu-sync-task-modal-footer">
                    <KuzhambuButton
                        testId={cancelTestId || `${modalProps.testId}-cancel-button`}
                        onClick={onCancel}
                    >
                        {cancelText}
                    </KuzhambuButton>
                    {onApply ? (
                        <KuzhambuButton
                            testId={applyTestId || `${modalProps.testId}-apply-button`}
                            type="primary"
                            disabled={!state.canApply}
                            loading={applying}
                            onClick={() => void onApply(result)}
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
                    <KuzhambuButton
                        testId={createTestId || `${modalProps.testId}-create-button`}
                        type="primary"
                        ariaLabel={createAriaLabel}
                        icon={createIcon}
                        loading={creating}
                        onClick={onCreate}
                    >
                        {createText}
                    </KuzhambuButton>
                </div>
            }
            onCancel={onCancel}
        >
            {statusContent}
            {renderBody(state)}
        </KuzhambuModal>
    );
};
