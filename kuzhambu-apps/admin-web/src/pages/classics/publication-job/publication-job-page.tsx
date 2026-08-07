import { ReloadOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import dayjs from "dayjs";
import { useEffect, useMemo, useState } from "react";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuListPage,
    KuzhambuTable,
    type KuzhambuTableProps,
    KuzhambuTag
} from "@/components";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { PublicationJobDetailDrawer } from "./publication-job-detail-drawer";
import {
    PUBLICATION_JOB_RESULT_TAG_TYPES,
    readPublicationContentTypeLabel,
    readPublicationJobResultLabel,
    readPublicationJobStatusLabel,
    readPublicationJobTypeLabel
} from "./publication-job-constants";
import * as service from "./publication-job-service";
import type { ClassicsPublicationJobQuery } from "./publication-job-service";
import type { ClassicsPublicationJobRecord } from "./publication-job-types";

import "./publication-job-page.css";

const normalizeKeyword = (value: string) => value.trim() || undefined;
const SEARCH_DEBOUNCE_MS = 500;
const formatTime = (value?: string | null) =>
    value ? dayjs(value).format("YYYY-MM-DD HH:mm:ss") : "-";
const middleCell = () => ({ className: "publication-job-table-cell" });

export const PublicationJobPage = () => {
    const [searchText, setSearchText] = useState("");
    const [query, setQuery] = useState<ClassicsPublicationJobQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [detailJobId, setDetailJobId] = useState<string | null>(null);
    const publicationJobPageQuery = useQuery({
        queryKey: ["classics", "publication-jobs", "page", query],
        queryFn: () => service.page(query),
        retry: false
    });
    const pageResult = publicationJobPageQuery.data;
    const records = useMemo(() => pageResult?.records || [], [pageResult?.records]);
    const columns = useMemo<KuzhambuTableProps<ClassicsPublicationJobRecord>["columns"]>(
        () => [
            {
                title: "稿件",
                key: "content",
                onCell: middleCell,
                render: (_, job) =>
                    `${readPublicationContentTypeLabel(job.contentType)}｜${job.contentTitleSnapshot || `#${job.contentId}`}`
            },
            {
                title: "动作",
                dataIndex: "jobType",
                width: 80,
                onCell: middleCell,
                render: (value: ClassicsPublicationJobRecord["jobType"]) => (
                    <KuzhambuTag type="accent">{readPublicationJobTypeLabel(value)}</KuzhambuTag>
                )
            },
            {
                title: "结果",
                dataIndex: "jobResultStatus",
                width: 100,
                onCell: middleCell,
                render: (value: ClassicsPublicationJobRecord["jobResultStatus"]) => (
                    <KuzhambuTag type={PUBLICATION_JOB_RESULT_TAG_TYPES[value]}>
                        {readPublicationJobResultLabel(value)}
                    </KuzhambuTag>
                )
            },
            {
                title: "里程碑",
                dataIndex: "jobStatus",
                width: 180,
                onCell: middleCell,
                render: (value: ClassicsPublicationJobRecord["jobStatus"]) => (
                    <KuzhambuTag type="info">{readPublicationJobStatusLabel(value)}</KuzhambuTag>
                )
            },
            {
                title: "尝试次数",
                key: "attempts",
                width: 100,
                onCell: middleCell,
                render: (_, job) => `${job.attemptCount}/${job.maxAttempts}`
            },
            {
                title: "请求时间",
                dataIndex: "requestedAt",
                width: 180,
                onCell: middleCell,
                render: formatTime
            },
            {
                key: "actions",
                fixed: "right",
                options: (job) => [
                    {
                        key: "view",
                        text: "查看",
                        testId: "classics-publication-jobs-view-button",
                        onClick: () => setDetailJobId(job.id)
                    }
                ]
            }
        ],
        []
    );

    useEffect(() => {
        const timeoutId = window.setTimeout(() => {
            setQuery((currentQuery) => ({
                ...currentQuery,
                keyword: normalizeKeyword(searchText),
                pageNo: DEFAULT_PAGE_NO
            }));
        }, SEARCH_DEBOUNCE_MS);
        return () => window.clearTimeout(timeoutId);
    }, [searchText]);

    const table = (
        <KuzhambuTable<ClassicsPublicationJobRecord>
            dataSource={records}
            rowKey="id"
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
            loading={publicationJobPageQuery.isFetching}
            ariaLabel="发布任务列表"
        />
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
                content={
                    <>
                        {publicationJobPageQuery.isError ? (
                            <KuzhambuAlert
                                showIcon
                                type="error"
                                title="发布任务加载失败"
                                description={
                                    publicationJobPageQuery.error instanceof Error
                                        ? publicationJobPageQuery.error.message
                                        : "请稍后重试"
                                }
                                action={
                                    <KuzhambuButton
                                        ariaLabel="重试加载发布任务"
                                        testId="classics-publication-jobs-retry-button"
                                        onClick={() => void publicationJobPageQuery.refetch()}
                                    >
                                        重试
                                    </KuzhambuButton>
                                }
                            />
                        ) : null}
                        {pageResult || !publicationJobPageQuery.isError ? table : null}
                    </>
                }
            />
            <PublicationJobDetailDrawer
                jobId={detailJobId}
                open={detailJobId !== null}
                onClose={() => setDetailJobId(null)}
            />
        </>
    );
};
