import { Descriptions } from "antd";
import { KuzhambuDrawer } from "@/components";
import type { GraphDeletionTaskRecord } from "../graph-deletion-task-types";

interface DeletionTaskDetailDrawerProps {
    onClose: () => void;
    open: boolean;
    task?: GraphDeletionTaskRecord | null;
}

export const DeletionTaskDetailDrawer = ({
    onClose,
    open,
    task
}: DeletionTaskDetailDrawerProps) => (
    <KuzhambuDrawer
        open={open}
        onClose={onClose}
        title="删除任务详情"
        size="middle"
        testId="knowledge-graph-deletion-task-detail-drawer"
    >
        <Descriptions bordered column={1} size="small">
            <Descriptions.Item label="任务号">{task?.id ?? "-"}</Descriptions.Item>
            <Descriptions.Item label="状态">{task?.status ?? "-"}</Descriptions.Item>
            <Descriptions.Item label="失败原因">{task?.failureReason ?? "-"}</Descriptions.Item>
        </Descriptions>
    </KuzhambuDrawer>
);
