import { useMemo, useState, type FormEvent } from "react";
import { useQuery } from "@tanstack/react-query";
import { BookOpen, ChevronRight, Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import * as sancaiService from "./sancai-service";
import type { SancaiCategoryRecord, SancaiEntryRecord, SancaiVolumeRecord } from "./sancai-types";

import "./sancai-page.css";

const PAGE_SIZE = 12;
const EMPTY_CATEGORIES: SancaiCategoryRecord[] = [];
const EMPTY_VOLUMES: SancaiVolumeRecord[] = [];
const EMPTY_ENTRIES: SancaiEntryRecord[] = [];

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

const readEntryText = (entry: SancaiEntryRecord) => {
    return (
        entry.summary?.trim() ||
        entry.translationText?.trim() ||
        entry.originalText?.trim() ||
        "暂无内容摘要"
    );
};

export const SancaiPage = () => {
    const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(null);
    const [selectedVolumeId, setSelectedVolumeId] = useState<number | null>(null);
    const [selectedEntryId, setSelectedEntryId] = useState<number | null>(null);
    const [keyword, setKeyword] = useState("");
    const [appliedKeyword, setAppliedKeyword] = useState<string | null>(null);
    const [pageNo, setPageNo] = useState(1);

    const categoriesQuery = useQuery({
        queryKey: ["portal", "classics", "sancai", "categories"],
        queryFn: sancaiService.listCategories
    });
    const volumesQuery = useQuery({
        queryKey: ["portal", "classics", "sancai", "volumes", selectedCategoryId],
        queryFn: () => sancaiService.listVolumes(selectedCategoryId)
    });
    const entriesQuery = useQuery({
        queryKey: [
            "portal",
            "classics",
            "sancai",
            "entries",
            selectedCategoryId,
            selectedVolumeId,
            appliedKeyword,
            pageNo
        ],
        queryFn: () =>
            sancaiService.pageEntries({
                categoryId: selectedCategoryId,
                volumeId: selectedVolumeId,
                keyword: appliedKeyword,
                pageNo,
                pageSize: PAGE_SIZE
            })
    });
    const detailQuery = useQuery({
        queryKey: ["portal", "classics", "sancai", "entry", selectedEntryId],
        queryFn: () => sancaiService.getEntry(selectedEntryId ?? 0),
        enabled: selectedEntryId !== null
    });

    const categories = categoriesQuery.data || EMPTY_CATEGORIES;
    const volumes = volumesQuery.data || EMPTY_VOLUMES;
    const entries = entriesQuery.data?.records || EMPTY_ENTRIES;
    const selectedEntry =
        detailQuery.data || entries.find((entry) => entry.id === selectedEntryId) || entries[0];
    const totalPage = entriesQuery.data?.totalPage || 1;

    const selectedScopeTitle = useMemo(() => {
        const category = categories.find((item) => item.id === selectedCategoryId);
        const volume = volumes.find((item) => item.id === selectedVolumeId);
        if (volume) {
            return readTitle(volume, "卷");
        }
        if (category) {
            return readTitle(category, "门类");
        }
        return "全部公开条目";
    }, [categories, selectedCategoryId, selectedVolumeId, volumes]);

    const applySearch = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        setAppliedKeyword(keyword.trim() || null);
        setPageNo(1);
        setSelectedEntryId(null);
    };

    const selectCategory = (categoryId: number | null) => {
        setSelectedCategoryId(categoryId);
        setSelectedVolumeId(null);
        setSelectedEntryId(null);
        setPageNo(1);
    };

    const selectVolume = (volumeId: number | null) => {
        setSelectedVolumeId(volumeId);
        setSelectedEntryId(null);
        setPageNo(1);
    };

    return (
        <main className="sancai-portal-page">
            <section className="sancai-browser" aria-label="三才图会在线展示">
                <aside className="sancai-browser-nav" aria-label="三才图会目录">
                    <div className="sancai-browser-title">
                        <BookOpen size={20} />
                        <div>
                            <p>Classics</p>
                            <h1>三才图会</h1>
                        </div>
                    </div>
                    <Button
                        className="sancai-nav-button"
                        variant={selectedCategoryId === null ? "default" : "ghost"}
                        onClick={() => selectCategory(null)}
                    >
                        全部公开条目
                    </Button>
                    <div className="sancai-nav-list">
                        {categories.map((category) => (
                            <Button
                                key={category.id}
                                className="sancai-nav-button"
                                variant={selectedCategoryId === category.id ? "default" : "ghost"}
                                onClick={() => selectCategory(category.id)}
                            >
                                {readTitle(category, "门类")}
                            </Button>
                        ))}
                    </div>
                </aside>

                <section className="sancai-browser-content">
                    <div className="sancai-browser-toolbar">
                        <div>
                            <p>当前范围</p>
                            <h2>{selectedScopeTitle}</h2>
                        </div>
                        <form className="sancai-search-form" onSubmit={applySearch}>
                            <Input
                                aria-label="搜索三才图会公开条目"
                                placeholder="搜索公开条目"
                                value={keyword}
                                onChange={(event) => setKeyword(event.target.value)}
                            />
                            <Button type="submit">
                                <Search size={16} />
                                搜索
                            </Button>
                        </form>
                    </div>

                    <div className="sancai-volume-strip" aria-label="三才图会卷目">
                        <Button
                            className="sancai-volume-button"
                            variant={selectedVolumeId === null ? "default" : "outline"}
                            onClick={() => selectVolume(null)}
                        >
                            全部卷目
                        </Button>
                        {volumes.map((volume) => (
                            <Button
                                key={volume.id}
                                className="sancai-volume-button"
                                variant={selectedVolumeId === volume.id ? "default" : "outline"}
                                onClick={() => selectVolume(volume.id)}
                            >
                                {readTitle(volume, "卷")}
                            </Button>
                        ))}
                    </div>

                    <div className="sancai-content-grid">
                        <div className="sancai-entry-list" aria-label="三才图会公开条目列表">
                            {entriesQuery.isLoading ? (
                                <p className="sancai-muted">正在加载公开条目...</p>
                            ) : null}
                            {!entriesQuery.isLoading && entries.length === 0 ? (
                                <p className="sancai-muted">暂无公开条目</p>
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
                                    onClick={() => setSelectedEntryId(entry.id)}
                                >
                                    <span>{readTitle(entry, "条目")}</span>
                                    <ChevronRight size={16} />
                                </button>
                            ))}
                            <div className="sancai-pagination">
                                <Button
                                    disabled={pageNo <= 1}
                                    variant="outline"
                                    onClick={() => setPageNo((value) => Math.max(1, value - 1))}
                                >
                                    上一页
                                </Button>
                                <span>
                                    {pageNo} / {totalPage}
                                </span>
                                <Button
                                    disabled={pageNo >= totalPage}
                                    variant="outline"
                                    onClick={() => setPageNo((value) => value + 1)}
                                >
                                    下一页
                                </Button>
                            </div>
                        </div>

                        <Card className="sancai-entry-detail" aria-label="三才图会条目详情">
                            {selectedEntry ? (
                                <>
                                    <p className="sancai-detail-kicker">公开已发布</p>
                                    <h2>{readTitle(selectedEntry, "条目")}</h2>
                                    <p className="sancai-detail-summary">
                                        {readEntryText(selectedEntry)}
                                    </p>
                                    <div className="sancai-detail-columns">
                                        <section>
                                            <h3>原文</h3>
                                            <p>{selectedEntry.originalText || "暂无原文"}</p>
                                        </section>
                                        <section>
                                            <h3>译文</h3>
                                            <p>{selectedEntry.translationText || "暂无译文"}</p>
                                        </section>
                                    </div>
                                </>
                            ) : (
                                <p className="sancai-muted">请选择一个公开条目查看详情</p>
                            )}
                        </Card>
                    </div>
                </section>
            </section>
        </main>
    );
};
