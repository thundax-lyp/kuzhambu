import { FileTextOutlined } from "@ant-design/icons";
import { App, Form, Input } from "antd";
import type { ReactNode } from "react";
import { useEffect, useMemo, useState } from "react";
import {
    KuzhambuForm,
    KuzhambuFormHiddenItem,
    KuzhambuFormItem,
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuMarkdownEditor,
    KuzhambuSegmentedDrawer,
    KuzhambuSelect,
    KuzhambuSyncTaskModal,
    type KuzhambuSyncTaskAdapter,
    type KuzhambuSyncTaskModalState
} from "@/components";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import * as aiCandidateService from "@/pages/classics/common/ai-candidate-service";
import type { AiCandidateRecord } from "@/pages/classics/common/ai-candidate-types";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import {
    AI_BUSINESS_CAPABILITY,
    type AiRefinementTaskRecord
} from "@/pages/classics/common/ai-refinement-task-types";
import { isSameId, normalizeNullableId } from "@/types/id";
import type { MingCustomsCommand } from "@/pages/classics/ming-custom/ming-custom-service";
import type { MingCustomsRecord } from "@/pages/classics/ming-custom/ming-custom-types";
import type { DictItem } from "@/types/dict";
import { ClassicsSummaryFormControl } from "@/pages/classics/common/classics-summary-form-control";
import "./ming-customs-edit-drawer.css";

type MingCustomsEditDrawerSection = "basic" | "tags" | "qa" | "versions";
const { TextArea } = Input;
const SUMMARY_CANDIDATE_POLL_INTERVAL_MS = 3000;
const REJECT_ERROR_TYPE = "USER_REJECTED";
const REJECT_ERROR_MESSAGE = "用户已拒绝该 AI 候选";

const isSummaryTaskActive = (task?: AiRefinementTaskRecord | null) =>
    task?.status === "PENDING" || task?.status === "RUNNING";

const getSummaryTaskCandidateId = (task?: AiRefinementTaskRecord | null) =>
    normalizeNullableId(task?.candidateIdText ?? task?.candidateId);

const getSummaryCandidateStableId = (candidate?: AiCandidateRecord | null) =>
    normalizeNullableId(candidate?.candidateIdText ?? candidate?.candidateId);

const summaryTaskAdapter: KuzhambuSyncTaskAdapter<AiRefinementTaskRecord> = {
    getId: (task) => aiRefinementTaskService.getTaskStableId(task.taskId, task.taskIdText),
    getMessage: (task) => task.errorMessage || task.resultPreview || "摘要任务处理中",
    getPhase: (task) => {
        if (isSummaryTaskActive(task)) return "tracking";
        if (task.status === "SUCCEEDED" || task.status === "PARTIAL") {
            return getSummaryTaskCandidateId(task) ? "result_ready" : "waiting_result";
        }
        if (task.status === "FAILED") return "failed";
        if (task.status === "CANCELLED") return "cancelled";
        return "tracking";
    },
    getResultKey: (task) => getSummaryTaskCandidateId(task) ?? "",
    getStatusLabel: (task) => `摘要任务${task.status}`
};

const selectLatestSummaryCandidate = (
    candidates: AiCandidateRecord[],
    trackedCandidateId?: string | null
) => {
    const normalizedTrackedCandidateId = normalizeNullableId(trackedCandidateId);
    return [...candidates]
        .filter(
            (candidate) =>
                aiRefinementTaskService.getNormalizedTaskCapability(candidate.capability) ===
                    "summary" &&
                candidate.status === "PENDING" &&
                (!normalizedTrackedCandidateId ||
                    isSameId(
                        candidate.candidateIdText ?? candidate.candidateId,
                        normalizedTrackedCandidateId
                    )) &&
                Boolean(candidate.resultPayload?.trim())
        )
        .sort((left, right) =>
            aiRefinementTaskService.sortNewestByRequestedAtThenId({
                left: {
                    id: left.candidateIdText || left.candidateId,
                    requestedAt: left.requestedAt
                },
                right: {
                    id: right.candidateIdText || right.candidateId,
                    requestedAt: right.requestedAt
                }
            })
        )[0];
};

