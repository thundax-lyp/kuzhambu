import { useState } from "react";
import { useInfiniteQuery, useQuery } from "@tanstack/react-query";
import { BookOpen, ChevronRight, Network } from "lucide-react";
import { useSearchParams } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { SancaiGraphView } from "./sancai-graph-view";
import * as sancaiService from "./sancai-service";
import type { SancaiCategoryRecord, SancaiEntryRecord } from "./sancai-types";

import "./sancai-page.css";

const BOOK_ENTRY_PAGE_SIZE = 100;
const STABLE_BOOK_QUERY_OPTIONS = {
    staleTime: Number.POSITIVE_INFINITY
};
const EMPTY_CATEGORIES: SancaiCategoryRecord[] = [];
const EMPTY_ENTRIES: SancaiEntryRecord[] = [];

const readPageTotalCount = (page: { count?: number | null; totalCount?: number | null }) => {
    return page.count ?? page.totalCount ?? null;
};

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

const readEntryIdParam = (searchParams: URLSearchParams) => {
    const value = Number.parseInt(searchParams.get("id") ?? "", 10);
    return Number.isFinite(value) && value > 0 ? value : null;
};

const readEntryImages = (entry?: SancaiEntryRecord | null) => {
    if (!entry) {
        return [];
    }

    const entryImages =
        entry.images
            ?.filter((image) => image.previewUrl)
            .map((image) => ({
                alt: image.title || "三才图会条目图片",
                caption: image.title || "三才图会图版",
                currentUsed: Boolean(image.currentUsed),
                key: String(image.id ?? image.previewUrl),
                url: image.previewUrl || ""
            })) || [];
    const visualAssetUrl =
        entry.currentVisualAsset?.generatedPreviewUrl || entry.currentVisualAsset?.sourcePreviewUrl;

    if (!visualAssetUrl || entryImages.some((image) => image.url === visualAssetUrl)) {
        return entryImages;
    }

    return [
        ...entryImages,
        {
            alt: `${readTitle(entry, "条目")}图像`,
            caption: "融合图像",
            currentUsed: entryImages.length === 0,
            key: `visual-${entry.currentVisualAsset?.visualAssetId ?? visualAssetUrl}`,
            url: visualAssetUrl
        }
    ];
};

