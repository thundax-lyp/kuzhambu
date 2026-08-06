import {
    ClockCircleOutlined,
    GlobalOutlined,
    LinkOutlined,
    ReloadOutlined,
    UserOutlined
} from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { App, DatePicker, Empty, Input, Typography } from "antd";
import type { Dayjs } from "dayjs";
import { useEffect, useMemo, useRef, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import {
    KuzhambuListPage,
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuDescriptions,
    KuzhambuSpace,
    KuzhambuTag,
    KuzhambuTable,
    type KuzhambuTableProps
} from "@/components";
import * as service from "./system-log-service";
import type { LogPageQuery } from "./system-log-service";
import type { LogRecord } from "./system-log-types";
import "./system-log-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    createDate: 176,
    type: 104,
    method: 96,
    requestUri: 280,
    user: 180,
    remoteAddr: 160
};

interface SystemLogFilters {
    period: [Dayjs | null, Dayjs | null] | null;
    remoteAddr: string;
    requestUri: string;
    userLoginName: string;
    userName: string;
}

const DEFAULT_SYSTEM_LOG_FILTERS: SystemLogFilters = {
    period: null,
    remoteAddr: "",
    requestUri: "",
    userLoginName: "",
    userName: ""
};

const SYSTEM_LOG_DATE_TIME_FORMAT = "YYYY-MM-DD HH:mm:ss";
const SEARCH_DEBOUNCE_MS = 500;

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readUserDisplay = (log: LogRecord) => {
    const name = log.createUser?.name;
    const loginName = log.createUser?.loginName;
    if (name && loginName) {
        return `${name} / ${loginName}`;
    }
    return name || loginName || "";
};

const methodTagType = (method?: string | null) => {
    const normalizedMethod = method?.toLowerCase();
    if (normalizedMethod === "get") {
        return "success";
    }
    if (normalizedMethod === "post") {
        return "accent";
    }
    if (normalizedMethod === "put" || normalizedMethod === "patch") {
        return "warning";
    }
    return "info";
};

