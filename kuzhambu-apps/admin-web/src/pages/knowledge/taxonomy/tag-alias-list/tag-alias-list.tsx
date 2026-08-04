import { Empty, Popconfirm, Typography } from "antd";
import { useMemo } from "react";
import { KuzhambuList, KuzhambuListItem, KuzhambuSpace, KuzhambuButton } from "@/components";

import type { TagAliasCreateCommand, TagAliasRemoveCommand } from "../taxonomy-service";
import type { TagAliasRecord } from "../taxonomy-types";

import { TagAliasCreateField } from "../tag-alias-create-field";
import type { TagAliasCreateFormValues } from "../tag-alias-create-field";

const { Text } = Typography;

interface TagAliasListProps {
    aliases: TagAliasRecord[];
    canEdit?: boolean;
    loading?: boolean;
    removingAliasId?: string | null;
    saving?: boolean;
    tagId: string;
    onCreate?: (request: TagAliasCreateCommand) => Promise<void> | void;
    onRemove?: (request: TagAliasRemoveCommand) => void;
}

const createAliasId = () => {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
        return crypto.randomUUID();
    }
    return `${Date.now()}`;
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
    const sortedAliases = useMemo(
        () =>
            [...aliases].sort((left, right) =>
                (left.name || "").localeCompare(right.name || "", "zh-CN")
            ),
        [aliases]
    );

    const createAlias = async (values: TagAliasCreateFormValues) => {
        if (!canEdit || !onCreate) {
            return;
        }
        await onCreate({
            id: createAliasId(),
            tagId,
            name: values.name.trim()
        });
    };

    return (
        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
            {canEdit ? (
                <TagAliasCreateField aliases={aliases} saving={saving} onSubmit={createAlias} />
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
                                                  testId="knowledge-taxonomy-tag-alias-delete-button"
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
