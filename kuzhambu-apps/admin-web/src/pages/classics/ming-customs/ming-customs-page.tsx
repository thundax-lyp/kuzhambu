import { useQuery } from "@tanstack/react-query";
import { Typography } from "antd";
import { useMemo, useState } from "react";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import * as service from "./ming-customs-service";
import type { MingCustomsQuery } from "./ming-customs-service";
import type { MingCustomsRecord } from "./ming-customs-types";
import "./ming-customs-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    title: 260,
    category: 140,
    chapter: 160,
    section: 140,
    visibility: 120,
    summary: 320
};

const visibilityLabels: Record<string, string> = {
    PUBLIC: "公开",
    PRIVATE: "私有"
};

const visibilityTagType = (visibility?: string | null) => {
    return visibility === "PUBLIC" ? "success" : "neutral";
};

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

export const MingCustomsPage = () => {
    const [query, setQuery] = useState<MingCustomsQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [searchText, setSearchText] = useState("");

    const mingCustomsQuery = useQuery({
        queryKey: ["ming-customs", "page", query],
        queryFn: () => service.page(query),
        retry: false
    });
    const pageResult = mingCustomsQuery.data;
    const records = useMemo(() => pageResult?.records || [], [pageResult?.records]);
    const totalCount = pageResult?.count ?? pageResult?.totalCount ?? 0;
    const currentPageNo = pageResult?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = pageResult?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;

    const searchMingCustoms = (value: string) => {
        setSearchText(value);
        setQuery((currentQuery) => ({
            ...currentQuery,
            keyword: normalizeSearch(value),
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const columns: KuzhambuTableProps<MingCustomsRecord>["columns"] = [
        {
            title: "标题",
            dataIndex: "title",
            key: "title",
            width: DEFAULT_COLUMN_WIDTHS.title,
            render: (title?: string | null) => <Text strong>{title || "未命名条目"}</Text>
        },
        {
            title: "分类",
            dataIndex: "category",
            key: "category",
            width: DEFAULT_COLUMN_WIDTHS.category,
            render: (category?: string | null) =>
                category ? <KuzhambuTag type="info">{category}</KuzhambuTag> : "未分类"
        },
        {
            title: "章节",
            dataIndex: "chapter",
            key: "chapter",
            width: DEFAULT_COLUMN_WIDTHS.chapter,
            render: (chapter?: string | null) => chapter || "未填写"
        },
        {
            title: "小节",
            dataIndex: "section",
            key: "section",
            width: DEFAULT_COLUMN_WIDTHS.section,
            render: (section?: string | null) => section || "未填写"
        },
        {
            title: "可见性",
            dataIndex: "visibility",
            key: "visibility",
            width: DEFAULT_COLUMN_WIDTHS.visibility,
            render: (visibility?: string | null) => (
                <KuzhambuTag type={visibilityTagType(visibility)}>
                    {visibility ? (visibilityLabels[visibility] ?? visibility) : "未设置"}
                </KuzhambuTag>
            )
        },
        {
            title: "摘要",
            dataIndex: "summary",
            key: "summary",
            width: DEFAULT_COLUMN_WIDTHS.summary,
            ellipsis: true,
            render: (summary?: string | null) => summary || <Text type="secondary">暂无摘要</Text>
        }
    ];

    return (
        <KuzhambuListPage<MingCustomsRecord>
            pageClassName="ming-customs-page"
            title="明代习俗"
            description="明代习俗专题条目治理入口。"
            subjectName="明代习俗"
            enableSearch
            searchValue={searchText}
            onSearchChange={searchMingCustoms}
            rowKey="id"
            loading={mingCustomsQuery.isLoading}
            dataSource={records}
            columns={columns}
            pagination={{
                current: currentPageNo,
                pageSize: currentPageSize,
                total: totalCount,
                onChange: (pageNo, pageSize) =>
                    setQuery((currentQuery) => ({
                        ...currentQuery,
                        pageNo,
                        pageSize
                    }))
            }}
            ariaLabel="明代习俗表格"
        />
    );
};
