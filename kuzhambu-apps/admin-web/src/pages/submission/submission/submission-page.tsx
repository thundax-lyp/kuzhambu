import { DeleteOutlined, ReloadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Select, Space, Typography } from "antd";
import { useMemo, useState } from "react";
import type { Key } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import type { KuzhambuTableProps, KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { SubmissionDetail } from "./components/submission-detail";
import { SubmissionEdit } from "./components/submission-edit";
import * as service from "./submission-service";
import type {
    SubmissionPageQuery,
    SubmissionSaveCommand,
    SubmissionStatus
} from "./submission-service";
import type { SubmissionRecord } from "./submission-types";
import "./submission-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    title: 300,
    status: 120,
    submittedAt: 180,
    images: 180
};

interface SubmissionFilters {
    status: SubmissionStatus | "ALL";
}

const DEFAULT_SUBMISSION_FILTERS: SubmissionFilters = {
    status: "ALL"
};

const submissionStatusLabels: Record<SubmissionStatus, string> = {
    SUBMITTED: "已提交",
    APPROVED: "已通过",
    REJECTED: "已驳回",
    CLOSED: "已关闭"
};

const submissionStatusOptions: Array<{ label: string; value: SubmissionStatus }> = [
    { value: "SUBMITTED", label: submissionStatusLabels.SUBMITTED },
    { value: "APPROVED", label: submissionStatusLabels.APPROVED },
    { value: "REJECTED", label: submissionStatusLabels.REJECTED },
    { value: "CLOSED", label: submissionStatusLabels.CLOSED }
];

const submissionStatusActions: Record<
    SubmissionStatus,
    Array<{
        key: string;
        text: string;
        status: SubmissionStatus;
        type?: "text" | "warning";
    }>
> = {
    SUBMITTED: [
        { key: "approve", text: "通过", status: "APPROVED" },
        { key: "reject", text: "驳回", status: "REJECTED", type: "warning" },
        { key: "close", text: "关闭", status: "CLOSED", type: "warning" }
    ],
    APPROVED: [{ key: "close", text: "关闭", status: "CLOSED", type: "warning" }],
    REJECTED: [
        { key: "reopen", text: "重开", status: "SUBMITTED" },
        { key: "close", text: "关闭", status: "CLOSED", type: "warning" }
    ],
    CLOSED: []
};

const readStatusFilterValue = (value: SubmissionStatus | "ALL") => {
    return value === "ALL" ? undefined : value;
};

const readSubmissionStatus = (status?: string | null) => {
    return status && status in submissionStatusLabels ? (status as SubmissionStatus) : null;
};

const readStatusLabel = (status?: string | null) => {
    const submissionStatus = readSubmissionStatus(status);
    return submissionStatus ? submissionStatusLabels[submissionStatus] : status || "未知";
};

const statusTagType = (status?: string | null) => {
    if (status === "SUBMITTED") {
        return "info";
    }
    if (status === "APPROVED") {
        return "success";
    }
    if (status === "REJECTED") {
        return "danger";
    }
    return "neutral";
};

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

const sortByMove = (
    submissions: SubmissionRecord[],
    sourceSubmission: SubmissionRecord,
    targetSubmission: SubmissionRecord,
    position: KuzhambuTableSortPosition
) => {
    const sourceIndex = submissions.findIndex(
        (submission) => submission.id === sourceSubmission.id
    );
    const targetIndex = submissions.findIndex(
        (submission) => submission.id === targetSubmission.id
    );
    if (sourceIndex < 0 || targetIndex < 0) {
        return submissions;
    }

    const nextSubmissions = [...submissions];
    const [movedSubmission] = nextSubmissions.splice(sourceIndex, 1);
    const nextTargetIndex = nextSubmissions.findIndex(
        (submission) => submission.id === targetSubmission.id
    );
    nextSubmissions.splice(
        position === "before" ? nextTargetIndex : nextTargetIndex + 1,
        0,
        movedSubmission
    );
    return nextSubmissions;
};

