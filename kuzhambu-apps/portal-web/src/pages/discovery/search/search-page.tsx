import { useEffect, useMemo, useRef, useState, type FormEvent } from "react";
import { useMutation } from "@tanstack/react-query";
import { Search, SlidersHorizontal, X } from "lucide-react";
import { useSearchParams } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { DiscoveryPagination, MultiOptionControl } from "./components/search-controls";
import { SearchResultList } from "./components/search-results";
import * as discoverySearchService from "./search-service";
import type {
    DiscoverySearchGroupResponse,
    DiscoverySearchItemResponse,
    DiscoverySearchRequest
} from "./search-types";
import {
    INITIAL_FORM_STATE,
    KNOWLEDGE_BASE_OPTIONS,
    SAMPLE_QUERIES,
    createClickCommand,
    flattenGroups,
    formatCount,
    hasAdvancedFilters,
    joinList,
    splitList,
    toFormState,
    toRequest,
    toSearchParams,
    type SearchFormState
} from "./search-utils";

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

                <SearchResultList
                    queryText={form.queryText}
                    results={results}
                    onResultClick={recordResultClick}
                />

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
