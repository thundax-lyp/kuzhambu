import { Form, Input, InputNumber } from "antd";
import { useEffect, useMemo } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuForm, KuzhambuFormHiddenItem, KuzhambuFormItem } from "@/components/kuzhambu-form";
import type { TagCategoryCreateCommand, TagCategoryUpdateCommand } from "../taxonomy-service";
import type { TagCategoryRecord } from "../taxonomy-types";

const { TextArea } = Input;

interface CategoryEditDrawerProps {
    open?: boolean;
    category?: TagCategoryRecord | null;
    saving?: boolean;
    onClose: () => void;
    onCreate?: (request: TagCategoryCreateCommand) => void;
    onSave?: (request: TagCategoryUpdateCommand) => void;
}

interface CategoryEditDrawerFormValues {
    id?: string | null;
    name: string;
    description?: string | null;
    priority: number;
}

const DEFAULT_PRIORITY = 1;

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const createCategoryId = () => {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
        return crypto.randomUUID();
    }
    return `${Date.now()}`;
};

const readFormValues = (category: TagCategoryRecord): CategoryEditDrawerFormValues => ({
    id: category.id,
    name: category.name,
    description: category.description || undefined,
    priority: category.priority ?? DEFAULT_PRIORITY
});

const toCreateRequest = (values: CategoryEditDrawerFormValues): TagCategoryCreateCommand => ({
    id: values.id || createCategoryId(),
    name: values.name.trim(),
    description: normalizeSearch(values.description),
    priority: values.priority
});

const toUpdateRequest = (values: CategoryEditDrawerFormValues): TagCategoryUpdateCommand => ({
    id: values.id || "",
    name: values.name.trim(),
    description: normalizeSearch(values.description),
    priority: values.priority
});

export const CategoryEditDrawer = ({
    open,
    category,
    saving = false,
    onClose,
    onCreate,
    onSave
}: CategoryEditDrawerProps) => {
    const [form] = Form.useForm<CategoryEditDrawerFormValues>();
    const visible = Boolean(open);
    const editing = Boolean(category?.id);
    const title = editing ? "编辑标签分类" : "新增标签分类";
    const saveButtonText = editing ? "保存" : "新增";
    const defaultCreateValues = useMemo(
        () => ({
            id: createCategoryId(),
            name: "",
            description: undefined,
            priority: DEFAULT_PRIORITY
        }),
        []
    );

    useEffect(() => {
        if (!visible) {
            return;
        }
        if (category) {
            form.setFieldsValue(readFormValues(category));
            return;
        }
        form.setFieldsValue(defaultCreateValues);
    }, [category, defaultCreateValues, form, visible]);

    const saveCategory = async () => {
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
            testId="knowledge-taxonomy-category-edit-drawer"
            className="knowledge-taxonomy-category-edit-drawer"
            title={title}
            open={visible}
            size="small"
            onClose={onClose}
            footerActions={[
                {
                    testId: "knowledge-taxonomy-category-cancel-button",
                    title: "取消",
                    disabled: saving,
                    action: onClose
                },
                {
                    testId: "knowledge-taxonomy-category-action-button",
                    title: saveButtonText,
                    type: "primary",
                    loading: saving,
                    action: saveCategory
                }
            ]}
        >
            <KuzhambuForm<CategoryEditDrawerFormValues>
                form={form}
                className="taxonomy-category-form"
            >
                <KuzhambuFormHiddenItem name="id">
                    <Input />
                </KuzhambuFormHiddenItem>
                <KuzhambuFormItem
                    name="name"
                    label="分类名"
                    rules={[
                        { required: true, message: "请输入分类名" },
                        { max: 128, message: "分类名最多 128 个字符" }
                    ]}
                >
                    <Input placeholder="例如：人物" />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="description" label="描述" layoutSize="large">
                    <TextArea rows={3} maxLength={512} showCount placeholder="补充分类说明" />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    name="priority"
                    label="排序优先级"
                    rules={[{ required: true, message: "请输入优先级" }]}
                >
                    <InputNumber
                        min={1}
                        style={{ width: "100%" }}
                        precision={0}
                        placeholder="数字越小越靠前"
                    />
                </KuzhambuFormItem>
            </KuzhambuForm>
        </KuzhambuDrawer>
    );
};
