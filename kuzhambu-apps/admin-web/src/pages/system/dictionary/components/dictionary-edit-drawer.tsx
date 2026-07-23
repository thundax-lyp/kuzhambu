import { Form, Input } from "antd";
import { useEffect } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuForm, KuzhambuFormHiddenItem, KuzhambuFormItem } from "@/components/kuzhambu-form";
import type { DictSaveCommand } from "../dictionary-service";
import type { DictRecord } from "../dictionary-types";

const { TextArea } = Input;

interface DictionaryEditDrawerProps {
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

export const DictionaryEditDrawer = ({
    open,
    dictionary,
    saving,
    onClose,
    onSave
}: DictionaryEditDrawerProps) => {
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
            testId="system-dictionary-dictionary-edit-drawer"
            className="dictionary-edit-drawer"
            title={dictionary ? "编辑字典项" : "新增字典项"}
            open={Boolean(open)}
            size="small"
            onClose={onClose}
            footerActions={[
                {
                    testId: "system-dictionary-dictionary-cancel-button",
                    title: "取消",
                    action: onClose
                },
                {
                    testId: "system-dictionary-dictionary-save-button",
                    title: "保存",
                    type: "primary",
                    loading: saving,
                    action: saveDictionary
                }
            ]}
        >
            <KuzhambuForm<DictFormValues> form={form} className="dictionary-edit-drawer-form">
                <KuzhambuFormHiddenItem name="id">
                    <Input />
                </KuzhambuFormHiddenItem>
                <KuzhambuFormItem
                    name="type"
                    label="字典类型"
                    rules={[{ required: true, message: "请输入字典类型" }]}
                >
                    <Input placeholder="例如：user_status" />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    name="label"
                    label="标签"
                    rules={[{ required: true, message: "请输入标签" }]}
                >
                    <Input placeholder="例如：启用" />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    name="value"
                    label="值"
                    rules={[{ required: true, message: "请输入值" }]}
                >
                    <Input placeholder="例如：ENABLED" />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="remarks" label="备注" layoutSize="large">
                    <TextArea rows={3} maxLength={200} showCount placeholder="补充使用说明" />
                </KuzhambuFormItem>
            </KuzhambuForm>
        </KuzhambuDrawer>
    );
};