const renderSummaryTaskStatus = ({
    creating,
    resultError,
    resultLoading,
    task,
    tracking
}: KuzhambuSyncTaskModalState<AiRefinementTaskRecord, AiCandidateRecord>) => {
    if (creating || resultLoading) {
        return <KuzhambuAlert showIcon type="info" title="正在生成候选摘要" />;
    }
    if (resultError) {
        return (
            <KuzhambuAlert
                showIcon
                type={tracking ? "info" : "warning"}
                title={tracking ? "候选摘要暂未返回" : "候选摘要加载失败"}
            />
        );
    }
    if (task) {
        const isFailed = task.status === "FAILED";
        return (
            <KuzhambuAlert
                showIcon
                type={isFailed ? "error" : isSummaryTaskActive(task) ? "info" : "success"}
                title={`摘要任务${task.status}`}
                description={task.errorMessage || task.resultPreview || "摘要任务处理中"}
            />
        );
    }
    return null;
};

interface MingCustomsFormValues {
    category: string;
    chapter: string;
    content: string;
    contentFormat: string;
    originalExcerpts: string;
    section: string;
    summary: string;
    title: string;
}

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const toMingCustomsFormValues = (record?: MingCustomsRecord | null): MingCustomsFormValues => ({
    category: record?.category || "",
    chapter: record?.chapter || "",
    content: record?.content || "",
    contentFormat: record?.contentFormat || "MARKDOWN",
    originalExcerpts: record?.originalExcerpts || "",
    section: record?.section || "",
    summary: record?.summary || "",
    title: record?.title || ""
});

const toMingCustomsCommand = (
    values: MingCustomsFormValues,
    record?: MingCustomsRecord | null
): MingCustomsCommand => ({
    id: record?.id,
    title: normalizeText(values.title),
    category: normalizeText(values.category),
    chapter: normalizeText(values.chapter),
    section: normalizeText(values.section),
    summary: normalizeText(values.summary),
    contentFormat: normalizeText(values.contentFormat) || "MARKDOWN",
    content: normalizeText(values.content),
    originalExcerpts: normalizeText(values.originalExcerpts)
});

interface MingCustomsEditDrawerProps {
    categoryOptions: DictItem[];
    entry?: MingCustomsRecord | null;
    loading?: boolean;
    mode: "create" | "edit";
    open: boolean;
    qaContent?: ReactNode;
    saving?: boolean;
    summaryCreating?: boolean;
    summaryTasks?: AiRefinementTaskRecord[];
    summaryTrackingTask?: AiRefinementTaskRecord | null;
    tagContent?: ReactNode;
    versionContent?: ReactNode;
    canRejectSummaryCandidate?: boolean;
    onChanged?: () => void | Promise<void>;
    onClose: () => void;
    onCreateSummaryTask?: () => void;
    onSummaryTaskChange?: (task: AiRefinementTaskRecord | null) => void;
    onSave: (command: MingCustomsCommand) => void;
}

