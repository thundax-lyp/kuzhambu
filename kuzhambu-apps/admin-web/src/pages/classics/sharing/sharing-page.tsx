import { FileSearchOutlined, ReloadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Descriptions, Select, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import * as shareService from "@/pages/classics/common/classics-share-service";
import type { ClassicsShareQuery } from "@/pages/classics/common/classics-share-service";
import type {
    ClassicsShareAccessRecord,
    ClassicsShareContentType,
    ClassicsShareLinkStatus,
    ClassicsShareRecord,
    ClassicsShareTargetRecord,
    ClassicsShareTargetStatus,
    ClassicsShareVisibility
} from "@/pages/classics/common/classics-share-types";
import "./sharing-page.css";

const { Text } = Typography;

type ShareContentTypeFilter = "ALL" | ClassicsShareContentType;
type ShareStatusFilter = "ALL" | ClassicsShareLinkStatus;
type ShareVisibilityFilter = "ALL" | ClassicsShareVisibility;

type ShareStatusTone = "danger" | "neutral" | "success" | "warning";

interface ShareFilters {
    contentType: ShareContentTypeFilter;
    status: ShareStatusFilter;
    visibility: ShareVisibilityFilter;
}

const DEFAULT_FILTERS: ShareFilters = {
    contentType: "ALL",
    status: "ALL",
    visibility: "ALL"
};

const SHARE_CONTENT_TYPE_OPTIONS = [
    { label: "全部", value: "ALL" },
    { label: "王圻文档", value: "WANGQI_DOCUMENT" },
    { label: "三才条目", value: "SANCAI_ENTRY" },
    { label: "明人志异", value: "MING_CUSTOMS" }
];

const SHARE_STATUS_OPTIONS: Array<{ label: string; value: ShareStatusFilter }> = [
    { label: "全部", value: "ALL" },
    { label: "生效", value: "ACTIVE" },
    { label: "已过期", value: "EXPIRED" },
    { label: "已撤销", value: "REVOKED" }
];

const SHARE_VISIBILITY_OPTIONS = [
    { label: "全部", value: "ALL" },
    { label: "公开", value: "PUBLIC" },
    { label: "私有", value: "PRIVATE" }
];

const shareStatusTone: Record<string, ShareStatusTone> = {
    ACTIVE: "success",
    EXPIRED: "warning",
    REVOKED: "danger"
};

const shareTargetStatusTone: Record<string, ShareStatusTone> = {
    AVAILABLE: "success",
    CONTENT_DELETED: "danger"
};

const readShareStatusLabel = (status?: ClassicsShareLinkStatus | string | null) => {
    return (
        {
            ACTIVE: "生效",
            EXPIRED: "已过期",
            REVOKED: "已撤销"
        }[status || ""] || "未知"
    );
};

const toOptionalQueryValue = (value: string) => {
    return value === "ALL" ? undefined : value;
};

const normalizeSearch = (value?: string | null) => {
    const trimmedValue = value?.trim();
    return trimmedValue || undefined;
};

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

const shareTypeLabel = (contentType?: string | null) => {
    if (contentType === "WANGQI_DOCUMENT") {
        return "王圻文档";
    }
    if (contentType === "SANCAI_ENTRY") {
        return "三才条目";
    }
    if (contentType === "MING_CUSTOMS") {
        return "明人志异";
    }
    return "未知类型";
};

const shareRecordLabel = (share: ClassicsShareRecord) => {
    const contentType = share.targets?.[0]?.contentType;
    return `${share.title || `分享 ${share.id}`}-${shareTypeLabel(contentType)}`;
};

const readStatusTagType = (status?: ClassicsShareLinkStatus | string | null) => {
    return shareStatusTone[status || ""] || "neutral";
};

const readTargetStatusLabel = (status?: ClassicsShareTargetStatus | string | null) => {
    return (
        {
            AVAILABLE: "可用",
            CONTENT_DELETED: "内容已删除"
        }[status || ""] || "未知"
    );
};

const readTargetStatusTagType = (status?: ClassicsShareTargetStatus | string | null) => {
    return shareTargetStatusTone[status || ""] || "neutral";
};

const isDeletedShareTarget = (target?: ClassicsShareTargetRecord | null) => {
    return target?.targetStatus === "CONTENT_DELETED";
};

const toShareLinkStatus = (
    status?: ClassicsShareLinkStatus | string | null
): ClassicsShareLinkStatus | undefined => {
    if (status === "ACTIVE" || status === "EXPIRED" || status === "REVOKED") {
        return status;
    }
    return undefined;
};

const readVisibilityLabel = (visibility?: ClassicsShareVisibility | string | null) => {
    return (
        {
            PRIVATE: "私有",
            PUBLIC: "公开"
        }[visibility || ""] || "未知"
    );
};

const readShareTypeByTargets = (targets?: ClassicsShareTargetRecord[] | null) => {
    return shareTypeLabel(targets?.[0]?.contentType);
};

const resolveNextShareStatus = (status?: ClassicsShareLinkStatus | string | null) => {
    return status === "ACTIVE" ? "REVOKED" : "ACTIVE";
};

const statusOptions = [
    { label: "生效", value: "ACTIVE" },
    { label: "已过期", value: "EXPIRED" },
    { label: "已撤销", value: "REVOKED" }
];

export const SharingPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<ShareFilters>(DEFAULT_FILTERS);
    const [query, setQuery] = useState<ClassicsShareQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [selectedShareId, setSelectedShareId] = useState<number | null>(null);
    const [accessRecordPageNo, setAccessRecordPageNo] = useState(DEFAULT_PAGE_NO);
    const [accessRecordTargetId, setAccessRecordTargetId] = useState<number | null>(null);

    const hasActiveFilters = useMemo(() => {
        return (
            searchText.trim() !== "" ||
            filters.status !== "ALL" ||
            filters.visibility !== "ALL" ||
            filters.contentType !== "ALL"
        );
    }, [filters.status, filters.visibility, filters.contentType, searchText]);

    const sharePageQuery = useQuery({
        queryKey: ["classics", "shares", "page", query],
        queryFn: () => shareService.page(query),
        retry: false
    });
    const detailQuery = useQuery({
        queryKey: ["classics", "shares", "detail", selectedShareId],
        queryFn: () => shareService.get(selectedShareId || 0),
        enabled: selectedShareId !== null,
        retry: false
    });
    const accessRecordQuery = useQuery({
        queryKey: [
            "classics",
            "shares",
            "access-records",
            selectedShareId,
            accessRecordPageNo,
            accessRecordTargetId,
            query.pageSize
        ],
        queryFn: () =>
            shareService.pageAccessRecords({
                pageNo: accessRecordPageNo,
                pageSize: query.pageSize || DEFAULT_PAGE_SIZE,
                shareLinkId: selectedShareId || 0,
                ...(accessRecordTargetId ? { shareTargetId: accessRecordTargetId } : {})
            }),
        enabled: selectedShareId !== null,
        retry: false
    });
    const shareListResult = sharePageQuery.data;
    const detailRecord = detailQuery.data;
    const accessRecords = useMemo(
        () => accessRecordQuery.data?.records || [],
        [accessRecordQuery.data?.records]
    );
    const shares = useMemo(() => shareListResult?.records || [], [shareListResult?.records]);

    const shareTotal = shareListResult?.count ?? shareListResult?.totalCount ?? 0;
    const accessTotal = accessRecordQuery.data?.count ?? accessRecordQuery.data?.totalCount ?? 0;

    const currentPageNo = shareListResult?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = shareListResult?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;

    const reloadShareData = async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["classics", "shares", "page"] }),
            queryClient.invalidateQueries({ queryKey: ["classics", "shares", "detail"] }),
            queryClient.invalidateQueries({ queryKey: ["classics", "shares", "access-records"] })
        ]);
    };

    const openShareDetail = (share: ClassicsShareRecord) => {
        setSelectedShareId(share.id);
    };

    const closeShareDetail = () => {
        setSelectedShareId(null);
        setAccessRecordPageNo(DEFAULT_PAGE_NO);
        setAccessRecordTargetId(null);
    };

    const updateStatusMutation = useMutation({
        mutationFn: shareService.updateStatus,
        onSuccess: async () => {
            await reloadShareData();
            messageApi.success("分享状态更新成功");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "分享状态更新失败");
        }
    });

    const confirmUpdateStatus = (
        share: ClassicsShareRecord,
        status: ClassicsShareLinkStatus | string
    ) => {
        confirm.danger({
            title: "更新分享状态",
            message: `确认将「${share.title || `分享 ${share.id}`}」更新为 ${readShareStatusLabel(
                status
            )}？`,
            okText: "更新",
            onConfirm: () => updateStatusMutation.mutateAsync({ id: share.id, status })
        });
    };

    const targetRecords = useMemo(() => detailRecord?.targets || [], [detailRecord?.targets]);

    const shareColumns: KuzhambuTableProps<ClassicsShareRecord>["columns"] = [
        {
            title: "标题",
            dataIndex: "title",
            key: "title",
            width: 220,
            render: (title?: string | null) => title || "-"
        },
        {
            title: "分享类型",
            dataIndex: "targets",
            key: "contentType",
            width: 150,
            render: (_targets, share) => readShareTypeByTargets(share?.targets)
        },
        {
            title: "状态",
            dataIndex: "status",
            key: "status",
            width: 120,
            render: (status?: ClassicsShareLinkStatus | string | null) => (
                <KuzhambuTag type={readStatusTagType(status)}>{status || "UNKNOWN"}</KuzhambuTag>
            )
        },
        {
            title: "可见性",
            dataIndex: "visibility",
            key: "visibility",
            width: 120,
            render: (visibility?: ClassicsShareVisibility | string | null) =>
                readVisibilityLabel(visibility)
        },
        {
            title: "访问次数",
            dataIndex: "accessCount",
            key: "accessCount",
            width: 120,
            render: (value?: number | null) => (typeof value === "number" ? value : "-")
        },
        {
            title: "发布时间",
            dataIndex: "issuedAt",
            key: "issuedAt",
            width: 190,
            render: formatDateTime
        },
        {
            title: "过期时间",
            dataIndex: "expiresAt",
            key: "expiresAt",
            width: 190,
            render: formatDateTime
        },
        {
            key: "actions",
            options: (share) => [
                {
                    key: "detail",
                    text: "查看",
                    ariaLabel: `查看 ${shareRecordLabel(share)}`,
                    onClick: openShareDetail
                },
                { type: "divider" },
                {
                    key: "status",
                    text: "更改状态",
                    ariaLabel: `更改 ${shareRecordLabel(share)} 状态`,
                    onClick: () => {
                        const nextStatus = resolveNextShareStatus(share.status);
                        confirmUpdateStatus(share, nextStatus);
                    }
                }
            ]
        }
    ];

    const targetColumns: KuzhambuTableProps<ClassicsShareTargetRecord>["columns"] = [
        {
            title: "内容类型",
            dataIndex: "contentType",
            key: "contentType",
            width: 150,
            render: (contentType?: string | null) => shareTypeLabel(contentType)
        },
        {
            title: "内容 ID",
            dataIndex: "contentId",
            key: "contentId",
            width: 120,
            render: (contentId?: number | null) => contentId ?? "-"
        },
        {
            title: "标题快照",
            dataIndex: "titleSnapshot",
            key: "titleSnapshot",
            render: (titleSnapshot?: string | null) => titleSnapshot || "-"
        },
        {
            title: "目标状态",
            dataIndex: "targetStatus",
            key: "targetStatus",
            width: 130,
            render: (status?: ClassicsShareTargetStatus | string | null) => (
                <KuzhambuTag type={readTargetStatusTagType(status)}>
                    {readTargetStatusLabel(status)}
                </KuzhambuTag>
            )
        }
    ];

    const accessRecordColumns: KuzhambuTableProps<ClassicsShareAccessRecord>["columns"] = [
        {
            title: "访问时间",
            dataIndex: "accessedAt",
            key: "accessedAt",
            width: 180,
            render: formatDateTime
        },
        {
            title: "结果",
            dataIndex: "accessResult",
            key: "accessResult",
            width: 120,
            render: (result?: string | null) => <Tag>{result || "UNKNOWN"}</Tag>
        },
        {
            title: "客户端快照",
            dataIndex: "clientSnapshot",
            key: "clientSnapshot",
            render: (value?: string | null) => value || "-"
        }
    ];

    return (
        <>
            <KuzhambuListPage<ClassicsShareRecord>
                pageClassName="sharing-page"
                title="分享管理"
                description="查看 Classics 分享链接、目标映射与访问记录，并支持状态回收管理。"
                subjectName="分享链接"
                enableSearch
                enableFilter
                searchPlaceholder="搜索分享标题"
                searchValue={searchText}
                filterActive={hasActiveFilters}
                filterFields={[
                    {
                        name: "contentType",
                        label: "内容类型",
                        render: () => (
                            <Select
                                aria-label="分享内容类型"
                                value={filters.contentType}
                                options={SHARE_CONTENT_TYPE_OPTIONS}
                                onChange={(contentType) =>
                                    setFilters((current) => ({
                                        ...current,
                                        contentType
                                    }))
                                }
                            />
                        )
                    },
                    {
                        name: "status",
                        label: "分享状态",
                        render: () => (
                            <Select
                                aria-label="分享状态"
                                value={filters.status}
                                options={SHARE_STATUS_OPTIONS}
                                onChange={(status) =>
                                    setFilters((current) => ({
                                        ...current,
                                        status: status || "ALL"
                                    }))
                                }
                            />
                        )
                    },
                    {
                        name: "visibility",
                        label: "可见性",
                        render: () => (
                            <Select
                                aria-label="分享可见性"
                                value={filters.visibility}
                                options={SHARE_VISIBILITY_OPTIONS}
                                onChange={(visibility) =>
                                    setFilters((current) => ({
                                        ...current,
                                        visibility: visibility || "ALL"
                                    }))
                                }
                            />
                        )
                    }
                ]}
                onFilterApply={() => {
                    setQuery((currentQuery) => ({
                        ...currentQuery,
                        contentType: toOptionalQueryValue(filters.contentType),
                        pageNo: DEFAULT_PAGE_NO,
                        status: toOptionalQueryValue(filters.status),
                        title: normalizeSearch(searchText),
                        visibility: toOptionalQueryValue(filters.visibility)
                    }));
                }}
                onFilterReset={() => {
                    setFilters(DEFAULT_FILTERS);
                    setSearchText("");
                    setQuery((currentQuery) => ({
                        ...currentQuery,
                        contentType: undefined,
                        pageNo: DEFAULT_PAGE_NO,
                        status: undefined,
                        visibility: undefined,
                        title: undefined
                    }));
                }}
                onSearchChange={(value) => {
                    setSearchText(value);
                    setQuery((currentQuery) => ({
                        ...currentQuery,
                        pageNo: DEFAULT_PAGE_NO,
                        title: normalizeSearch(value)
                    }));
                }}
                pageActions={
                    <Button icon={<ReloadOutlined />} onClick={() => void sharePageQuery.refetch()}>
                        刷新
                    </Button>
                }
                dataSource={shares}
                rowKey="id"
                rowSelection={undefined}
                pagination={{
                    current: currentPageNo,
                    pageSize: currentPageSize,
                    total: shareTotal,
                    pageSizeOptions: ["10", "20", "50", "100"],
                    onChange: (nextPageNo, nextPageSize) =>
                        setQuery((currentQuery) => ({
                            ...currentQuery,
                            pageNo: nextPageNo,
                            pageSize: nextPageSize
                        })),
                    showSizeChanger: true
                }}
                columns={shareColumns}
                loading={sharePageQuery.isLoading}
                scroll={{ x: 1120 }}
                ariaLabel="分享列表"
            />

            <KuzhambuDrawer
                destroyOnClose
                open={Boolean(selectedShareId)}
                title="分享详情"
                size="middle"
                onClose={closeShareDetail}
            >
                <KuzhambuSpace orientation="vertical" size="middle" style={{ width: "100%" }}>
                    <Descriptions title="分享信息" bordered size="small" column={2}>
                        <Descriptions.Item label="标题">
                            {detailRecord?.title || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="分享码">
                            {detailRecord?.shareToken || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="状态">
                            <KuzhambuTag type={readStatusTagType(detailRecord?.status)}>
                                {detailRecord?.status || "UNKNOWN"}
                            </KuzhambuTag>
                        </Descriptions.Item>
                        <Descriptions.Item label="可见性">
                            {readVisibilityLabel(detailRecord?.visibility)}
                        </Descriptions.Item>
                        <Descriptions.Item label="可用访问次数">
                            {detailRecord?.accessCount ?? 0}
                        </Descriptions.Item>
                        <Descriptions.Item label="发布时间">
                            {formatDateTime(detailRecord?.issuedAt)}
                        </Descriptions.Item>
                        <Descriptions.Item label="过期时间">
                            {formatDateTime(detailRecord?.expiresAt)}
                        </Descriptions.Item>
                        <Descriptions.Item label="分享链接">
                            {detailRecord?.shareUrl || "-"}
                        </Descriptions.Item>
                    </Descriptions>

                    <KuzhambuSpace align="end">
                        <Select<ClassicsShareLinkStatus>
                            value={toShareLinkStatus(detailRecord?.status)}
                            placeholder="更新状态"
                            aria-label="更新状态"
                            style={{ width: 180 }}
                            onChange={(status) => {
                                if (!detailRecord?.id) {
                                    return;
                                }
                                confirmUpdateStatus(detailRecord, status);
                            }}
                            options={statusOptions}
                        />
                        <Button
                            icon={<FileSearchOutlined />}
                            onClick={() => {
                                if (!detailRecord?.id) {
                                    return;
                                }
                                void accessRecordQuery.refetch();
                            }}
                        >
                            刷新访问记录
                        </Button>
                    </KuzhambuSpace>

                    <Text strong>关联内容</Text>
                    <KuzhambuTable<ClassicsShareTargetRecord>
                        ariaLabel="分享目标列表"
                        rowKey="id"
                        rowSelection={undefined}
                        dataSource={targetRecords}
                        columns={targetColumns}
                        loading={detailQuery.isLoading}
                        rowClassName={(target) =>
                            isDeletedShareTarget(target) ? "sharing-target-row-deleted" : ""
                        }
                        scroll={{ x: 760 }}
                    />

                    <Text strong>访问记录</Text>
                    <KuzhambuTable<ClassicsShareAccessRecord>
                        ariaLabel="访问记录列表"
                        rowKey="id"
                        rowSelection={undefined}
                        dataSource={accessRecords}
                        loading={accessRecordQuery.isLoading}
                        columns={accessRecordColumns}
                        pagination={{
                            current: accessRecordPageNo,
                            pageSize: query.pageSize || DEFAULT_PAGE_SIZE,
                            total: accessTotal,
                            pageSizeOptions: ["10", "20", "50", "100"],
                            onChange: (nextPageNo, nextPageSize) => {
                                setAccessRecordPageNo(nextPageNo);
                                if (nextPageSize) {
                                    setQuery((currentQuery) => ({
                                        ...currentQuery,
                                        pageSize: nextPageSize
                                    }));
                                }
                            },
                            showSizeChanger: true
                        }}
                    />
                </KuzhambuSpace>
            </KuzhambuDrawer>
        </>
    );
};
