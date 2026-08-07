import { useMutation, useQuery } from "@tanstack/react-query";
import { DatePicker, Input, Tag, Typography } from "antd";
import type { Dayjs } from "dayjs";
import { useState } from "react";
import {
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuSpace,
    KuzhambuTable,
    type KuzhambuTableProps
} from "@/components";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import * as service from "@/pages/discovery/qa-console/qa-console-service";
import type { DiscoveryQaSessionDetailRecord } from "@/pages/discovery/qa-console/qa-console-types";
import * as currentUserService from "@/service/current-user-service";
import { QaSessionDetailDrawer } from "@/pages/discovery/qa-console/qa-session-detail-drawer";

const { Text } = Typography;
const { RangePicker } = DatePicker;
const DEFAULT_PAGE_SIZE = 10;

const formatSessionStatus = (value?: string | null) => {
    if (value === "OPEN") {
        return "打开";
    }
    if (value === "REMOVED") {
        return "已删除";
    }
    return value ?? "-";
};

const formatDate = (value?: number | string | null) => {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    const date = typeof value === "number" ? new Date(value) : new Date(value);
    if (Number.isNaN(date.getTime())) {
        return String(value);
    }
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
};

const isSameSessionQuery = (
    left: service.DiscoveryQaSessionPageQuery,
    right: service.DiscoveryQaSessionPageQuery
) => JSON.stringify(left) === JSON.stringify(right);

