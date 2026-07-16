import { Empty, Form, Input, Popconfirm, Typography } from "antd";
import { useMemo } from "react";
import { KuzhambuList, KuzhambuListItem } from "@/components/kuzhambu-list";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type { TagAliasCreateCommand, TagAliasRemoveCommand } from "../taxonomy-service";
import type { TagAliasRecord } from "../taxonomy-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { Text } = Typography;

interface TagAliasListProps {
    aliases: TagAliasRecord[];
    canEdit?: boolean;
    loading?: boolean;
    removingAliasId?: string | null;
    saving?: boolean;
    tagId: string;
    onCreate?: (request: TagAliasCreateCommand) => void;
    onRemove?: (request: TagAliasRemoveCommand) => void;
}

interface TagAliasFormValues {
    name: string;
}

const createAliasId = () => {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
        return crypto.randomUUID();
    }
    return `${Date.now()}`;
};

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readSourceLabel = (source?: string | null) => {
    switch (source) {
        case "MANUAL":
            return "人工";
        case "AI_EXTRACTED":
            return "AI 提取";
        default:
            return source || "-";
    }
};

export const TagAliasList = ({
    aliases,
    canEdit = false,
    loading = false,
    removingAliasId,
    saving = false,
    tagId,
    onCreate,
    onRemove
}: TagAliasListProps) => {
    const [form] = Form.useForm<TagAliasFormValues>();
    const sortedAliases = useMemo(
        () =>
            [...aliases].sort((left, right) =>
                (left.name || "").localeCompare(right.name || "", "zh-CN")
            ),
        [aliases]
    );

    const createAlias = async () => {
        if (!canEdit || !onCreate) {
            return;
        }
        const values = await form.validateFields();
        onCreate({
            id: createAliasId(),
            tagId,
            name: values.name.trim()
        });
        form.resetFields();
    };

    return (
        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
            {canEdit ? (
                <Form<TagAliasFormValues> form={form} layout="inline">
                    <Form.Item
                        name="name"
                        style={{ flex: 1, marginBottom: 0 }}
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
                                                alias.name?.toLowerCase() ===
                                                normalizedValue.toLowerCase()
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
                                void createAlias();
                            }}
                        />
                    </Form.Item>
                    <Form.Item style={{ marginBottom: 0 }}>
                        <KuzhambuButton
                            name="新增别名"
                            type="primary"
                            loading={saving}
                            onClick={() => void createAlias()}
                        >
                            新增别名
                        </KuzhambuButton>
                    </Form.Item>
                </Form>
            ) : null}

            {sortedAliases.length > 0 ? (
                <KuzhambuList
                    bordered
                    loading={loading}
                    dataSource={sortedAliases}
                    renderItem={(alias) => (
                        <KuzhambuListItem
                            key={alias.id}
                            actions={
                                canEdit && onRemove
                                    ? [
                                          <Popconfirm
                                              key="remove"
                                              title="删除别名"
                                              description="删除后需重新新增。"
                                              okText="删除"
                                              cancelText="取消"
                                              onConfirm={() => onRemove({ id: alias.id })}
                                          >
                                              <KuzhambuButton
                                                  name="删除"
                                                  type="link"
                                                  danger
                                                  loading={removingAliasId === alias.id}
                                              >
                                                  删除
                                              </KuzhambuButton>
                                          </Popconfirm>
                                      ]
                                    : undefined
                            }
                        >
                            <KuzhambuSpace split={<Text type="secondary">|</Text>} wrap>
                                <Text>{alias.name || alias.id}</Text>
                                <Text type="secondary">来源：{readSourceLabel(alias.source)}</Text>
                            </KuzhambuSpace>
                        </KuzhambuListItem>
                    )}
                />
            ) : (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无标签别名" />
            )}
        </KuzhambuSpace>
    );
};
