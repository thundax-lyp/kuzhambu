import { Descriptions, Empty, Spin, Typography } from "antd";
import { KuzhambuSegmentedDrawer, KuzhambuSpace } from "@/components";
import type { KuzhambuSegmentedDrawerSection } from "@/components";
import type {
    GraphExtractionTaskDetailRecord,
    GraphExtractionTaskDrawerSection,
    GraphExtractionTaskRecord
} from "../graph-extraction-types";
import { TaskCandidatePanel } from "../task-candidate-panel";
import { TaskDispositionPanel } from "../task-disposition-panel";
import { TaskExecutionPanel } from "../task-execution-panel";

const { Text } = Typography;

interface TaskDetailDrawerProps {
    activeSection: GraphExtractionTaskDrawerSection;
    detail: GraphExtractionTaskDetailRecord | null;
    loading?: boolean;
    onCancel?: (task: GraphExtractionTaskRecord) => void;
    onClose: () => void;
    onDiscard?: (task: GraphExtractionTaskRecord) => void;
    onMerge?: (task: GraphExtractionTaskRecord) => void;
    onRegenerate?: (task: GraphExtractionTaskRecord) => void;
    onReplace?: (task: GraphExtractionTaskRecord) => void;
    onRetry?: (task: GraphExtractionTaskRecord) => void;
    onSectionChange: (section: GraphExtractionTaskDrawerSection) => void;
    open: boolean;
}

const formatTimestamp = (value?: number | string | null) => {
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

const TaskDetailPlaceholder = ({ children, testId }: { children: string; testId: string }) => (
    <Empty data-testid={testId} description={children} />
);

export const TaskDetailDrawer = ({
    activeSection,
    detail,
    loading = false,
    open,
    onCancel,
    onClose,
    onDiscard,
    onMerge,
    onRegenerate,
    onReplace,
    onRetry,
    onSectionChange
}: TaskDetailDrawerProps) => {
    const task = detail?.task ?? null;
    const sections: Array<KuzhambuSegmentedDrawerSection<GraphExtractionTaskDrawerSection>> = [
        {
            content: (
                <Descriptions
                    data-testid="knowledge-graph-extraction-task-detail-overview-section"
                    column={1}
                    bordered
                    size="small"
                >
                    <Descriptions.Item label="任务号">{task?.taskId || "-"}</Descriptions.Item>
                    <Descriptions.Item label="批次号">{task?.batchJobId || "-"}</Descriptions.Item>
                    <Descriptions.Item label="任务类型">{task?.taskType || "-"}</Descriptions.Item>
                    <Descriptions.Item label="触发来源">
                        {task?.triggerSource || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="父任务号">
                        {task?.parentTaskId || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="任务状态">{task?.status || "-"}</Descriptions.Item>
                    <Descriptions.Item label="批量范围 JSON">
                        {task?.selectionScopeJson || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="仅替换未确认结果">
                        {formatReplaceUnconfirmedOnly(task?.replaceUnconfirmedOnly)}
                    </Descriptions.Item>
                    <Descriptions.Item label="AI Call ID">
                        {task?.aiCallId || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="AI Candidate ID">
                        {task?.aiCandidateId || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="错误类型">{task?.errorType || "-"}</Descriptions.Item>
                    <Descriptions.Item label="错误信息">
                        {task?.errorMessage || task?.failureReason || "-"}
                    </Descriptions.Item>
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
            ),
            label: "概览",
            value: "OVERVIEW"
        },
        {
            content: <TaskExecutionPanel detail={detail} />,
            label: "执行过程",
            value: "EXECUTION"
        },
        {
            content: <TaskCandidatePanel candidate={detail?.candidate ?? null} />,
            label: "候选预览",
            value: "CANDIDATE"
        },
        {
            content: (
                <TaskDispositionPanel
                    detail={detail}
                    onCancel={onCancel}
                    onDiscard={onDiscard}
                    onMerge={onMerge}
                    onRegenerate={onRegenerate}
                    onReplace={onReplace}
                    onRetry={onRetry}
                />
            ),
            label: "候选处置",
            value: "DISPOSITION"
        }
    ];

    return (
        <KuzhambuSegmentedDrawer<GraphExtractionTaskDrawerSection>
            activeSection={activeSection}
            destroyOnHidden
            open={open}
            sectionClassName="knowledge-graph-extraction-task-detail-drawer-section"
            sections={sections}
            size="large"
            testId="knowledge-graph-extraction-task-detail-drawer"
            title="抽取任务详情"
            onClose={onClose}
            onSectionChange={onSectionChange}
        >
            <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                <Text type="secondary">
                    {detail?.source.contentType ?? task?.sourceContentType ?? "-"} / #
                    {detail?.source.contentRef.contentRefId ?? task?.sourceContentId ?? "-"}
                </Text>
                {loading ? <Spin tip="任务详情加载中" /> : null}
            </KuzhambuSpace>
        </KuzhambuSegmentedDrawer>
    );
};