export const SubmissionPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const canEditSubmission = hasPermission("submission:submission:edit");
    const [query, setQuery] = useState<SubmissionPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE,
        sortDirection: "ASC"
    });
    const [filters, setFilters] = useState<SubmissionFilters>(DEFAULT_SUBMISSION_FILTERS);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const [editorOpen, setEditorOpen] = useState(false);
    const [detailSubmission, setDetailSubmission] = useState<SubmissionRecord | null>(null);
    const hasSelectedSubmissions = selectedRowKeys.length > 0;
    const hasActiveFilters = Boolean(filters.status !== "ALL");

    const submissionQuery = useQuery({
        queryKey: ["submission", "page", query],
        queryFn: () => service.pageSubmissions(query),
        retry: false
    });
    const submissionPage = submissionQuery.data;
    const submissions = useMemo(() => submissionPage?.records || [], [submissionPage?.records]);
    const totalCount = submissionPage?.count ?? submissionPage?.totalCount ?? 0;
    const currentPageNo = submissionPage?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = submissionPage?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;

    const invalidateSubmissionPage = async () => {
        await queryClient.invalidateQueries({ queryKey: ["submission", "page"] });
    };

    const createMutation = useMutation({
        mutationFn: service.createSubmission,
        onSuccess: async () => {
            setEditorOpen(false);
            await invalidateSubmissionPage();
            messageApi.success("提交内容已创建");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "创建失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: service.removeSubmissions,
        onSuccess: async () => {
            setSelectedRowKeys([]);
            await invalidateSubmissionPage();
            messageApi.success("提交内容已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });

    const statusMutation = useMutation({
        mutationFn: service.changeSubmissionStatus,
        onSuccess: async () => {
            await invalidateSubmissionPage();
            messageApi.success("提交状态已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "状态更新失败");
        }
    });

    const sortMutation = useMutation({
        mutationFn: service.sortSubmissions,
        onSuccess: async () => {
            await invalidateSubmissionPage();
            messageApi.success("提交顺序已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "排序失败");
        }
    });

    const updateQuery = (values: Partial<SubmissionPageQuery>) => {
        setSelectedRowKeys([]);
        setQuery((currentQuery) => {
            const nextQuery = { ...currentQuery, ...values };
            return {
                status: nextQuery.status,
                submittedAtBegin: nextQuery.submittedAtBegin,
                submittedAtEnd: nextQuery.submittedAtEnd,
                sortDirection: nextQuery.sortDirection || "ASC",
                pageNo: values.pageNo || DEFAULT_PAGE_NO,
                pageSize: values.pageSize || currentQuery.pageSize || DEFAULT_PAGE_SIZE
            };
        });
    };

    const applyFilters = () => {
        updateQuery({
            status: readStatusFilterValue(filters.status)
        });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_SUBMISSION_FILTERS);
        updateQuery({
            status: undefined
        });
    };

    const openCreateEditor = () => {
        setEditorOpen(true);
    };

    const closeEditor = () => {
        if (createMutation.isPending) {
            return;
        }
        setEditorOpen(false);
    };

    const saveSubmission = (request: SubmissionSaveCommand) => {
        createMutation.mutate(request);
    };

    const confirmDelete = (ids: string[]) => {
        confirm.danger({
            title: "删除提交内容",
            message: `确认删除 ${ids.length} 条提交内容？`,
            description: "图片会解除业务绑定，存储对象由存储模块清理。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(ids)
        });
    };

    const moveSubmission = (
        sourceSubmission: SubmissionRecord,
        targetSubmission: SubmissionRecord,
        position: KuzhambuTableSortPosition
    ) => {
        if (!canEditSubmission || sourceSubmission.id === targetSubmission.id) {
            return;
        }
        const nextSubmissions = sortByMove(
            submissions,
            sourceSubmission,
            targetSubmission,
            position
        );
        sortMutation.mutate({
            orderedIds: nextSubmissions.map((submission) => submission.id),
            sortDirection: query.sortDirection || "ASC"
        });
    };

    const changeStatus = (submission: SubmissionRecord, status: SubmissionStatus) => {
        if (!canEditSubmission || submission.status === status) {
            return;
        }
        statusMutation.mutate({
            id: submission.id,
            status
        });
    };

    const readStatusActions = (submission: SubmissionRecord) => {
        const currentStatus = readSubmissionStatus(submission.status);
        if (!currentStatus) {
            return [];
        }
        return submissionStatusActions[currentStatus].map((action) => ({
            key: action.key,
            text: action.text,
            type: action.type,
            ariaLabel: `${action.text} ${submission.title}`,
            disabled: !canEditSubmission || statusMutation.isPending,
            onClick: () => changeStatus(submission, action.status)
        }));
    };

    const columns: KuzhambuTableProps<SubmissionRecord>["columns"] = [
        {
            title: "内容",
            dataIndex: "title",
            key: "title",
            width: DEFAULT_COLUMN_WIDTHS.title,
            ellipsis: true,
            render: (_, submission) => (
                <div className="submission-title-cell">
                    <Text strong>{submission.title}</Text>
                    <Text className="submission-content-preview" ellipsis>
                        {submission.content}
                    </Text>
                </div>
            )
        },
        {
            title: "状态",
            dataIndex: "status",
            key: "status",
            width: DEFAULT_COLUMN_WIDTHS.status,
            render: (status?: string | null) => (
                <KuzhambuTag type={statusTagType(status)}>{readStatusLabel(status)}</KuzhambuTag>
            )
        },
        {
            title: "时间",
            dataIndex: "submittedAt",
            key: "submittedAt",
            width: DEFAULT_COLUMN_WIDTHS.submittedAt,
            render: (submittedAt?: string | null) => <Text>{formatDateTime(submittedAt)}</Text>
        },
        {
            title: "图片",
            dataIndex: "imageObjectIds",
            key: "images",
            width: DEFAULT_COLUMN_WIDTHS.images,
            render: (imageObjectIds?: string[] | null) => {
                if (!imageObjectIds?.length) {
                    return <Text type="secondary">未上传</Text>;
                }
                return (
                    <Space wrap size={4}>
                        {imageObjectIds.slice(0, 3).map((id) => (
                            <KuzhambuTag key={id}>
                                <span className="submission-image-id">{id}</span>
                            </KuzhambuTag>
                        ))}
                        {imageObjectIds.length > 3 ? (
                            <KuzhambuTag>+{imageObjectIds.length - 3}</KuzhambuTag>
                        ) : null}
                    </Space>
                );
            }
        },
        {
            key: "actions",
            options: (submission) => [
                {
                    key: "view",
                    text: "查看",
                    ariaLabel: `查看 ${submission.title}`,
                    onClick: () => setDetailSubmission(submission)
                },
                ...readStatusActions(submission),
                { type: "divider" as const },
                {
                    key: "delete",
                    text: "删除",
                    type: "danger" as const,
                    ariaLabel: `删除 ${submission.title}`,
                    disabled: !canEditSubmission,
                    onClick: () => confirmDelete([submission.id])
                }
            ]
        }
    ];

    return (
        <>
            <KuzhambuListPage<SubmissionRecord>
                pageClassName="submission-page"
                title="提交内容"
                description="管理第三方通过开放接口提交的标题、正文和图片资料。"
                subjectName="提交内容"
                enableAdd={canEditSubmission}
                addText="新增提交"
                enableFilter
                onAdd={openCreateEditor}
                filterActive={hasActiveFilters}
                filterFields={[
                    {
                        name: "status",
                        label: "状态",
                        render: () => (
                            <Select<SubmissionStatus | "ALL">
                                value={filters.status}
                                options={[
                                    { value: "ALL", label: "全部" },
                                    ...submissionStatusOptions
                                ]}
                                onChange={(status) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        status
                                    }))
                                }
                            />
                        )
                    }
                ]}
                onFilterApply={applyFilters}
                onFilterReset={resetFilters}
                pageActions={
                    <Button
                        icon={<ReloadOutlined />}
                        loading={submissionQuery.isFetching}
                        onClick={() => submissionQuery.refetch()}
                    >
                        刷新
                    </Button>
                }
                selectedCount={selectedRowKeys.length}
                batchActions={
                    <Space wrap>
                        <Button
                            danger
                            icon={<DeleteOutlined />}
                            disabled={!hasSelectedSubmissions || !canEditSubmission}
                            loading={deleteMutation.isPending}
                            onClick={() => confirmDelete(selectedRowKeys.map(String))}
                        >
                            批量删除
                        </Button>
                    </Space>
                }
                rowKey="id"
                className="submission-table"
                columns={columns}
                dataSource={submissions}
                loading={
                    submissionQuery.isFetching || sortMutation.isPending || statusMutation.isPending
                }
                onSort={moveSubmission}
                pagination={{
                    current: currentPageNo,
                    pageSize: currentPageSize,
                    total: totalCount,
                    showTotal: (total) => `${total} 条提交内容`,
                    onChange: (pageNo, pageSize) => updateQuery({ pageNo, pageSize })
                }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys
                }}
                sortable={canEditSubmission}
            />

            <SubmissionEdit
                key={editorOpen ? "create" : "closed"}
                open={editorOpen}
                saving={createMutation.isPending}
                onClose={closeEditor}
                onSave={saveSubmission}
            />

            <SubmissionDetail
                submission={detailSubmission}
                statusLabels={submissionStatusLabels}
                onClose={() => setDetailSubmission(null)}
            />
        </>
    );
};
