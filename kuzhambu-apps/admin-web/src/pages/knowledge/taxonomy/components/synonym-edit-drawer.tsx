import { Form, Input } from "antd";
import { useEffect, useMemo } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuForm, KuzhambuFormItem } from "@/components/kuzhambu-form";
import type { SynonymCreateCommand, SynonymUpdateCommand } from "../taxonomy-service";
import type { SynonymRecord } from "../taxonomy-types";

interface SynonymEditDrawerProps {
    open?: boolean;
    saving?: boolean;
    synonym?: SynonymRecord | null;
    onClose: () => void;
    onCreate?: (request: SynonymCreateCommand) => void;
    onSave?: (request: SynonymUpdateCommand) => void;
}

interface SynonymEditDrawerFormValues {
    id?: string | null;
    synonym: string;
    term: string;
}

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const createSynonymId = () => {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
        return crypto.randomUUID();
    }
    return `${Date.now()}`;
};

const toFormValues = (record: SynonymRecord): SynonymEditDrawerFormValues => ({
    id: record.id,
    synonym: record.synonym || "",
    term: record.term || ""
});

const toCreateRequest = (values: SynonymEditDrawerFormValues): SynonymCreateCommand => ({
    id: values.id || createSynonymId(),
    synonym: values.synonym.trim(),
    term: values.term.trim()
});

const toUpdateRequest = (values: SynonymEditDrawerFormValues): SynonymUpdateCommand => ({
    id: values.id || "",
    synonym: values.synonym.trim(),
    term: values.term.trim()
});

export const SynonymEditDrawer = ({
    open,
    saving = false,
    synonym,
    onClose,
    onCreate,
    onSave
}: SynonymEditDrawerProps) => {
    const [form] = Form.useForm<SynonymEditDrawerFormValues>();
    const visible = Boolean(open);
    const editing = Boolean(synonym?.id);
    const title = editing ? "编辑同义词" : "新增同义词";
    const saveButtonText = editing ? "保存" : "新增";
    const defaultCreateValues = useMemo(
        () => ({
            id: createSynonymId(),
            synonym: "",
            term: ""
        }),
        []
    );

    useEffect(() => {
        if (!visible) {
            return;
        }
        if (synonym) {
            form.setFieldsValue(toFormValues(synonym));
            return;
        }
        form.setFieldsValue(defaultCreateValues);
    }, [defaultCreateValues, form, synonym, visible]);

    const saveSynonym = async () => {
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
            testId="knowledge-taxonomy-synonym-edit-drawer"
            className="knowledge-taxonomy-synonym-edit-drawer"
            title={title}
            open={visible}
            size="small"
            onClose={onClose}
            footerActions={[
                {
                    testId: "knowledge-taxonomy-synonym-cancel-button",
                    title: "取消",
                    disabled: saving,
                    action: onClose
                },
                {
                    testId: "knowledge-taxonomy-synonym-action-button",
                    title: saveButtonText,
                    type: "primary",
                    loading: saving,
                    action: () => void saveSynonym()
                }
            ]}
        >
            <KuzhambuForm<SynonymEditDrawerFormValues>
                form={form}
                className="taxonomy-synonym-form"
            >
                <Form.Item name="id" hidden>
                    <Input />
                </Form.Item>
                <KuzhambuFormItem
                    name="term"
                    label="术语"
                    rules={[
                        { required: true, message: "请输入术语" },
                        { max: 128, message: "术语最多 128 个字符" },
                        {
                            validator: async (_, value?: string) => {
                                if (!normalizeText(value)) {
                                    throw new Error("请输入术语");
                                }
                            }
                        }
                    ]}
                >
                    <Input maxLength={128} placeholder="例如：王圻" />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    name="synonym"
                    label="同义词"
                    rules={[
                        { required: true, message: "请输入同义词" },
                        { max: 128, message: "同义词最多 128 个字符" },
                        {
                            validator: async (_, value?: string) => {
                                const currentTerm = normalizeText(form.getFieldValue("term"));
                                const currentSynonym = normalizeText(value);
                                if (!currentSynonym) {
                                    throw new Error("请输入同义词");
                                }
                                if (currentTerm && currentTerm === currentSynonym) {
                                    throw new Error("术语与同义词不能相同");
                                }
                            }
                        }
                    ]}
                >
                    <Input maxLength={128} placeholder="例如：王元翰" />
                </KuzhambuFormItem>
            </KuzhambuForm>
        </KuzhambuDrawer>
    );
};
