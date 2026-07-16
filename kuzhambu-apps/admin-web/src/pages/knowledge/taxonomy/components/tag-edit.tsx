import { Form, Input, Select } from "antd";
import { useEffect, useMemo } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import type { TagCreateCommand, TagUpdateCommand } from "../taxonomy-service";
import type { TagCategoryRecord, TagRecord } from "../taxonomy-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { TextArea } = Input;

interface TagEditProps {
    categories: TagCategoryRecord[];
    open?: boolean;
    saving?: boolean;
    tag?: TagRecord | null;
    onClose: () => void;
    onCreate?: (request: TagCreateCommand) => void;
    onSave?: (request: TagUpdateCommand) => void;
}

interface TagEditFormValues {
    categoryId?: string | null;
    description?: string | null;
    id?: string | null;
    name: string;
}

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const createTagId = () => {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
        return crypto.randomUUID();
    }
    return `${Date.now()}`;
};

const toFormValues = (tag: TagRecord): TagEditFormValues => ({
    categoryId: tag.categoryId || undefined,
    description: tag.description || undefined,
    id: tag.id,
    name: tag.name
});

const toCreateRequest = (values: TagEditFormValues): TagCreateCommand => ({
    id: values.id || createTagId(),
    name: values.name.trim(),
    categoryId: normalizeText(values.categoryId),
    description: normalizeText(values.description)
});

const toUpdateRequest = (values: TagEditFormValues): TagUpdateCommand => ({
    id: values.id || "",
    name: values.name.trim(),
    categoryId: normalizeText(values.categoryId),
    description: normalizeText(values.description)
});

export const TagEdit = ({
    categories,
    open,
    saving = false,
    tag,
    onClose,
    onCreate,
    onSave
}: TagEditProps) => {
    const [form] = Form.useForm<TagEditFormValues>();
    const visible = Boolean(open);
    const editing = Boolean(tag?.id);
    const title = editing ? "编辑统一标签" : "新增统一标签";
    const saveButtonText = editing ? "保存" : "新增";
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
    const defaultCreateValues = useMemo(
        () => ({
            categoryId: undefined,
            description: undefined,
            id: createTagId(),
            name: ""
        }),
        []
    );

    useEffect(() => {
        if (!visible) {
            return;
        }
        if (tag) {
            form.setFieldsValue(toFormValues(tag));
            return;
        }
        form.setFieldsValue(defaultCreateValues);
    }, [defaultCreateValues, form, tag, visible]);

    const saveTag = async () => {
        const values = await form.validateFields();
        if (editing && onSave) {
            onSave(toUpdateRequest(values));
            return;
        }
        if (!editing && onCreate) {
            onCreate(toCreateRequest(values));
        }
    };

    return (
        <KuzhambuDrawer
            className="knowledge-taxonomy-tag-editor"
            title={title}
            open={visible}
            size="small"
            onClose={onClose}
            footer={
                <div className="knowledge-taxonomy-tag-editor-footer">
                    <KuzhambuButton
                        testId="knowledge-taxonomy-tag-cancel-button"
                        disabled={saving}
                        onClick={onClose}
                    >
                        取消
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-taxonomy-tag-action-button"
                        type="primary"
                        loading={saving}
                        onClick={saveTag}
                    >
                        {saveButtonText}
                    </KuzhambuButton>
                </div>
            }
        >
            <Form<TagEditFormValues> form={form} layout="vertical" className="taxonomy-tag-form">
                <Form.Item name="id" hidden>
                    <Input />
                </Form.Item>
                <Form.Item
                    name="name"
                    label="标签名"
                    rules={[
                        { required: true, message: "请输入标签名" },
                        { max: 128, message: "标签名最多 128 个字符" }
                    ]}
                >
                    <Input placeholder="例如：王圻" />
                </Form.Item>
                <Form.Item name="categoryId" label="分类">
                    <Select
                        allowClear
                        showSearch
                        optionFilterProp="label"
                        placeholder="选择标签分类"
                        options={categoryOptions}
                    />
                </Form.Item>
                <Form.Item name="description" label="描述">
                    <TextArea rows={4} maxLength={1024} showCount placeholder="补充标签说明" />
                </Form.Item>
            </Form>
        </KuzhambuDrawer>
    );
};
