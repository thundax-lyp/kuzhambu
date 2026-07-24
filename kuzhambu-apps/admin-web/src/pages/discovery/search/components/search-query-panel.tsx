import { SearchOutlined } from "@ant-design/icons";
import { DatePicker, Input } from "antd";
import dayjs from "dayjs";
import type { Dayjs } from "dayjs";
import type { ReactNode } from "react";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import { KuzhambuSelect } from "@/components/kuzhambu-select";

export interface SearchFormState {
    dateFrom: string;
    dateTo: string;
    knowledgeBases: string[];
    pageNo: string;
    pageSize: string;
    queryText: string;
}

interface SearchQueryPanelProps {
    content: ReactNode;
    filterActive: boolean;
    form: SearchFormState;
    knowledgeBaseOptions: Array<{ label: string; value: string }>;
    loading: boolean;
    onClearFilters: () => void;
    onSearch: () => void;
    onUpdateDateRange: (dates: null | [Dayjs | null, Dayjs | null]) => void;
    onUpdateField: (key: keyof SearchFormState, value: string | string[]) => void;
}

export const SearchQueryPanel = ({
    content,
    filterActive,
    form,
    knowledgeBaseOptions,
    loading,
    onClearFilters,
    onSearch,
    onUpdateDateRange,
    onUpdateField
}: SearchQueryPanelProps) => {
    return (
        <KuzhambuListPage
            pageClassName="search-page"
            title="检索"
            description="公开已发布内容。"
            subjectName="内容"
            enableFilter
            filterText="高级"
            enableSearch
            searchShortcut="⌘K"
            searchValue={form.queryText}
            searchPlaceholder="搜索公开已发布内容..."
            onSearchChange={(queryText) => onUpdateField("queryText", queryText)}
            filterActive={filterActive}
            filterFields={[
                {
                    name: "queryText",
                    label: "搜索词",
                    render: () => (
                        <Input
                            placeholder="输入古籍、实体或正文关键词"
                            value={form.queryText}
                            onChange={(event) => onUpdateField("queryText", event.target.value)}
                        />
                    )
                },
                {
                    name: "knowledgeBases",
                    label: "知识库",
                    render: () => (
                        <KuzhambuSelect
                            mode="multiple"
                            allowClear
                            options={knowledgeBaseOptions}
                            placeholder="全部知识库"
                            value={form.knowledgeBases}
                            onChange={(value) => onUpdateField("knowledgeBases", value)}
                        />
                    )
                },
                {
                    name: "dateRange",
                    label: "时间范围",
                    render: () => (
                        <DatePicker.RangePicker
                            value={[
                                form.dateFrom ? dayjs(form.dateFrom) : null,
                                form.dateTo ? dayjs(form.dateTo) : null
                            ]}
                            onChange={onUpdateDateRange}
                        />
                    )
                }
            ]}
            onFilterApply={onSearch}
            onFilterReset={onClearFilters}
            pageActions={
                <KuzhambuButton
                    ariaLabel="搜索"
                    icon={<SearchOutlined />}
                    loading={loading}
                    testId="discovery-search-submit-button"
                    type="primary"
                    onClick={onSearch}
                >
                    搜索
                </KuzhambuButton>
            }
            content={content}
        />
    );
};
