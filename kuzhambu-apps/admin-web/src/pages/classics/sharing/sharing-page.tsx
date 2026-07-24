import { ReloadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App } from "antd";
import { useMemo, useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuListPage, KuzhambuButton } from "@/components";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { DEFAULT_FILTERS, type ShareFilters } from "./components/sharing-filter-config";
import { SharingDetailDrawer } from "./components/sharing-detail-drawer";
import { SharingFilterPanel } from "./components/sharing-filter-panel";
import { createSharingTableColumns } from "./components/sharing-table";
import * as shareService from "@/pages/classics/common/classics-share-service";
import type { ClassicsShareQuery } from "@/pages/classics/common/classics-share-service";
import type {
    ClassicsShareLinkStatus,
    ClassicsShareRecord
} from "@/pages/classics/common/classics-share-types";

import "./sharing-page.css";

const toOptionalQueryValue = (value: string) => {
    return value === "ALL" ? undefined : value;
};

const normalizeSearch = (value?: string | null) => {
    const trimmedValue = value?.trim();
    return trimmedValue || undefined;
};

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
    const [detailShareId, setDetailShareId] = useState<number | null>(null);
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
    const shareDetailQuery = useQuery({
        queryKey: ["classics", "shares", "detail", detailShareId],
        queryFn: () => shareService.get(detailShareId || 0),
        enabled: detailShareId !== null,
        retry: false
    });
    const shareAccessRecordPageQuery = useQuery({
        queryKey: [
            "classics",
            "shares",
            "access-records",
            detailShareId,
            accessRecordPageNo,
            accessRecordTargetId,
            query.pageSize
        ],
        queryFn: () =>
            shareService.pageAccessRecords({
                pageNo: accessRecordPageNo,
                pageSize: query.pageSize || DEFAULT_PAGE_SIZE,
                shareLinkId: detailShareId || 0,
                ...(accessRecordTargetId ? { shareTargetId: accessRecordTargetId } : {})
            }),
        enabled: detailShareId !== null,
        retry: false
    });
    const shareListResult = sharePageQuery.data;
    const detailShareRecord = shareDetailQuery.data;
    const accessRecords = useMemo(
        () => shareAccessRecordPageQuery.data?.records || [],
        [shareAccessRecordPageQuery.data?.records]
    );
    const shares = useMemo(() => shareListResult?.records || [], [shareListResult?.records]);

    const shareTotal = shareListResult?.count ?? shareListResult?.totalCount ?? 0;
    const accessTotal =
        shareAccessRecordPageQuery.data?.count ?? shareAccessRecordPageQuery.data?.totalCount ?? 0;

    const currentPageNo = shareListResult?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = shareListResult?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;

    const reloadShareData = async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["classics", "shares", "page"] }),
            queryClient.invalidateQueries({ queryKey: ["classics", "shares", "detail"] }),
            queryClient.invalidateQueries({ queryKey: ["classics", "shares", "access-records"] })
        ]);
    };

    const openShareDetailDrawer = (share: ClassicsShareRecord) => {
        setDetailShareId(share.id);
    };

    const closeShareDetailDrawer = () => {
        setDetailShareId(null);
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
        const isRestore = status === "ACTIVE";
        confirm.danger({
            title: isRestore ? "恢复分享" : "撤销分享",
            message: isRestore
                ? `确认恢复「${share.title || `分享 ${share.id}`}」？恢复后同一分享链接将重新可访问。`
                : `确认撤销「${share.title || `分享 ${share.id}`}」？撤销后 Portal 将不可访问该分享。`,
            okText: isRestore ? "恢复" : "撤销",
            onConfirm: () => updateStatusMutation.mutateAsync({ id: share.id, status })
        });
    };

    const targetRecords = useMemo(
        () => detailShareRecord?.targets || [],
        [detailShareRecord?.targets]
    );

    const shareColumns = createSharingTableColumns({
        onStatusChange: confirmUpdateStatus,
        onView: openShareDetailDrawer
    });

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
                filterFields={SharingFilterPanel({
                    filters,
                    onChange: setFilters
                })}
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
                    <KuzhambuButton
                        testId="classics-sharing-sharing-refresh-button"
                        icon={<ReloadOutlined />}
                        onClick={() => void sharePageQuery.refetch()}
                    >
                        刷新
                    </KuzhambuButton>
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

            <SharingDetailDrawer
                accessLoading={shareAccessRecordPageQuery.isLoading}
                accessPageNo={accessRecordPageNo}
                accessPageSize={query.pageSize || DEFAULT_PAGE_SIZE}
                accessRecords={accessRecords}
                accessTotal={accessTotal}
                detailLoading={shareDetailQuery.isLoading}
                open={Boolean(detailShareId)}
                share={detailShareRecord}
                targetRecords={targetRecords}
                onAccessPageChange={(nextPageNo, nextPageSize) => {
                    setAccessRecordPageNo(nextPageNo);
                    if (nextPageSize) {
                        setQuery((currentQuery) => ({
                            ...currentQuery,
                            pageSize: nextPageSize
                        }));
                    }
                }}
                onClose={closeShareDetailDrawer}
                onRefreshAccessRecords={() => {
                    if (!detailShareRecord?.id) {
                        return;
                    }
                    void shareAccessRecordPageQuery.refetch();
                }}
                onStatusChange={confirmUpdateStatus}
            />
        </>
    );
};
