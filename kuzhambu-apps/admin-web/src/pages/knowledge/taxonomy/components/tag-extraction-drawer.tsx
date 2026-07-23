import { Form, Input, InputNumber, Select, Switch } from "antd";
import { useEffect, useMemo, useState } from "react";
import type { Key } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type { TagCandidateApplyCommand, TagExtractionCommand } from "../taxonomy-service";
import type { TagExtractionCandidateRecord, TagExtractionResultRecord } from "../taxonomy-types";
import { TagExtractionCandidateTable } from "./tag-extraction-candidate-table";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { TextArea } = Input;

const CONTENT_TYPE_OPTIONS = [
    { label: "SANCAI_ENTRY", value: "SANCAI_ENTRY" },
    { label: "WANGQI_DOCUMENT", value: "WANGQI_DOCUMENT" },
    { label: "MING_CUSTOMS", value: "MING_CUSTOMS" }
];

interface TagExtractionDrawerProps {
    applying?: boolean;
    extracting?: boolean;
    open?: boolean;
    result?: TagExtractionResultRecord | null;
    onApply: (request: TagCandidateApplyCommand) => void;
    onClose: () => void;
    onExtract: (request: TagExtractionCommand) => void;
    onResetResult: () => void;
}

interface TagExtractionFormValues {
    allowNewTags?: boolean;
    contentText: string;
    contentTitle?: string | null;
    maxTags?: number | null;
    modelId: number;
    modelName: string;
    promptVersionId?: number | null;
    reviewNote?: string | null;
    sourceContentId: string;
    sourceContentType: string;
}

const DEFAULT_VALUES: Partial<TagExtractionFormValues> = {
    allowNewTags: true,
    maxTags: 10,
    sourceContentType: "SANCAI_ENTRY"
};

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readCandidateKey = (candidate: TagExtractionCandidateRecord, index: number) => {
    return `${candidate.name || "tag"}-${candidate.matchedExistingTagId || "new"}-${index}`;
};

export const TagExtractionDrawer = ({
    applying = false,
    extracting = false,
    open,
    result,
    onApply,
    onClose,
    onExtract,
    onResetResult
}: TagExtractionDrawerProps) => {
    const confirm = useKuzhambuConfirm();
    const [form] = Form.useForm<TagExtractionFormValues>();
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const visible = Boolean(open);
    const candidates = useMemo(() => result?.candidates || [], [result?.candidates]);
    const selectedTags = useMemo(
        () =>
            selectedRowKeys
                .map((key) =>
                    candidates.find(
                        (candidate, index) => readCandidateKey(candidate, index) === key
                    )
                )
                .filter(Boolean) as TagExtractionCandidateRecord[],
        [candidates, selectedRowKeys]
    );

    useEffect(() => {
        if (!visible) {
            return;
        }
        form.setFieldsValue(DEFAULT_VALUES);
    }, [form, visible]);

    const closeDrawer = () => {
        if (extracting || applying) {
            return;
        }
        onClose();
    };

    const extractTags = async () => {
        const values = await form.validateFields();
        onExtract({
            sourceContentType: values.sourceContentType,
            sourceContentId: values.sourceContentId.trim(),
            contentTitle: normalizeText(values.contentTitle),
            contentText: values.contentText.trim(),
            modelId: values.modelId,
            modelName: values.modelName.trim(),
            promptVersionId: values.promptVersionId ?? undefined,
            maxTags: values.maxTags ?? 10,
            allowNewTags: values.allowNewTags ?? true
        });
    };

    const reExtractTags = () => {
        onResetResult();
        setSelectedRowKeys([]);
    };

    const applySelectedTags = async () => {
        if (!result?.aiCandidateId || selectedTags.length === 0) {
            return;
        }
        const values = form.getFieldsValue();
        confirm.danger({
            title: "应用 AI 标签候选",
            message: "将把选中候选进入标签审核治理",
            okText: "应用",
            onConfirm: () =>
                onApply({
                    aiCandidateId: result.aiCandidateId || 0,
                    selectedTags,
                    reviewNote: normalizeText(values.reviewNote)
                })
        });
    };

    return (
        <KuzhambuDrawer
            testId="knowledge-taxonomy-tag-extraction-drawer"
            className="knowledge-taxonomy-tag-extraction-drawer"
            title="AI 抽取标签"
            open={visible}
            size="middle"
            onClose={closeDrawer}
            footerActions={[
                {
                    testId: "knowledge-taxonomy-tag-extraction-cancel-button",
                    title: "取消",
                    disabled: extracting || applying,
                    action: closeDrawer
                },
                {
                    testId: "knowledge-taxonomy-tag-extraction-action-button",
                    title: "开始抽取",
                    type: "primary",
                    loading: extracting,
                    action: extractTags
                }
            ]}
        >
            <Form<TagExtractionFormValues>
                form={form}
                layout="vertical"
                className="knowledge-taxonomy-tag-extraction-form"
                initialValues={DEFAULT_VALUES}
            >
                <Form.Item name="sourceContentType" label="内容类型" rules={[{ required: true }]}>
                    <Select options={CONTENT_TYPE_OPTIONS} />
                </Form.Item>
                <Form.Item name="sourceContentId" label="内容 ID" rules={[{ required: true }]}>
                    <Input />
                </Form.Item>
                <Form.Item name="contentTitle" label="内容标题">
                    <Input />
                </Form.Item>
                <Form.Item name="contentText" label="内容片段" rules={[{ required: true }]}>
                    <TextArea rows={6} />
                </Form.Item>
                <div className="knowledge-taxonomy-tag-extraction-model-grid">
                    <Form.Item name="modelId" label="模型 ID" rules={[{ required: true }]}>
                        <InputNumber min={1} />
                    </Form.Item>
                    <Form.Item name="modelName" label="模型名称" rules={[{ required: true }]}>
                        <Input />
                    </Form.Item>
                    <Form.Item name="promptVersionId" label="提示词版本 ID">
                        <InputNumber min={1} />
                    </Form.Item>
                    <Form.Item name="maxTags" label="最大标签数">
                        <InputNumber min={1} max={50} />
                    </Form.Item>
                </div>
                <Form.Item name="allowNewTags" label="允许创建新标签" valuePropName="checked">
                    <Switch defaultChecked />
                </Form.Item>

                {result ? (
                    <div className="knowledge-taxonomy-tag-extraction-result">
                        <TagExtractionCandidateTable
                            candidates={candidates}
                            selectedRowKeys={selectedRowKeys}
                            onSelectionChange={setSelectedRowKeys}
                        />
                        <Form.Item name="reviewNote" label="审核备注">
                            <TextArea rows={3} />
                        </Form.Item>
                        <KuzhambuSpace wrap>
                            <KuzhambuButton
                                testId="knowledge-taxonomy-tag-extraction-action-button-2"
                                type="primary"
                                disabled={!result.aiCandidateId || selectedTags.length === 0}
                                loading={applying}
                                onClick={applySelectedTags}
                            >
                                应用选中标签
                            </KuzhambuButton>
                            <KuzhambuButton
                                testId="knowledge-taxonomy-tag-extraction-action-button-3"
                                disabled={extracting || applying}
                                onClick={reExtractTags}
                            >
                                重新抽取
                            </KuzhambuButton>
                        </KuzhambuSpace>
                    </div>
                ) : null}
            </Form>
        </KuzhambuDrawer>
    );
};
