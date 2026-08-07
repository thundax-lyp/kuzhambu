import { Form, Input } from "antd";
import { KuzhambuButton, KuzhambuForm, KuzhambuFormItem } from "@/components";
import type { TagAliasRecord } from "@/pages/knowledge/taxonomy/taxonomy-types";
import "./tag-alias-create-field.css";

export interface TagAliasCreateFormValues {
    name: string;
}

interface TagAliasCreateFieldProps {
    aliases: TagAliasRecord[];
    saving?: boolean;
    onSubmit: (values: TagAliasCreateFormValues) => Promise<void> | void;
}

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

export const TagAliasCreateField = ({
    aliases,
    saving = false,
    onSubmit
}: TagAliasCreateFieldProps) => {
    const [form] = Form.useForm<TagAliasCreateFormValues>();

    const submitAlias = async () => {
        const values = await form.validateFields();
        await onSubmit(values);
        form.resetFields();
    };

    return (
        <KuzhambuForm<TagAliasCreateFormValues>
            form={form}
            className="tag-alias-create-field"
            component="div"
            itemGap="none"
        >
            <KuzhambuFormItem
                name="name"
                colProps={{ flex: "1 1 0", className: "tag-alias-create-field-name" }}
                rules={[
                    { required: true, message: "请输入别名" },
                    { max: 128, message: "别名最多 128 个字符" },
                    {
                        validator: async (_, value?: string) => {
                            const normalizedValue = normalizeText(value);
                            if (!normalizedValue) {
                                return;
                            }
                            if (
                                aliases.some(
                                    (alias) =>
                                        alias.name?.toLowerCase() === normalizedValue.toLowerCase()
                                )
                            ) {
                                throw new Error("该别名已存在");
                            }
                        }
                    }
                ]}
            >
                <Input
                    disabled={saving}
                    maxLength={128}
                    placeholder="新增标签别名"
                    onPressEnter={(event) => {
                        event.preventDefault();
                        void submitAlias();
                    }}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem
                colProps={{ flex: "0 0 auto", className: "tag-alias-create-field-action" }}
            >
                <KuzhambuButton
                    testId="knowledge-taxonomy-tag-alias-action-button"
                    type="primary"
                    loading={saving}
                    onClick={() => void submitAlias()}
                >
                    新增别名
                </KuzhambuButton>
            </KuzhambuFormItem>
        </KuzhambuForm>
    );
};
