import { GlobalOutlined, IdcardOutlined } from "@ant-design/icons";
import { Input } from "antd";
import type { AuditOptionKeys } from "../audit-log-service";
import { KuzhambuSelect, type KuzhambuListPageFilterField, type OptionsRecord } from "@/components";

export interface AuditLogFilters {
    objectType: string;
    objectId: string;
    action: string;
    operatorType: string;
    operatorId: string;
    source: string;
    requestId: string;
    beginDate: string;
    endDate: string;
}

interface AuditLogFilterFieldsOptions {
    auditOptions?: Partial<OptionsRecord<AuditOptionKeys>>;
    filters: AuditLogFilters;
    loading?: boolean;
    onChange: (filters: AuditLogFilters) => void;
}

const optionItems = (options?: OptionsRecord[string]) => [
    { value: "ALL", label: "全部" },
    ...(options || []).map((option) => ({
        value: option.value,
        label: option.label || option.value
    }))
];

export const createAuditLogFilterFields = ({
    auditOptions,
    filters,
    loading = false,
    onChange
}: AuditLogFilterFieldsOptions): KuzhambuListPageFilterField[] => {
    const updateFilters = (values: Partial<AuditLogFilters>) => {
        onChange({ ...filters, ...values });
    };

    return [
        {
            name: "objectType",
            label: "对象类型",
            render: () => (
                <KuzhambuSelect
                    value={filters.objectType}
                    options={optionItems(auditOptions?.objectTypes)}
                    loading={loading}
                    onChange={(objectType) => updateFilters({ objectType })}
                />
            )
        },
        {
            name: "action",
            label: "动作",
            render: () => (
                <KuzhambuSelect
                    value={filters.action}
                    options={optionItems(auditOptions?.actions)}
                    loading={loading}
                    onChange={(action) => updateFilters({ action })}
                />
            )
        },
        {
            name: "operatorType",
            label: "操作者类型",
            render: () => (
                <KuzhambuSelect
                    value={filters.operatorType}
                    options={optionItems(auditOptions?.operatorTypes)}
                    loading={loading}
                    onChange={(operatorType) => updateFilters({ operatorType })}
                />
            )
        },
        {
            name: "objectId",
            label: "对象 ID",
            render: () => (
                <Input
                    allowClear
                    prefix={<IdcardOutlined />}
                    value={filters.objectId}
                    onChange={(event) => updateFilters({ objectId: event.target.value })}
                />
            )
        },
        {
            name: "operatorId",
            label: "操作者 ID",
            render: () => (
                <Input
                    allowClear
                    value={filters.operatorId}
                    onChange={(event) => updateFilters({ operatorId: event.target.value })}
                />
            )
        },
        {
            name: "source",
            label: "来源",
            render: () => (
                <Input
                    allowClear
                    prefix={<GlobalOutlined />}
                    value={filters.source}
                    onChange={(event) => updateFilters({ source: event.target.value })}
                />
            )
        },
        {
            name: "requestId",
            label: "请求 ID",
            render: () => (
                <Input
                    allowClear
                    value={filters.requestId}
                    onChange={(event) => updateFilters({ requestId: event.target.value })}
                />
            )
        },
        {
            name: "beginDate",
            label: "开始时间",
            render: () => (
                <Input
                    allowClear
                    placeholder="2026-05-19 00:00:00"
                    value={filters.beginDate}
                    onChange={(event) => updateFilters({ beginDate: event.target.value })}
                />
            )
        },
        {
            name: "endDate",
            label: "结束时间",
            render: () => (
                <Input
                    allowClear
                    placeholder="2026-05-19 23:59:59"
                    value={filters.endDate}
                    onChange={(event) => updateFilters({ endDate: event.target.value })}
                />
            )
        }
    ];
};
