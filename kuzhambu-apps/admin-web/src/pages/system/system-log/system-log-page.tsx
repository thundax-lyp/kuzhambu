import {
    ClockCircleOutlined,
    GlobalOutlined,
    LinkOutlined,
    ReloadOutlined,
    UserOutlined
} from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Button, Input, Space, Typography } from "antd";
import { useMemo, useState } from "react";
import { SandwishListPage } from "@/components/sandwish-list-page";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import * as service from "./system-log-service";
import type { LogPageQuery } from "./system-log-service";
import type { LogRecord } from "./system-log-types";
import "./system-log-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    createDate: 176,
    title: 220,
    type: 104,
    method: 96,
    requestUri: 280,
    user: 180,
    remoteAddr: 160
};

interface SystemLogFilters {
    beginDate: string;
    endDate: string;
    remoteAddr: string;
    requestUri: string;
    userLoginName: string;
    userName: string;
}

const DEFAULT_SYSTEM_LOG_FILTERS: SystemLogFilters = {
    beginDate: "",
    endDate: "",
    remoteAddr: "",
    requestUri: "",
    userLoginName: "",
    userName: ""
};

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
    const [query, setQuery] = useState<LogPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<SystemLogFilters>(DEFAULT_SYSTEM_LOG_FILTERS);
    const hasActiveFilters = Boolean(
        filters.beginDate.trim() ||
        filters.endDate.trim() ||
        filters.remoteAddr.trim() ||
        filters.requestUri.trim() ||
        filters.userLoginName.trim() ||
        filters.userName.trim()
    );

    const logQuery = useQuery({
        queryKey: ["system-log", "page", query],
        queryFn: () => service.pageLogs(query),
        retry: false
    });
    const logPage = logQuery.data;
    const logs = useMemo(() => logPage?.records || [], [logPage?.records]);
    const totalCount = logPage?.count ?? logPage?.totalCount ?? 0;
    const currentPageNo = logPage?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = logPage?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;

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

    const searchLogs = (value: string) => {
        setSearchText(value);
        updateQuery({ title: normalizeSearch(value) });
    };

    const applyFilters = () => {
        updateQuery({
            beginDate: normalizeSearch(filters.beginDate),
            endDate: normalizeSearch(filters.endDate),
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
                <Space size={7}>
                    <ClockCircleOutlined className="system-log-time-icon" />
                    <span>{createDate}</span>
                </Space>
            )
        },
        {
            title: "标题",
            dataIndex: "title",
            key: "title",
            width: DEFAULT_COLUMN_WIDTHS.title,
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
                requestUri ? <Text code>{requestUri}</Text> : null
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
        <SandwishListPage<LogRecord>
            pageClassName="system-log-page"
            title="系统日志"
            description="查看后台操作日志、请求记录和审计线索。"
            subjectName="日志"
            enableFilter
            enableSearch
            searchShortcut="⌘K"
            searchValue={searchText}
            searchPlaceholder="搜索日志标题..."
            onSearchChange={searchLogs}
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
                    name: "beginDate",
                    label: "开始时间",
                    render: () => (
                        <Input
                            allowClear
                            placeholder="2026-05-14 00:00:00"
                            value={filters.beginDate}
                            onChange={(event) =>
                                setFilters((currentFilters) => ({
                                    ...currentFilters,
                                    beginDate: event.target.value
                                }))
                            }
                        />
                    )
                },
                {
                    name: "endDate",
                    label: "结束时间",
                    render: () => (
                        <Input
                            allowClear
                            placeholder="2026-05-14 23:59:59"
                            value={filters.endDate}
                            onChange={(event) =>
                                setFilters((currentFilters) => ({
                                    ...currentFilters,
                                    endDate: event.target.value
                                }))
                            }
                        />
                    )
                }
            ]}
            onFilterApply={applyFilters}
            onFilterReset={resetFilters}
            pageActions={
                <Button icon={<ReloadOutlined />} onClick={() => logQuery.refetch()}>
                    刷新
                </Button>
            }
            rowKey="id"
            className="system-log-table"
            columns={columns}
            dataSource={logs}
            loading={logQuery.isFetching}
            scroll={{ x: 1220 }}
            expandable={{
                expandedRowRender: (log) => (
                    <div className="system-log-detail">
                        <div>
                            <span>请求参数</span>
                            <Text>{log.requestParams || ""}</Text>
                        </div>
                        <div>
                            <span>User-Agent</span>
                            <Text>{log.userAgent || ""}</Text>
                        </div>
                        <div>
                            <span>部门</span>
                            <Text>
                                {log.createUser?.department?.namePath ||
                                    log.createUser?.department?.name ||
                                    ""}
                            </Text>
                        </div>
                        <div>
                            <span>备注</span>
                            <Text>{log.remarks || ""}</Text>
                        </div>
                    </div>
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
            locale={{
                emptyText: logQuery.isError
                    ? "系统日志加载失败，请确认权限和接口状态。"
                    : "暂无系统日志"
            }}
        />
    );
};
