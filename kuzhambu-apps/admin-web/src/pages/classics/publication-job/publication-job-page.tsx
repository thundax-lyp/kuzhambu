import { ReloadOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import type { ColumnsType } from "antd/es/table";
import type { Key } from "react";
import dayjs from "dayjs";
import { useMemo, useState } from "react";
import { KuzhambuButton, KuzhambuListPage, KuzhambuTag } from "@/components";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { PublicationJobDetailDrawer } from "./publication-job-detail-drawer";
import { readPublicationJobStatusLabel } from "./publication-job-labels";
import * as service from "./publication-job-service";
import type { ClassicsPublicationJobQuery } from "./publication-job-service";
import type { ClassicsPublicationJobRecord } from "./publication-job-types";

import "./publication-job-page.css";

const normalizeKeyword = (value: string) => value.trim() || undefined;
const JOB_TYPE_LABELS = { PUBLISH: "发布", OFFLINE: "下线" } as const;
const RESULT_LABELS = { RUNNING: "执行中", FAILED: "失败", SUCCEEDED: "成功" } as const;
const RESULT_TYPES = { RUNNING: "info", FAILED: "danger", SUCCEEDED: "success" } as const;
const CONTENT_TYPE_LABELS = {
    SANCAI_ENTRY: "三才图会",
    WANGQI_DOCUMENT: "王琪",
    MING_CUSTOMS: "明"
} as const;
const formatTime = (value?: string | null) =>
    value ? dayjs(value).format("YYYY-MM-DD HH:mm:ss") : "-";

export const PublicationJobPage = () => {
    const [searchText, setSearchText] = useState("");
    const [query, setQuery] = useState<ClassicsPublicationJobQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [detailJobId, setDetailJobId] = useState<string | null>(null);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const publicationJobPageQuery = useQuery({
        queryKey: ["classics", "publication-jobs", "page", query],
        queryFn: () => service.page(query),
        retry: false
    });
    const publicationJobDetailQuery = useQuery({
        queryKey: ["classics", "publication-jobs", "detail", detailJobId],
        queryFn: () => service.get({ id: detailJobId || "" }),
        enabled: detailJobId !== null,
        retry: false
    });
    const pageResult = publicationJobPageQuery.data;
    const records = useMemo(() => pageResult?.records || [], [pageResult?.records]);
    const columns = useMemo<ColumnsType<ClassicsPublicationJobRecord>>(
        () => [
            {
                title: "稿件",
                key: "content",
                width: 120,
                render: (_, job) =>
                    `${CONTENT_TYPE_LABELS[job.contentType]}｜${job.contentTitleSnapshot || `#${job.contentId}`}`
            },
            {
                title: "动作",
                dataIndex: "jobType",
                width: 80,
                render: (value: ClassicsPublicationJobRecord["jobType"]) => (
                    <KuzhambuTag type="accent">{JOB_TYPE_LABELS[value]}</KuzhambuTag>
                )
            },
            {
                title: "结果",
                dataIndex: "jobResultStatus",
                width: 60,
                render: (value: ClassicsPublicationJobRecord["jobResultStatus"]) => (
                    <KuzhambuTag type={RESULT_TYPES[value]}>{RESULT_LABELS[value]}</KuzhambuTag>
                )
            },
            {
                title: "里程碑",
                dataIndex: "jobStatus",
                width: 80,
                render: (value: ClassicsPublicationJobRecord["jobStatus"]) => (
                    <KuzhambuTag type="info">{readPublicationJobStatusLabel(value)}</KuzhambuTag>
                )
            },
            {
                title: "尝试次数",
                key: "attempts",
                width: 60,
                render: (_, job) => `${job.attemptCount}/${job.maxAttempts}`
            },
            {
                title: "请求时间",
                dataIndex: "requestedAt",
                width: 80,
                render: formatTime
            },
            {
                key: "actions",
                fixed: "right",
                render: (_, job) => (
                    <KuzhambuButton
                        testId="classics-publication-jobs-view-button"
                        type="link"
                        onClick={() => setDetailJobId(job.id)}
                    >
                        查看
                    </KuzhambuButton>
                )
            }
        ],
        []
    );

    return (
        <>
            <KuzhambuListPage<ClassicsPublicationJobRecord>
                pageClassName="publication-job-page"
                title="发布任务"
                description="查看稿件发布与下线任务的执行里程碑、外部引用和清理状态。"
                subjectName="发布任务"
                enableSearch
                searchPlaceholder="搜索稿件标题或任务 ID"
                searchValue={searchText}
                onSearchChange={(value) => {
                    setSearchText(value);
                    setQuery((currentQuery) => ({
                        ...currentQuery,
                        keyword: normalizeKeyword(value),
                        pageNo: DEFAULT_PAGE_NO
                    }));
                }}
                pageActions={
                    <KuzhambuButton
                        testId="classics-publication-jobs-refresh-button"
                        icon={<ReloadOutlined />}
                        loading={publicationJobPageQuery.isFetching}
                        onClick={() => void publicationJobPageQuery.refetch()}
                    >
                        刷新
                    </KuzhambuButton>
                }
                dataSource={records}
                rowKey="id"
                rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys
                }}
                pagination={{
                    current: pageResult?.pageNo || query.pageNo,
                    pageSize: pageResult?.pageSize || query.pageSize,
                    total: pageResult?.count ?? pageResult?.totalCount ?? 0,
                    pageSizeOptions: ["10", "20", "50", "100"],
                    showSizeChanger: true,
                    onChange: (pageNo, pageSize) =>
                        setQuery((currentQuery) => ({ ...currentQuery, pageNo, pageSize }))
                }}
                columns={columns}
                loading={publicationJobPageQuery.isLoading}
                scroll={{ x: 1280 }}
                ariaLabel="发布任务列表"
            />
            <PublicationJobDetailDrawer
                job={publicationJobDetailQuery.data}
                loading={publicationJobDetailQuery.isLoading}
                open={detailJobId !== null}
                onClose={() => setDetailJobId(null)}
            />
        </>
    );
};
