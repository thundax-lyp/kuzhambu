import { EditOutlined } from "@ant-design/icons";
import { Table, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuSpace, KuzhambuSpaceCompact } from "@/components/kuzhambu-space";
import type { AiCapabilityMappingRecord } from "../capability-mappings-types";

export interface MappingTableRow extends AiCapabilityMappingRecord {
    capabilityName: string;
    modelName: string;
    modelTags: string[];
    outputMode: string;
    requiredTags: string[];
}

export interface CapabilityMappingTableProps {
    canEditConfig: boolean;
    dataSource: MappingTableRow[];
    loading: boolean;
    onChangeEnabled: (record: AiCapabilityMappingRecord, enabled: boolean) => void;
    onOpenEdit: (record: AiCapabilityMappingRecord) => void;
}

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const timestamp = Date.parse(value);
    if (Number.isNaN(timestamp)) {
        return value;
    }
    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(timestamp));
};

export const CapabilityMappingTable = ({
    canEditConfig,
    dataSource,
    loading,
    onChangeEnabled,
    onOpenEdit
}: CapabilityMappingTableProps) => {
    const columns: ColumnsType<MappingTableRow> = [
        {
            title: "scope",
            dataIndex: "scope",
            key: "scope"
        },
        {
            title: "capability",
            dataIndex: "capability",
            key: "capability"
        },
        {
            title: "capabilityName",
            dataIndex: "capabilityName",
            key: "capabilityName"
        },
        {
            title: "requiredTags",
            dataIndex: "requiredTags",
            key: "requiredTags",
            render: (tags: string[] = []) => (
                <KuzhambuSpace>
                    {tags.map((tag) => (
                        <Tag key={tag}>{tag}</Tag>
                    ))}
                </KuzhambuSpace>
            )
        },
        {
            title: "outputMode",
            dataIndex: "outputMode",
            key: "outputMode"
        },
        {
            title: "modelName",
            dataIndex: "modelName",
            key: "modelName"
        },
        {
            title: "enabled",
            dataIndex: "enabled",
            key: "enabled",
            render: (enabled: boolean) => (
                <Tag color={enabled ? "green" : "default"}>{enabled ? "启用" : "禁用"}</Tag>
            )
        },
        {
            title: "configuredAt",
            dataIndex: "configuredAt",
            key: "configuredAt",
            render: formatDateTime
        },
        {
            key: "actions",
            render: (_, record) => (
                <KuzhambuSpaceCompact>
                    <KuzhambuButton
                        testId="ai-capability-mappings-capability-mappings-configure-model-button"
                        icon={<EditOutlined />}
                        disabled={!canEditConfig}
                        onClick={() => onOpenEdit(record)}
                    >
                        配置模型
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="ai-capability-mappings-capability-mappings-disable-or-enable-button"
                        disabled={!canEditConfig}
                        onClick={() => onChangeEnabled(record, !record.enabled)}
                    >
                        {record.enabled ? "禁用" : "启用"}
                    </KuzhambuButton>
                </KuzhambuSpaceCompact>
            )
        }
    ];

    return (
        <Table<MappingTableRow>
            aria-label="AI 能力映射列表"
            rowKey={(record) => record.mappingId || `${record.scope}-${record.capability}`}
            className="capability-mappings-table"
            columns={columns}
            dataSource={dataSource}
            loading={loading}
            pagination={{ pageSize: 10, showSizeChanger: true }}
        />
    );
};
