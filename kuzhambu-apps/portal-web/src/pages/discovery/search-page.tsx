import { useEffect, useMemo, useRef, useState, type FormEvent, type MouseEvent } from "react";
import { useMutation } from "@tanstack/react-query";
import { Search, SlidersHorizontal, X } from "lucide-react";
import { Link, useSearchParams } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
    Pagination,
    PaginationContent,
    PaginationItem,
    PaginationNext,
    PaginationPrevious
} from "@/components/ui/pagination";
import {
    Select,
    SelectContent,
    SelectGroup,
    SelectItem,
    SelectTrigger,
    SelectValue
} from "@/components/ui/select";
import * as discoverySearchService from "./search-service";
import type {
    DiscoverySearchClickEventRequest,
    DiscoverySearchGroupResponse,
    DiscoverySearchItemResponse,
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

const DISPLAY_LABELS: Record<string, string> = {
    CLASSICS: "古籍内容",
    MING_CUSTOMS: "明代习俗",
    PRIVATE: "内部可见",
    PUBLIC: "公开可见",
    PUBLISHED: "已发布",
    SANCAI_ENTRY: "三才图会",
    WANGQI_DOCUMENT: "王圻文档"
};

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
const PAGE_SIZE_OPTIONS = ["10", "20", "50"] as const;
const SEARCH_ITEM_CONTENT_TYPES = new Set(["SANCAI_ENTRY", "WANGQI_DOCUMENT", "MING_CUSTOMS"]);

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

const toSearchItemPath = (contentType?: string | null, contentId?: number | string | null) => {
    if (contentType && SEARCH_ITEM_CONTENT_TYPES.has(contentType) && contentId) {
        return `/discovery/search-item?type=${encodeURIComponent(contentType)}&id=${encodeURIComponent(String(contentId))}`;
    }

    return null;
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

const toDisplayLabel = (value?: string | null, fallback = "未标注") => {
    if (!value) {
        return fallback;
    }

    return DISPLAY_LABELS[value] ?? fallback;
};

const flattenGroups = (groups: DiscoverySearchGroupResponse[]) => {
    return groups.flatMap((group, groupIndex) =>
        (group.items ?? []).map((item, itemIndex) => ({
            group,
            groupIndex,
            item,
            itemIndex
        }))
    );
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

const escapeRegExp = (value: string) => {
    return value.replace(/[.*+?^${}()|[\]\\]/gu, "\\$&");
};

const renderQueryHighlight = (text: string, queryText: string) => {
    const terms = splitList(queryText).filter((term) => term.length > 0);
    if (!terms.length) {
        return text;
    }

    const pattern = new RegExp(`(${terms.map(escapeRegExp).join("|")})`, "giu");
    const nodes: Array<string | JSX.Element> = [];
    let lastIndex = 0;
    let match = pattern.exec(text);

    while (match) {
        if (match.index > lastIndex) {
            nodes.push(text.slice(lastIndex, match.index));
        }

        nodes.push(<mark key={`query-mark-${match.index}`}>{match[0]}</mark>);
        lastIndex = match.index + match[0].length;
        match = pattern.exec(text);
    }

    if (lastIndex < text.length) {
        nodes.push(text.slice(lastIndex));
    }

    return nodes.length ? nodes : text;
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

interface DiscoveryPaginationProps {
    currentPage: number;
    disabled: boolean;
    pageSize: number;
    total: number;
    onChange: (pageNo: number, pageSize: number) => void;
}

const DiscoveryPagination = ({
    currentPage,
    disabled,
    pageSize,
    total,
    onChange
}: DiscoveryPaginationProps) => {
    const totalPage = Math.max(1, Math.ceil(total / pageSize));
    const canGoPrevious = currentPage > 1;
    const canGoNext = currentPage < totalPage;
    const handlePageClick =
        (enabled: boolean, nextPage: number) => (event: MouseEvent<HTMLAnchorElement>) => {
            event.preventDefault();
            if (disabled || !enabled) {
                return;
            }
            onChange(nextPage, pageSize);
        };

    return (
        <div className="portal-discovery-pagination">
            <Pagination aria-label="搜索结果分页" className="portal-discovery-pager">
                <PaginationContent>
                    <PaginationItem>
                        <PaginationPrevious
                            aria-disabled={disabled || !canGoPrevious}
                            aria-label="上一页"
                            data-disabled={disabled || !canGoPrevious}
                            href="#"
                            text=""
                            onClick={handlePageClick(canGoPrevious, Math.max(1, currentPage - 1))}
                        />
                    </PaginationItem>
                    <PaginationItem>
                        <span className="portal-discovery-page-indicator">
                            第 {currentPage} / {totalPage} 页
                        </span>
                    </PaginationItem>
                    <PaginationItem>
                        <PaginationNext
                            aria-disabled={disabled || !canGoNext}
                            aria-label="下一页"
                            data-disabled={disabled || !canGoNext}
                            href="#"
                            text=""
                            onClick={handlePageClick(canGoNext, currentPage + 1)}
                        />
                    </PaginationItem>
                </PaginationContent>
            </Pagination>
            <div className="portal-discovery-pagination-extra">
                <span>共 {formatCount(total)} 条</span>
                <Select
                    value={String(pageSize)}
                    onValueChange={(value) => onChange(1, Number.parseInt(value, 10) || pageSize)}
                >
                    <SelectTrigger aria-label="每页数量" size="sm">
                        <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectGroup>
                            {PAGE_SIZE_OPTIONS.map((option) => (
                                <SelectItem key={option} value={option}>
                                    每页 {option} 条
                                </SelectItem>
                            ))}
                        </SelectGroup>
                    </SelectContent>
                </Select>
            </div>
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

    const response = searchMutation.data;
    const results = useMemo(() => flattenGroups(response?.groups ?? []), [response?.groups]);
    const currentPageNo = Number.parseInt(form.pageNo, 10) || 1;
    const currentPageSize = Number.parseInt(form.pageSize, 10) || 10;
    const totalCount = response?.totalCount ?? results.length;
    const summaryText = useMemo(() => {
        if (searchMutation.isPending) {
            return "正在检索知识中心";
        }

        if (searchMutation.isError) {
            return "检索暂时不可用，请稍后重试";
        }

        if (!response) {
            return "";
        }

        return `共 ${formatCount(response.totalCount)} 条命中`;
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
        const nextForm = {
            ...form,
            pageNo: INITIAL_FORM_STATE.pageNo
        };
        const request = toRequest(nextForm);

        const nextSearchParams = toSearchParams(nextForm);
        if (nextSearchParams.toString() === searchParams.toString()) {
            runSearch(request);
            return;
        }

        setForm(nextForm);
        submittedFormRef.current = nextForm;
        setSearchParams(nextSearchParams);
    };

    const handlePageChange = (pageNo: number, pageSize: number) => {
        const nextForm = {
            ...form,
            pageNo: String(pageNo),
            pageSize: String(pageSize)
        };
        const request = toRequest(nextForm);

        const nextSearchParams = toSearchParams(nextForm);
        if (nextSearchParams.toString() === searchParams.toString()) {
            runSearch(request);
            return;
        }

        setForm(nextForm);
        submittedFormRef.current = nextForm;
        setSearchParams(nextSearchParams);
    };

    const recordResultClick = (
        group: DiscoverySearchGroupResponse,
        item: DiscoverySearchItemResponse
    ) => {
        const command = response?.searchEventId
            ? createClickCommand(response.searchEventId, group, item)
            : null;

        if (command) {
            void discoverySearchService.recordSearchClickEvent(command);
        }
    };

    const shouldShowZeroResult =
        !searchMutation.isPending && !searchMutation.isError && response?.totalCount === 0;

    return (
        <main className="portal-shell">
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
                    </div>
                ) : null}

                {searchMutation.isError ? (
                    <Card className="portal-empty">
                        <strong>检索暂时不可用</strong>
                        <p>服务恢复后会按当前搜索词和筛选条件继续检索。</p>
                    </Card>
                ) : null}

                {results.length ? (
                    <Card className="portal-discovery-group">
                        <div className="portal-discovery-hit-list">
                            {results.map(({ group, groupIndex, item, itemIndex }) => {
                                const hitKey = `${group.groupKey || `group-${groupIndex}`}-${item.resultRank ?? itemIndex}`;
                                const targetPath = toSearchItemPath(
                                    item.contentType,
                                    item.contentId
                                );
                                const content = (
                                    <div className="portal-discovery-hit-body">
                                        <div className="portal-discovery-hit-title">
                                            <div className="portal-discovery-hit-tags">
                                                <span>
                                                    {toDisplayLabel(item.contentDomain, "其他来源")}
                                                </span>
                                                <span>
                                                    {toDisplayLabel(item.contentType, "其他类型")}
                                                </span>
                                            </div>
                                            <h3>
                                                {renderQueryHighlight(
                                                    item.title || "未命名结果",
                                                    form.queryText
                                                )}
                                            </h3>
                                        </div>
                                        <p className="portal-discovery-hit-summary">
                                            {renderHighlightText(item.highlightText) ||
                                                renderQueryHighlight(
                                                    item.summary || "",
                                                    form.queryText
                                                ) ||
                                                "暂无摘要"}
                                        </p>
                                    </div>
                                );

                                if (targetPath) {
                                    return (
                                        <Link
                                            aria-label={`打开搜索结果：${item.title || "未命名结果"}`}
                                            className="portal-discovery-hit"
                                            key={hitKey}
                                            rel="noreferrer"
                                            target="_blank"
                                            to={targetPath}
                                            onClick={() => recordResultClick(group, item)}
                                        >
                                            {content}
                                        </Link>
                                    );
                                }

                                return (
                                    <button
                                        aria-label={`打开搜索结果：${item.title || "未命名结果"}`}
                                        className="portal-discovery-hit"
                                        key={hitKey}
                                        type="button"
                                        onClick={() => recordResultClick(group, item)}
                                    >
                                        {content}
                                    </button>
                                );
                            })}
                        </div>
                    </Card>
                ) : null}

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
                    <DiscoveryPagination
                        currentPage={currentPageNo}
                        disabled={searchMutation.isPending}
                        pageSize={currentPageSize}
                        total={totalCount}
                        onChange={handlePageChange}
                    />
                ) : null}
            </section>
        </main>
    );
};
