import { useEffect, useMemo, useRef, useState, type FormEvent } from "react";
import { useMutation } from "@tanstack/react-query";
import { ArrowRight, Search, SlidersHorizontal, X } from "lucide-react";
import { Link, useSearchParams } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
    Sheet,
    SheetClose,
    SheetContent,
    SheetDescription,
    SheetFooter,
    SheetHeader,
    SheetTitle
} from "@/components/ui/sheet";
import * as discoverySearchService from "./search-service";
import type {
    DiscoverySearchClickEventRequest,
    DiscoverySearchGroupResponse,
    DiscoverySearchItemResponse,
    DiscoverySearchPreviewRequest,
    DiscoverySearchRequest
} from "./search-types";

const KNOWLEDGE_BASE_OPTIONS = [
    {
        label: "三才图会",
        value: "SANCAI_ENTRY"
    },
    {
        label: "王圻文档",
        value: "WANGQI_DOCUMENT"
    },
    {
        label: "明代习俗",
        value: "MING_CUSTOMS"
    }
] as const;

const SAMPLE_QUERIES = [
    {
        knowledgeBases: "SANCAI_ENTRY",
        label: "三才图会",
        queryText: "图谱里的礼器"
    },
    {
        knowledgeBases: "WANGQI_DOCUMENT",
        label: "王圻文档",
        queryText: "明代官制"
    },
    {
        knowledgeBases: "MING_CUSTOMS",
        label: "明代习俗",
        queryText: "节令"
    }
] as const;

const DEFAULT_PAGE_SIZE = "10";

interface SearchFormState {
    categoryCodes: string;
    contentStatuses: string;
    dateFrom: string;
    dateTo: string;
    knowledgeBases: string;
    pageNo: string;
    pageSize: string;
    queryText: string;
    tagNames: string;
    visibilityScopes: string;
}

const INITIAL_FORM_STATE: SearchFormState = {
    categoryCodes: "",
    contentStatuses: "",
    dateFrom: "",
    dateTo: "",
    knowledgeBases: "",
    pageNo: "1",
    pageSize: DEFAULT_PAGE_SIZE,
    queryText: "",
    tagNames: "",
    visibilityScopes: ""
};

const hasAdvancedFilters = (form: SearchFormState) => {
    return Boolean(form.dateFrom || form.dateTo || form.knowledgeBases);
};

const toFormState = (searchParams: URLSearchParams): SearchFormState => {
    return {
        categoryCodes: "",
        contentStatuses: "",
        dateFrom: searchParams.get("dateFrom") ?? "",
        dateTo: searchParams.get("dateTo") ?? "",
        knowledgeBases: searchParams.get("knowledgeBases") ?? "",
        pageNo: searchParams.get("pageNo") ?? INITIAL_FORM_STATE.pageNo,
        pageSize: searchParams.get("pageSize") ?? INITIAL_FORM_STATE.pageSize,
        queryText: searchParams.get("q") ?? "",
        tagNames: "",
        visibilityScopes: ""
    };
};

const appendParam = (searchParams: URLSearchParams, key: string, value: string) => {
    const normalizedValue = value.trim();
    if (normalizedValue) {
        searchParams.set(key, normalizedValue);
    }
};

const toSearchParams = (form: SearchFormState) => {
    const searchParams = new URLSearchParams();
    appendParam(searchParams, "q", form.queryText);
    appendParam(searchParams, "knowledgeBases", form.knowledgeBases);
    appendParam(searchParams, "dateFrom", form.dateFrom);
    appendParam(searchParams, "dateTo", form.dateTo);
    if (form.pageNo !== INITIAL_FORM_STATE.pageNo) {
        appendParam(searchParams, "pageNo", form.pageNo);
    }
    if (form.pageSize !== INITIAL_FORM_STATE.pageSize) {
        appendParam(searchParams, "pageSize", form.pageSize);
    }

    return searchParams;
};

const splitList = (value: string) => {
    const tokens = value
        .split(/[\n,，、\s]+/gu)
        .map((token) => token.trim())
        .filter(Boolean);

    return Array.from(new Set(tokens));
};