export const SancaiPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(null);
    const [detailView, setDetailView] = useState<"reading" | "graph">("reading");
    const selectedEntryId = readEntryIdParam(searchParams);

    const categoriesQuery = useQuery({
        queryKey: ["portal", "classics", "sancai", "categories"],
        queryFn: sancaiService.listCategories,
        ...STABLE_BOOK_QUERY_OPTIONS
    });
    const entriesQuery = useInfiniteQuery({
        queryKey: ["portal", "classics", "sancai", "entries", selectedCategoryId],
        queryFn: ({ pageParam }) =>
            sancaiService.pageEntries({
                categoryId: selectedCategoryId,
                pageNo: pageParam,
                pageSize: BOOK_ENTRY_PAGE_SIZE
            }),
        initialPageParam: 1,
        getNextPageParam: (lastPage, allPages) => {
            const currentPageNo = lastPage.pageNo || allPages.length;
            if (lastPage.totalPage && currentPageNo < lastPage.totalPage) {
                return currentPageNo + 1;
            }

            const loadedCount = allPages.reduce(
                (count, page) => count + (page.records?.length || 0),
                0
            );
            const totalCount = readPageTotalCount(lastPage) ?? loadedCount;
            if (loadedCount >= totalCount) {
                return undefined;
            }
            return currentPageNo + 1;
        },
        ...STABLE_BOOK_QUERY_OPTIONS
    });
    const categories = categoriesQuery.data || EMPTY_CATEGORIES;
    const entries = entriesQuery.data?.pages.flatMap((page) => page.records || []) || EMPTY_ENTRIES;
    const loadedEntryCount = entries.length;
    const totalEntryCount = entriesQuery.data?.pages[0]
        ? (readPageTotalCount(entriesQuery.data.pages[0]) ?? loadedEntryCount)
        : loadedEntryCount;
    const activeEntryId = selectedEntryId || entries[0]?.id || null;
    const detailQuery = useQuery({
        queryKey: ["portal", "classics", "sancai", "entry", activeEntryId],
        queryFn: () => sancaiService.getEntry(activeEntryId ?? 0),
        enabled: activeEntryId !== null,
        ...STABLE_BOOK_QUERY_OPTIONS
    });
    const selectedEntry =
        detailQuery.data || entries.find((entry) => entry.id === activeEntryId) || entries[0];
    const selectedEntryImages = readEntryImages(selectedEntry);
    const graphQuery = useQuery({
        queryKey: ["portal", "classics", "sancai", "entry-graph", activeEntryId],
        queryFn: () => sancaiService.getEntryGraph(activeEntryId ?? 0),
        enabled: detailView === "graph" && activeEntryId !== null,
        ...STABLE_BOOK_QUERY_OPTIONS
    });

    const clearSelectedEntryParam = () => {
        const next = new URLSearchParams(searchParams);
        next.delete("id");
        setSearchParams(next);
    };

    const selectCategory = (categoryId: number | null) => {
        setSelectedCategoryId(categoryId);
        clearSelectedEntryParam();
    };

    const selectEntry = (entryId: number) => {
        const next = new URLSearchParams(searchParams);
        next.set("id", String(entryId));
        setSearchParams(next);
    };

    return (
        <main className="sancai-portal-page">
            <section className="sancai-browser" aria-label="三才图会在线展示">
                <aside className="sancai-browser-nav" aria-label="三才图会目录">
                    <div className="sancai-browser-title">
                        <BookOpen aria-hidden="true" size={20} />
                        <div>
                            <p>古籍阅览</p>
                            <h1>三才图会</h1>
                            <span>明刊本图文条目</span>
                        </div>
                    </div>
                    <div className="sancai-nav-list">
                        <Button
                            className={
                                selectedCategoryId === null
                                    ? "sancai-nav-button is-active"
                                    : "sancai-nav-button"
                            }
                            variant="ghost"
                            onClick={() => selectCategory(null)}
                        >
                            <span className="sancai-category-summary">
                                <span>总目</span>
                            </span>
                        </Button>
                        {categories.map((category) => (
                            <Button
                                key={category.id}
                                className={
                                    selectedCategoryId === category.id
                                        ? "sancai-nav-button is-active"
                                        : "sancai-nav-button"
                                }
                                variant="ghost"
                                onClick={() => selectCategory(category.id)}
                            >
                                {category.thumbnailUrl ? (
                                    <img
                                        alt={category.thumbnailTitle || readTitle(category, "门类")}
                                        className="sancai-category-thumb"
                                        src={category.thumbnailUrl}
                                    />
                                ) : null}
                                <span className="sancai-category-summary">
                                    <span>{readTitle(category, "门类")}</span>
                                </span>
                            </Button>
                        ))}
                    </div>
                </aside>

                <section className="sancai-browser-content">
                    <div className="sancai-content-grid">
                        <div className="sancai-entry-list" aria-label="三才图会条目列表">
                            {entriesQuery.isLoading ? (
                                <p className="sancai-muted">正在加载条目...</p>
                            ) : null}
                            {!entriesQuery.isLoading && entries.length === 0 ? (
                                <p className="sancai-muted">暂无条目</p>
                            ) : null}
                            {entries.map((entry) => (
                                <button
                                    key={entry.id}
                                    className={
                                        selectedEntry?.id === entry.id
                                            ? "sancai-entry-item is-active"
                                            : "sancai-entry-item"
                                    }
                                    type="button"
                                    onClick={() => selectEntry(entry.id)}
                                >
                                    <span>{readTitle(entry, "条目")}</span>
                                    <ChevronRight size={16} />
                                </button>
                            ))}
                            {entriesQuery.hasNextPage ? (
                                <Button
                                    className="sancai-entry-more-button"
                                    disabled={entriesQuery.isFetchingNextPage}
                                    variant="outline"
                                    onClick={() => entriesQuery.fetchNextPage()}
                                >
                                    {entriesQuery.isFetchingNextPage
                                        ? "正在加载..."
                                        : `加载更多条目（${loadedEntryCount} / ${totalEntryCount}）`}
                                </Button>
                            ) : null}
                        </div>

                        <Card
                            className={
                                detailView === "graph"
                                    ? "sancai-entry-detail is-graph-view"
                                    : "sancai-entry-detail"
                            }
                            aria-label="三才图会条目详情"
                        >
                            {selectedEntry ? (
                                <>
                                    <header className="sancai-detail-header">
                                        <h2>{readTitle(selectedEntry, "条目")}</h2>
                                        <div
                                            className="sancai-detail-tabs"
                                            aria-label="稿件详情视图"
                                            role="tablist"
                                        >
                                            <button
                                                aria-controls="sancai-reading-panel"
                                                aria-selected={detailView === "reading"}
                                                id="sancai-reading-tab"
                                                role="tab"
                                                type="button"
                                                onClick={() => setDetailView("reading")}
                                            >
                                                <BookOpen aria-hidden="true" size={15} />
                                                阅读
                                            </button>
                                            <button
                                                aria-controls="sancai-graph-panel"
                                                aria-selected={detailView === "graph"}
                                                id="sancai-graph-tab"
                                                role="tab"
                                                type="button"
                                                onClick={() => setDetailView("graph")}
                                            >
                                                <Network aria-hidden="true" size={15} />
                                                知识图谱
                                                {graphQuery.data?.visible ? (
                                                    <span>{graphQuery.data.nodes.length}</span>
                                                ) : null}
                                            </button>
                                        </div>
                                    </header>
                                    {detailView === "reading" ? (
                                        <div
                                            className="sancai-reading-panel"
                                            aria-labelledby="sancai-reading-tab"
                                            id="sancai-reading-panel"
                                            role="tabpanel"
                                        >
                                            {selectedEntryImages.length ? (
                                                <div
                                                    className="sancai-figure-list"
                                                    aria-label="三才图会正文图版"
                                                >
                                                    {selectedEntryImages.map((image) => (
                                                        <figure
                                                            key={image.key}
                                                            className={
                                                                image.currentUsed
                                                                    ? "sancai-entry-figure is-cover"
                                                                    : "sancai-entry-figure"
                                                            }
                                                        >
                                                            <img alt={image.alt} src={image.url} />
                                                            <figcaption>{image.caption}</figcaption>
                                                        </figure>
                                                    ))}
                                                </div>
                                            ) : null}
                                            {selectedEntry.tags?.length ? (
                                                <div
                                                    className="sancai-tag-list"
                                                    aria-label="三才图会条目标签"
                                                >
                                                    {selectedEntry.tags.map((tag) => (
                                                        <span
                                                            key={tag.id ?? tag.tagName}
                                                            className="sancai-tag"
                                                        >
                                                            {tag.tagName}
                                                        </span>
                                                    ))}
                                                </div>
                                            ) : null}
                                            {selectedEntry.summary?.trim() ? (
                                                <section className="sancai-detail-section">
                                                    <h3>提要</h3>
                                                    <p>{selectedEntry.summary}</p>
                                                </section>
                                            ) : null}
                                            {selectedEntry.originalText?.trim() ? (
                                                <section className="sancai-detail-section">
                                                    <h3>原文</h3>
                                                    <p>{selectedEntry.originalText}</p>
                                                </section>
                                            ) : null}
                                            {selectedEntry.translationText?.trim() ? (
                                                <section className="sancai-detail-section">
                                                    <h3>译文</h3>
                                                    <p>{selectedEntry.translationText}</p>
                                                </section>
                                            ) : null}
                                            {selectedEntry.currentVisualAsset?.visualDescription?.trim() ||
                                            selectedEntry.currentVisualAsset?.fusionDescription?.trim() ? (
                                                <section className="sancai-detail-section">
                                                    <h3>图像说明</h3>
                                                    <p>
                                                        {selectedEntry.currentVisualAsset
                                                            ?.visualDescription ||
                                                            selectedEntry.currentVisualAsset
                                                                ?.fusionDescription}
                                                    </p>
                                                </section>
                                            ) : null}
                                        </div>
                                    ) : (
                                        <div
                                            className="sancai-graph-panel"
                                            aria-labelledby="sancai-graph-tab"
                                            id="sancai-graph-panel"
                                            role="tabpanel"
                                        >
                                            {graphQuery.isLoading ? (
                                                <div className="sancai-graph-state">
                                                    <p>正在加载稿件图谱...</p>
                                                </div>
                                            ) : null}
                                            {graphQuery.isError ? (
                                                <div className="sancai-graph-state">
                                                    <p>稿件图谱加载失败，请稍后重试。</p>
                                                    <Button
                                                        variant="outline"
                                                        onClick={() => graphQuery.refetch()}
                                                    >
                                                        重新加载
                                                    </Button>
                                                </div>
                                            ) : null}
                                            {graphQuery.data ? (
                                                <SancaiGraphView
                                                    key={selectedEntry.id}
                                                    graph={graphQuery.data}
                                                />
                                            ) : null}
                                        </div>
                                    )}
                                </>
                            ) : (
                                <p className="sancai-muted">请选择一个条目查看详情</p>
                            )}
                        </Card>
                    </div>
                </section>
            </section>
        </main>
    );
};