export const QaSessionTable = () => {
    const confirm = useKuzhambuConfirm();
    const [title, setTitle] = useState("");
    const [range, setRange] = useState<[Dayjs | null, Dayjs | null] | null>(null);
    const [query, setQuery] = useState<service.DiscoveryQaSessionPageQuery>({
        openedAtEnd: null,
        openedAtStart: null,
        pageNo: 1,
        pageSize: DEFAULT_PAGE_SIZE,
        title: null
    });
    const [detailSessionId, setDetailSessionId] = useState<string | null>(null);
    const [operationText, setOperationText] = useState<string | null>(null);
    const currentUserQuery = useQuery({
        queryFn: currentUserService.getCurrentUserInfo,
        queryKey: ["current-user", "info"]
    });
    const requesterUserId = currentUserQuery.data?.id ?? null;
    const sessionPageQuery = useQuery({
        queryFn: () => service.pageQaSessions(query),
        queryKey: ["discovery-qa-console", "session-page", query]
    });
    const deleteSessionMutation = useMutation({
        mutationFn: service.deleteQaSession,
        onSuccess: async (_, command) => {
            if (detailSessionId === command.sessionId) {
                setDetailSessionId(null);
            }
            setOperationText(`会话 ${command.sessionId} 已删除`);
            await sessionPageQuery.refetch();
        },
        onError: (error) => {
            setOperationText(error instanceof Error ? error.message : "会话删除失败");
        }
    });
    const exportSessionMutation = useMutation({
        mutationFn: service.createQaSessionExport,
        onSuccess: (record, command) => {
            const exportName = record.filename ?? record.exportStatus ?? "-";
            setOperationText(`会话 ${command.sessionId} 导出已创建：${exportName}`);
        },
        onError: (error) => {
            setOperationText(error instanceof Error ? error.message : "会话导出失败");
        }
    });
    const pageData = sessionPageQuery.data;
    const rows = pageData?.records ?? [];

    const querySessions = () => {
        const nextQuery = {
            openedAtEnd: range?.[1]?.endOf("day").toISOString() ?? null,
            openedAtStart: range?.[0]?.startOf("day").toISOString() ?? null,
            pageNo: 1,
            pageSize: DEFAULT_PAGE_SIZE,
            title: title.trim() || null
        };
        setDetailSessionId(null);
        if (isSameSessionQuery(query, nextQuery)) {
            void sessionPageQuery.refetch();
            return;
        }
        setQuery(nextQuery);
    };

    const confirmDeleteSession = (sessionId: string) => {
        if (requesterUserId === null) {
            setOperationText("当前管理员信息尚未加载完成");
            return;
        }
        confirm.danger({
            title: "删除问答会话",
            message: `确认删除会话 ${sessionId}？`,
            description: "删除后 Portal 不再展示该会话，Admin 仍保留审计查看和导出入口。",
            okText: "删除",
            onConfirm: () => deleteSessionMutation.mutateAsync({ requesterUserId, sessionId })
        });
    };

    const exportSession = (sessionId: string) => {
        if (requesterUserId === null) {
            setOperationText("当前管理员信息尚未加载完成");
            return;
        }
        exportSessionMutation.mutate({ format: "CSV", requesterUserId, sessionId });
    };

    const columns: KuzhambuTableProps<DiscoveryQaSessionDetailRecord>["columns"] = [
        {
            title: "标题",
            dataIndex: "title",
            key: "title",
            render: (value?: string | null) => value ?? "-"
        },
        {
            title: "拥有者",
            dataIndex: "ownerUserId",
            key: "ownerUserId",
            width: 120,
            render: (value?: number | null) => value ?? "-"
        },
        {
            title: "创建时间",
            dataIndex: "openedAt",
            key: "openedAt",
            width: 140,
            render: (value?: number | null) => formatDate(value)
        },
        {
            title: "状态",
            dataIndex: "status",
            key: "status",
            width: 96,
            render: (value?: string | null) => (
                <Tag color={value === "REMOVED" ? "default" : "processing"}>
                    {formatSessionStatus(value)}
                </Tag>
            )
        },
        {
            key: "actions",
            options: (record) => {
                const sessionId = String(record.id ?? "");
                return [
                    {
                        key: "view",
                        text: "查看",
                        testId: "discovery-qa-console-qa-console-view-session-button",
                        onClick: () => setDetailSessionId(sessionId)
                    },
                    {
                        key: "export",
                        text: "导出",
                        testId: "discovery-qa-console-qa-console-export-session-button",
                        disabled: exportSessionMutation.isPending || requesterUserId === null,
                        onClick: () => exportSession(sessionId)
                    },
                    { key: "delete-divider", type: "divider" },
                    {
                        key: "delete",
                        text: "删除",
                        type: "danger",
                        testId: "discovery-qa-console-qa-console-delete-session-button",
                        disabled:
                            record.status === "REMOVED" ||
                            deleteSessionMutation.isPending ||
                            requesterUserId === null,
                        onClick: () => confirmDeleteSession(sessionId)
                    }
                ];
            }
        }
    ];

    return (
        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
            <KuzhambuCard title="会话管理" size="small">
                <KuzhambuSpace align="end" wrap>
                    <label className="qa-console-form-item">
                        <Text type="secondary">标题</Text>
                        <Input
                            allowClear
                            aria-label="标题"
                            value={title}
                            onChange={(event) => setTitle(event.target.value)}
                            style={{ width: 220 }}
                        />
                    </label>
                    <label className="qa-console-form-item">
                        <Text type="secondary">创建时间</Text>
                        <RangePicker aria-label="创建时间" value={range} onChange={setRange} />
                    </label>
                    <KuzhambuButton
                        testId="discovery-qa-console-qa-console-load-session-button"
                        loading={sessionPageQuery.isFetching}
                        onClick={querySessions}
                        type="primary"
                    >
                        查询
                    </KuzhambuButton>
                </KuzhambuSpace>
            </KuzhambuCard>

            <KuzhambuCard className="qa-console-card-spaced" title="会话记录" size="small">
                <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                    {operationText ? <Text type="secondary">{operationText}</Text> : null}
                    <KuzhambuTable
                        ariaLabel="问答会话表格"
                        columns={columns}
                        dataSource={rows}
                        pagination={{
                            current: pageData?.pageNo ?? query.pageNo ?? 1,
                            onChange: (pageNo) => setQuery((current) => ({ ...current, pageNo })),
                            pageSize: DEFAULT_PAGE_SIZE,
                            showTotal: (total) => `共 ${total} 条`,
                            showSizeChanger: false,
                            total: pageData?.totalCount ?? pageData?.count ?? 0
                        }}
                        rowKey={(record) => record.id ?? "-"}
                        loading={sessionPageQuery.isFetching}
                        scroll={{ x: 780 }}
                        size="small"
                    />
                </KuzhambuSpace>
            </KuzhambuCard>
            <QaSessionDetailDrawer
                onClose={() => setDetailSessionId(null)}
                sessionId={detailSessionId}
            />
        </KuzhambuSpace>
    );
};
