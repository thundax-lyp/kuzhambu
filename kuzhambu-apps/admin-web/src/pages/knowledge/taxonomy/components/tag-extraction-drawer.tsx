import { Form, Input, InputNumber, Switch } from "antd";
import { useEffect, useMemo, useState } from "react";
import type { Key } from "react";
import {
    KuzhambuDrawer,
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuSpace,
    KuzhambuButton,
    KuzhambuSelect
} from "@/components";

import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";

import type { TagCandidateApplyCommand, TagExtractionCommand } from "../taxonomy-service";
import type { TagExtractionCandidateRecord, TagExtractionResultRecord } from "../taxonomy-types";
import { TagExtractionCandidateTable } from "./tag-extraction-candidate-table";

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
            <KuzhambuForm<TagExtractionFormValues>
                form={form}
                className="knowledge-taxonomy-tag-extraction-form"
                initialValues={DEFAULT_VALUES}
            >
                <KuzhambuFormItem
                    name="sourceContentType"
                    label="内容类型"
                    rules={[{ required: true }]}
                >
                    <KuzhambuSelect options={CONTENT_TYPE_OPTIONS} />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    name="sourceContentId"
                    label="内容 ID"
                    rules={[{ required: true }]}
                >
                    <Input />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="contentTitle" label="内容标题">
                    <Input />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    name="contentText"
                    label="内容片段"
                    layoutSize="large"
                    rules={[{ required: true }]}
                >
                    <TextArea rows={6} />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="modelId" label="模型 ID" rules={[{ required: true }]}>
                    <InputNumber min={1} style={{ width: "100%" }} />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="modelName" label="模型名称" rules={[{ required: true }]}>
                    <Input />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="promptVersionId" label="提示词版本 ID">
                    <InputNumber min={1} style={{ width: "100%" }} />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="maxTags" label="最大标签数">
                    <InputNumber min={1} max={50} style={{ width: "100%" }} />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    name="allowNewTags"
                    label="允许创建新标签"
                    valuePropName="checked"
                >
                    <Switch defaultChecked />
                </KuzhambuFormItem>

                {result ? (
                    <>
                        <KuzhambuFormItem colon={false} label="候选标签" layoutSize="large">
                            <TagExtractionCandidateTable
                                candidates={candidates}
                                selectedRowKeys={selectedRowKeys}
                                onSelectionChange={setSelectedRowKeys}
                            />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem name="reviewNote" label="审核备注" layoutSize="large">
                            <TextArea rows={3} />
                        </KuzhambuFormItem>
                    </>
                ) : null}
            </KuzhambuForm>
            {result ? (
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
            ) : null}
        </KuzhambuDrawer>
    );
};
