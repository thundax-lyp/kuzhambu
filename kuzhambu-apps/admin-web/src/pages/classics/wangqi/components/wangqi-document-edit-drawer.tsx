import { FileTextOutlined } from "@ant-design/icons";
import { App, Form, Input } from "antd";
import { useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";
import { KuzhambuSegmentedDrawer } from "@/components/kuzhambu-segmented-drawer";
import { KuzhambuSyncTaskModal } from "@/components/kuzhambu-sync-task-modal";
import type { KuzhambuSyncTaskAdapter } from "@/components/kuzhambu-sync-task-modal";
import { WangqiDocumentBasicSection } from "./wangqi-document-basic-section";
import { WangqiDocumentQaSection } from "./wangqi-document-qa-section";
import { WangqiDocumentSourceSection } from "./wangqi-document-source-section";
import { WangqiDocumentTagsSection } from "./wangqi-document-tags-section";
import { WangqiDocumentVersionsSection } from "./wangqi-document-versions-section";
import {
    toWangqiDocumentCommand,
    toWangqiDocumentFormValues,
    type WangqiDocumentFormValues
} from "./wangqi-document-form-values";
import * as aiCandidateService from "@/pages/classics/common/ai-candidate-service";
import type { AiCandidateRecord } from "@/pages/classics/common/ai-candidate-types";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import type { WangqiDocumentCommand } from "../wangqi-service";
import type { WangqiDocumentRecord } from "../wangqi-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { TextArea } = Input;
const SUMMARY_CANDIDATE_POLL_INTERVAL_MS = 3000;

type WangqiDocumentEditDrawerSection = "basic" | "tags" | "qa" | "source" | "versions";

interface WangqiDocumentEditDrawerProps {
    document?: WangqiDocumentRecord | null;
    loading?: boolean;
    mode: "create" | "edit";
    open: boolean;
    qaContent?: ReactNode;
    saving?: boolean;
    sourceFileContent?: ReactNode;
    tagContent?: ReactNode;
    versionContent?: ReactNode;
    creatingSummaryTask?: boolean;
    summaryTasks?: AiRefinementTaskRecord[];
    summaryTrackingTask?: AiRefinementTaskRecord | null;
    onCreateSummaryTask?: () => void;
    onClose: () => void;
    onSave: (command: WangqiDocumentCommand) => void;
}

const SUMMARY_TASK_STATUS_LABELS: Record<string, string> = {
    PENDING: "排队中",
    RUNNING: "运行中",
    SUCCEEDED: "已完成",
    PARTIAL: "部分完成",
    FAILED: "失败",
    CANCELLED: "已取消"
};

const SUMMARY_TASK_ALERT_TYPES: Record<string, "success" | "info" | "warning" | "error"> = {
    PENDING: "info",
    RUNNING: "info",
    SUCCEEDED: "success",
    PARTIAL: "warning",
    FAILED: "error",
    CANCELLED: "warning"
};

const sortTasksByNewest = (left: AiRefinementTaskRecord, right: AiRefinementTaskRecord) => {
    if (left.requestedAt && right.requestedAt && left.requestedAt !== right.requestedAt) {
        return right.requestedAt.localeCompare(left.requestedAt);
    }
    return right.taskId - left.taskId;
};

const getSummaryTaskDescription = (task: AiRefinementTaskRecord) => {
    const failureText = aiRefinementTaskService.getTaskFailureText(
        task.failureStage,
        task.errorType,
        task.errorMessage
    );
    if (failureText) {
        return failureText;
    }
    if (task.status === "SUCCEEDED" || task.status === "PARTIAL") {
        if (!task.candidateId) {
            return "任务已完成，正在等待候选摘要落库并回填。";
        }
        return "任务完成后会刷新候选摘要并回填到 AI 摘要输入框。";
    }
    if (task.status === "PENDING" || task.status === "RUNNING") {
        return "任务执行期间会持续刷新状态，完成后回填候选摘要。";
    }
    return "可重新生成摘要任务。";
};

const isSummaryTaskActive = (task?: AiRefinementTaskRecord | null) => {
    return task?.status === "PENDING" || task?.status === "RUNNING";
};

const isSummaryTaskCompleted = (task?: AiRefinementTaskRecord | null) => {
    return task?.status === "SUCCEEDED" || task?.status === "PARTIAL";
};

const summaryTaskAdapter: KuzhambuSyncTaskAdapter<AiRefinementTaskRecord> = {
    getId: (task) => task.taskId,
    getMessage: getSummaryTaskDescription,
    getPhase: (task) => {
        if (isSummaryTaskActive(task)) {
            return "tracking";
        }
        if (isSummaryTaskCompleted(task)) {
            return task.candidateId ? "result_ready" : "waiting_result";
        }
        if (task.status === "CANCELLED") {
            return "cancelled";
        }
        if (task.status === "FAILED") {
            return "failed";
        }
        return "tracking";
    },
    getResultKey: (task) => task.candidateId,
    getStatusLabel: (task) => {
        const statusLabel = SUMMARY_TASK_STATUS_LABELS[task.status] || task.status;
        return `摘要任务${statusLabel}`;
    }
};

const selectLatestSummaryCandidate = (
    candidates: AiCandidateRecord[],
    trackedCandidateId?: number | null
) => {
    return [...candidates]
        .filter(
            (candidate) =>
                candidate.capability === "summary" &&
                candidate.status === "PENDING" &&
                (!trackedCandidateId || candidate.candidateId === trackedCandidateId) &&
                typeof candidate.resultPayload === "string" &&
                candidate.resultPayload.trim().length > 0
        )
        .sort((left, right) => {
            if (left.requestedAt && right.requestedAt && left.requestedAt !== right.requestedAt) {
                return right.requestedAt.localeCompare(left.requestedAt);
            }
            return right.candidateId - left.candidateId;
        })[0];
};

export const WangqiDocumentEditDrawer = ({
    document,
    loading = false,
    mode,
    open,
    qaContent,
    saving = false,
    sourceFileContent,
    tagContent,
    versionContent,
    creatingSummaryTask = false,
    summaryTasks = [],
    summaryTrackingTask,
    onCreateSummaryTask,
    onClose,
    onSave
}: WangqiDocumentEditDrawerProps) => {
    const { message: messageApi } = App.useApp();
    const [form] = Form.useForm<WangqiDocumentFormValues>();
    const [activeSection, setActiveSection] = useState<WangqiDocumentEditDrawerSection>("basic");
    const [isSummaryModalOpen, setIsSummaryModalOpen] = useState(false);
    const [summaryDraft, setSummaryDraft] = useState("");
    const [loadedSummaryCandidateId, setLoadedSummaryCandidateId] = useState<number | null>(null);
    const documentId = mode === "edit" ? document?.id : undefined;
    const latestSummaryTaskFromList = useMemo(() => {
        return [...summaryTasks]
            .filter((task) => task.capability === "summary")
            .sort(sortTasksByNewest)[0];
    }, [summaryTasks]);
    const trackedSummaryTaskFromList = useMemo(() => {
        return summaryTasks.find((task) => task.taskId === summaryTrackingTask?.taskId);
    }, [summaryTasks, summaryTrackingTask?.taskId]);
    const summaryTrackingTaskId = summaryTrackingTask?.taskId;
    const latestSummaryTask =
        trackedSummaryTaskFromList || summaryTrackingTask || latestSummaryTaskFromList;

    useEffect(() => {
        if (!open) {
            return;
        }
        form.setFieldsValue(toWangqiDocumentFormValues(mode === "edit" ? document : null));
    }, [document, form, mode, open]);

    const closeModel = () => {
        setActiveSection("basic");
        setIsSummaryModalOpen(false);
        onClose();
    };

    const saveDocument = async () => {
        const values = await form.validateFields();
        onSave(toWangqiDocumentCommand(values, mode === "edit" ? document : null));
    };

    const openSummaryModal = () => {
        setSummaryDraft(form.getFieldValue("summary") || "");
        setLoadedSummaryCandidateId(null);
        setIsSummaryModalOpen(true);
    };

    const closeSummaryModal = () => {
        setIsSummaryModalOpen(false);
    };

    const requestSummaryTask = () => {
        if (!onCreateSummaryTask) {
            messageApi.warning("请先保存王圻文档后再使用 AI 摘要");
            return;
        }
        onCreateSummaryTask();
    };

    const loadSummaryCandidate = async (task: AiRefinementTaskRecord | null) => {
        if (!documentId) {
            return null;
        }
        const trackedCandidateId = task?.candidateId ?? null;
        if (summaryTrackingTaskId && !trackedCandidateId) {
            return null;
        }
        const candidates = await aiCandidateService.list({
            contentId: documentId,
            contentType: "WANGQI_DOCUMENT",
            capability: "summary",
            status: "PENDING"
        });
        return selectLatestSummaryCandidate(candidates, trackedCandidateId) ?? null;
    };

    const updateSummaryDraftFromCandidate = (candidate: AiCandidateRecord | null) => {
        if (!candidate || candidate.candidateId === loadedSummaryCandidateId) {
            return;
        }
        setLoadedSummaryCandidateId(candidate.candidateId);
        setSummaryDraft(candidate.resultPayload?.trim() || "");
    };

    const applySummaryDraft = () => {
        form.setFieldValue("summary", summaryDraft);
        setIsSummaryModalOpen(false);
        messageApi.success("摘要已写入基础信息");
    };

    const sections = [
        {
            label: "基础信息",
            value: "basic",
            content: (
                <WangqiDocumentBasicSection mode={mode} onOpenSummaryModal={openSummaryModal} />
            )
        },
        {
            label: "标签",
            value: "tags",
            content: <WangqiDocumentTagsSection content={tagContent} />,
            visible: mode === "edit"
        },
        {
            label: "问答",
            value: "qa",
            content: <WangqiDocumentQaSection content={qaContent} />,
            visible: mode === "edit"
        },
        {
            label: "文件",
            value: "source",
            content: <WangqiDocumentSourceSection content={sourceFileContent} />,
            visible: mode === "edit"
        },
        {
            label: "版本",
            value: "versions",
            content: <WangqiDocumentVersionsSection content={versionContent} />,
            visible: mode === "edit"
        }
    ] satisfies Array<{
        content: ReactNode;
        label: string;
        value: WangqiDocumentEditDrawerSection;
        visible?: boolean;
    }>;

    return (
        <KuzhambuSegmentedDrawer
            activeSection={activeSection}
            sectionClassName="wangqi-document-edit-drawer-section"
            sections={sections}
            segmentedClassName="wangqi-document-edit-drawer-sections"
            testId="classics-wangqi-document-editor-drawer"
            title={mode === "create" ? "新增王圻文档" : "编辑王圻文档"}
            open={open}
            size="large"
            loading={loading}
            destroyOnHidden
            onClose={closeModel}
            onSectionChange={setActiveSection}
            renderSectionContent={(content) => (
                <Form<WangqiDocumentFormValues>
                    form={form}
                    colon={false}
                    labelCol={{ flex: "88px" }}
                    layout="horizontal"
                    className="wangqi-document-edit-drawer-form"
                >
                    {content}
                </Form>
            )}
            footer={
                <div className="wangqi-document-edit-drawer-footer">
                    <KuzhambuButton
                        testId="classics-wangqi-document-cancel-button"
                        onClick={closeModel}
                    >
                        取消
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="classics-wangqi-document-save-button"
                        type="primary"
                        loading={saving}
                        onClick={saveDocument}
                    >
                        保存
                    </KuzhambuButton>
                </div>
            }
        >
            <KuzhambuSyncTaskModal<AiRefinementTaskRecord, AiCandidateRecord>
                testId="classics-wangqi-document-summary-ai-modal"
                className="wangqi-summary-modal"
                title="AI 摘要"
                open={isSummaryModalOpen}
                width={880}
                applyDisabled={!summaryDraft.trim()}
                applyTestId="classics-wangqi-document-summary-ai-apply-button"
                createAriaLabel="生成 AI 摘要"
                createIcon={<FileTextOutlined />}
                createTestId="classics-wangqi-document-summary-ai-generate-button"
                createText="生成摘要"
                creating={creatingSummaryTask}
                fetchResult={loadSummaryCandidate}
                fetchTask={(taskId) => aiRefinementTaskService.getTask({ taskId: Number(taskId) })}
                pollIntervalMs={SUMMARY_CANDIDATE_POLL_INTERVAL_MS}
                resultQueryKey={["WANGQI_DOCUMENT", documentId, "summary"]}
                task={latestSummaryTask}
                taskAdapter={summaryTaskAdapter}
                trackTask={Boolean(summaryTrackingTaskId)}
                onApply={applySummaryDraft}
                onCancel={closeSummaryModal}
                onCreate={requestSummaryTask}
                onResultChange={updateSummaryDraftFromCandidate}
                renderStatus={({ creating, resultLoading, task }) => {
                    if (creating) {
                        return (
                            <KuzhambuAlert
                                showIcon
                                className="kuzhambu-sync-task-modal-status"
                                type="info"
                                title="正在创建摘要任务"
                                description="任务创建成功后会自动进入状态跟踪。"
                            />
                        );
                    }
                    if (task) {
                        const statusLabel = SUMMARY_TASK_STATUS_LABELS[task.status] || task.status;
                        return (
                            <KuzhambuAlert
                                showIcon
                                className="kuzhambu-sync-task-modal-status"
                                type={SUMMARY_TASK_ALERT_TYPES[task.status] || "info"}
                                title={`摘要任务${statusLabel}`}
                                description={getSummaryTaskDescription(task)}
                            />
                        );
                    }
                    if (resultLoading) {
                        return (
                            <KuzhambuAlert
                                showIcon
                                className="kuzhambu-sync-task-modal-status"
                                type="info"
                                title="正在加载候选摘要"
                                description="任务完成后会自动刷新候选摘要。"
                            />
                        );
                    }
                    return null;
                }}
                renderBody={({ creating, resultLoading }) => (
                    <>
                        <div className="wangqi-summary-modal-compare-grid">
                            <Form
                                className="wangqi-summary-modal-card"
                                colon={false}
                                layout="vertical"
                            >
                                <Form.Item label="当前摘要">
                                    <TextArea
                                        aria-label="AI摘要当前摘要"
                                        value={form.getFieldValue("summary") || ""}
                                        readOnly
                                        autoSize={resolveTextAreaAutoSize({
                                            minRows: 8,
                                            maxRows: 14
                                        })}
                                    />
                                </Form.Item>
                            </Form>
                            <Form
                                className="wangqi-summary-modal-card"
                                colon={false}
                                layout="vertical"
                            >
                                <Form.Item label="AI 摘要">
                                    <TextArea
                                        aria-label="AI摘要候选摘要"
                                        value={summaryDraft}
                                        placeholder={
                                            creating || resultLoading
                                                ? "AI 摘要生成中..."
                                                : "暂无候选摘要，可先点击摘要生成，或手动编辑后采用"
                                        }
                                        autoSize={resolveTextAreaAutoSize({
                                            minRows: 8,
                                            maxRows: 14
                                        })}
                                        onChange={(event) => setSummaryDraft(event.target.value)}
                                    />
                                </Form.Item>
                            </Form>
                        </div>
                        <Form
                            className="wangqi-summary-modal-card wangqi-summary-modal-content-card"
                            colon={false}
                            layout="vertical"
                        >
                            <Form.Item label="正文">
                                <TextArea
                                    aria-label="AI摘要参考正文"
                                    value={form.getFieldValue("content") || ""}
                                    readOnly
                                    autoSize={resolveTextAreaAutoSize({ minRows: 8, maxRows: 14 })}
                                />
                            </Form.Item>
                        </Form>
                    </>
                )}
            />
        </KuzhambuSegmentedDrawer>
    );
};
