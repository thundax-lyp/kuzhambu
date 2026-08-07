import {
    ClockCircleOutlined,
    GlobalOutlined,
    IdcardOutlined,
    ReloadOutlined
} from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Avatar, Empty, Input, Tag } from "antd";
import { useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { useCurrentAccessToken } from "@/auth/hooks/use-current-access-token";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { ADMIN_API_BASE_URL } from "@/api/http";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import {
    KuzhambuButton,
    KuzhambuListPage,
    KuzhambuSelect,
    KuzhambuSpace,
    KuzhambuTag,
    KuzhambuText,
    type KuzhambuListPageFilterField,
    type KuzhambuTableProps,
    type OptionsRecord
} from "@/components";
import { AuditLogDetail } from "./audit-log-detail";
import * as service from "./audit-log-service";
import type { AuditLogPageQuery } from "./audit-log-service";
import type { AuditLogRecord } from "./audit-log-types";

import "./audit-log-page.css";

const DEFAULT_COLUMN_WIDTHS = {
    occurredAt: 180,
    object: 260,
    action: 120,
    operator: 180,
    source: 140
};

const ADMIN_OPERATOR_TYPE = "USER";

interface AuditLogFilters {
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

const DEFAULT_AUDIT_LOG_FILTERS: AuditLogFilters = {
    objectType: "ALL",
    objectId: "",
    action: "ALL",
    operatorType: "ALL",
    operatorId: "",
    source: "",
    requestId: "",
    beginDate: "",
    endDate: ""
};

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readSelectValue = (value: string) => {
    return value === "ALL" ? undefined : value;
};

const optionItems = (options?: OptionsRecord[string]) => [
    { value: "ALL", label: "全部" },
    ...(options || []).map((option) => ({
        value: option.value,
        label: option.label || option.value
    }))
];

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    });
};

const readObjectDisplay = (log: AuditLogRecord) => {
    return log.objectDisplayName || log.objectId || "-";
};

const readObjectTypeLabel = (log: AuditLogRecord) => {
    return log.objectTypeLabel || log.objectType || "未知对象";
};

const getInitials = (value?: string | null) => {
    const normalizedValue = value?.trim() || "U";
    return Array.from(normalizedValue.replace(/\s+/g, "")).slice(0, 2).join("");
};

const readOperatorName = (log: AuditLogRecord) => {
    return log.operatorName || log.operatorId || "-";
};

const readOperatorUser = (log: AuditLogRecord) => {
    const name = readOperatorName(log);
    const avatarUrl =
        log.operatorType === ADMIN_OPERATOR_TYPE && log.operatorId
            ? `${ADMIN_API_BASE_URL}/sys/user/avatar?id=${encodeURIComponent(log.operatorId)}`
            : undefined;
    return { avatarUrl, name };
};

const renderOperator = (log: AuditLogRecord, accessToken: string | null) => {
    const user = readOperatorUser(log);
    const avatarUrl = toAuthenticatedResourceUrl(user.avatarUrl, accessToken);
    return (
        <KuzhambuSpace size={8} className="audit-log-operator-cell">
            <Avatar size={28} src={avatarUrl}>
                {getInitials(user.name)}
            </Avatar>
            <KuzhambuText className="audit-log-operator-name" ellipsis>
                {user.name}
            </KuzhambuText>
        </KuzhambuSpace>
    );
};

