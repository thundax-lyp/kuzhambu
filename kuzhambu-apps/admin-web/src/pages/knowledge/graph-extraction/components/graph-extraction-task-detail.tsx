import { Button, Descriptions } from "antd";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import type { GraphExtractionTaskRecord } from "../graph-extraction-types";

interface GraphExtractionTaskDetailProps {
    applying?: boolean;
    canApply?: boolean;
    loading?: boolean;
    open: boolean;
    task?: GraphExtractionTaskRecord | null;
    onApply: () => void;
    onClose: () => void;
}

const formatTimestamp = (value?: number | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString("zh-CN", { hour12: false });
};

const formatReplaceUnconfirmedOnly = (value?: boolean | null) => {
    if (value === undefined || value === null) {
        return "-";
    }
    return value ? "是" : "否";
};

export const GraphExtractionTaskDetail = ({
    applying = false,
    canApply = false,
    loading = false,
    open,
    task,
    onApply,
    onClose
}: GraphExtractionTaskDetailProps) => {
    return (
        <KuzhambuDrawer
            title="抽取任务详情"
            open={open}
            size="middle"
            loading={loading}
            onClose={onClose}
            footer={
                <Button
                    type="primary"
                    disabled={!canApply || !task?.aiCandidateId || task?.status === "APPLIED"}
                    loading={applying}
                    onClick={onApply}
                >
                    应用候选结果
                </Button>
            }
        >
            <Descriptions column={1} bordered size="small">
                <Descriptions.Item label="任务号">{task?.taskId || "-"}</Descriptions.Item>
                <Descriptions.Item label="批次号">{task?.batchJobId || "-"}</Descriptions.Item>
                <Descriptions.Item label="任务类型">{task?.taskType || "-"}</Descriptions.Item>
                <Descriptions.Item label="触发来源">{task?.triggerSource || "-"}</Descriptions.Item>
                <Descriptions.Item label="父任务号">{task?.parentTaskId || "-"}</Descriptions.Item>
                <Descriptions.Item label="任务状态">{task?.status || "-"}</Descriptions.Item>
                <Descriptions.Item label="批量范围 JSON">
                    {task?.selectionScopeJson || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="仅替换未确认结果">
                    {formatReplaceUnconfirmedOnly(task?.replaceUnconfirmedOnly)}
                </Descriptions.Item>
                <Descriptions.Item label="AI Call ID">{task?.aiCallId || "-"}</Descriptions.Item>
                <Descriptions.Item label="AI Candidate ID">
                    {task?.aiCandidateId || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="错误类型">{task?.errorType || "-"}</Descriptions.Item>
                <Descriptions.Item label="错误信息">{task?.errorMessage || "-"}</Descriptions.Item>
                <Descriptions.Item label="请求时间">
                    {formatTimestamp(task?.requestedAt)}
                </Descriptions.Item>
                <Descriptions.Item label="完成时间">
                    {formatTimestamp(task?.completedAt)}
                </Descriptions.Item>
                <Descriptions.Item label="应用时间">
                    {formatTimestamp(task?.appliedAt)}
                </Descriptions.Item>
            </Descriptions>
        </KuzhambuDrawer>
    );
};
