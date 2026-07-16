import { Form, Input, InputNumber } from "antd";
import { useEffect, useMemo } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import type { TagCategoryCreateCommand, TagCategoryUpdateCommand } from "../taxonomy-service";
import type { TagCategoryRecord } from "../taxonomy-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { TextArea } = Input;

interface CategoryEditProps {
    open?: boolean;
    category?: TagCategoryRecord | null;
    saving?: boolean;
    onClose: () => void;
    onCreate?: (request: TagCategoryCreateCommand) => void;
    onSave?: (request: TagCategoryUpdateCommand) => void;
}

interface CategoryEditFormValues {
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

const readFormValues = (category: TagCategoryRecord): CategoryEditFormValues => ({
    id: category.id,
    name: category.name,
    description: category.description || undefined,
    priority: category.priority ?? DEFAULT_PRIORITY
});

const toCreateRequest = (values: CategoryEditFormValues): TagCategoryCreateCommand => ({
    id: values.id || createCategoryId(),
    name: values.name.trim(),
    description: normalizeSearch(values.description),
    priority: values.priority
});

const toUpdateRequest = (values: CategoryEditFormValues): TagCategoryUpdateCommand => ({
    id: values.id || "",
    name: values.name.trim(),
    description: normalizeSearch(values.description),
    priority: values.priority
});

export const CategoryEdit = ({
    open,
    category,
    saving = false,
    onClose,
    onCreate,
    onSave
}: CategoryEditProps) => {
    const [form] = Form.useForm<CategoryEditFormValues>();
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
            className="knowledge-taxonomy-category-editor"
            title={title}
            open={visible}
            size="small"
            onClose={onClose}
            footer={
                <div className="knowledge-taxonomy-category-editor-footer">
                    <KuzhambuButton
                        testId="knowledge-taxonomy-category-cancel-button"
                        disabled={saving}
                        onClick={onClose}
                    >
                        取消
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-taxonomy-category-action-button"
                        type="primary"
                        loading={saving}
                        onClick={saveCategory}
                    >
                        {saveButtonText}
                    </KuzhambuButton>
                </div>
            }
        >
            <Form<CategoryEditFormValues>
                form={form}
                layout="vertical"
                className="taxonomy-category-form"
            >
                <Form.Item name="id" hidden>
                    <Input />
                </Form.Item>
                <Form.Item
                    name="name"
                    label="分类名"
                    rules={[
                        { required: true, message: "请输入分类名" },
                        { max: 128, message: "分类名最多 128 个字符" }
                    ]}
                >
                    <Input placeholder="例如：人物" />
                </Form.Item>
                <Form.Item name="description" label="描述">
                    <TextArea rows={3} maxLength={512} showCount placeholder="补充分类说明" />
                </Form.Item>
                <Form.Item
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
                </Form.Item>
            </Form>
        </KuzhambuDrawer>
    );
};
