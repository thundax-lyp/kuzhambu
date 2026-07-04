import { useEffect, useMemo, useState, type FormEvent } from "react";
import { useMutation } from "@tanstack/react-query";
import { ArrowRight, Search } from "lucide-react";
import { Link, useSearchParams } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import * as discoverySearchService from "./search-service";
import type {
    DiscoverySearchClickRequest,
    DiscoverySearchGroupResponse,
    DiscoverySearchItemResponse,
    DiscoverySearchRequest
} from "./search-types";

const SEARCH_FIELDS = [
    {
        description: "古籍、实体、标签都可以直接输入",
        key: "primary",
        label: "搜索词",
        name: "queryText",
        placeholder: "例如：朱熹、礼制、三才图会"
    },
    {
        description: "可用英文逗号或中文顿号分隔",
        key: "knowledgeBases",
        label: "知识库",
        name: "knowledgeBases",
        placeholder: "classics, knowledge"
    },
    {
        description: "面向分类门类的精确过滤",
        key: "categoryCodes",
        label: "门类",
        name: "categoryCodes",
        placeholder: "SANCAI_ENTRY, WANGQI_DOCUMENT"
    },
    {
        description: "支持标签名精确过滤",
        key: "tagNames",
        label: "标签",
        name: "tagNames",
        placeholder: "礼制, 建筑, 民俗"
    },
    {
        description: "内容状态筛选",
        key: "contentStatuses",
        label: "状态",
        name: "contentStatuses",
        placeholder: "PUBLISHED, DRAFT"
    },
    {
        description: "可见性范围筛选",
        key: "visibilityScopes",
        label: "可见性",
        name: "visibilityScopes",
        placeholder: "PUBLIC, SHARED"
    }
] as const;