export const AuditLogPage = () => {
    const canViewAuditLog = hasPermission("audit:view");
    const accessToken = useCurrentAccessToken();
    const [query, setQuery] = useState<AuditLogPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<AuditLogFilters>(DEFAULT_AUDIT_LOG_FILTERS);
    const [detailLogId, setDetailLogId] = useState<string | null>(null);
    const hasActiveFilters = Boolean(
        filters.objectType !== "ALL" ||
        filters.objectId.trim() ||
        filters.action !== "ALL" ||
        filters.operatorType !== "ALL" ||
        filters.operatorId.trim() ||
        filters.source.trim() ||
        filters.requestId.trim() ||
        filters.beginDate.trim() ||
        filters.endDate.trim()
    );

    const auditOptionsQuery = useQuery({
        queryKey: ["audit-log", "options"],
        queryFn: service.getAuditOptions,
        enabled: canViewAuditLog,
        retry: false
    });
    const auditLogQuery = useQuery({
        queryKey: ["audit-log", "page", query],
        queryFn: () => service.pageAuditLogs(query),
        enabled: canViewAuditLog,
        retry: false
    });
    const auditLogPage = auditLogQuery.data;
    const auditLogs = useMemo(() => auditLogPage?.records || [], [auditLogPage?.records]);
    const totalCount = auditLogPage?.count ?? auditLogPage?.totalCount ?? 0;
    const currentPageNo = auditLogPage?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = auditLogPage?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;
    const auditOptions = auditOptionsQuery.data;
    const filterFields = useMemo<KuzhambuListPageFilterField[]>(() => {
        const updateFilters = (values: Partial<AuditLogFilters>) => {
            setFilters((currentFilters) => ({ ...currentFilters, ...values }));
        };
        const loading = auditOptionsQuery.isFetching;

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
    }, [auditOptions, auditOptionsQuery.isFetching, filters]);

    if (!canViewAuditLog) {
        return <Empty description="缺少 audit:view 权限" />;
    }

    const updateQuery = (values: Partial<AuditLogPageQuery>) => {
        setQuery((currentQuery) => {
            const nextQuery = { ...currentQuery, ...values };
            return {
                objectType: nextQuery.objectType,
                objectId: nextQuery.objectId,
                action: nextQuery.action,
                operatorType: nextQuery.operatorType,
                operatorId: nextQuery.operatorId,
                source: nextQuery.source,
                requestId: nextQuery.requestId,
                beginDate: nextQuery.beginDate,
                endDate: nextQuery.endDate,
                pageNo: values.pageNo || DEFAULT_PAGE_NO,
                pageSize: values.pageSize || currentQuery.pageSize || DEFAULT_PAGE_SIZE
            };
        });
    };

    const searchObjectId = (value: string) => {
        setSearchText(value);
        updateQuery({ objectId: normalizeSearch(value) });
    };

    const applyFilters = () => {
        updateQuery({
            objectType: readSelectValue(filters.objectType),
            objectId: normalizeSearch(filters.objectId || searchText),
            action: readSelectValue(filters.action),
            operatorType: readSelectValue(filters.operatorType),
            operatorId: normalizeSearch(filters.operatorId),
            source: normalizeSearch(filters.source),
            requestId: normalizeSearch(filters.requestId),
            beginDate: normalizeSearch(filters.beginDate),
            endDate: normalizeSearch(filters.endDate)
        });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_AUDIT_LOG_FILTERS);
        setSearchText("");
        updateQuery({
            objectType: undefined,
            objectId: undefined,
            action: undefined,
            operatorType: undefined,
            operatorId: undefined,
            source: undefined,
            requestId: undefined,
            beginDate: undefined,
            endDate: undefined
        });
    };

    const columns: KuzhambuTableProps<AuditLogRecord>["columns"] = [
        {
            title: "时间",
            dataIndex: "occurredAt",
            key: "occurredAt",
            width: DEFAULT_COLUMN_WIDTHS.occurredAt,
            render: (occurredAt?: string | null) => (
                <KuzhambuSpace size={7}>
                    <ClockCircleOutlined className="audit-log-time-icon" />
                    <span>{formatDateTime(occurredAt)}</span>
                </KuzhambuSpace>
            )
        },
        {
            title: "对象",
            key: "object",
            width: DEFAULT_COLUMN_WIDTHS.object,
            ellipsis: true,
            render: (_, log) => (
                <div className="audit-log-object-cell">
                    <KuzhambuText strong>{readObjectDisplay(log)}</KuzhambuText>
                    <KuzhambuSpace size={6} wrap>
                        <KuzhambuTag type="accent">{readObjectTypeLabel(log)}</KuzhambuTag>
                        {log.version ? <Tag>v{log.version}</Tag> : null}
                    </KuzhambuSpace>
                </div>
            )
        },
        {
            title: "动作",
            dataIndex: "actionLabel",
            key: "action",
            width: DEFAULT_COLUMN_WIDTHS.action,
            render: (_, log) => (
                <KuzhambuTag type="success">{log.actionLabel || log.action || "-"}</KuzhambuTag>
            )
        },
        {
            title: "操作者",
            key: "operator",
            width: DEFAULT_COLUMN_WIDTHS.operator,
            ellipsis: true,
            render: (_, log) => renderOperator(log, accessToken)
        },
        {
            title: "来源",
            dataIndex: "source",
            key: "source",
            width: DEFAULT_COLUMN_WIDTHS.source,
            render: (source?: string | null) => source || "-"
        },
        {
            title: "摘要",
            dataIndex: "summary",
            key: "summary",
            ellipsis: true,
            render: (summary?: string | null) => summary || "-"
        },
        {
            key: "actions",
            options: (log) => [
                {
                    key: "view",
                    text: "查看",
                    ariaLabel: `查看审计日志 ${log.id}`,
                    onClick: () => setDetailLogId(log.id)
                }
            ]
        }
    ];

    return (
        <>
            <KuzhambuListPage<AuditLogRecord>
                pageClassName="audit-log-page"
                title="审计日志"
                description="查看关键业务对象的变更记录、操作者和字段差异。"
                subjectName="审计日志"
                enableFilter
                enableSearch
                searchShortcut="⌘K"
                searchValue={searchText}
                searchPlaceholder="搜索对象 ID..."
                onSearchChange={searchObjectId}
                filterActive={hasActiveFilters}
                filterFields={filterFields}
                onFilterApply={applyFilters}
                onFilterReset={resetFilters}
                pageActions={
                    <KuzhambuButton
                        testId="audit-audit-log-audit-log-refresh-button"
                        icon={<ReloadOutlined />}
                        loading={auditLogQuery.isFetching}
                        onClick={() => auditLogQuery.refetch()}
                    >
                        刷新
                    </KuzhambuButton>
                }
                rowKey="id"
                className="audit-log-table"
                columns={columns}
                dataSource={auditLogs}
                loading={auditLogQuery.isFetching}
                scroll={{ x: 1320 }}
                pagination={{
                    current: currentPageNo,
                    pageSize: currentPageSize,
                    total: totalCount,
                    showTotal: (total) => `共 ${total} 条`,
                    onChange: (pageNo, pageSize) => updateQuery({ pageNo, pageSize })
                }}
                locale={{
                    emptyText: auditLogQuery.isError
                        ? "审计日志加载失败，请确认权限和接口状态。"
                        : "暂无审计日志"
                }}
            />

            <AuditLogDetail
                accessToken={accessToken}
                auditLogId={detailLogId}
                onClose={() => setDetailLogId(null)}
            />
        </>
    );
};
