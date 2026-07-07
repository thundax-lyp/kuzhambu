import { Button, Form, Input, Select, Typography } from "antd";
import { useMemo } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type { TagBatchReviewCommand } from "../taxonomy-service";
import type { TagCategoryRecord, TagRecord } from "../taxonomy-types";

const { Text } = Typography;
const { TextArea } = Input;

interface TagBatchReviewPanelProps {
    categories: TagCategoryRecord[];
    decision: "APPROVE" | "REJECT";
    open: boolean;
    reviewing: boolean;
    selectedTags: TagRecord[];
    onClose: () => void;
    onSubmit: (request: TagBatchReviewCommand) => void;
}

interface TagBatchReviewFormValues {
    categoryId?: string | null;
    reviewNote?: string | null;
}

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readSelectedTagSummary = (tags: TagRecord[]) => {
    const names = tags.slice(0, 5).map((tag) => tag.name || tag.id);
    if (tags.length <= 5) {
        return names.join("、") || "-";
    }
    return `${names.join("、")} 等 ${tags.length} 个`;
};

export const TagBatchReviewPanel = ({
    categories,
    decision,
    open,
    reviewing,
    selectedTags,
    onClose,
    onSubmit
}: TagBatchReviewPanelProps) => {
    const [form] = Form.useForm<TagBatchReviewFormValues>();
    const approving = decision === "APPROVE";
    const title = approving ? "批量通过标签" : "批量拒绝标签";
    const submitText = approving ? "确认通过" : "确认拒绝";
    const categoryId = Form.useWatch("categoryId", form);
    const categoryOptions = useMemo(
        () =>
            categories
                .filter((category) => category.status !== "DISABLED")
                .map((category) => ({
                    label: category.name,
                    value: category.id
                })),
        [categories]
    );

    const submitReview = async () => {
        const values = await form.validateFields();
        onSubmit({
            tagIds: selectedTags.map((tag) => tag.id),
            decision,
            categoryId: approving ? normalizeText(values.categoryId) : undefined,
            reviewNote: normalizeText(values.reviewNote)
        });
    };

    return (
        <KuzhambuDrawer
            className="knowledge-taxonomy-tag-batch-review-panel"
            title={title}
            open={open}
            size="small"
            onClose={onClose}
            footer={
                <div className="knowledge-taxonomy-drawer-footer">
                    <Button disabled={reviewing} onClick={onClose}>
                        取消
                    </Button>
                    <Button
                        type="primary"
                        danger={!approving}
                        loading={reviewing}
                        disabled={approving && !categoryId}
                        onClick={submitReview}
                    >
                        {submitText}
                    </Button>
                </div>
            }
        >
            <div className="knowledge-taxonomy-tag-batch-panel">
                <KuzhambuSpace orientation="vertical" size={4}>
                    <Text strong>已选标签：{selectedTags.length} 个</Text>
                    <Text type="secondary">{readSelectedTagSummary(selectedTags)}</Text>
                </KuzhambuSpace>
                <Form<TagBatchReviewFormValues>
                    form={form}
                    layout="vertical"
                    className="knowledge-taxonomy-tag-batch-form"
                >
                    {approving ? (
                        <Form.Item
                            name="categoryId"
                            label="正式分类"
                            rules={[{ required: true, message: "请选择正式分类" }]}
                        >
                            <Select
                                aria-label="批量审核正式分类"
                                placeholder="选择通过后写入的正式分类"
                                showSearch
                                optionFilterProp="label"
                                options={categoryOptions}
                            />
                        </Form.Item>
                    ) : null}
                    <Form.Item name="reviewNote" label="审核备注">
                        <TextArea aria-label="批量审核备注" rows={5} maxLength={512} showCount />
                    </Form.Item>
                </Form>
            </div>
        </KuzhambuDrawer>
    );
};
