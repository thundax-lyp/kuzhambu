import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import * as shareService from "./share-service";
import type { ClassicsShareSearchQuery } from "./share-types";

const CONTENT_TYPE_OPTIONS = [
    { label: "全部分类", value: "" },
    { label: "三才图会", value: "SANCAI_ENTRY" },
    { label: "万启文档", value: "WANGQI_DOCUMENT" },
    { label: "明代民俗", value: "MING_CUSTOMS" }
];

const CONTENT_TYPE_LABELS = new Map(
    CONTENT_TYPE_OPTIONS.filter((option) => option.value).map((option) => [
        option.value,
        option.label
    ])
);

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "未设置";
    }

    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
};

const toIsoStartOfDay = (value: string) => {
    return value ? new Date(`${value}T00:00:00`).toISOString() : null;
};

const toIsoEndOfDay = (value: string) => {
    return value ? new Date(`${value}T23:59:59`).toISOString() : null;
};

const formatContentType = (value?: string | null) => {
    return value ? (CONTENT_TYPE_LABELS.get(value) ?? value) : "-";
};

export const SharePage = () => {
    const [title, setTitle] = useState("");
    const [contentType, setContentType] = useState("");
    const [issuedAfter, setIssuedAfter] = useState("");
    const [issuedBefore, setIssuedBefore] = useState("");
    const [query, setQuery] = useState<ClassicsShareSearchQuery>({
        pageNo: 1,
        pageSize: 20
    });
    const shareListQuery = useQuery({
        queryFn: () => shareService.listShares(query),
        queryKey: ["classics", "shares", "list", query],
        retry: false
    });
    const records = shareListQuery.data?.records ?? [];
    const resultSummary = useMemo(() => {
        if (shareListQuery.isLoading) {
            return "正在加载分享列表";
        }
        if (shareListQuery.isError) {
            return "分享列表加载失败";
        }
        return `共 ${shareListQuery.data?.totalCount ?? 0} 条公开分享`;
    }, [shareListQuery.data?.totalCount, shareListQuery.isError, shareListQuery.isLoading]);

    const applyFilters = () => {
        setQuery({
            contentType: contentType || null,
            issuedAfter: toIsoStartOfDay(issuedAfter),
            issuedBefore: toIsoEndOfDay(issuedBefore),
            pageNo: 1,
            pageSize: 20,
            title: title.trim() || null
        });
    };

    const resetFilters = () => {
        setTitle("");
        setContentType("");
        setIssuedAfter("");
        setIssuedBefore("");
        setQuery({
            pageNo: 1,
            pageSize: 20
        });
    };

    return (
        <main className="portal-shell">
            <header className="portal-header">
                <div>
                    <p className="portal-kicker">公开分享</p>
                    <h1>分享列表</h1>
                </div>
                <Link className="portal-action" to="/">
                    返回首页
                </Link>
            </header>

            <section className="portal-filter" aria-label="分享筛选">
                <label>
                    <span>标题</span>
                    <input
                        value={title}
                        placeholder="搜索分享标题或内容标题"
                        onChange={(event) => setTitle(event.target.value)}
                    />
                </label>
                <label>
                    <span>分类</span>
                    <select
                        value={contentType}
                        onChange={(event) => setContentType(event.target.value)}
                    >
                        {CONTENT_TYPE_OPTIONS.map((option) => (
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>
                </label>
                <label>
                    <span>开始时间</span>
                    <input
                        type="date"
                        value={issuedAfter}
                        onChange={(event) => setIssuedAfter(event.target.value)}
                    />
                </label>
                <label>
                    <span>结束时间</span>
                    <input
                        type="date"
                        value={issuedBefore}
                        onChange={(event) => setIssuedBefore(event.target.value)}
                    />
                </label>
                <div className="portal-filter-actions">
                    <button type="button" onClick={resetFilters}>
                        重置
                    </button>
                    <button type="button" onClick={applyFilters}>
                        查询
                    </button>
                </div>
            </section>

            <section className="portal-list" aria-label="分享列表">
                <div className="portal-list-summary">{resultSummary}</div>
                {records.length ? (
                    records.map((record) => {
                        const itemKey = `${record.shareLinkId}-${record.priority}`;
                        const itemContent = (
                            <>
                                <div>
                                    <p>{record.shareTitle || "未命名分享"}</p>
                                    <h2>
                                        {record.titleSnapshot || `内容 ${record.contentId || "-"}`}
                                    </h2>
                                </div>
                                <dl>
                                    <div>
                                        <dt>分类</dt>
                                        <dd>{formatContentType(record.contentType)}</dd>
                                    </div>
                                    <div>
                                        <dt>版本</dt>
                                        <dd>{record.contentVersionNo ?? "-"}</dd>
                                    </div>
                                    <div>
                                        <dt>分享时间</dt>
                                        <dd>{formatDateTime(record.issuedAt)}</dd>
                                    </div>
                                    <div>
                                        <dt>过期时间</dt>
                                        <dd>{formatDateTime(record.expiresAt)}</dd>
                                    </div>
                                </dl>
                            </>
                        );

                        return record.shareToken ? (
                            <Link
                                className="portal-list-item"
                                key={itemKey}
                                to={`/share/${record.shareToken}`}
                            >
                                {itemContent}
                            </Link>
                        ) : (
                            <article className="portal-list-item" key={itemKey}>
                                {itemContent}
                            </article>
                        );
                    })
                ) : (
                    <div className="portal-empty">暂无符合条件的公开分享。</div>
                )}
            </section>
        </main>
    );
};
