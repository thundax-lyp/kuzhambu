import { ReloadOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { KuzhambuButton, KuzhambuListPage } from "@/components";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { PublicationJobDetailDrawer } from "./publication-job-detail-drawer";
import { createPublicationJobTableColumns } from "./publication-job-table";
import * as service from "./publication-jobs-service";
import type { ClassicsPublicationJobQuery } from "./publication-jobs-service";
import type { ClassicsPublicationJobRecord } from "./publication-jobs-types";

import "./publication-jobs-page.css";

const normalizeKeyword = (value: string) => value.trim() || undefined;

export const PublicationJobsPage = () => {
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
    const publicationJobDetailQuery = useQuery({
        queryKey: ["classics", "publication-jobs", "detail", detailJobId],
        queryFn: () => service.get({ id: detailJobId || "" }),
        enabled: detailJobId !== null,
        retry: false
    });
    const pageResult = publicationJobPageQuery.data;
    const records = useMemo(() => pageResult?.records || [], [pageResult?.records]);
    const columns = useMemo(
        () =>
            createPublicationJobTableColumns({
                onView: (job: ClassicsPublicationJobRecord) => setDetailJobId(job.id)
            }),
        []
    );

    return (
        <>
            <KuzhambuListPage<ClassicsPublicationJobRecord>
                pageClassName="publication-jobs-page"
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