export const MingCustomsEditDrawer = ({
    categoryOptions,
    entry,
    loading = false,
    mode,
    open,
    qaContent,
    saving = false,
    summaryCreating = false,
    summaryTasks = [],
    summaryTrackingTask,
    tagContent,
    versionContent,
    canRejectSummaryCandidate = false,
    onChanged,
    onClose,
    onCreateSummaryTask,
    onSummaryTaskChange,
    onSave
}: MingCustomsEditDrawerProps) => {
    const { message: messageApi } = App.useApp();
    const [form] = Form.useForm<MingCustomsFormValues>();
    const contentFormat = Form.useWatch("contentFormat", { form, preserve: true }) || "MARKDOWN";
    const [activeSection, setActiveSection] = useState<MingCustomsEditDrawerSection>("basic");
    const [summaryModalOpen, setSummaryModalOpen] = useState(false);
    const [summaryApplying, setSummaryApplying] = useState(false);
    const [summaryRejecting, setSummaryRejecting] = useState(false);
    const [summaryDraft, setSummaryDraft] = useState("");
    const [loadedSummaryCandidateId, setLoadedSummaryCandidateId] = useState<string | null>(null);
    const entryId = mode === "edit" ? entry?.id : undefined;
    const latestSummaryTaskFromList = useMemo(
        () =>
            [...summaryTasks]
                .filter(
                    (task) =>
                        aiRefinementTaskService.getNormalizedTaskCapability(task.capability) ===
                        "summary"
                )
                .sort((left, right) =>
                    aiRefinementTaskService.sortNewestByRequestedAtThenId({
                        left: { id: left.taskIdText || left.taskId, requestedAt: left.requestedAt },
                        right: {
                            id: right.taskIdText || right.taskId,
                            requestedAt: right.requestedAt
                        }
                    })
                )[0],
        [summaryTasks]
    );
    const trackedSummaryTask = summaryTasks.find((task) =>
        isSameId(task.taskId, summaryTrackingTask?.taskId)
    );
    const latestSummaryTask =
        trackedSummaryTask || summaryTrackingTask || latestSummaryTaskFromList || null;
    const summaryTrackingTaskId = summaryTrackingTask?.taskId;
    const summaryLocked =
        summaryCreating ||
        isSummaryTaskActive(latestSummaryTask) ||
        (summaryModalOpen && Boolean(summaryTrackingTaskId));

    useEffect(() => {
        if (!open) {
            return;
        }
        form.setFieldsValue(toMingCustomsFormValues(mode === "edit" ? entry : null));
    }, [entry, form, mode, open]);

    const saveEntry = async () => {
        const values = await form.validateFields();
        onSave(toMingCustomsCommand(values, mode === "edit" ? entry : null));
    };

    const closeDrawer = () => {
        setActiveSection("basic");
        setSummaryModalOpen(false);
        onClose();
    };

    const openSummaryModal = () => {
        setSummaryDraft(form.getFieldValue("summary") || "");
        setLoadedSummaryCandidateId(null);
        setSummaryModalOpen(true);
    };

    const loadSummaryCandidate = async (task: AiRefinementTaskRecord | null) => {
        if (!entryId) return null;
        const trackedCandidateId = getSummaryTaskCandidateId(task);
        if (summaryTrackingTaskId && !trackedCandidateId) return null;
        const candidates = await aiCandidateService.list({
            contentId: entryId,
            contentType: "MING_CUSTOMS",
            capability: AI_BUSINESS_CAPABILITY.CLASSICS_SUMMARY,
            status: "PENDING"
        });
        return selectLatestSummaryCandidate(candidates, trackedCandidateId) ?? null;
    };

    const updateSummaryDraft = (candidate: AiCandidateRecord | null) => {
        const candidateId = getSummaryCandidateStableId(candidate);
        if (!candidate || candidateId === loadedSummaryCandidateId) return;
        setLoadedSummaryCandidateId(candidateId);
        setSummaryDraft(candidate.resultPayload?.trim() || "");
    };

    const applySummaryDraft = async (candidate: AiCandidateRecord | null) => {
        if (!entryId || !candidate) return;
        setSummaryApplying(true);
        try {
            await aiCandidateService.apply({
                candidateId: candidate.candidateIdText || candidate.candidateId,
                contentId: entryId,
                contentType: "MING_CUSTOMS",
                capability: AI_BUSINESS_CAPABILITY.CLASSICS_SUMMARY,
                objectId: candidate.objectId,
                resultFormat: candidate.resultFormat?.trim() || "TEXT",
                resultPayload: summaryDraft,
                changeSummary: "AI 应用：摘要"
            });
            form.setFieldValue("summary", summaryDraft);
            await onChanged?.();
            setSummaryModalOpen(false);
            messageApi.success("摘要已写入基础信息");
        } catch (error) {
            messageApi.error(error instanceof Error ? error.message : "AI 候选应用失败");
        } finally {
            setSummaryApplying(false);
        }
    };

    const rejectSummaryCandidate = async (candidate: AiCandidateRecord | null) => {
        if (!candidate) {
            messageApi.warning("暂无可拒绝的候选摘要");
            return;
        }
        setSummaryRejecting(true);
        try {
            await aiCandidateService.reject({
                candidateId: candidate.candidateIdText || candidate.candidateId,
                errorType: REJECT_ERROR_TYPE,
                errorMessage: REJECT_ERROR_MESSAGE
            });
            await onChanged?.();
            setSummaryModalOpen(false);
            messageApi.success("候选摘要已拒绝");
        } catch (error) {
            messageApi.error(error instanceof Error ? error.message : "拒绝候选摘要失败");
        } finally {
            setSummaryRejecting(false);
        }
    };

    const basicContent = (
        <KuzhambuForm<MingCustomsFormValues> form={form} className="ming-customs-edit-drawer-form">
            <KuzhambuFormHiddenItem name="contentFormat" />
            <KuzhambuFormItem
                name="title"
                label="稿件"
                rules={[{ required: true, message: "请输入稿件标题" }]}
            >
                <Input aria-label="明代习俗稿件标题" maxLength={100} showCount />
            </KuzhambuFormItem>
            <KuzhambuFormItem
                name="category"
                label="分类"
                rules={[{ required: true, message: "请选择分类" }]}
            >
                <KuzhambuSelect
                    aria-label="明代习俗编辑分类"
                    options={categoryOptions.map((option) => ({
                        label: option.label,
                        value: option.value
                    }))}
                    placeholder="选择分类"
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem name="chapter" label="章节">
                <Input aria-label="明代习俗章节" maxLength={100} showCount />
            </KuzhambuFormItem>
            <KuzhambuFormItem name="section" label="小节">
                <Input aria-label="明代习俗小节" maxLength={100} showCount />
            </KuzhambuFormItem>
            <KuzhambuFormItem name="summary" label="概述" layoutSize="large">
                <ClassicsSummaryFormControl
                    aiButtonTestId="classics-ming-customs-summary-ai-button"
                    ariaLabel="明代习俗概述"
                    disabled={summaryLocked}
                    maxLength={500}
                    mode={mode}
                    showCount
                    onOpenAiSummary={openSummaryModal}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem name="content" label="正文" layoutSize="large">
                {contentFormat === "MARKDOWN" ? (
                    <KuzhambuMarkdownEditor
                        ariaLabel="明代习俗正文"
                        minHeight={360}
                        testIdPrefix="classics-ming-customs-content-markdown"
                    />
                ) : (
                    <TextArea
                        aria-label={`明代习俗正文（${contentFormat}）`}
                        autoSize={resolveTextAreaAutoSize({ minRows: 12, maxRows: 20 })}
                    />
                )}
            </KuzhambuFormItem>
            <KuzhambuFormItem name="originalExcerpts" label="原文摘录" layoutSize="large">
                <KuzhambuMarkdownEditor
                    ariaLabel="明代习俗原文摘录"
                    minHeight={240}
                    testIdPrefix="classics-ming-customs-excerpts-markdown"
                />
            </KuzhambuFormItem>
        </KuzhambuForm>
    );

    const sections = [
        { label: "基础信息", value: "basic", content: basicContent },
        { label: "标签", value: "tags", content: tagContent, visible: mode === "edit" },
        { label: "问答", value: "qa", content: qaContent, visible: mode === "edit" },
        { label: "版本", value: "versions", content: versionContent, visible: mode === "edit" }
    ] satisfies Array<{
        content: ReactNode;
        label: string;
        value: MingCustomsEditDrawerSection;
        visible?: boolean;
    }>;

    return (
        <KuzhambuSegmentedDrawer
            activeSection={activeSection}
            sections={sections}
            sectionClassName="ming-customs-edit-drawer-section"
            segmentedClassName="ming-customs-edit-drawer-sections"
            showSegmented={mode === "edit"}
            testId="classics-ming-customs-ming-customs-editor-drawer"
            title={mode === "create" ? "新增明代习俗" : "编辑明代习俗"}
            open={open}
            size="large"
            loading={loading}
            destroyOnHidden
            onClose={closeDrawer}
            onSectionChange={setActiveSection}
            footerActions={[
                {
                    testId: "classics-ming-customs-ming-customs-cancel-button",
                    title: "取消",
                    action: closeDrawer
                },
                {
                    testId: "classics-ming-customs-ming-customs-create-button",
                    title: "保存",
                    type: "primary",
                    loading: saving,
                    action: saveEntry
                }
            ]}
        >
            <KuzhambuSyncTaskModal<AiRefinementTaskRecord, AiCandidateRecord>
                testId="classics-ming-customs-summary-ai-modal"
                className="ming-customs-summary-modal"
                title="AI 摘要"
                open={summaryModalOpen}
                width={880}
                applyDisabled={({ creating, resultLoading, tracking }) =>
                    creating ||
                    resultLoading ||
                    tracking ||
                    summaryRejecting ||
                    !summaryDraft.trim()
                }
                applyTestId="classics-ming-customs-summary-ai-apply-button"
                createAriaLabel="生成明代习俗 AI 摘要"
                createIcon={<FileTextOutlined />}
                createTestId="classics-ming-customs-summary-ai-generate-button"
                createText="生成摘要"
                creating={summaryCreating}
                applying={summaryApplying}
                onCancel={() => setSummaryModalOpen(false)}
                workflow={{
                    ...summaryTaskAdapter,
                    task: latestSummaryTask,
                    createTask: () => onCreateSummaryTask?.(),
                    fetchResult: loadSummaryCandidate,
                    fetchTask: (taskId) => aiRefinementTaskService.getTask({ taskId }),
                    applyResult: applySummaryDraft,
                    onTaskChange: onSummaryTaskChange,
                    onResultChange: updateSummaryDraft,
                    pollIntervalMs: SUMMARY_CANDIDATE_POLL_INTERVAL_MS,
                    resultQueryKey: ["MING_CUSTOMS", entryId, "summary"],
                    trackTask: Boolean(summaryTrackingTaskId)
                }}
                renderStatus={renderSummaryTaskStatus}
                renderFooterActions={({ creating, result, resultLoading, tracking }) => (
                    <KuzhambuButton
                        testId="classics-ming-customs-summary-ai-reject-button"
                        disabled={
                            !canRejectSummaryCandidate ||
                            !result ||
                            creating ||
                            resultLoading ||
                            tracking ||
                            summaryApplying ||
                            summaryRejecting
                        }
                        loading={summaryRejecting}
                        onClick={() => void rejectSummaryCandidate(result)}
                    >
                        拒绝
                    </KuzhambuButton>
                )}
                renderBody={({ creating, resultLoading, tracking }) => (
                    <div className="ming-customs-summary-modal-grid">
                        <label>
                            当前摘要
                            <TextArea
                                aria-label="明代习俗当前摘要"
                                value={form.getFieldValue("summary") || ""}
                                readOnly
                                autoSize={resolveTextAreaAutoSize({ minRows: 8, maxRows: 14 })}
                            />
                        </label>
                        <label>
                            AI 摘要
                            <TextArea
                                aria-label="明代习俗候选摘要"
                                value={summaryDraft}
                                disabled={creating || resultLoading || tracking}
                                autoSize={resolveTextAreaAutoSize({ minRows: 8, maxRows: 14 })}
                                onChange={(event) => setSummaryDraft(event.target.value)}
                            />
                        </label>
                    </div>
                )}
            />
        </KuzhambuSegmentedDrawer>
    );
};
