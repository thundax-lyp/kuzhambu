import { Typography } from "antd";
import type { Key } from "react";
import { KuzhambuSwitch } from "@/components/kuzhambu-switch";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import { readApiSourceMeta, readCapabilityMeta } from "../ai-models-metadata";
import type { AiModelRecord } from "../ai-models-types";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    displayName: 180,
    modelName: 300,
    apiSource: 120,
    capabilities: 112,
    enabled: 96,
    registeredAt: 120
};

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const timestamp = Date.parse(value);
    if (Number.isNaN(timestamp)) {
        return value;
    }
    const date = new Date(timestamp);
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${date.getFullYear()}-${month}-${day}`;
};

const readModelName = (record: AiModelRecord) => {
    return record.displayName?.trim() || record.modelName;
};

const centerColumnTitle = (title: string) => (
    <span className="ai-models-center-column-title">{title}</span>
);

interface AiModelTableProps {
    canEditConfig: boolean;
    changing: boolean;
    dataSource: AiModelRecord[];
    loading: boolean;
    locale: KuzhambuTableProps<AiModelRecord>["locale"];
    onChangeEnabled: (record: AiModelRecord, enabled: boolean) => void;
    onDelete: (record: AiModelRecord) => void;
    onOpenEdit: (record: AiModelRecord) => void;
    onSelectedRowKeysChange: (keys: Key[]) => void;
    selectedRowKeys: Key[];
}

export const AiModelTable = ({
    canEditConfig,
    changing,
    dataSource,
    loading,
    locale,
    onChangeEnabled,
    onDelete,
    onOpenEdit,
    onSelectedRowKeysChange,
    selectedRowKeys
}: AiModelTableProps) => {
    const columns: KuzhambuTableProps<AiModelRecord>["columns"] = [
        {
            title: "模型名称",
            dataIndex: "displayName",
            key: "displayName",
            width: DEFAULT_COLUMN_WIDTHS.displayName,
            ellipsis: true,
            render: (value: string | null, record) => value || record.modelName
        },
        {
            title: "模型标识",
            dataIndex: "modelName",
            key: "modelName",
            minWidth: DEFAULT_COLUMN_WIDTHS.modelName,
            width: DEFAULT_COLUMN_WIDTHS.modelName,
            ellipsis: true,
            render: (modelName: string) => (
                <Text strong ellipsis title={modelName}>
                    {modelName}
                </Text>
            )
        },
        {
            title: centerColumnTitle("供应商"),
            dataIndex: "apiSource",
            key: "apiSource",
            align: "center",
            className: "ai-models-center-column",
            width: DEFAULT_COLUMN_WIDTHS.apiSource,
            render: (apiSource: string) => {
                const apiSourceMeta = readApiSourceMeta(apiSource);
                return <KuzhambuTag type={apiSourceMeta.type}>{apiSourceMeta.label}</KuzhambuTag>;
            }
        },
        {
            title: centerColumnTitle("能力"),
            dataIndex: "capabilities",
            key: "capabilities",
            align: "center",
            className: "ai-models-center-column",
            width: DEFAULT_COLUMN_WIDTHS.capabilities,
            render: (tags: string[] = []) => (
                <div className="ai-models-capabilities">
                    {tags.map((tag) => {
                        const capabilityMeta = readCapabilityMeta(tag);
                        return (
                            <KuzhambuTag key={tag} type={capabilityMeta.type}>
                                {capabilityMeta.label}
                            </KuzhambuTag>
                        );
                    })}
                </div>
            )
        },
        {
            title: centerColumnTitle("状态"),
            dataIndex: "enabled",
            key: "enabled",
            align: "center",
            className: "ai-models-center-column",
            width: DEFAULT_COLUMN_WIDTHS.enabled,
            render: (enabled: boolean, record) => (
                <KuzhambuSwitch
                    checked={enabled}
                    checkedChildren="启用"
                    unCheckedChildren="禁用"
                    aria-label={`切换 ${readModelName(record)} 状态，当前${enabled ? "启用" : "禁用"}`}
                    disabled={!canEditConfig || changing}
                    onChange={(checked) => onChangeEnabled(record, checked)}
                />
            )
        },
        {
            title: centerColumnTitle("注册时间"),
            dataIndex: "registeredAt",
            key: "registeredAt",
            align: "center",
            className: "ai-models-center-column",
            width: DEFAULT_COLUMN_WIDTHS.registeredAt,
            render: formatDateTime
        },
        {
            key: "actions",
            options: (record) => [
                {
                    key: "edit",
                    text: "编辑",
                    ariaLabel: `编辑 ${readModelName(record)}`,
                    disabled: !canEditConfig,
                    onClick: () => onOpenEdit(record)
                },
                { type: "divider" },
                {
                    key: "delete",
                    text: "删除",
                    type: "danger",
                    ariaLabel: `删除 ${readModelName(record)}`,
                    disabled: !canEditConfig,
                    onClick: () => onDelete(record)
                }
            ]
        }
    ];

    return (
        <KuzhambuTable<AiModelRecord>
            ariaLabel="模型列表"
            rowKey="id"
            className="ai-models-table"
            columns={columns}
            dataSource={dataSource}
            loading={loading}
            pagination={{ pageSize: 10, showSizeChanger: true }}
            scroll={{ x: 1052 }}
            rowSelection={{
                selectedRowKeys,
                onChange: onSelectedRowKeysChange,
                getCheckboxProps: () => ({
                    disabled: !canEditConfig
                })
            }}
            locale={locale}
        />
    );
};
