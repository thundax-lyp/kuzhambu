import { Form, Input } from "antd";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import type { TagAliasRecord } from "@/pages/knowledge/taxonomy/taxonomy-types";
import "./tag-alias-create-field.css";

interface TagAliasCreateFieldProps {
    aliases: TagAliasRecord[];
    saving?: boolean;
    onSubmit: () => void;
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
    return (
        <>
            <Form.Item
                name="name"
                className="tag-alias-create-field-name"
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
                        onSubmit();
                    }}
                />
            </Form.Item>
            <Form.Item className="tag-alias-create-field-action">
                <KuzhambuButton
                    testId="knowledge-taxonomy-tag-alias-action-button"
                    type="primary"
                    loading={saving}
                    onClick={onSubmit}
                >
                    新增别名
                </KuzhambuButton>
            </Form.Item>
        </>
    );
};
