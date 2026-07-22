import { FileTextOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { App, Form, Input, Segmented } from "antd";
import { useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuModal } from "@/components/kuzhambu-modal";
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
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import type { WangqiDocumentCommand } from "../wangqi-service";
import type { WangqiDocumentRecord } from "../wangqi-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { TextArea } = Input;
const SUMMARY_CANDIDATE_POLL_INTERVAL_MS = 3000;

type WangqiDocumentEditDrawerSection = "basic" | "tags" | "qa" | "source" | "versions";
type SummaryTaskAlertType = "success" | "info" | "warning" | "error";

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

const SUMMARY_TASK_ALERT_TYPES: Record<string, SummaryTaskAlertType> = {
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

const isSummaryTaskActive = (task?: AiRefinementTaskRecord) => {
    return task?.status === "PENDING" || task?.status === "RUNNING";
};

const isSummaryTaskCompleted = (task?: AiRefinementTaskRecord) => {
    return task?.status === "SUCCEEDED" || task?.status === "PARTIAL";
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
    const trackedSummaryTaskQuery = useQuery({
        queryKey: ["classics", "wangqi", "refinement", "task", summaryTrackingTaskId],
        queryFn: () => aiRefinementTaskService.getTask({ taskId: summaryTrackingTaskId ?? 0 }),
        enabled: isSummaryModalOpen && Boolean(summaryTrackingTaskId),
        retry: false,
        refetchInterval: (query) => {
            const task = query.state.data;
            if (!summaryTrackingTaskId) {
                return false;
            }
            if (!task) {
                return SUMMARY_CANDIDATE_POLL_INTERVAL_MS;
            }
            if (isSummaryTaskActive(task)) {
                return SUMMARY_CANDIDATE_POLL_INTERVAL_MS;
            }
            if (isSummaryTaskCompleted(task) && !task.candidateId) {
                return SUMMARY_CANDIDATE_POLL_INTERVAL_MS;
            }
            return false;
        }
    });
    const latestSummaryTask =
        trackedSummaryTaskQuery.data ||
        trackedSummaryTaskFromList ||
        summaryTrackingTask ||
        latestSummaryTaskFromList;
    const latestSummaryTaskId = latestSummaryTask?.taskId;
    const trackedSummaryCandidateId =
        summaryTrackingTaskId && latestSummaryTask?.taskId === summaryTrackingTaskId
            ? latestSummaryTask.candidateId
            : null;
    const isLatestSummaryTaskCompleted = isSummaryTaskCompleted(latestSummaryTask);
    const shouldPollSummaryCandidates =
        creatingSummaryTask ||
        isSummaryTaskActive(latestSummaryTask) ||
        Boolean(
            summaryTrackingTaskId &&
            isLatestSummaryTaskCompleted &&
            trackedSummaryCandidateId &&
            loadedSummaryCandidateId !== trackedSummaryCandidateId
        );

    const summaryCandidatesQuery = useQuery({
        queryKey: ["ai", "candidates", "WANGQI_DOCUMENT", documentId, "summary", "modal"],
        queryFn: () =>
            aiCandidateService.list({
                contentId: documentId,
                contentType: "WANGQI_DOCUMENT",
                capability: "summary",
                status: "PENDING"
            }),
        enabled: isSummaryModalOpen && Boolean(documentId),
        retry: false,
        refetchInterval: () =>
            shouldPollSummaryCandidates ? SUMMARY_CANDIDATE_POLL_INTERVAL_MS : false
    });
    const { refetch: refetchSummaryCandidates } = summaryCandidatesQuery;

    const latestSummaryCandidate = useMemo(() => {
        if (creatingSummaryTask || (summaryTrackingTaskId && !trackedSummaryCandidateId)) {
            return undefined;
        }
        const candidates = summaryCandidatesQuery.data || [];
        return [...candidates]
            .filter(
                (candidate) =>
                    candidate.capability === "summary" &&
                    candidate.status === "PENDING" &&
                    (!trackedSummaryCandidateId ||
                        candidate.candidateId === trackedSummaryCandidateId) &&
                    typeof candidate.resultPayload === "string" &&
                    candidate.resultPayload.trim().length > 0
            )
            .sort((left, right) => {
                if (
                    left.requestedAt &&
                    right.requestedAt &&
                    left.requestedAt !== right.requestedAt
                ) {
                    return right.requestedAt.localeCompare(left.requestedAt);
                }
                return right.candidateId - left.candidateId;
            })[0];
    }, [
        creatingSummaryTask,
        summaryCandidatesQuery.data,
        summaryTrackingTaskId,
        trackedSummaryCandidateId
    ]);
    const summaryTaskAlert = useMemo(() => {
        if (creatingSummaryTask) {
            return {
                description: "任务创建成功后会自动进入状态跟踪。",
                title: "正在创建摘要任务",
                type: "info" as const
            };
        }
        if (summaryTrackingTaskId && !latestSummaryTask) {
            return {
                description: "任务已提交，正在等待任务状态返回。",
                title: "正在跟踪摘要任务",
                type: "info" as const
            };
        }
        if (latestSummaryTask) {
            const statusLabel =
                SUMMARY_TASK_STATUS_LABELS[latestSummaryTask.status] || latestSummaryTask.status;
            return {
                description: getSummaryTaskDescription(latestSummaryTask),
                title: `摘要任务${statusLabel}`,
                type: SUMMARY_TASK_ALERT_TYPES[latestSummaryTask.status] || ("info" as const)
            };
        }
        if (summaryCandidatesQuery.isFetching) {
            return {
                description: "任务完成后会自动刷新 AI 摘要。",
                title: "正在加载候选摘要",
                type: "info" as const
            };
        }
        return null;
    }, [
        creatingSummaryTask,
        latestSummaryTask,
        summaryCandidatesQuery.isFetching,
        summaryTrackingTaskId
    ]);

    useEffect(() => {
        if (!open) {
            return;
        }
        form.setFieldsValue(toWangqiDocumentFormValues(mode === "edit" ? document : null));
    }, [document, form, mode, open]);

    useEffect(() => {
        if (!isSummaryModalOpen || !latestSummaryCandidate) {
            return;
        }
        if (latestSummaryCandidate.candidateId === loadedSummaryCandidateId) {
            return;
        }
        const timer = window.setTimeout(() => {
            setLoadedSummaryCandidateId(latestSummaryCandidate.candidateId);
            setSummaryDraft(latestSummaryCandidate.resultPayload?.trim() || "");
        }, 0);
        return () => window.clearTimeout(timer);
    }, [isSummaryModalOpen, latestSummaryCandidate, loadedSummaryCandidateId]);

    useEffect(() => {
        if (!isSummaryModalOpen || !isLatestSummaryTaskCompleted) {
            return;
        }
        void refetchSummaryCandidates();
    }, [
        isSummaryModalOpen,
        isLatestSummaryTaskCompleted,
        latestSummaryTaskId,
        trackedSummaryCandidateId,
        refetchSummaryCandidates
    ]);

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

    const applySummaryDraft = () => {
        form.setFieldValue("summary", summaryDraft);
        setIsSummaryModalOpen(false);
        messageApi.success("摘要已写入基础信息");
    };

    const sectionOptions = [
        { label: "基础信息", value: "basic" },
        ...(mode === "edit"
            ? [
                  { label: "标签", value: "tags" },
                  { label: "问答", value: "qa" },
                  { label: "文件", value: "source" },
                  { label: "版本", value: "versions" }
              ]
            : [])
    ];

    const sectionContent: Record<WangqiDocumentEditDrawerSection, ReactNode> = {
        basic: <WangqiDocumentBasicSection mode={mode} onOpenSummaryModal={openSummaryModal} />,
        tags: <WangqiDocumentTagsSection content={tagContent} />,
        qa: <WangqiDocumentQaSection content={qaContent} />,
        source: <WangqiDocumentSourceSection content={sourceFileContent} />,
        versions: <WangqiDocumentVersionsSection content={versionContent} />
    };

    return (
        <KuzhambuDrawer
            testId="classics-wangqi-document-editor-drawer"
            title={mode === "create" ? "新增王圻文档" : "编辑王圻文档"}
            open={open}
            size="large"
            loading={loading}
            destroyOnHidden
            extra={
                <Segmented
                    className="wangqi-document-edit-drawer-sections"
                    options={sectionOptions}
                    value={activeSection}
                    onChange={(value) => setActiveSection(value as WangqiDocumentEditDrawerSection)}
                />
            }
            onClose={closeModel}
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
            <KuzhambuModal
                testId="classics-wangqi-document-summary-ai-modal"
                className="wangqi-summary-modal"
                title={
                    <div className="wangqi-summary-modal-title">
                        <span>AI 摘要</span>
                        <KuzhambuButton
                            testId="classics-wangqi-document-summary-ai-generate-button"
                            type="primary"
                            ariaLabel="生成 AI 摘要"
                            icon={<FileTextOutlined />}
                            loading={creatingSummaryTask}
                            onClick={requestSummaryTask}
                        >
                            生成摘要
                        </KuzhambuButton>
                    </div>
                }
                open={isSummaryModalOpen}
                width={880}
                destroyOnHidden
                footer={
                    <div className="wangqi-summary-modal-footer">
                        <KuzhambuButton
                            testId="classics-wangqi-document-summary-ai-cancel-button"
                            onClick={closeSummaryModal}
                        >
                            取消
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="classics-wangqi-document-summary-ai-apply-button"
                            type="primary"
                            disabled={!summaryDraft.trim()}
                            onClick={applySummaryDraft}
                        >
                            采用
                        </KuzhambuButton>
                    </div>
                }
                onCancel={closeSummaryModal}
            >
                {summaryTaskAlert ? (
                    <KuzhambuAlert
                        showIcon
                        type={summaryTaskAlert.type}
                        title={summaryTaskAlert.title}
                        description={summaryTaskAlert.description}
                    />
                ) : null}
                <div className="wangqi-summary-modal-compare-grid">
                    <Form className="wangqi-summary-modal-card" colon={false} layout="vertical">
                        <Form.Item label="当前摘要">
                            <TextArea
                                aria-label="AI摘要当前摘要"
                                value={form.getFieldValue("summary") || ""}
                                readOnly
                                autoSize={resolveTextAreaAutoSize({ minRows: 8, maxRows: 14 })}
                            />
                        </Form.Item>
                    </Form>
                    <Form className="wangqi-summary-modal-card" colon={false} layout="vertical">
                        <Form.Item label="AI 摘要">
                            <TextArea
                                aria-label="AI摘要候选摘要"
                                value={summaryDraft}
                                placeholder={
                                    creatingSummaryTask || summaryCandidatesQuery.isFetching
                                        ? "AI 摘要生成中..."
                                        : "暂无候选摘要，可先点击摘要生成，或手动编辑后采用"
                                }
                                autoSize={resolveTextAreaAutoSize({ minRows: 8, maxRows: 14 })}
                                onChange={(event) => setSummaryDraft(event.target.value)}
                            />
                        </Form.Item>
                        {summaryCandidatesQuery.isError && !shouldPollSummaryCandidates ? (
                            <KuzhambuAlert
                                showIcon
                                type="warning"
                                title="候选摘要加载失败"
                                description="请稍后重试加载候选摘要。"
                            />
                        ) : null}
                        {summaryCandidatesQuery.isError && shouldPollSummaryCandidates ? (
                            <KuzhambuAlert
                                showIcon
                                type="info"
                                title="候选摘要暂未返回"
                                description="AI 任务仍在跟踪中，系统会继续刷新候选摘要。"
                            />
                        ) : null}
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
            </KuzhambuModal>
            <Form<WangqiDocumentFormValues>
                form={form}
                colon={false}
                labelCol={{ flex: "88px" }}
                layout="horizontal"
                className="wangqi-document-edit-drawer-form"
            >
                <div className="wangqi-document-edit-drawer-section">
                    {sectionContent[activeSection]}
                </div>
            </Form>
        </KuzhambuDrawer>
    );
};