const SAMPLE_QUERIES = [
    {
        categoryCodes: "SANCAI_ENTRY",
        label: "三才图会",
        queryText: "图谱里的礼器"
    },
    {
        categoryCodes: "WANGQI_DOCUMENT",
        label: "王圻文档",
        queryText: "明代官制"
    },
    {
        categoryCodes: "MING_CUSTOMS",
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

const toFormState = (searchParams: URLSearchParams): SearchFormState => {
    return {
        categoryCodes: searchParams.get("categoryCodes") ?? "",
        contentStatuses: searchParams.get("contentStatuses") ?? "",
        dateFrom: searchParams.get("dateFrom") ?? "",
        dateTo: searchParams.get("dateTo") ?? "",
        knowledgeBases: searchParams.get("knowledgeBases") ?? "",
        pageNo: searchParams.get("pageNo") ?? INITIAL_FORM_STATE.pageNo,
        pageSize: searchParams.get("pageSize") ?? INITIAL_FORM_STATE.pageSize,
        queryText: searchParams.get("q") ?? "",
        tagNames: searchParams.get("tagNames") ?? "",
        visibilityScopes: searchParams.get("visibilityScopes") ?? ""
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
    appendParam(searchParams, "categoryCodes", form.categoryCodes);
    appendParam(searchParams, "tagNames", form.tagNames);
    appendParam(searchParams, "contentStatuses", form.contentStatuses);
    appendParam(searchParams, "visibilityScopes", form.visibilityScopes);
    appendParam(searchParams, "dateFrom", form.dateFrom);
    appendParam(searchParams, "dateTo", form.dateTo);
    appendParam(searchParams, "pageNo", form.pageNo);
    appendParam(searchParams, "pageSize", form.pageSize);

    return searchParams;
};

const splitList = (value: string) => {
    const tokens = value
        .split(/[\n,，、\s]+/gu)
        .map((token) => token.trim())
        .filter(Boolean);

    return Array.from(new Set(tokens));
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
    searchLogId: string,
    group: DiscoverySearchGroupResponse,
    item: DiscoverySearchItemResponse
): DiscoverySearchClickRequest | null => {
    if (
        !searchLogId ||
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
        searchLogId,
        targetPath: item.targetPath ?? null
    };
};

export const DiscoverySearchPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [form, setForm] = useState<SearchFormState>(() => toFormState(searchParams));
    const searchMutation = useMutation({
        mutationFn: (request: DiscoverySearchRequest) =>
            discoverySearchService.searchDiscovery(request)
    });
    const { mutate: runSearch, reset: resetSearch } = searchMutation;

    useEffect(() => {
        const nextForm = toFormState(searchParams);
        const request = toRequest(nextForm);

        if (request.queryText) {
            runSearch(request);
            return;
        }

        resetSearch();
    }, [runSearch, resetSearch, searchParams]);

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
            return "等待输入关键词后发起检索";
        }

        return `共 ${formatCount(response.totalCount)} 条命中，分布在 ${formatCount(response.groupCount)} 个分组中`;
    }, [response, searchMutation.isError, searchMutation.isPending]);

    const updateField = (key: keyof SearchFormState, value: string) => {
        setForm((current) => ({
            ...current,
            [key]: value
        }));
    };

    const applySample = (sample: (typeof SAMPLE_QUERIES)[number]) => {
        setForm((current) => ({
            ...current,
            categoryCodes: sample.categoryCodes,
            queryText: sample.queryText
        }));
    };

    const handleReset = () => {
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

        setSearchParams(nextSearchParams);
    };

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const request = toRequest(form);

        if (!request.queryText) {
            setSearchParams(new URLSearchParams());
            return;
        }

        const nextSearchParams = toSearchParams(form);
        if (nextSearchParams.toString() === searchParams.toString()) {
            runSearch(request);
            return;
        }

        setSearchParams(nextSearchParams);
    };

    const handleClick = (
        group: DiscoverySearchGroupResponse,
        item: DiscoverySearchItemResponse
    ) => {
        const command = response?.searchLogId
            ? createClickCommand(response.searchLogId, group, item)
            : null;

        if (command) {
            void discoverySearchService.recordSearchClick(command);
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

            <section className="portal-discovery-hero">
                <div className="portal-discovery-copy">
                    <p className="portal-discovery-tag">
                        <Search aria-hidden="true" size={16} />
                        先把词找准，再把来源找全
                    </p>
                    <h2>以知识为中心的跨库搜索入口</h2>
                    <p>
                        这里会把检索词、知识库、门类、标签和可见性条件一起提交给 Discovery
                        后端，再按知识分组返回结果，方便直接跳转到内容页或继续追踪命中来源。
                    </p>
                </div>
                <div className="portal-discovery-stat">
                    <span>当前草稿</span>
                    <strong>{requestPreview.queryText || "未填写关键词"}</strong>
                    <small>
                        预设分页 {requestPreview.pageNo ?? 1} / {requestPreview.pageSize ?? 10}
                    </small>
                </div>
            </section>

            <form className="portal-discovery-layout" onSubmit={handleSubmit}>
                <Card className="portal-discovery-panel">
                    <div className="portal-discovery-panel-header">
                        <div>
                            <p className="portal-kicker">检索条件</p>
                            <h2>查询与过滤</h2>
                        </div>
                        <div className="portal-discovery-samples">
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
                    </div>

                    <div className="portal-discovery-form-grid">
                        {SEARCH_FIELDS.map((field) => (
                            <Label key={field.key} className="portal-filter-field">
                                <span>{field.label}</span>
                                <Input
                                    name={field.name}
                                    placeholder={field.placeholder}
                                    value={form[field.name]}
                                    onChange={(event) =>
                                        updateField(field.name, event.target.value)
                                    }
                                />
                                <em>{field.description}</em>
                            </Label>
                        ))}

                        <Label className="portal-filter-field">
                            <span>起始日期</span>
                            <Input
                                name="dateFrom"
                                type="date"
                                value={form.dateFrom}
                                onChange={(event) => updateField("dateFrom", event.target.value)}
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

                        <Label className="portal-filter-field">
                            <span>页码</span>
                            <Input
                                name="pageNo"
                                min={1}
                                type="number"
                                value={form.pageNo}
                                onChange={(event) => updateField("pageNo", event.target.value)}
                            />
                            <em>默认从第 1 页开始</em>
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
                            <em>默认每页 10 条</em>
                        </Label>
                    </div>

                    <div className="portal-discovery-actions">
                        <Button type="button" variant="outline" onClick={handleReset}>
                            重置条件
                        </Button>
                        <Button disabled={searchMutation.isPending} type="submit">
                            {searchMutation.isPending ? "检索中..." : "开始检索"}
                        </Button>
                    </div>
                </Card>

                <aside className="portal-discovery-sidebar">
                    <Card className="portal-discovery-summary">
                        <div>
                            <p className="portal-kicker">检索摘要</p>
                            <h2>{summaryText}</h2>
                        </div>
                        <dl>
                            <div>
                                <dt>搜索日志</dt>
                                <dd>{response?.searchLogId ?? "-"}</dd>
                            </div>
                            <div>
                                <dt>回显词</dt>
                                <dd>
                                    {response?.displayQueryText ?? requestPreview.queryText ?? "-"}
                                </dd>
                            </div>
                            <div>
                                <dt>总命中</dt>
                                <dd>{formatCount(response?.totalCount)}</dd>
                            </div>
                            <div>
                                <dt>分组数</dt>
                                <dd>{formatCount(response?.groupCount)}</dd>
                            </div>
                        </dl>
                    </Card>

                    <Card className="portal-discovery-summary portal-discovery-summary-soft">
                        <div>
                            <p className="portal-kicker">检索提示</p>
                            <h2>结果点击会回传搜索日志</h2>
                        </div>
                        <p>
                            每个结果项都会携带分组键、全局排序和组内排序，点击后会发送点击日志，方便后端后续做重排、命中分析和路径追踪。
                        </p>
                    </Card>
                </aside>
            </form>

            <section className="portal-discovery-results" aria-label="Discovery 搜索结果">
                {searchMutation.isError ? (
                    <Card className="portal-empty">
                        <strong>检索失败</strong>
                        <p>请检查输入条件后重新提交搜索。</p>
                    </Card>
                ) : null}

                {!response && !searchMutation.isPending ? (
                    <Card className="portal-empty">
                        <strong>等待检索</strong>
                        <p>输入关键词后点击“开始检索”，系统会按知识分组返回命中项。</p>
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
                                                    <span>
                                                        跳转路径 {item.targetPath || "未提供"}
                                                    </span>
                                                </div>
                                            </>
                                        );

                                        return item.targetPath ? (
                                            <Link
                                                className="portal-discovery-hit"
                                                key={hitKey}
                                                to={item.targetPath}
                                                onClick={() => handleClick(group, item)}
                                            >
                                                {content}
                                                <span className="portal-discovery-hit-arrow">
                                                    <ArrowRight aria-hidden="true" size={16} />
                                                </span>
                                            </Link>
                                        ) : (
                                            <Card className="portal-discovery-hit" key={hitKey}>
                                                {content}
                                            </Card>
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
            </section>
        </main>
    );
};