const joinList = (values: string[]) => values.join(", ");

const hasListValue = (value: string, token: string) => splitList(value).includes(token);

const splitPreviewBody = (value?: string | null) => {
    const text = value?.trim();
    if (!text) {
        return ["暂无正文。"];
    }
    const sourceParagraphs = text
        .replace(/\r\n/gu, "\n")
        .replace(/\r/gu, "\n")
        .split(/\n+/gu)
        .map((paragraph) => paragraph.trim())
        .filter(Boolean);

    return sourceParagraphs.flatMap((paragraph) => {
        if (paragraph.length <= 180) {
            return [paragraph];
        }
        const sentences = paragraph.match(/[^。！？!?；;]+[。！？!?；;]?/gu) ?? [paragraph];
        const chunks: string[] = [];
        let current = "";
        sentences.forEach((sentence) => {
            const next = `${current}${sentence}`;
            if (current && next.length > 180) {
                chunks.push(current);
                current = sentence;
                return;
            }
            current = next;
        });
        if (current) {
            chunks.push(current);
        }
        return chunks;
    });
};

const toIsoStartOfDay = (value: string) => {
    return value ? new Date(`${value}T00:00:00`).toISOString() : null;
};

const toIsoEndOfDay = (value: string) => {
    return value ? new Date(`${value}T23:59:59`).toISOString() : null;
};

const formatCount = (value?: number | null) => {
    return value ?? 0;
};

const toRequest = (form: SearchFormState): DiscoverySearchRequest => {
    return {
        categoryCodes: splitList(form.categoryCodes),
        contentStatuses: splitList(form.contentStatuses),
        dateFrom: toIsoStartOfDay(form.dateFrom),
        dateTo: toIsoEndOfDay(form.dateTo),
        knowledgeBases: splitList(form.knowledgeBases),
        pageNo: Number.parseInt(form.pageNo, 10) || 1,
        pageSize: Number.parseInt(form.pageSize, 10) || 10,
        queryText: form.queryText.trim(),
        tagNames: splitList(form.tagNames),
        visibilityScopes: splitList(form.visibilityScopes)
    };
};

const formatItemMeta = (item: DiscoverySearchItemResponse) => {
    return [
        item.contentDomain || "未知域",
        item.contentType || "未知类型",
        `全局 ${item.resultRank ?? "-"} / 组内 ${item.groupRank ?? "-"}`
    ].join(" · ");
};

const renderHighlightText = (highlightText: string | null | undefined) => {
    if (!highlightText) {
        return null;
    }

    const nodes: Array<string | JSX.Element> = [];
    const markPattern = /<mark>(.*?)<\/mark>/giu;
    let lastIndex = 0;
    let match = markPattern.exec(highlightText);

    while (match) {
        if (match.index > lastIndex) {
            nodes.push(highlightText.slice(lastIndex, match.index));
        }

        nodes.push(<mark key={`mark-${match.index}`}>{match[1]}</mark>);
        lastIndex = match.index + match[0].length;
        match = markPattern.exec(highlightText);
    }

    if (lastIndex < highlightText.length) {
        nodes.push(highlightText.slice(lastIndex));
    }

    return nodes;
};

const createClickCommand = (
    searchEventId: string,
    group: DiscoverySearchGroupResponse,
    item: DiscoverySearchItemResponse
): DiscoverySearchClickEventRequest | null => {
    if (
        !searchEventId ||
        !group.groupKey ||
        !item.contentDomain ||
        !item.contentType ||
        !item.contentId ||
        item.resultRank == null ||
        item.groupRank == null
    ) {
        return null;
    }

    return {
        contentDomain: item.contentDomain,
        contentId: item.contentId,
        contentTitle: item.title ?? null,
        contentType: item.contentType,
        groupRank: item.groupRank,
        resultGroupKey: group.groupKey,
        resultRank: item.resultRank,
        searchEventId,
        targetPath: item.targetPath ?? null
    };
};

