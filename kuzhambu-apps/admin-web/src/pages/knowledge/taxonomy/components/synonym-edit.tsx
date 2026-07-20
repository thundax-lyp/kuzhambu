import { Form, Input } from "antd";
import { useEffect, useMemo } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import type { SynonymCreateCommand, SynonymUpdateCommand } from "../taxonomy-service";
import type { SynonymRecord } from "../taxonomy-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

interface SynonymEditProps {
    open?: boolean;
    saving?: boolean;
    synonym?: SynonymRecord | null;
    onClose: () => void;
    onCreate?: (request: SynonymCreateCommand) => void;
    onSave?: (request: SynonymUpdateCommand) => void;
}

interface SynonymEditFormValues {
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

const toFormValues = (record: SynonymRecord): SynonymEditFormValues => ({
    id: record.id,
    synonym: record.synonym || "",
    term: record.term || ""
});

const toCreateRequest = (values: SynonymEditFormValues): SynonymCreateCommand => ({
    id: values.id || createSynonymId(),
    synonym: values.synonym.trim(),
    term: values.term.trim()
});

const toUpdateRequest = (values: SynonymEditFormValues): SynonymUpdateCommand => ({
    id: values.id || "",
    synonym: values.synonym.trim(),
    term: values.term.trim()
});

export const SynonymEdit = ({
    open,
    saving = false,
    synonym,
    onClose,
    onCreate,
    onSave
}: SynonymEditProps) => {
    const [form] = Form.useForm<SynonymEditFormValues>();
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
            testId="knowledge-taxonomy-synonym-editor-drawer"
            className="knowledge-taxonomy-synonym-editor"
            title={title}
            open={visible}
            size="small"
            onClose={onClose}
            footer={
                <div className="knowledge-taxonomy-synonym-editor-footer">
                    <KuzhambuButton
                        testId="knowledge-taxonomy-synonym-cancel-button"
                        disabled={saving}
                        onClick={onClose}
                    >
                        取消
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-taxonomy-synonym-action-button"
                        type="primary"
                        loading={saving}
                        onClick={() => void saveSynonym()}
                    >
                        {saveButtonText}
                    </KuzhambuButton>
                </div>
            }
        >
            <Form<SynonymEditFormValues>
                form={form}
                layout="vertical"
                className="taxonomy-synonym-form"
            >
                <Form.Item name="id" hidden>
                    <Input />
                </Form.Item>
                <Form.Item
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
                </Form.Item>
                <Form.Item
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
                </Form.Item>
            </Form>
        </KuzhambuDrawer>
    );
};
