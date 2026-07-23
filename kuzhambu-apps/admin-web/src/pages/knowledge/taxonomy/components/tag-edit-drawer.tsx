import { Form, Input, Select } from "antd";
import { useEffect, useMemo } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuForm, KuzhambuFormHiddenItem, KuzhambuFormItem } from "@/components/kuzhambu-form";
import type { TagCreateCommand, TagUpdateCommand } from "../taxonomy-service";
import type { TagCategoryRecord, TagRecord } from "../taxonomy-types";

const { TextArea } = Input;

interface TagEditDrawerProps {
    categories: TagCategoryRecord[];
    open?: boolean;
    saving?: boolean;
    tag?: TagRecord | null;
    onClose: () => void;
    onCreate?: (request: TagCreateCommand) => void;
    onSave?: (request: TagUpdateCommand) => void;
}

interface TagEditDrawerFormValues {
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

const toFormValues = (tag: TagRecord): TagEditDrawerFormValues => ({
    categoryId: tag.categoryId || undefined,
    description: tag.description || undefined,
    id: tag.id,
    name: tag.name
});

const toCreateRequest = (values: TagEditDrawerFormValues): TagCreateCommand => ({
    id: values.id || createTagId(),
    name: values.name.trim(),
    categoryId: normalizeText(values.categoryId),
    description: normalizeText(values.description)
});

const toUpdateRequest = (values: TagEditDrawerFormValues): TagUpdateCommand => ({
    id: values.id || "",
    name: values.name.trim(),
    categoryId: normalizeText(values.categoryId),
    description: normalizeText(values.description)
});

export const TagEditDrawer = ({
    categories,
    open,
    saving = false,
    tag,
    onClose,
    onCreate,
    onSave
}: TagEditDrawerProps) => {
    const [form] = Form.useForm<TagEditDrawerFormValues>();
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
            testId="knowledge-taxonomy-tag-edit-drawer"
            className="knowledge-taxonomy-tag-edit-drawer"
            title={title}
            open={visible}
            size="small"
            onClose={onClose}
            footerActions={[
                {
                    testId: "knowledge-taxonomy-tag-cancel-button",
                    title: "取消",
                    disabled: saving,
                    action: onClose
                },
                {
                    testId: "knowledge-taxonomy-tag-action-button",
                    title: saveButtonText,
                    type: "primary",
                    loading: saving,
                    action: saveTag
                }
            ]}
        >
            <KuzhambuForm<TagEditDrawerFormValues> form={form} className="taxonomy-tag-form">
                <KuzhambuFormHiddenItem name="id">
                    <Input />
                </KuzhambuFormHiddenItem>
                <KuzhambuFormItem
                    name="name"
                    label="标签名"
                    rules={[
                        { required: true, message: "请输入标签名" },
                        { max: 128, message: "标签名最多 128 个字符" }
                    ]}
                >
                    <Input placeholder="例如：王圻" />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="categoryId" label="分类">
                    <Select
                        allowClear
                        showSearch
                        optionFilterProp="label"
                        placeholder="选择标签分类"
                        options={categoryOptions}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="description" label="描述" layoutSize="large">
                    <TextArea rows={4} maxLength={1024} showCount placeholder="补充标签说明" />
                </KuzhambuFormItem>
            </KuzhambuForm>
        </KuzhambuDrawer>
    );
};
