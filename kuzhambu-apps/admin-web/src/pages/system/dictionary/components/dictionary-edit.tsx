import { Form, Input } from "antd";
import { useEffect } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import type { DictSaveCommand } from "../dictionary-service";
import type { DictRecord } from "../dictionary-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { TextArea } = Input;

interface DictionaryEditProps {
    open?: boolean;
    dictionary?: DictRecord | null;
    saving?: boolean;
    onClose: () => void;
    onSave: (request: DictSaveCommand) => void;
}

interface DictFormValues {
    id?: string | null;
    type: string;
    label: string;
    value: string;
    remarks?: string | null;
}

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readFormRequest = (values: DictFormValues): DictSaveCommand => {
    return {
        id: values.id,
        type: values.type.trim(),
        label: values.label.trim(),
        value: values.value.trim(),
        remarks: normalizeSearch(values.remarks)
    };
};

const toFormValues = (dictionary: DictRecord): DictFormValues => {
    return {
        id: dictionary.id,
        type: dictionary.type,
        label: dictionary.label,
        value: dictionary.value,
        remarks: dictionary.remarks
    };
};

export const DictionaryEdit = ({
    open,
    dictionary,
    saving,
    onClose,
    onSave
}: DictionaryEditProps) => {
    const [form] = Form.useForm<DictFormValues>();

    useEffect(() => {
        if (!open) {
            return;
        }
        if (dictionary) {
            form.setFieldsValue(toFormValues(dictionary));
            return;
        }
        form.resetFields();
    }, [dictionary, form, open]);

    const saveDictionary = async () => {
        const values = await form.validateFields();
        onSave(readFormRequest(values));
    };

    return (
        <KuzhambuDrawer
            className="dictionary-edit-drawer"
            title={dictionary ? "编辑字典项" : "新增字典项"}
            open={Boolean(open)}
            size="small"
            onClose={onClose}
            footer={
                <div className="dictionary-edit-footer">
                    <KuzhambuButton name="取消" onClick={onClose}>
                        取消
                    </KuzhambuButton>
                    <KuzhambuButton
                        name="保存"
                        type="primary"
                        loading={saving}
                        onClick={saveDictionary}
                    >
                        保存
                    </KuzhambuButton>
                </div>
            }
        >
            <Form<DictFormValues> form={form} layout="vertical" className="dictionary-editor-form">
                <Form.Item name="id" hidden>
                    <Input />
                </Form.Item>
                <Form.Item
                    name="type"
                    label="字典类型"
                    rules={[{ required: true, message: "请输入字典类型" }]}
                >
                    <Input placeholder="例如：user_status" />
                </Form.Item>
                <Form.Item
                    name="label"
                    label="标签"
                    rules={[{ required: true, message: "请输入标签" }]}
                >
                    <Input placeholder="例如：启用" />
                </Form.Item>
                <Form.Item
                    name="value"
                    label="值"
                    rules={[{ required: true, message: "请输入值" }]}
                >
                    <Input placeholder="例如：ENABLED" />
                </Form.Item>
                <Form.Item name="remarks" label="备注">
                    <TextArea rows={3} maxLength={200} showCount placeholder="补充使用说明" />
                </Form.Item>
            </Form>
        </KuzhambuDrawer>
    );
};
