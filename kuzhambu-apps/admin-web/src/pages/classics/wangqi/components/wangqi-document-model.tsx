import {
    BlockOutlined,
    BoldOutlined,
    FileTextOutlined,
    OrderedListOutlined,
    UnorderedListOutlined
} from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Markdown } from "@tiptap/markdown";
import { EditorContent, useEditor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import { App, DatePicker, Form, Input, Segmented, Select, Switch, Typography } from "antd";
import { useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuModal } from "@/components/kuzhambu-modal";
import {
    toWangqiDocumentCommand,
    toWangqiDocumentFormValues,
    type WangqiDocumentFormValues
} from "./wangqi-document-form-values";
import * as aiCandidateService from "@/pages/classics/common/ai-candidate-service";
import type { WangqiDocumentCommand } from "../wangqi-service";
import type { WangqiDocumentRecord } from "../wangqi-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { Text } = Typography;
const { TextArea } = Input;
const SUMMARY_CANDIDATE_POLL_INTERVAL_MS = 3000;

type WangqiDocumentModelSection = "basic" | "refinement" | "tags" | "qa" | "source" | "versions";

export interface WangqiDocumentModelProps {
    document?: WangqiDocumentRecord | null;
    loading?: boolean;
    mode: "create" | "edit";
    open: boolean;
    qaContent?: ReactNode;
    refinementContent?: ReactNode;
    saving?: boolean;
    sourceFileContent?: ReactNode;
    tagContent?: ReactNode;
    versionContent?: ReactNode;
    creatingSummaryTask?: boolean;
    onCreateSummaryTask?: () => void;
    onClose: () => void;
    onSave: (command: WangqiDocumentCommand) => void;
}

interface WangqiRichTextEditorProps {
    value?: string;
    onChange?: (value: string) => void;
}

const WangqiRichTextEditor = ({ value, onChange }: WangqiRichTextEditorProps) => {
    const extensions = useMemo(() => [StarterKit, Markdown], []);
    const editor = useEditor({
        extensions,
        content: value || "",
        contentType: "markdown",
        editorProps: {
            attributes: {
                "aria-label": "王圻文档正文",
                class: "wangqi-rich-text-editor-content"
            }
        },
        immediatelyRender: false,
        onUpdate: ({ editor: currentEditor }) => {
            onChange?.(currentEditor.getMarkdown());
        }
    });

    useEffect(() => {
        if (!editor || value === editor.getMarkdown()) {
            return;
        }
        editor.commands.setContent(value || "", { contentType: "markdown" });
    }, [editor, value]);

    const runCommand = (command: () => void) => {
        command();
        editor?.commands.focus();
    };

    return (
        <div className="wangqi-rich-text-editor" aria-label="王圻 Tiptap 编辑器">
            <div className="wangqi-rich-text-editor-toolbar">
                <KuzhambuButton
                    testId="classics-wangqi-markdown-heading-button"
                    className={
                        editor?.isActive("heading", { level: 2 })
                            ? "wangqi-rich-text-editor-toolbar-button-active"
                            : undefined
                    }
                    icon={<Text>H2</Text>}
                    onClick={() =>
                        runCommand(() => editor?.chain().focus().toggleHeading({ level: 2 }).run())
                    }
                />
                <KuzhambuButton
                    testId="classics-wangqi-markdown-bold-button"
                    className={
                        editor?.isActive("bold")
                            ? "wangqi-rich-text-editor-toolbar-button-active"
                            : undefined
                    }
                    icon={<BoldOutlined />}
                    onClick={() => runCommand(() => editor?.chain().focus().toggleBold().run())}
                />
                <KuzhambuButton
                    testId="classics-wangqi-markdown-list-button"
                    className={
                        editor?.isActive("bulletList")
                            ? "wangqi-rich-text-editor-toolbar-button-active"
                            : undefined
                    }
                    icon={<UnorderedListOutlined />}
                    onClick={() =>
                        runCommand(() => editor?.chain().focus().toggleBulletList().run())
                    }
                />
                <KuzhambuButton
                    testId="classics-wangqi-markdown-ordered-list-button"
                    className={
                        editor?.isActive("orderedList")
                            ? "wangqi-rich-text-editor-toolbar-button-active"
                            : undefined
                    }
                    icon={<OrderedListOutlined />}
                    onClick={() =>
                        runCommand(() => editor?.chain().focus().toggleOrderedList().run())
                    }
                />
                <KuzhambuButton
                    testId="classics-wangqi-markdown-quote-button"
                    className={
                        editor?.isActive("blockquote")
                            ? "wangqi-rich-text-editor-toolbar-button-active"
                            : undefined
                    }
                    icon={<BlockOutlined />}
                    onClick={() =>
                        runCommand(() => editor?.chain().focus().toggleBlockquote().run())
                    }
                />
            </div>
            <EditorContent editor={editor} />
        </div>
    );
};

export const WangqiDocumentModel = ({
    document,
    loading = false,
    mode,
    open,
    qaContent,
    refinementContent,
    saving = false,
    sourceFileContent,
    tagContent,
    versionContent,
    creatingSummaryTask = false,
    onCreateSummaryTask,
    onClose,
    onSave
}: WangqiDocumentModelProps) => {
    const { message: messageApi } = App.useApp();
    const [form] = Form.useForm<WangqiDocumentFormValues>();
    const [activeSection, setActiveSection] = useState<WangqiDocumentModelSection>("basic");
    const [isSummaryModalOpen, setIsSummaryModalOpen] = useState(false);
    const [summaryDraft, setSummaryDraft] = useState("");
    const [loadedSummaryCandidateId, setLoadedSummaryCandidateId] = useState<number | null>(null);
    const documentId = mode === "edit" ? document?.id : undefined;

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
        refetchInterval: () => (creatingSummaryTask ? SUMMARY_CANDIDATE_POLL_INTERVAL_MS : false)
    });

    const latestSummaryCandidate = useMemo(() => {
        const candidates = summaryCandidatesQuery.data || [];
        return [...candidates]
            .filter(
                (candidate) =>
                    candidate.capability === "summary" &&
                    candidate.status === "PENDING" &&
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
    }, [summaryCandidatesQuery.data]);

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
                  { label: "内容处理", value: "refinement" },
                  { label: "标签", value: "tags" },
                  { label: "问答", value: "qa" },
                  { label: "文件", value: "source" },
                  { label: "版本", value: "versions" }
              ]
            : [])
    ];

    const basicContent = (
        <div className="wangqi-document-model-basic">
            <div className="wangqi-document-model-basic-head">
                <Form.Item
                    name="title"
                    label="标题"
                    rules={[{ required: true, message: "请输入标题" }]}
                >
                    <Input aria-label="王圻文档标题" maxLength={120} showCount />
                </Form.Item>
                <div className="wangqi-document-model-grid">
                    <Form.Item name="contentFormat" label="格式">
                        <Select
                            aria-label="王圻文档正文格式"
                            options={[
                                { label: "Markdown", value: "MARKDOWN" },
                                { label: "HTML", value: "HTML" }
                            ]}
                        />
                    </Form.Item>
                    <Form.Item name="documentTime" label="文档时间">
                        <DatePicker
                            aria-label="王圻文档时间"
                            picker="month"
                            format="YYYY-MM"
                            className="wangqi-document-model-date-picker"
                        />
                    </Form.Item>
                    <Form.Item name="isPublic" label="可见性" valuePropName="checked">
                        <Switch
                            aria-label="王圻文档公开状态"
                            checkedChildren="公开"
                            unCheckedChildren="私有"
                        />
                    </Form.Item>
                </div>
            </div>
            <div className="wangqi-document-model-text-fields">
                <Form.Item label="摘要" className="wangqi-document-model-form-item-top">
                    <div className="wangqi-document-summary-field">
                        {mode === "edit" ? (
                            <div className="wangqi-document-summary-field-action">
                                <KuzhambuButton
                                    testId="classics-wangqi-wangqi-summary-ai-button"
                                    type="primary"
                                    ariaLabel="AI 摘要"
                                    icon={<FileTextOutlined />}
                                    onClick={openSummaryModal}
                                >
                                    AI 摘要
                                </KuzhambuButton>
                            </div>
                        ) : null}
                        <Form.Item name="summary" noStyle>
                            <TextArea
                                aria-label="王圻文档摘要"
                                autoSize={resolveTextAreaAutoSize({ minRows: 4, maxRows: 8 })}
                                maxLength={500}
                                showCount
                            />
                        </Form.Item>
                    </div>
                </Form.Item>
                <Form.Item
                    name="content"
                    label="正文"
                    className="wangqi-document-model-form-item-top"
                >
                    <WangqiRichTextEditor />
                </Form.Item>
            </div>
        </div>
    );

    const sectionContent: Record<WangqiDocumentModelSection, ReactNode> = {
        basic: basicContent,
        refinement: refinementContent || <Text type="secondary">暂无内容处理任务</Text>,
        tags: tagContent || <Text type="secondary">暂无标签</Text>,
        qa: qaContent || <Text type="secondary">暂无问答</Text>,
        source: sourceFileContent || <Text type="secondary">暂无原始文件</Text>,
        versions: versionContent || <Text type="secondary">暂无版本</Text>
    };

    return (
        <KuzhambuDrawer
            title={mode === "create" ? "新增王圻文档" : "编辑王圻文档"}
            open={open}
            size="large"
            loading={loading}
            destroyOnHidden
            extra={
                <Segmented
                    className="wangqi-document-model-sections"
                    options={sectionOptions}
                    value={activeSection}
                    onChange={(value) => setActiveSection(value as WangqiDocumentModelSection)}
                />
            }
            onClose={closeModel}
            footer={
                <div className="wangqi-document-model-footer">
                    <KuzhambuButton
                        testId="classics-wangqi-wangqi-document-cancel-button"
                        onClick={closeModel}
                    >
                        取消
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="classics-wangqi-wangqi-document-create-button"
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
                title="AI 摘要"
                open={isSummaryModalOpen}
                width={880}
                destroyOnHidden
                footer={
                    <div className="wangqi-summary-modal-footer">
                        <KuzhambuButton
                            testId="classics-wangqi-wangqi-summary-ai-cancel-button"
                            onClick={closeSummaryModal}
                        >
                            取消
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="classics-wangqi-wangqi-summary-ai-apply-button"
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
                <div className="wangqi-summary-modal-toolbar">
                    <KuzhambuButton
                        testId="classics-wangqi-wangqi-summary-ai-create-button"
                        type="primary"
                        ariaLabel="摘要"
                        icon={<FileTextOutlined />}
                        loading={creatingSummaryTask}
                        onClick={requestSummaryTask}
                    >
                        摘要
                    </KuzhambuButton>
                </div>
                {creatingSummaryTask || summaryCandidatesQuery.isFetching ? (
                    <KuzhambuAlert
                        showIcon
                        type="info"
                        title={creatingSummaryTask ? "正在创建摘要任务" : "正在加载候选摘要"}
                        description="任务完成后会自动刷新 AI 摘要。"
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
                        {summaryCandidatesQuery.isError ? (
                            <KuzhambuAlert
                                showIcon
                                type="warning"
                                title="候选摘要加载失败"
                                description="AI 任务可能仍在执行，请稍后重新打开。"
                            />
                        ) : null}
                    </Form>
                </div>
            </KuzhambuModal>
            <Form<WangqiDocumentFormValues>
                form={form}
                colon={false}
                labelCol={{ flex: "88px" }}
                layout="horizontal"
                className="wangqi-document-model-form"
            >
                <div className="wangqi-document-model-section">{sectionContent[activeSection]}</div>
            </Form>
        </KuzhambuDrawer>
    );
};