export const SystemLogPage = () => {
    const { message: messageApi } = App.useApp();
    const canViewSystemLog = hasPermission("system:log:view");
    const [query, setQuery] = useState<LogPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [searchText, setSearchText] = useState("");
    const searchTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const [filters, setFilters] = useState<SystemLogFilters>(DEFAULT_SYSTEM_LOG_FILTERS);
    const hasActiveFilters = Boolean(
        filters.period ||
        filters.remoteAddr.trim() ||
        filters.requestUri.trim() ||
        filters.userLoginName.trim() ||
        filters.userName.trim()
    );

    const systemLogPageQuery = useQuery({
        queryKey: ["system-log", "page", query],
        queryFn: () => service.pageEvents(query),
        enabled: canViewSystemLog,
        retry: false
    });

    const logPage = systemLogPageQuery.data;
    const logs = useMemo(() => logPage?.records || [], [logPage?.records]);
    const totalCount = logPage?.count ?? logPage?.totalCount ?? 0;
    const currentPageNo = logPage?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = logPage?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;

    useEffect(() => {
        return () => {
            if (searchTimerRef.current) {
                clearTimeout(searchTimerRef.current);
            }
        };
    }, []);

    if (!canViewSystemLog) {
        return <Empty description="缺少 system:log:view 权限" />;
    }

    const updateQuery = (values: Partial<LogPageQuery>) => {
        setQuery((currentQuery) => {
            const nextQuery = { ...currentQuery, ...values };
            return {
                title: nextQuery.title,
                userLoginName: nextQuery.userLoginName,
                userName: nextQuery.userName,
                remoteAddr: nextQuery.remoteAddr,
                requestUri: nextQuery.requestUri,
                beginDate: nextQuery.beginDate,
                endDate: nextQuery.endDate,
                pageNo: DEFAULT_PAGE_NO,
                pageSize: currentQuery.pageSize || DEFAULT_PAGE_SIZE
            };
        });
    };

    const searchEvents = (value: string) => {
        setSearchText(value);
        if (searchTimerRef.current) {
            clearTimeout(searchTimerRef.current);
        }
        searchTimerRef.current = setTimeout(() => {
            updateQuery({ title: normalizeSearch(value) });
        }, SEARCH_DEBOUNCE_MS);
    };

    const applyFilters = () => {
        const [beginDate, endDate] = filters.period || [];
        if (beginDate && endDate && endDate.isBefore(beginDate)) {
            messageApi.error("结束时间不能早于开始时间");
            return;
        }
        updateQuery({
            beginDate: beginDate?.format(SYSTEM_LOG_DATE_TIME_FORMAT),
            endDate: endDate?.format(SYSTEM_LOG_DATE_TIME_FORMAT),
            remoteAddr: normalizeSearch(filters.remoteAddr),
            requestUri: normalizeSearch(filters.requestUri),
            userLoginName: normalizeSearch(filters.userLoginName),
            userName: normalizeSearch(filters.userName)
        });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_SYSTEM_LOG_FILTERS);
        updateQuery({
            beginDate: undefined,
            endDate: undefined,
            remoteAddr: undefined,
            requestUri: undefined,
            userLoginName: undefined,
            userName: undefined
        });
    };

    const columns: KuzhambuTableProps<LogRecord>["columns"] = [
        {
            title: "时间",
            dataIndex: "createDate",
            key: "createDate",
            width: DEFAULT_COLUMN_WIDTHS.createDate,
            render: (createDate?: string | null) => (
                <KuzhambuSpace size={7}>
                    <ClockCircleOutlined className="system-log-time-icon" />
                    <span>{createDate}</span>
                </KuzhambuSpace>
            )
        },
        {
            title: "标题",
            dataIndex: "title",
            key: "title",
            ellipsis: true,
            render: (title?: string | null) => <Text strong>{title}</Text>
        },
        {
            title: "类型",
            dataIndex: "type",
            key: "type",
            width: DEFAULT_COLUMN_WIDTHS.type,
            render: (type?: string | null) =>
                type ? <KuzhambuTag type="info">{type}</KuzhambuTag> : null
        },
        {
            title: "方法",
            dataIndex: "method",
            key: "method",
            width: DEFAULT_COLUMN_WIDTHS.method,
            render: (method?: string | null) =>
                method ? <KuzhambuTag type={methodTagType(method)}>{method}</KuzhambuTag> : null
        },
        {
            title: "请求地址",
            dataIndex: "requestUri",
            key: "requestUri",
            width: DEFAULT_COLUMN_WIDTHS.requestUri,
            ellipsis: true,
            render: (requestUri?: string | null) =>
                requestUri ? (
                    <Text className="system-log-code" code>
                        {requestUri}
                    </Text>
                ) : null
        },
        {
            title: "用户",
            key: "createUser",
            width: DEFAULT_COLUMN_WIDTHS.user,
            ellipsis: true,
            render: (_, log) => readUserDisplay(log) || null
        },
        {
            title: "来源",
            dataIndex: "remoteAddr",
            key: "remoteAddr",
            width: DEFAULT_COLUMN_WIDTHS.remoteAddr,
            render: (remoteAddr?: string | null) => remoteAddr || null
        }
    ];

    return (
        <KuzhambuListPage<LogRecord>
            pageClassName="system-log-page"
            title="系统日志"
            description="查看后台操作日志、请求记录和审计线索。"
            subjectName="日志"
            enableFilter
            enableSearch
            searchShortcut="⌘K"
            searchValue={searchText}
            searchPlaceholder="搜索系统日志标题..."
            onSearchChange={searchEvents}
            filterActive={hasActiveFilters}
            filterFields={[
                {
                    name: "userLoginName",
                    label: "登录名",
                    render: () => (
                        <Input
                            allowClear
                            placeholder="admin"
                            prefix={<UserOutlined />}
                            value={filters.userLoginName}
                            onChange={(event) =>
                                setFilters((currentFilters) => ({
                                    ...currentFilters,
                                    userLoginName: event.target.value
                                }))
                            }
                        />
                    )
                },
                {
                    name: "userName",
                    label: "用户名",
                    render: () => (
                        <Input
                            allowClear
                            placeholder="张三"
                            value={filters.userName}
                            onChange={(event) =>
                                setFilters((currentFilters) => ({
                                    ...currentFilters,
                                    userName: event.target.value
                                }))
                            }
                        />
                    )
                },
                {
                    name: "remoteAddr",
                    label: "来源",
                    render: () => (
                        <Input
                            allowClear
                            placeholder="127.0.0.1"
                            prefix={<GlobalOutlined />}
                            value={filters.remoteAddr}
                            onChange={(event) =>
                                setFilters((currentFilters) => ({
                                    ...currentFilters,
                                    remoteAddr: event.target.value
                                }))
                            }
                        />
                    )
                },
                {
                    name: "requestUri",
                    label: "请求地址",
                    render: () => (
                        <Input
                            allowClear
                            placeholder="/api/sys/user/page"
                            prefix={<LinkOutlined />}
                            value={filters.requestUri}
                            onChange={(event) =>
                                setFilters((currentFilters) => ({
                                    ...currentFilters,
                                    requestUri: event.target.value
                                }))
                            }
                        />
                    )
                },
                {
                    name: "period",
                    label: "时间范围",
                    render: () => (
                        <DatePicker.RangePicker
                            aria-label="日志时间范围"
                            format={SYSTEM_LOG_DATE_TIME_FORMAT}
                            showTime
                            value={filters.period}
                            onChange={(period) =>
                                setFilters((currentFilters) => ({
                                    ...currentFilters,
                                    period
                                }))
                            }
                        />
                    )
                }
            ]}
            onFilterApply={applyFilters}
            onFilterReset={resetFilters}
            pageActions={
                <KuzhambuButton
                    testId="system-system-log-system-log-refresh-button"
                    icon={<ReloadOutlined />}
                    loading={systemLogPageQuery.isFetching}
                    onClick={() => void systemLogPageQuery.refetch()}
                >
                    刷新
                </KuzhambuButton>
            }
            content={
                <>
                    {systemLogPageQuery.isError ? (
                        <KuzhambuAlert
                            showIcon
                            type="error"
                            title="系统日志加载失败"
                            description={
                                logs.length > 0
                                    ? "当前展示的是上次成功加载的数据，本次查询未更新。"
                                    : systemLogPageQuery.error instanceof Error
                                      ? systemLogPageQuery.error.message
                                      : "请确认权限和接口状态后重试。"
                            }
                            action={
                                <KuzhambuButton
                                    ariaLabel="重试加载系统日志"
                                    testId="system-system-log-retry-button"
                                    onClick={() => void systemLogPageQuery.refetch()}
                                >
                                    重试
                                </KuzhambuButton>
                            }
                        />
                    ) : null}
                    <KuzhambuTable<LogRecord>
                        ariaLabel="日志列表"
                        rowKey="id"
                        className="system-log-table"
                        columns={columns}
                        dataSource={logs}
                        loading={systemLogPageQuery.isFetching}
                        scroll={{ x: 1220 }}
                        expandable={{
                            expandedRowRender: (log) => (
                                <KuzhambuDescriptions
                                    ariaLabel={`系统日志 ${log.id} 详情`}
                                    className="system-log-detail"
                                    column={1}
                                    size="small"
                                    items={[
                                        {
                                            key: "requestParams",
                                            label: "请求参数",
                                            children: (
                                                <span className="system-log-detail-text">
                                                    {log.requestParams || "-"}
                                                </span>
                                            )
                                        },
                                        {
                                            key: "userAgent",
                                            label: "User-Agent",
                                            children: (
                                                <span className="system-log-detail-text">
                                                    {log.userAgent || "-"}
                                                </span>
                                            )
                                        },
                                        {
                                            key: "department",
                                            label: "部门",
                                            children: (
                                                <span className="system-log-detail-text">
                                                    {log.createUser?.department?.namePath ||
                                                        log.createUser?.department?.name ||
                                                        "-"}
                                                </span>
                                            )
                                        },
                                        {
                                            key: "remarks",
                                            label: "备注",
                                            children: (
                                                <span className="system-log-detail-text">
                                                    {log.remarks || "-"}
                                                </span>
                                            )
                                        }
                                    ]}
                                />
                            )
                        }}
                        pagination={{
                            current: currentPageNo,
                            pageSize: currentPageSize,
                            total: totalCount,
                            showTotal: (total) => `共 ${total} 条`,
                            onChange: (pageNo, pageSize) => {
                                setQuery((currentQuery) => ({
                                    ...currentQuery,
                                    pageNo,
                                    pageSize
                                }));
                            }
                        }}
                        locale={{ emptyText: "暂无系统日志" }}
                    />
                </>
            }
        />
    );
};