const createPreviewRequest = (
    item: DiscoverySearchItemResponse
): DiscoverySearchPreviewRequest | null => {
    if (!item.contentType || !item.contentId) {
        return null;
    }

    return {
        contentId: item.contentId,
        contentType: item.contentType
    };
};

interface MultiOptionControlProps {
    description: string;
    label: string;
    name: keyof SearchFormState;
    onToggle: (name: keyof SearchFormState, value: string) => void;
    options: ReadonlyArray<{
        label: string;
        value: string;
    }>;
    value: string;
}

const MultiOptionControl = ({
    description,
    label,
    name,
    onToggle,
    options,
    value
}: MultiOptionControlProps) => {
    return (
        <div className="portal-filter-field" role="group" aria-label={label}>
            <span>{label}</span>
            <input name={name} type="hidden" value={value} />
            <div className="portal-discovery-samples">
                {options.map((option) => {
                    const selected = hasListValue(value, option.value);

                    return (
                        <Button
                            aria-pressed={selected}
                            key={option.value}
                            type="button"
                            variant={selected ? "default" : "outline"}
                            onClick={() => onToggle(name, option.value)}
                        >
                            {option.label}
                        </Button>
                    );
                })}
            </div>
            <em>{description}</em>
        </div>
    );
};

export const DiscoverySearchPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [form, setForm] = useState<SearchFormState>(() => toFormState(searchParams));
    const [showAdvancedFilters, setShowAdvancedFilters] = useState(() =>
        hasAdvancedFilters(toFormState(searchParams))
    );
    const submittedFormRef = useRef<SearchFormState | null>(null);
    const searchMutation = useMutation({
        mutationFn: (request: DiscoverySearchRequest) =>
            discoverySearchService.searchDiscovery(request)
    });
    const previewMutation = useMutation({
        mutationFn: (request: DiscoverySearchPreviewRequest) =>
            discoverySearchService.previewSearchResult(request)
    });
    const { mutate: runSearch } = searchMutation;

    useEffect(() => {
        const submittedForm = submittedFormRef.current;
        submittedFormRef.current = null;

        const nextForm = submittedForm ?? toFormState(searchParams);
        if (!submittedForm) {
            setForm(nextForm);
        }

        setShowAdvancedFilters(hasAdvancedFilters(nextForm));
        const request = toRequest(nextForm);

        runSearch(request);
    }, [runSearch, searchParams]);

    const requestPreview = useMemo(() => toRequest(form), [form]);
    const response = searchMutation.data;
    const groups = response?.groups ?? [];
    const summaryText = useMemo(() => {
        if (searchMutation.isPending) {
            return "正在检索知识中心";
        }

        if (searchMutation.isError) {
            return "检索失败，请调整条件后重试";
        }

        if (!response) {
            return "";
        }

        return `共 ${formatCount(response.totalCount)} 条命中，分布在 ${formatCount(response.groupCount)} 个分组中`;
    }, [response, searchMutation.isError, searchMutation.isPending]);

    const updateField = (key: keyof SearchFormState, value: string) => {
        setForm((current) => ({
            ...current,
            [key]: value
        }));
    };

    const toggleListValue = (key: keyof SearchFormState, value: string) => {
        setForm((current) => {
            const values = splitList(current[key]);
            const nextValues = values.includes(value)
                ? values.filter((candidate) => candidate !== value)
                : [...values, value];

            return {
                ...current,
                [key]: joinList(nextValues)
            };
        });
    };

    const applySample = (sample: (typeof SAMPLE_QUERIES)[number]) => {
        setForm((current) => ({
            ...current,
            knowledgeBases: sample.knowledgeBases,
            queryText: sample.queryText
        }));
    };

    const handleReset = () => {
        submittedFormRef.current = null;
        setForm(INITIAL_FORM_STATE);
        setSearchParams(new URLSearchParams());
    };

    const handleClearFilters = () => {
        const nextForm: SearchFormState = {
            ...INITIAL_FORM_STATE,
            queryText: form.queryText
        };
        setForm(nextForm);

        const nextSearchParams = toSearchParams(nextForm);
        const request = toRequest(nextForm);
        if (nextSearchParams.toString() === searchParams.toString()) {
            runSearch(request);
            return;
        }

        submittedFormRef.current = nextForm;
        setSearchParams(nextSearchParams);
    };

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const request = toRequest(form);

        const nextSearchParams = toSearchParams(form);
        if (nextSearchParams.toString() === searchParams.toString()) {
            runSearch(request);
            return;
        }

        submittedFormRef.current = form;
        setSearchParams(nextSearchParams);
    };

    const handlePaginationSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const request = toRequest(form);

        const nextSearchParams = toSearchParams(form);
        if (nextSearchParams.toString() === searchParams.toString()) {
            runSearch(request);
            return;
        }

        submittedFormRef.current = form;
        setSearchParams(nextSearchParams);
    };

    const handleClick = (
        group: DiscoverySearchGroupResponse,
        item: DiscoverySearchItemResponse
    ) => {
        const command = response?.searchEventId
            ? createClickCommand(response.searchEventId, group, item)
            : null;

        if (command) {
            void discoverySearchService.recordSearchClickEvent(command);
        }

        const previewRequest = createPreviewRequest(item);
        if (previewRequest) {
            previewMutation.mutate(previewRequest);
        }
    };

    const shouldShowZeroResult =
        !searchMutation.isPending && !searchMutation.isError && response?.totalCount === 0;

    return (
        <main className="portal-shell">
            <header className="portal-header">
                <div>
                    <p className="portal-kicker">知识中心 · Discovery</p>
                    <h1>发现检索台</h1>
                </div>
                <Button asChild className="portal-action" size="lg" variant="outline">
                    <Link to="/">返回首页</Link>
                </Button>
            </header>

            <form className="portal-discovery-search" onSubmit={handleSubmit}>
                <section className="portal-discovery-hero" aria-label="Discovery 搜索">
                    <h2>一词入检，先见古籍中的相关线索</h2>
                    <Label className="portal-discovery-search-box">
                        <span className="portal-search-label">搜索词</span>
                        <Search aria-hidden="true" size={22} />
                        <Input
                            autoFocus
                            name="queryText"
                            placeholder="朱熹、礼制、三才图会"
                            value={form.queryText}
                            onChange={(event) => updateField("queryText", event.target.value)}
                        />
                        {form.queryText ? (
                            <Button
                                aria-label="清空搜索词"
                                className="portal-discovery-clear-query"
                                size="icon"
                                type="button"
                                variant="ghost"
                                onClick={() => updateField("queryText", "")}
                            >
                                <X aria-hidden="true" size={18} />
                            </Button>
                        ) : null}
                        <Button disabled={searchMutation.isPending} type="submit">
                            {searchMutation.isPending ? "检索中..." : "开始检索"}
                        </Button>
                    </Label>
                    <div className="portal-discovery-quick-row">
                        <span>试试：</span>
                        {SAMPLE_QUERIES.map((sample) => (
                            <Button
                                key={sample.label}
                                type="button"
                                variant="outline"
                                onClick={() => applySample(sample)}
                            >
                                {sample.label}
                            </Button>
                        ))}
                    </div>
                    <div className="portal-discovery-toolbar">
                        <Button
                            aria-expanded={showAdvancedFilters}
                            type="button"
                            variant="outline"
                            onClick={() => setShowAdvancedFilters((current) => !current)}
                        >
                            <SlidersHorizontal aria-hidden="true" size={16} />
                            高级筛选
                        </Button>
                        <Button type="button" variant="ghost" onClick={handleClearFilters}>
                            清除筛选条件
                        </Button>
                    </div>
                </section>

                {showAdvancedFilters ? (
                    <Card className="portal-discovery-panel">
                        <div className="portal-discovery-panel-header">
                            <div>
                                <p className="portal-kicker">高级筛选</p>
                                <h2>限定知识库和时间</h2>
                            </div>
                        </div>

                        <div className="portal-discovery-form-grid">
                            <MultiOptionControl
                                description="可多选，提交后由后端按知识库范围过滤"
                                label="知识库"
                                name="knowledgeBases"
                                options={KNOWLEDGE_BASE_OPTIONS}
                                value={form.knowledgeBases}
                                onToggle={toggleListValue}
                            />

                            <Label className="portal-filter-field">
                                <span>起始日期</span>
                                <Input
                                    name="dateFrom"
                                    type="date"
                                    value={form.dateFrom}
                                    onChange={(event) =>
                                        updateField("dateFrom", event.target.value)
                                    }
                                />
                                <em>按 ISO-8601 起始时间提交</em>
                            </Label>

                            <Label className="portal-filter-field">
                                <span>结束日期</span>
                                <Input
                                    name="dateTo"
                                    type="date"
                                    value={form.dateTo}
                                    onChange={(event) => updateField("dateTo", event.target.value)}
                                />
                                <em>按 ISO-8601 结束时间提交</em>
                            </Label>
                        </div>

                        <div className="portal-discovery-actions">
                            <Button type="button" variant="outline" onClick={handleReset}>
                                重置条件
                            </Button>
                            <Button type="button" variant="outline" onClick={handleClearFilters}>
                                清除筛选条件
                            </Button>
                            <Button disabled={searchMutation.isPending} type="submit">
                                {searchMutation.isPending ? "检索中..." : "开始检索"}
                            </Button>
                        </div>
                    </Card>
                ) : null}
            </form>

            <section className="portal-discovery-results" aria-label="Discovery 搜索结果">
                {response || searchMutation.isPending || searchMutation.isError ? (
                    <div className="portal-discovery-result-summary">
                        <strong>{summaryText}</strong>
                        {response?.searchEventId ? (
                            <span>事件 {response.searchEventId}</span>
                        ) : null}
                        {response ? (
                            <span>
                                回显词：
                                {response.displayQueryText ?? requestPreview.queryText ?? "-"}
                            </span>
                        ) : null}
                    </div>
                ) : null}

                {searchMutation.isError ? (
                    <Card className="portal-empty">
                        <strong>检索失败</strong>
                        <p>请检查输入条件后重新提交搜索。</p>
                    </Card>
                ) : null}

                {groups.map((group, index) => {
                    const groupKey = group.groupKey || `group-${index}`;
                    const items = group.items ?? [];

                    return (
                        <Card className="portal-discovery-group" key={groupKey}>
                            <header>
                                <div>
                                    <p>{group.groupKey || "未命名分组"}</p>
                                    <h2>{group.groupTitle || `检索分组 ${index + 1}`}</h2>
                                </div>
                                <strong>{formatCount(group.count)} 条</strong>
                            </header>

                            {items.length ? (
                                <div className="portal-discovery-hit-list">
                                    {items.map((item, itemIndex) => {
                                        const hitKey = `${groupKey}-${item.resultRank ?? itemIndex}`;
                                        const content = (
                                            <>
                                                <div className="portal-discovery-hit-title">
                                                    <p>
                                                        {item.contentDomain || "未知域"} ·{" "}
                                                        {item.contentType || "未知类型"}
                                                    </p>
                                                    <h3>{item.title || "未命名结果"}</h3>
                                                </div>
                                                <p className="portal-discovery-hit-summary">
                                                    {renderHighlightText(item.highlightText) ||
                                                        item.summary ||
                                                        "暂无摘要"}
                                                </p>
                                                <div className="portal-discovery-hit-meta">
                                                    <span>{formatItemMeta(item)}</span>
                                                </div>
                                            </>
                                        );

                                        return (
                                            <button
                                                aria-label={`打开搜索预览：${item.title || "未命名结果"}`}
                                                className="portal-discovery-hit"
                                                key={hitKey}
                                                type="button"
                                                onClick={() => handleClick(group, item)}
                                            >
                                                {content}
                                                <span className="portal-discovery-hit-arrow">
                                                    <ArrowRight aria-hidden="true" size={16} />
                                                </span>
                                            </button>
                                        );
                                    })}
                                </div>
                            ) : (
                                <div className="portal-empty">该分组暂无可见结果。</div>
                            )}
                        </Card>
                    );
                })}

                {shouldShowZeroResult ? (
                    <Card className="portal-empty">
                        <strong>没有找到匹配内容</strong>
                        <p>可以保留搜索词，先清除筛选条件再重新检索。</p>
                        <Button type="button" variant="outline" onClick={handleClearFilters}>
                            清除筛选条件
                        </Button>
                    </Card>
                ) : null}

                {response ? (
                    <form
                        className="portal-discovery-pagination"
                        aria-label="搜索结果分页"
                        onSubmit={handlePaginationSubmit}
                    >
                        <Label className="portal-filter-field">
                            <span>页码</span>
                            <Input
                                name="pageNo"
                                min={1}
                                type="number"
                                value={form.pageNo}
                                onChange={(event) => updateField("pageNo", event.target.value)}
                            />
                        </Label>

                        <Label className="portal-filter-field">
                            <span>每页数量</span>
                            <Input
                                name="pageSize"
                                min={1}
                                type="number"
                                value={form.pageSize}
                                onChange={(event) => updateField("pageSize", event.target.value)}
                            />
                        </Label>

                        <Button disabled={searchMutation.isPending} type="submit">
                            应用分页
                        </Button>
                    </form>
                ) : null}
            </section>

            <Sheet
                open={
                    previewMutation.isPending ||
                    Boolean(previewMutation.data) ||
                    previewMutation.isError
                }
                onOpenChange={(open) => {
                    if (!open) {
                        previewMutation.reset();
                    }
                }}
            >
                <SheetContent className="portal-search-preview-sheet">
                    <SheetHeader>
                        <SheetDescription>检索预览</SheetDescription>
                        <SheetTitle>{previewMutation.data?.title ?? "搜索命中内容"}</SheetTitle>
                    </SheetHeader>

                    <div className="portal-search-preview-body">
                        {previewMutation.isPending ? (
                            <div className="portal-empty">正在读取搜索预览。</div>
                        ) : null}

                        {previewMutation.isError ? (
                            <div className="portal-empty">当前内容不可预览或已经不可见。</div>
                        ) : null}

                        {previewMutation.data ? (
                            <>
                                <dl className="portal-search-preview-meta">
                                    <div>
                                        <dt>知识库</dt>
                                        <dd>{previewMutation.data.knowledgeBase ?? "-"}</dd>
                                    </div>
                                    <div>
                                        <dt>门类</dt>
                                        <dd>
                                            {previewMutation.data.categoryName ??
                                                previewMutation.data.categoryCode ??
                                                "-"}
                                        </dd>
                                    </div>
                                </dl>
                                {previewMutation.data.summary ? (
                                    <section className="portal-search-preview-section">
                                        <h3>摘要</h3>
                                        <p>{previewMutation.data.summary}</p>
                                    </section>
                                ) : null}
                                <section className="portal-search-preview-section">
                                    <h3>正文</h3>
                                    {splitPreviewBody(previewMutation.data.bodyText).map(
                                        (paragraph, index) => (
                                            <p key={`${paragraph}-${index}`}>{paragraph}</p>
                                        )
                                    )}
                                </section>
                                {previewMutation.data.tagNames?.length ? (
                                    <div className="portal-search-preview-tags">
                                        {previewMutation.data.tagNames.map((tagName) => (
                                            <span key={tagName}>{tagName}</span>
                                        ))}
                                    </div>
                                ) : null}
                            </>
                        ) : null}
                    </div>

                    <SheetFooter className="portal-search-preview-footer">
                        <SheetClose asChild>
                            <Button type="button" variant="outline">
                                关闭预览
                            </Button>
                        </SheetClose>
                    </SheetFooter>
                </SheetContent>
            </Sheet>
        </main>
    );
};
